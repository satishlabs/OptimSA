package com.pst.optim.siebel;

import java.sql.Timestamp;
import org.w3c.dom.Element;
import java.sql.ResultSetMetaData;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.w3c.dom.Node;
import java.io.OutputStream;
import java.io.IOException;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.w3c.dom.Document;
import java.io.Writer;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Connection;
import com.els.util.Logging;
import java.io.InputStream;
import org.apache.log4j.Logger;

public class AuditSiebelArc extends SiebelArc
{
    protected String dbOwner;
    private boolean isInited;
    
    public AuditSiebelArc() {
        this.dbOwner = "SIEBEL";
        this.isInited = false;
        this.logr = Logger.getLogger((Class)AuditSiebelArc.class);
    }
    
    public String initArc(final InputStream inputStream, final String dsn, final String server, final String connectionClass, final String binaryEncoding, final String collection, final boolean useODMCollection, final boolean useOptimConnect) {
        this.logr.info((Object)"Creating AuditSiebelArc");
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
    
    protected void getQuery(final Connection connection) {
        try {
            this.logr.info((Object)"Inside getQuery of AuditSiebelArc class");
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
            final ResultSet executeQuery = connection.createStatement(1004, 1008).executeQuery(str);
            String string = null;
            String string2 = "";
            if (executeQuery.next()) {
                string = executeQuery.getString("AUDIT_LOG");
                this.logr.info((Object)("Clob string is : " + string));
                final String string3 = "SELECT DISTINCT LOGIN FROM S_USER  WHERE PAR_ROW_ID ='" + executeQuery.getString("USER_ID") + "'";
                this.logr.info((Object)("sql 1st: " + string3));
                final Statement statement = connection.createStatement();
                final ResultSet executeQuery2 = statement.executeQuery(string3);
                if (executeQuery2.next()) {
                    string2 = executeQuery2.getString("LOGIN");
                }
                statement.close();
            }
            executeQuery.previous();
            if (string != null) {
                int index = 0;
                int i = 0;
                final ArrayList<Object> list = new ArrayList<Object>();
                while (i < string.length()) {
                    final int index2 = string.indexOf(42, i);
                    String str2 = string.charAt(i) + "";
                    final String string4 = string.charAt(i + 1) + "";
                    try {
                        str2 += (int)Integer.valueOf(string4);
                    }
                    catch (Exception ex2) {}
                    final int is_numeric = is_numeric(str2);
                    if (is_numeric != 0) {
                        list.add(string.substring(index2 + 1, index2 + 1 + is_numeric));
                        ++index;
                    }
                    else {
                        list.add(index, null);
                        ++index;
                    }
                    i = index2 + is_numeric + 1;
                }
                this.outputdoc = this.QueryResponse_Audit(executeQuery, list, string2);
            }
            else {
                this.outputdoc = this.QueryResponse(executeQuery);
            }
        }
        catch (SQLException ex) {
            this.logr.error((Object)"OSA-03: SQL Error on Data retrieval", (Throwable)ex);
            this.reqType = null;
            this.outputdoc = this.createError(-3, "OSA-03: SQL Error on Data retrieval", Logging.getCurrentDate() + ":" + ex.getMessage());
        }
    }
    
    public void getXML(final Writer writer, final Document document) throws IOException {
        if (this.isInited) {
            final XMLSerializer xmlSerializer = new XMLSerializer(writer, new OutputFormat(document));
            xmlSerializer.asDOMSerializer();
            xmlSerializer.serialize(document);
            return;
        }
        throw new RuntimeException("Object never initialized");
    }
    
    public void getXML(final OutputStream outputStream, final Document document) throws IOException {
        if (this.isInited) {
            final XMLSerializer xmlSerializer = new XMLSerializer(outputStream, new OutputFormat(document));
            xmlSerializer.asDOMSerializer();
            xmlSerializer.serialize(document);
            return;
        }
        throw new RuntimeException("Object never initialized");
    }
    
    public Document getOutputDocument() {
        return this.outputdoc;
    }
    
    protected Document QueryResponse_Audit(final ResultSet set, final ArrayList<Object> list, final String s) {
        this.logr.info((Object)"Creating QueryResponse for Audit");
        Document document = null;
        String s2 = null;
        try {
            final ResultSetMetaData metaData = set.getMetaData();
            document = this.createDocument();
            final Element documentElement = document.getDocumentElement();
            for (int i = 0; i < list.size(); ++i) {
                final String s3 = (String) list.get(i);
                final String string = s3.charAt(0) + "";
                this.logr.info((Object)("Inside For loop of vtList where k is " + i));
                final int int1 = Integer.parseInt(s3.charAt(1) + "");
                final int n = i + 1;
                final int n2 = n + int1 + 1;
                final int n3 = n2 + int1 + 1;
                if (string.equalsIgnoreCase("J")) {
                    this.logr.info((Object)"Inside J type ");
                    for (int j = 0; j < int1; ++j) {
                        final int n4 = j;
                        i = n4 + n3;
                        if (set.next()) {
                            set.moveToInsertRow();
                            set.updateString("FIELD_NAME", (Object)list.get(n4 + n) + "");
                            set.updateString("NEW_VAL", (Object)list.get(n4 + n2) + "");
                            set.updateString("OLD_VAL", (Object)list.get(n4 + n3) + "");
                            final Element element = document.createElement("row");
                            documentElement.appendChild(element);
                            for (int k = 1; k <= metaData.getColumnCount(); ++k) {
                                final int columnType = metaData.getColumnType(k);
                                s2 = metaData.getColumnTypeName(k);
                                final String columnName = metaData.getColumnName(k);
                                if (columnType == 91 || columnType == 93) {
                                    final Timestamp timestamp = set.getTimestamp(k);
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
                                    String s4;
                                    if (columnType == -1) {
                                        if (set.getBytes(k) != null) {
                                            s4 = new String(set.getBytes(k), this.binaryEncoding);
                                        }
                                        else {
                                            s4 = set.getString(k);
                                        }
                                    }
                                    else {
                                        s4 = set.getString(k);
                                    }
                                    final Element element3 = document.createElement("value");
                                    element3.setAttribute("field", columnName);
                                    element3.appendChild(document.createTextNode(s4));
                                    element.appendChild(element3);
                                }
                            }
                            final Element element4 = document.createElement("value");
                            element4.setAttribute("field", "COLUMN_NAME");
                            element4.appendChild(document.createTextNode(""));
                            element.appendChild(element4);
                            final Element element5 = document.createElement("value");
                            element5.setAttribute("field", "Employee_Login");
                            element5.appendChild(document.createTextNode(s));
                            element.appendChild(element5);
                        }
                        set.previous();
                    }
                }
                else if (string.equalsIgnoreCase("C")) {
                    this.logr.info((Object)"Inside C type ");
                    this.logr.info((Object)"Creating connection with SiebelPool");
                    final Connection connection = ((DataSource)new InitialContext().lookup("java:comp/env/jdbc/SiebelDataSource")).getConnection();
                    final String string2 = "SELECT DISTINCT FIELD_NAME,COL_NAME FROM " + this.dbOwner + "." + "S_AUDIT_FIELD";
                    this.logr.info((Object)("Running SQL Query : " + string2));
                    final Statement statement = connection.createStatement();
                    final ResultSet executeQuery = statement.executeQuery(string2);
                    final HashMap<Object, String> hashMap = new HashMap<Object, String>();
                    while (executeQuery.next()) {
                        hashMap.put(executeQuery.getString("COL_NAME"), executeQuery.getString("FIELD_NAME"));
                    }
                    executeQuery.close();
                    statement.close();
                    connection.close();
                    for (int l = 0; l < int1; ++l) {
                        final int n5 = l;
                        System.out.println();
                        i = n5 + n3;
                        if (set.next()) {
                            set.moveToInsertRow();
                            set.updateString("FIELD_NAME", (String)hashMap.get(list.get(n5 + n)));
                            set.updateString("NEW_VAL", (Object)list.get(n5 + n2) + "");
                            set.updateString("OLD_VAL", (Object)list.get(n5 + n3) + "");
                            final Element element6 = document.createElement("row");
                            documentElement.appendChild(element6);
                            for (int n6 = 1; n6 <= metaData.getColumnCount(); ++n6) {
                                final int columnType2 = metaData.getColumnType(n6);
                                s2 = metaData.getColumnTypeName(n6);
                                final String columnName2 = metaData.getColumnName(n6);
                                if (columnType2 == 91 || columnType2 == 93) {
                                    final Timestamp timestamp2 = set.getTimestamp(n6);
                                    String format2;
                                    if (timestamp2 != null) {
                                        format2 = new SimpleDateFormat("MM'/'dd'/'yyyy HH':'mm':'ss").format(timestamp2);
                                    }
                                    else {
                                        format2 = "";
                                    }
                                    if (format2 != "") {
                                        final Element element7 = document.createElement("value");
                                        element7.setAttribute("field", columnName2);
                                        element7.appendChild(document.createTextNode(format2));
                                        element6.appendChild(element7);
                                    }
                                }
                                else if (!this.isPseudoField(columnName2)) {
                                    String s5;
                                    if (columnType2 == -1) {
                                        if (set.getBytes(n6) != null) {
                                            s5 = new String(set.getBytes(n6), this.binaryEncoding);
                                        }
                                        else {
                                            s5 = set.getString(n6);
                                        }
                                    }
                                    else {
                                        s5 = set.getString(n6);
                                    }
                                    final Element element8 = document.createElement("value");
                                    element8.setAttribute("field", columnName2);
                                    element8.appendChild(document.createTextNode(s5));
                                    element6.appendChild(element8);
                                }
                            }
                            final Element element9 = document.createElement("value");
                            element9.setAttribute("field", "COLUMN_NAME");
                            element9.appendChild(document.createTextNode((Object)list.get(n5 + n) + ""));
                            element6.appendChild(element9);
                            final Element element10 = document.createElement("value");
                            element10.setAttribute("field", "Employee_Login");
                            element10.appendChild(document.createTextNode(s));
                            element6.appendChild(element10);
                        }
                        set.previous();
                    }
                }
            }
        }
        catch (UnsupportedEncodingException ex) {
            this.logr.fatal((Object)("QueryResponse:Encoding Error in QueryResponse at " + s2), (Throwable)ex);
            this.reqType = null;
            document = this.createError(-3, "SQL Error in Query", Logging.getCurrentDate() + ":" + ex.getMessage());
        }
        catch (SQLException ex2) {
            this.logr.fatal((Object)("QueryResponse:SQL Error in QueryResponse at " + s2), (Throwable)ex2);
            this.reqType = null;
            document = this.createError(-3, "SQL Error in Query", Logging.getCurrentDate() + ":" + ex2.getMessage());
        }
        catch (Exception ex3) {}
        return document;
    }
    
    public static int is_numeric(final String s) {
        try {
            return Integer.parseInt(s);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}