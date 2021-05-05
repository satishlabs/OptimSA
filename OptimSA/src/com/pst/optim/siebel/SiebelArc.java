package com.pst.optim.siebel;

import com.pst.datasource.PSTConnector;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;
import java.sql.Timestamp;
import java.sql.ResultSetMetaData;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMImplementation;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.sql.ResultSet;
import java.io.OutputStream;
import java.io.IOException;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Connection;
import com.els.util.Logging;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

public class SiebelArc implements ArcReadable
{
    public static final String COPYRIGHT = "(C)Copyright IBM Corp. 2007";
    protected Document doc;
    protected Document outputdoc;
    public static final String SCCS = "$Header: /users1/aa/cvsroot/com.ibm.optim.aa.siebel.dg.2.5.2/optimsa/java/com/pst/optim/siebel/SiebelArc.java,v 1.10 2009-07-20 14:43:29 ehan Exp $";
    protected static final String INIT = "siebel-xmlext-fields-req";
    protected static final String INITRESPONSE = "siebel-xmlext-fields-ret";
    protected static final String QUERY = "siebel-xmlext-query-req";
    protected static final String QUERYRESPONSE = "siebel-xmlext-query-ret";
    protected static final String ERRORRESPONSE = "siebel-xmlext-status";
    protected static final String BUSCOMP = "buscomp";
    protected static final String REMOTESOURCE = "remote-source";
    protected static final String ID = "id";
    protected static final String SUPPORT = "support";
    protected static final String FIELD = "field";
    protected static final String MAXROWS = "max-rows";
    protected static final String SEARCHSTRING = "search-string";
    protected static final String MATCH = "match";
    protected static final String SEARCHSPEC = "search-spec";
    protected static final String SORTSPEC = "sort-spec";
    protected static final String NODE = "node";
    protected static final String SORT = "sort";
    protected static final String ROW = "row";
    protected static final String STATUSCODE = "status-code";
    protected static final String ERRORFIELD = "error-field";
    protected static final String ERRORTEXT = "error-text";
    protected static final String VALUE = "value";
    protected static final int UNSUPPORTED = -1;
    protected static final String UNSUPPORTEDTITLE = "Unsupported Method";
    protected static final String dateFormat = "MM'/'dd'/'yyyy HH':'mm':'ss";
    protected String connectionClass;
    public static final String[] PREFIXES;
    protected String searchstring;
    protected String matchfield;
    protected String matchvalue;
    protected String dsn;
    protected String guid;
    protected String table;
    protected String server;
    protected String binaryEncoding;
    protected String reqType;
    public boolean isQuery;
    private boolean isInited;
    protected Logger logr;
    protected String collection;
    protected String OrderBySort;
    protected boolean useODMCollection;
    protected boolean useOptimConnect;
    

    public SiebelArc(final InputStream inputStream, final String s, final String s2, final String s3, final String s4, final String s5, final boolean b, final boolean b2) {
        this.connectionClass = null;
        this.searchstring = "";
        this.matchfield = "";
        this.matchvalue = "";
        this.dsn = null;
        this.guid = null;
        this.table = null;
        this.server = null;
        this.binaryEncoding = null;
        this.reqType = null;
        this.isQuery = false;
        this.isInited = false;
        this.logr = null;
        this.collection = null;
        this.OrderBySort = "";
        this.useODMCollection = false;
        this.useOptimConnect = false;
        this.initArc(inputStream, s, s2, s3, s4, s5, b, b2);
    }
    
    public SiebelArc() {
        this.connectionClass = null;
        this.searchstring = "";
        this.matchfield = "";
        this.matchvalue = "";
        this.dsn = null;
        this.guid = null;
        this.table = null;
        this.server = null;
        this.binaryEncoding = null;
        this.reqType = null;
        this.isQuery = false;
        this.isInited = false;
        this.logr = null;
        this.collection = null;
        this.OrderBySort = "";
        this.useODMCollection = false;
        this.useOptimConnect = false;
        this.logr = Logger.getLogger((Class)SiebelArc.class);
    }
    
