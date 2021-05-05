package com.pst.optim.servlet;

import org.xml.sax.InputSource;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.io.PrintWriter;
import org.xml.sax.SAXException;
import java.io.Writer;
import com.pst.optim.siebel.CmndResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import javax.servlet.ServletException;
import com.pst.app.Pr0cmnd;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.Properties;

import com.pst.optim.siebel.SiebelRestore;
import javax.servlet.http.HttpServlet;

public class OptimRunner extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    public static final String COPYRIGHT = "(C)Copyright IBM Corp. 2007, 2008, 2009, 2010";
    private String programPath;
    private String inputPath;
    private String outputPath;
    private boolean sync;
    private boolean bypassOptimSA;
    protected String initAttServer;
    protected String initVerbosity;
    protected String initConnectClass;
    protected String initArcClass;
    protected String initDSN;
    protected final String ARC_CLASS = "com.pst.optim.siebel.SiebelArc";
    protected SiebelRestore sr;
    protected String server;
    protected String dsn;
    protected String connectClass;
    protected String siebelDB;
    protected String siebelDBAlias;
    protected long statusThreadSleepTime;
    protected String dbOwner;
    public static final String SCCS = "$Header: /users1/aa/cvsroot/com.ibm.optim.aa.siebel.dg.2.5.2/optimsa/java/com/pst/optim/servlet/OptimRunner.java,v 1.16 2009-07-21 18:02:06 ehan Exp $";
    protected String log_level;
    protected Logger log4;
    protected String refTables;
    protected boolean overrideAtt;
    protected String attPath;
    protected boolean useOptimConnect;
    protected String os;
    
    public OptimRunner() {
        this.programPath = null;
        this.inputPath = null;
        this.outputPath = null;
        this.sync = false;
        this.bypassOptimSA = false;
        this.initAttServer = "AttServer";
        this.initVerbosity = "Verbosity";
        this.initConnectClass = "ConnectionClassName";
        this.initArcClass = "ArcClass";
        this.initDSN = "ODMDSN";
        this.sr = new SiebelRestore();
        this.server = null;
        this.dsn = null;
        this.connectClass = null;
        this.siebelDB = null;
        this.siebelDBAlias = null;
        this.statusThreadSleepTime = 5000L;
        this.dbOwner = "SIEBEL";
        this.log_level = "ALL";
        this.log4 = null;
        this.overrideAtt = false;
        this.useOptimConnect = false;
        this.os = "";
    }
    
    public void init() throws ServletException {
        super.init();
        this.log4 = Logger.getLogger((Class)OptimRunner.class);
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
        this.programPath = properties.getProperty("ProgramFilePath");
        this.inputPath = properties.getProperty("InputFilePath");
        this.outputPath = properties.getProperty("OutputFilePath");
        this.sync = Boolean.valueOf(properties.getProperty("Synchronous"));
        this.bypassOptimSA = Boolean.valueOf(properties.getProperty("BypassOptimSA"));
        this.statusThreadSleepTime = Long.valueOf(properties.getProperty("StatusThreadSleepTime"));
        this.refTables = properties.getProperty("RefTables");
        try {
            if (!this.bypassOptimSA) {
                this.log4.info((Object)"Initializing Pr0cmd...");
                Pr0cmnd.init(this.programPath, this.inputPath, this.outputPath, this.sync, this.refTables);
                try {
                    this.os = System.getProperty("os.name").substring(0, 7);
                }
                catch (Exception ex3) {}
                if (this.os.equalsIgnoreCase("Windows")) {
                    if (!this.programPath.endsWith("\\") || !this.inputPath.endsWith("\\") || !this.outputPath.endsWith("\\")) {
                        this.log4.error((Object)"OSA-05: Invalid file path");
                    }
                }
                else if (!this.programPath.endsWith("//") || !this.inputPath.endsWith("//") || !this.outputPath.endsWith("//")) {
                    this.log4.error((Object)"OSA-05: Invalid file path");
                }
            }
            else {
                this.log4.info((Object)"Initializing parameters to bypass Pr0cmd...");
                this.server = properties.getProperty(this.initAttServer);
                this.dsn = properties.getProperty(this.initDSN);
                this.connectClass = properties.getProperty(this.initConnectClass);
                this.dbOwner = properties.getProperty("DBOwner");
                this.useOptimConnect = Boolean.valueOf(properties.getProperty("UseOptimConnect"));
            }
        }
        catch (Exception ex2) {
            throw new ServletException((Throwable)ex2);
        }
    }
    
    protected void processRequest(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        final PrintWriter writer = httpServletResponse.getWriter();
        String s = "";
        String s2 = "";
        String s3 = "";
        String s4 = "";
        try {
            final Document document = this.getDocument((InputStream)httpServletRequest.getInputStream());
            this.log4.info((Object)"GEt Document");
            final Element documentElement = document.getDocumentElement();
            String string;
            if (this.bypassOptimSA) {
                this.log4.info((Object)"Bypassing Pr0cmd.");
                this.siebelDB = documentElement.getAttribute("server");
                this.siebelDBAlias = documentElement.getAttribute("dbalias");
                this.sr.setDbOwner(this.dbOwner);
                this.sr.restore(this.dsn, this.server, this.connectClass, documentElement, this.overrideAtt, this.attPath, this.useOptimConnect);
                s = this.sr.getTable();
                s2 = this.sr.getGuid();
                s3 = this.sr.getMatch_value();
                s4 = this.sr.getRow_id();
                try {
                    this.sr.updateRestoreStatus(this.statusThreadSleepTime);
                    if (this.sr.getReturnValue() == -1) {
                        string = "Restore process initialized but restore failed";
                    }
                    else {
                        string = "Restore process initialized and completed";
                    }
                }
                catch (Exception ex4) {
                    string = "Restore process complete but status update failed";
                }
            }
            else {
                this.log4.info((Object)"Instantiating Pr0cmd object");
                final Pr0cmnd pr0cmnd = new Pr0cmnd(documentElement.getAttribute("server"), documentElement.getAttribute("dbalias"), documentElement.getAttribute("pstdir"));
                pr0cmnd.writeOutput(documentElement);
                s = pr0cmnd.getTableName();
                s2 = pr0cmnd.getGUID();
                s3 = pr0cmnd.getFieldValue();
                s4 = pr0cmnd.getRowId();
                if (pr0cmnd.pr0cmnd(pr0cmnd.getCurrentInputFile()) == -1) {
                    string = "Restore failed to initialize";
                }
                else {
                    string = "Restore process initialized. Log file is " + pr0cmnd.getLogfileName();
                }
            }
            final CmndResponse cmndResponse = new CmndResponse(s, s2, s3, s4, string);
            cmndResponse.getXML((Writer)writer, cmndResponse.getDocument());
        }
        catch (SAXException ex) {
            final CmndResponse cmndResponse2 = new CmndResponse("UnKnown", "Unknown", "Unknown", "Unknown", "Improper XML format:" + ex.getMessage());
            cmndResponse2.getXML((Writer)writer, cmndResponse2.getDocument());
        }
        catch (InterruptedException ex2) {
            final CmndResponse cmndResponse3 = new CmndResponse(s, s2, s3, s4, "Execution failed due to:" + ex2.getMessage());
            cmndResponse3.getXML((Writer)writer, cmndResponse3.getDocument());
        }
        catch (Exception ex3) {
            final CmndResponse cmndResponse4 = new CmndResponse(s, s2, s3, s4, "Unknown Exception :" + ex3.getMessage());
            cmndResponse4.getXML((Writer)writer, cmndResponse4.getDocument());
        }
        finally {
            writer.close();
        }
    }
    
    protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        this.processRequest(httpServletRequest, httpServletResponse);
    }
    
    protected void doPost(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        this.processRequest(httpServletRequest, httpServletResponse);
    }
    
    public String getServletInfo() {
        return "Short description";
    }
    
    private Document getDocument(final InputStream byteStream) throws SAXException, IOException {
        Document document;
        try {
            final DOMParser domParser = new DOMParser();
            domParser.parse(new InputSource(byteStream));
            document = domParser.getDocument();
        }
        catch (SAXException ex) {
            this.log4.error((Object)"getDocument:SAX Error in getDocument", (Throwable)ex);
            throw ex;
        }
        catch (IOException ex2) {
            this.log4.error((Object)"getDocument:IO Error in getDocument", (Throwable)ex2);
            throw ex2;
        }
        return document;
    }
}