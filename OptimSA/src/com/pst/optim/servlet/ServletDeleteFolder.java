package com.pst.optim.servlet;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;
import java.sql.Connection;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import java.io.PrintWriter;
import java.io.Writer;
import com.pst.optim.siebel.CmndResponse;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.Properties;
import com.pst.optim.siebel.SiebelRestore;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServlet;

public class ServletDeleteFolder extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    public static final String COPYRIGHT = "(C)Copyright IBM Corp. 2007, 2008, 2009, 2010";
    protected String temppath;
    protected String log_level;
    protected Logger log4;
    protected SiebelRestore sr;
    protected String server;
    protected String dsn;
    protected String connectClass;
    protected String siebelDB;
    protected String siebelDBAlias;
    protected long statusThreadSleepTime;
    protected String dbOwner;
    protected String initAttServer;
    protected String initConnectClass;
    protected String initArcClass;
    protected String initDSN;
    protected boolean useOptimConnect;
    
    public ServletDeleteFolder() {
        this.temppath = null;
        this.log_level = "ALL";
        this.log4 = null;
        this.sr = new SiebelRestore();
        this.server = null;
        this.dsn = null;
        this.connectClass = null;
        this.siebelDB = null;
        this.siebelDBAlias = null;
        this.statusThreadSleepTime = 5000L;
        this.dbOwner = "SIEBEL";
        this.initAttServer = "AttServer";
        this.initConnectClass = "ConnectionClassName";
        this.initArcClass = "ArcClass";
        this.initDSN = "ODMDSN";
        this.useOptimConnect = false;
    }
    
    public void init() throws ServletException {
        super.init();
        this.log4 = Logger.getLogger((Class)ServletDeleteFolder.class);
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
        this.dbOwner = properties.getProperty("DBOwner");
        this.useOptimConnect = Boolean.valueOf(properties.getProperty("UseOptimConnect"));
    }
    
    protected void processRequest(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String str = null;
        final PrintWriter writer = httpServletResponse.getWriter();
        String table = "";
        String guid = "";
        String match_value = "";
        String row_id = "";
        String substring = "";
        try {
            final Document document = this.getDocument((InputStream)httpServletRequest.getInputStream());
            this.log4.info((Object)"GET Document");
            final Element documentElement = document.getDocumentElement();
            this.temppath = documentElement.getAttribute("temppath");
            this.log4.info((Object)("Got the temp path to resotre : " + this.temppath));
            this.sr.parseParams(documentElement);
            this.sr.setDbOwner(this.dbOwner);
            table = this.sr.getTable();
            guid = this.sr.getGuid();
            match_value = this.sr.getMatch_value();
            row_id = this.sr.getRow_id();
            final String restoreRowId = this.sr.getRestoreRowId();
            try {
                substring = System.getProperty("os.name").substring(0, 7);
            }
            catch (Exception ex2) {}
            String pathname;
            String pathname2;
            if (substring.equalsIgnoreCase("Windows")) {
                pathname = this.temppath + "\\" + restoreRowId + "\\Att";
                pathname2 = this.temppath + "\\" + restoreRowId;
                this.log4.info((Object)("Got the att path : " + pathname));
                this.log4.info((Object)("Got the delete path : " + pathname2));
            }
            else {
                pathname = this.temppath + "//" + restoreRowId + "//Att";
                pathname2 = this.temppath + "//" + restoreRowId;
                this.log4.info((Object)("Got the att path : " + pathname));
                this.log4.info((Object)("Got the delete path : " + pathname2));
            }
            final File file = new File(pathname);
            final File file2 = new File(pathname2);
            final Connection connection = this.sr.getConnection(this.connectClass, this.dsn, guid, this.server, this.useOptimConnect);
            final String runQueryFileType = this.runQueryFileType(restoreRowId, this.dbOwner, table);
            Label_0854: {
                if (runQueryFileType.equals("FILE")) {
                    if (file.isDirectory()) {
                        final long lng = this.runQueryFileSize(connection) / 1048576L;
                        long lng2;
                        if (lng <= 5L) {
                            lng2 = 10000L;
                        }
                        else {
                            lng2 = lng * 1000L + 5000L;
                        }
                        final long n = new Date().getTime() + lng2;
                        while (true) {
                        Label_0783:
                            while (true) {
                                while (file.list().length > 0) {
                                    if (new Date().getTime() > n) {
                                        this.runQueryDeleteAtt(restoreRowId, this.dbOwner, table);
                                        str = "Operation timed-out.Restore Failed.";
                                        this.log4.info((Object)("Completed, Status = " + str + ", FileSize=" + lng + ", TimeOutMS=" + lng2 + ", RowId=" + restoreRowId));
                                        break Label_0854;
                                    }
                                }
                                file.delete();
                                this.log4.info((Object)("The " + file.getPath() + " is deleted"));
                                final long n2 = new Date().getTime() + 3000L;
                                while (!file2.delete()) {
                                    if (new Date().getTime() > n2) {
                                        this.log4.info((Object)("The " + file2.getPath() + " is deleted"));
                                        str = "Restore process initialized and completed";
                                        continue Label_0783;
                                    }
                                }
                                break;
                            }
                            continue;
                        }
                    }
                    this.log4.info((Object)"This is not a directory");
                    str = "Restore process initialized but restore failed";
                }
            }
            if (runQueryFileType.equals("URL")) {
                this.log4.info((Object)"This is a URL");
                str = "Restore process initialized";
            }
            if (runQueryFileType.equals("") || runQueryFileType == null) {
                this.log4.info((Object)"Restore Failed at OptimRunner");
                str = "Restore process initialized but restore failed";
            }
            final CmndResponse cmndResponse = new CmndResponse(table, guid, match_value, row_id, str);
            cmndResponse.getXML((Writer)writer, cmndResponse.getDocument());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            final CmndResponse cmndResponse2 = new CmndResponse(table, guid, match_value, row_id, "Unknown Exception :" + ex.getMessage());
            cmndResponse2.getXML((Writer)writer, cmndResponse2.getDocument());
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
    
    private long runQueryFileSize(final Connection connection) {
        long lng = 0L;
        final String match_field = this.sr.getMatch_field();
        final String match_value = this.sr.getMatch_value();
        final String table = this.sr.getTable();
        try {
            final String string = "select file_size from " + table + " where " + match_field + " = '" + match_value + "'";
            this.log4.info((Object)("Running SQL Query : " + string));
            final ResultSet executeQuery = connection.createStatement().executeQuery(string);
            while (executeQuery.next()) {
                lng = executeQuery.getInt("file_size");
            }
        }
        catch (SQLException ex) {
            this.log4.info((Object)"OSA-02: SQL Error on Siebel Attachment table for filesize", (Throwable)ex);
        }
        this.log4.info((Object)("file size : " + lng));
        return lng;
    }
    
    private String runQueryFileType(final String str, final String str2, final String str3) {
        String string = "";
        try {
            final Connection connection = ((DataSource)new InitialContext().lookup("java:comp/env/jdbc/SiebelDataSource")).getConnection();
            final String string2 = "select FILE_SRC_TYPE from " + str2 + "." + str3 + " where ROW_ID = '" + str + "'";
            this.log4.info((Object)("Running SQL Query : " + string2));
            final ResultSet executeQuery = connection.createStatement().executeQuery(string2);
            while (executeQuery.next()) {
                string = executeQuery.getString("FILE_SRC_TYPE");
            }
            connection.close();
        }
        catch (Exception ex) {
            this.log4.info((Object)"OSA-02: SQL Error on Siebel attachment table for file src type : ", (Throwable)ex);
        }
        this.log4.info((Object)("File Src Type from Siebel attachment table : " + string));
        return string;
    }
    
    private void runQueryDeleteAtt(final String str, final String str2, final String str3) {
        this.log4.info((Object)"Siebel attachment deleting method called  : ");
        try {
            final Connection connection = ((DataSource)new InitialContext().lookup("java:comp/env/jdbc/SiebelDataSource")).getConnection();
            final String string = "Delete from " + str2 + "." + str3 + " where ROW_ID = '" + str + "'";
            this.log4.info((Object)("Running SQL Query : " + string));
            connection.createStatement().executeUpdate(string);
        }
        catch (Exception ex) {
            this.log4.info((Object)"OSA-02: SQL Error on Siebel attachment deleting the record", (Throwable)ex);
        }
    }
}