    @Override
    public String initArc(final InputStream inputStream, final String dsn, final String server, final String connectionClass, final String binaryEncoding, final String collection, final boolean useODMCollection, final boolean useOptimConnect) {
        this.logr.info((Object)"Creating SiebelArc");
        this.server = server;
        this.dsn = dsn;
        this.connectionClass = connectionClass;
        this.binaryEncoding = binaryEncoding;
        this.collection = collection;
        this.useODMCollection = useODMCollection;
        this.useOptimConnect = useOptimConnect;
        this.doc = this.getDocument(inputStream);
        if (this.doc != null) {
            this.parseParams(this.doc);
            final Connection connection = this.getConnection(connectionClass, dsn, this.guid, server, collection, useODMCollection, useOptimConnect);
            if (connection == null) {
                this.reqType = null;
                this.outputdoc = this.createError(-3, "SQL Error in Connection object", Logging.getCurrentDate() + ":" + "OSA-01: Unable to create a connection to the archive file");
                this.isInited = true;
                return "true";
            }
            if (this.reqType.equals("siebel-xmlext-fields-req")) {
                this.getFields(connection);
            }
            else if (this.reqType.equals("siebel-xmlext-query-req")) {
                this.getQuery(connection);
            }
            else {
                this.getUnsupported();
            }
        }
        this.isInited = true;
        return "false";
    }
    
    protected void getFields(final Connection connection) {
        try {
            this.outputdoc = this.InitResponse(connection.getMetaData().getColumns(null, null, this.getTable(), null));
        }
        catch (SQLException ex) {
            this.logr.error((Object)"SQL Error in getFields", (Throwable)ex);
            this.reqType = null;
            this.outputdoc = this.createError(-3, "SQL Error in Init", Logging.getCurrentDate() + ":" + ex.getMessage());
        }
    }
    
    protected void getQuery(final Connection connection) {
        try {
            this.isQuery = true;
            this.logr.info((Object)"Starting the Query");
            String str = "select * from " + this.table + " where " + this.matchfield + "= '" + this.matchvalue + "'";
            this.logr.info((Object)("sql 1st: " + str));
            if (this.OrderBySort != "") {
                this.logr.info((Object)"Inside if condition of getQuery");
                str += this.OrderBySort;
                this.logr.info((Object)"end if condition of getQuery");
            }
            this.logr.info((Object)("sql 2nd: " + str));
            this.outputdoc = this.QueryResponse(connection.createStatement().executeQuery(str));
        }
        catch (SQLException ex) {
            this.logr.error((Object)"OSA-03: SQL Error on Data retrieval", (Throwable)ex);
            this.reqType = null;
            this.outputdoc = this.createError(-3, "OSA-03: SQL Error on Data retrieval", Logging.getCurrentDate() + ":" + ex.getMessage());
        }
    }
    
    protected void getUnsupported() {
        this.outputdoc = this.createError(-1, "Unsupported Method", "This message is unsupported for this service");
        this.logr.error((Object)"OSA-08: Server has received unsupported request type");
    }
    
    @Override
    public void getXML(final Writer writer, final Document document) throws IOException {
        if (this.isInited) {
            final XMLSerializer xmlSerializer = new XMLSerializer(writer, new OutputFormat(document));
            xmlSerializer.asDOMSerializer();
            xmlSerializer.serialize(document);
            return;
        }
        throw new RuntimeException("Object never initialized");
    }
    
    @Override
    public void getXML(final OutputStream outputStream, final Document document) throws IOException {
        if (this.isInited) {
            final XMLSerializer xmlSerializer = new XMLSerializer(outputStream, new OutputFormat(document));
            xmlSerializer.asDOMSerializer();
            xmlSerializer.serialize(document);
            return;
        }
        throw new RuntimeException("Object never initialized");
    }
    
