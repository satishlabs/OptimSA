package com.pst.optim.servlet;

import javax.servlet.ServletInputStream;
import java.io.PrintWriter;
import java.io.Writer;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.io.InputStream;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.Properties;

import com.pst.datasource.FlushPool;
import com.pst.optim.siebel.ArcReadable;
import javax.servlet.http.HttpServlet;

public class AuditTrialDecodeServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    public static final String COPYRIGHT = "(C)Copyright IBM Corp. 2007, 2008, 2009, 2010";
    protected String initAttServer;
    protected String initVerbosity;
    protected String initConnectClass;
    protected String initArcClass;
    protected String initDSN;
    protected String initBinaryEncoding;
    protected final String ARC_CLASS = "com.pst.optim.siebel.AuditSiebelArc";
    protected String arcClass;
    protected String tmpInt;
    protected ArcReadable sa;
    protected String server;
    protected String dsn;
    protected String connectClass;
    protected String binaryEncoding;
    protected String log_level;
    protected Logger log4;
    public static final String SCCS = "$Header: /users1/aa/cvsroot/com.ibm.optim.aa.siebel.dg.2.5.2/optimsa/java/com/pst/optim/servlet/AuditTrialDecodeServlet.java,v 1.10 2009-06-29 18:46:32 ehan Exp $";
    protected String collection;
    protected boolean useODMCollection;
    protected boolean useOptimConnect;
    protected String result;
    
    public AuditTrialDecodeServlet() {
        this.initAttServer = "AttServer";
        this.initVerbosity = "Verbosity";
        this.initConnectClass = "ConnectionClassName";
        this.initArcClass = "ArcClass";
        this.initDSN = "ODMDSN";
        this.initBinaryEncoding = "BinaryEncoding";
        this.arcClass = null;
        this.tmpInt = null;
        this.sa = null;
        this.server = null;
        this.dsn = null;
        this.connectClass = null;
        this.binaryEncoding = null;
        this.log_level = "ALL";
        this.log4 = null;
        this.collection = null;
        this.useODMCollection = false;
        this.useOptimConnect = false;
        this.result = null;
    }
    
    public void init() throws ServletException {
        super.init();
        this.log4 = Logger.getLogger((Class)AuditTrialDecodeServlet.class);
        final Properties properties = new Properties();
        try {
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("optim.properties");
            if (resourceAsStream == null) {
                this.log4.error((Object)"Cannot load properties file from classpath.  Trying to get from user home");
                resourceAsStream = new FileInputStream(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "optim.properties"));
            }
            properties.load(resourceAsStream);
        }
        catch (IOException ex) {
            this.log4.error((Object)"OSA-07:Unable to load optim.properties file");
            ex.printStackTrace();
        }
        this.server = properties.getProperty(this.initAttServer);
        this.dsn = properties.getProperty(this.initDSN);
        this.connectClass = properties.getProperty(this.initConnectClass);
        this.binaryEncoding = properties.getProperty(this.initBinaryEncoding);
        this.collection = properties.getProperty("Collection");
        this.useODMCollection = Boolean.valueOf(properties.getProperty("UseODMCollection"));
        this.useOptimConnect = Boolean.valueOf(properties.getProperty("UseOptimConnect"));
        this.log4.info((Object)("arcClass : " + this.arcClass));
        this.log4.info((Object)("Init params UseOptimConnect : " + this.useOptimConnect));
        this.log4.info((Object)("Init params UseODMCollection : " + this.useODMCollection));
        if (this.arcClass == null || this.arcClass.trim().equals("")) {
            this.arcClass = "com.pst.optim.siebel.AuditSiebelArc";
        }
        this.log4.info((Object)("arcClass : " + this.arcClass));
        final FlushPool flushPool = new FlushPool();
        flushPool.setCacheFlushCycle(Long.parseLong(properties.getProperty("FlushCycle")));
        flushPool.start();
    }
    
    protected void processRequest(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        this.log4.info((Object)"Incoming Request for AuditTrialServlet, processRequest");
        httpServletResponse.setContentType("text/xml;charset=UTF-8");
        final PrintWriter writer = httpServletResponse.getWriter();
        final ServletInputStream inputStream = httpServletRequest.getInputStream();
        try {
            this.sa = this.getAuditSiebelArc((InputStream)inputStream, this.dsn, this.server, this.connectClass, this.collection, this.useODMCollection, this.useOptimConnect);
            if (this.result.equals("true")) {
                this.log4.error((Object)"OSA-01: Unable to create a connection to the archive file");
                this.sa.getXML((Writer)writer, this.sa.getOutputDocument());
                writer.close();
            }
            else {
                this.log4.info((Object)("Init params GUID : " + this.sa.getGUID()));
                this.log4.info((Object)("Init params table : " + this.sa.getTable()));
                this.log4.info((Object)("Init datasource table : " + this.sa.getDataSource()));
                this.log4.info((Object)("Init params server : " + this.server));
                this.sa.getXML((Writer)writer, this.sa.getOutputDocument());
                writer.close();
            }
        }
        catch (ClassNotFoundException ex) {}
        catch (IllegalAccessException ex2) {}
        catch (InstantiationException ex3) {}
    }
    
    protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        this.processRequest(httpServletRequest, httpServletResponse);
    }
    
    protected void doPost(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        this.processRequest(httpServletRequest, httpServletResponse);
    }
    
    public String getServletInfo() {
        return "Servlet to handle requests coming from Siebel";
    }
    
    public ArcReadable getAuditSiebelArc(final InputStream inputStream, final String s, final String s2, final String s3, final String s4, final boolean b, final boolean b2) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final ArcReadable arcReadable = (ArcReadable)Class.forName(this.arcClass).newInstance();
        this.result = arcReadable.initArc(inputStream, s, s2, s3, this.binaryEncoding, s4, b, b2);
        return arcReadable;
    }
}