    protected Document InitResponse(final ResultSet set) {
        Document document;
        try {
            document = this.createDocument();
            final Element documentElement = document.getDocumentElement();
            while (set.next()) {
                final String string = set.getString("COLUMN_NAME");
                if (!this.isPseudoField(string)) {
                    final Element element = document.createElement("support");
                    element.setAttribute("field", string);
                    documentElement.appendChild(element);
                }
            }
        }
        catch (SQLException ex) {
            this.logr.error((Object)"InitResponse:SQL Error in InitResponse", (Throwable)ex);
            this.reqType = null;
            document = this.createError(-3, "SQL Error in Init", Logging.getCurrentDate() + ":" + ex.getMessage());
        }
        return document;
    }
    
    protected Document createDocument() {
        final DOMImplementation domImplementation = DOMImplementationImpl.getDOMImplementation();
        final DocumentType documentType = domImplementation.createDocumentType("XML", null, null);
        Document document;
        if (this.reqType != null && this.reqType.equals("siebel-xmlext-fields-req")) {
            document = domImplementation.createDocument(null, "siebel-xmlext-fields-ret", documentType);
        }
        else if (this.reqType != null && this.reqType.equals("siebel-xmlext-query-req")) {
            document = domImplementation.createDocument(null, "siebel-xmlext-query-ret", documentType);
        }
        else {
            document = domImplementation.createDocument(null, "siebel-xmlext-status", documentType);
        }
        return document;
    }
    
    protected Document QueryResponse(final ResultSet set) {
        this.logr.debug((Object)"Creating QueryResponse");
        String columnTypeName = null;
        Document document;
        try {
            final ResultSetMetaData metaData = set.getMetaData();
            document = this.createDocument();
            final Element documentElement = document.getDocumentElement();
            while (set.next()) {
                final Element element = document.createElement("row");
                documentElement.appendChild(element);
                for (int i = 1; i <= metaData.getColumnCount(); ++i) {
                    final int columnType = metaData.getColumnType(i);
                    columnTypeName = metaData.getColumnTypeName(i);
                    final String columnName = metaData.getColumnName(i);
                    if (columnType == 91 || columnType == 93) {
                        final Timestamp timestamp = set.getTimestamp(i);
                        String format;
                        if (timestamp != null) {
                            format = new SimpleDateFormat("MM'/'dd'/'yyyy HH':'mm':'ss").format(timestamp);
                        }
                        else {
                            format = "";
                        }
                        if (format != "") {
                            final Element element2 = document.createElement("value");
                            element2.setAttribute("field", columnName);
                            element2.appendChild(document.createTextNode(format));
                            element.appendChild(element2);
                        }
                    }
                    else if (!this.isPseudoField(columnName)) {
                        String s;
                        if (columnType == -1) {
                            if (set.getBytes(i) != null) {
                                s = new String(set.getBytes(i), this.binaryEncoding);
                            }
                            else {
                                s = set.getString(i);
                            }
                        }
                        else {
                            s = set.getString(i);
                        }
                        final Element element3 = document.createElement("value");
                        element3.setAttribute("field", columnName);
                        element3.appendChild(document.createTextNode(s));
                        element.appendChild(element3);
                    }
                }
            }
        }
        catch (UnsupportedEncodingException ex) {
            this.logr.fatal((Object)("QueryResponse:Encoding Error in QueryResponse at " + columnTypeName), (Throwable)ex);
            this.reqType = null;
            document = this.createError(-3, "SQL Error in Query", Logging.getCurrentDate() + ":" + ex.getMessage());
        }
        catch (SQLException ex2) {
            this.logr.fatal((Object)("QueryResponse:SQL Error in QueryResponse at " + columnTypeName), (Throwable)ex2);
            this.reqType = null;
            document = this.createError(-3, "SQL Error in Query", Logging.getCurrentDate() + ":" + ex2.getMessage());
        }
        return document;
    }
    
    @Override
    public Document getOutputDocument() {
        return this.outputdoc;
    }
    
    protected Document getDocument(final InputStream byteStream) {
        Document document = null;
        try {
            final DOMParser domParser = new DOMParser();
            domParser.parse(new InputSource(byteStream));
            document = domParser.getDocument();
        }
        catch (SAXException ex) {
            this.logr.error((Object)"getDocument:SAX Error in getDocument", (Throwable)ex);
            this.reqType = null;
            this.outputdoc = this.createError(-2, "Error parsing document", Logging.getCurrentDate() + ":" + ex.getMessage());
        }
        catch (IOException ex2) {
            this.logr.error((Object)"getDocument:IO Error in getDocument", (Throwable)ex2);
            this.reqType = null;
            this.outputdoc = this.createError(-4, "InputStream Error", Logging.getCurrentDate() + ":" + ex2.getMessage());
        }
        return document;
    }
    
    @Override
    public String getDataSource() {
        return this.dsn;
    }
    
    @Override
    public String getGUID() {
        return this.guid;
    }
    
    @Override
    public String getTable() {
        return this.table;
    }
    
    protected void parseParams(final Document document) {
        try {
            final Element documentElement = document.getDocumentElement();
            this.reqType = documentElement.getNodeName();
            this.logr.info((Object)("parseParams:Request Type : " + this.reqType));
            if (this.reqType.equals("siebel-xmlext-fields-req") || this.reqType.equals("siebel-xmlext-query-req")) {
                final NodeList elementsByTagName = documentElement.getElementsByTagName("buscomp");
                final NodeList elementsByTagName2 = documentElement.getElementsByTagName("remote-source");
                if (elementsByTagName.getLength() > 0 && elementsByTagName2.getLength() > 0) {
                    final Node item = elementsByTagName.item(0);
                    final String attribute = ((Element)item).getAttribute("id");
                    this.table = ((Text)item.getFirstChild()).getNodeValue();
                    this.guid = ((Text)elementsByTagName2.item(0).getFirstChild()).getNodeValue();
                    this.logr.info((Object)("parseParams:" + attribute));
                }
            }
            if (this.reqType.equals("siebel-xmlext-query-req")) {
                final NodeList elementsByTagName3 = documentElement.getElementsByTagName("match");
                final NodeList elementsByTagName4 = documentElement.getElementsByTagName("sort");
                if (elementsByTagName3 != null && elementsByTagName3.getLength() > 0) {
                    final Node item2 = elementsByTagName3.item(0);
                    this.matchfield = ((Element)item2).getAttribute("field");
                    this.matchvalue = ((Text)item2.getFirstChild()).getNodeValue();
                    this.logr.info((Object)("\nmatchfield " + this.matchfield));
                    this.logr.info((Object)("matchvalue " + this.matchvalue));
                }
                if (elementsByTagName4 != null && elementsByTagName4.getLength() > 0) {
                    String s = "order by";
                    for (int i = 0; i < elementsByTagName4.getLength(); ++i) {
                        final Node item3 = elementsByTagName4.item(i);
                        final String attribute2 = ((Element)item3).getAttribute("field");
                        String s2;
                        if (((Text)item3.getFirstChild()).getNodeValue().equalsIgnoreCase("DESCENDING")) {
                            s2 = "DESC";
                        }
                        else {
                            s2 = "ASC";
                        }
                        s = s + " " + attribute2 + " " + s2;
                        if (i != elementsByTagName4.getLength() - 1) {
                            s += ",";
                        }
                        this.logr.info((Object)("sortcol :" + attribute2));
                        this.logr.info((Object)("sortval :" + s2));
                    }
                    this.logr.info((Object)("Order By String : (Shakti) :" + s));
                    this.OrderBySort = s;
                }
            }
        }
        catch (Exception ex) {
            this.logr.error((Object)"OSA-10: Error during XML request parse", (Throwable)ex);
            ex.printStackTrace();
            this.logr.error((Object)ex.getMessage());
            this.reqType = null;
            this.outputdoc = this.createError(-3, "OSA-10: Error during XML request parse", Logging.getCurrentDate() + ":" + ex.getMessage());
        }
    }
    
    protected boolean isPseudoField(final String s) {
        boolean equalsIgnoreCase = false;
        boolean equalsIgnoreCase2 = false;
        boolean equalsIgnoreCase3 = false;
        boolean equalsIgnoreCase4 = false;
        boolean equalsIgnoreCase5 = false;
        boolean equalsIgnoreCase6 = false;
        boolean equalsIgnoreCase7 = false;
        boolean equalsIgnoreCase8 = false;
        boolean equalsIgnoreCase9 = false;
        try {
            equalsIgnoreCase = s.substring(0, SiebelArc.PREFIXES[0].length()).equalsIgnoreCase(SiebelArc.PREFIXES[0]);
        }
        catch (StringIndexOutOfBoundsException ex) {}
        try {
            equalsIgnoreCase2 = s.substring(0, SiebelArc.PREFIXES[1].length()).equalsIgnoreCase(SiebelArc.PREFIXES[1]);
        }
        catch (StringIndexOutOfBoundsException ex2) {}
        try {
            equalsIgnoreCase3 = s.substring(0, SiebelArc.PREFIXES[2].length()).equalsIgnoreCase(SiebelArc.PREFIXES[2]);
        }
        catch (StringIndexOutOfBoundsException ex3) {}
        try {
            equalsIgnoreCase4 = s.substring(0, SiebelArc.PREFIXES[3].length()).equalsIgnoreCase(SiebelArc.PREFIXES[3]);
        }
        catch (StringIndexOutOfBoundsException ex4) {}
        try {
            equalsIgnoreCase5 = s.substring(0, SiebelArc.PREFIXES[4].length()).equalsIgnoreCase(SiebelArc.PREFIXES[4]);
        }
        catch (StringIndexOutOfBoundsException ex5) {}
        try {
            equalsIgnoreCase6 = s.substring(0, SiebelArc.PREFIXES[5].length()).equalsIgnoreCase(SiebelArc.PREFIXES[5]);
        }
        catch (StringIndexOutOfBoundsException ex6) {}
        try {
            equalsIgnoreCase7 = s.substring(0, SiebelArc.PREFIXES[6].length()).equalsIgnoreCase(SiebelArc.PREFIXES[6]);
        }
        catch (StringIndexOutOfBoundsException ex7) {}
        try {
            equalsIgnoreCase8 = s.substring(0, SiebelArc.PREFIXES[7].length()).equalsIgnoreCase(SiebelArc.PREFIXES[7]);
        }
        catch (StringIndexOutOfBoundsException ex8) {}
        try {
            equalsIgnoreCase9 = s.substring(0, SiebelArc.PREFIXES[8].length()).equalsIgnoreCase(SiebelArc.PREFIXES[8]);
        }
        catch (StringIndexOutOfBoundsException ex9) {}
        return equalsIgnoreCase | equalsIgnoreCase2 | equalsIgnoreCase3 | equalsIgnoreCase4 | equalsIgnoreCase5 | equalsIgnoreCase6 | equalsIgnoreCase7 | equalsIgnoreCase8 | equalsIgnoreCase9;
    }
    
    protected void processNodes(final NodeList list) {
    }
    
    protected void processSort(final NodeList list) {
    }
    
    protected Document createError(final int i, final String s, final String s2) {
        final Document document = this.createDocument();
        final Element documentElement = document.getDocumentElement();
        final Element element = document.createElement("status-code");
        final Text textNode = document.createTextNode(String.valueOf(i));
        documentElement.appendChild(element);
        element.appendChild(textNode);
        final Element element2 = document.createElement("error-field");
        final Text textNode2 = document.createTextNode(s);
        documentElement.appendChild(element2);
        element2.appendChild(textNode2);
        final Element element3 = document.createElement("error-text");
        final Text textNode3 = document.createTextNode(s2);
        documentElement.appendChild(element3);
        element3.appendChild(textNode3);
        return document;
    }
    
    @Override
    public Connection getConnection(final String s, final String s2, final String s3, final String s4, final String s5, final boolean b, final boolean b2) {
        return PSTConnector.getConnector(s).getConnection(s2, (String)null, s3, s4, s5, b, b2);
    }
    
    static {
        PREFIXES = new String[] { "SRATT", "OPTYATT", "OPPTY_ATT", "OPTY_ATT", "ACTATT", "PRDATT", "ORDATT", "QTEATT", "IBMATT" };
    }
}