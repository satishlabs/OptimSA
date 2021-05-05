package com.pst.optim.siebel;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.w3c.dom.Element;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import com.els.util.Logging;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import java.sql.ResultSet;

public class SiebelArcBC extends SiebelArc
{
    public static final String COPYRIGHT = "(C)Copyright IBM Corp. 2007";
    public static final String HEADER = "$Header: /users1/aa/cvsroot/com.ibm.optim.aa.siebel.dg.2.5.2/optimsa/java/com/pst/optim/siebel/SiebelArcBC.java,v 1.6 2009-06-29 18:46:31 ehan Exp $";
    protected static final String S_SRV_REQ = "S_SRV_REQ";
    protected static final String S_SRV_REQ_X = "S_SRV_REQ_X";
    protected static final String PAR_ROW_ID = "PAR_ROW_ID";
    protected static final String ROW_ID = "ROW_ID";
    
    @Override
    protected Document InitResponse(final ResultSet set) {
        Document document;
        try {
            final ResultSetMetaData metaData = set.getMetaData();
            document = this.createDocument();
            final Element documentElement = document.getDocumentElement();
            set.next();
            for (int i = 1; i <= metaData.getColumnCount(); ++i) {
                final String formatField = this.formatField(metaData.getColumnName(i));
                if (!this.isPseudoField(formatField)) {
                    final Element element = document.createElement("support");
                    element.setAttribute("field", formatField);
                    documentElement.appendChild(element);
                }
            }
        }
        catch (SQLException ex) {
            this.logr.error((Object)"SiebelArcBC.InitResponse:SQL Error in InitResponse", (Throwable)ex);
            this.reqType = null;
            document = this.createError(-3, "SQL Error in Init", Logging.getCurrentDate() + ":" + ex.getMessage());
        }
        return document;
    }
    
    @Override
    protected Document QueryResponse(final ResultSet set) {
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
                    columnTypeName = metaData.getColumnTypeName(i);
                    final int columnType = metaData.getColumnType(i);
                    String s = null;
                    final String formatField = this.formatField(metaData.getColumnName(i));
                    if (columnType == 91 || columnType == 93) {
                        final Timestamp timestamp = set.getTimestamp(i);
                        if (timestamp != null) {
                            s = new SimpleDateFormat("MM'/'dd'/'yyyy HH':'mm':'ss").format(timestamp);
                        }
                        else {
                            s = "";
                        }
                    }
                    else if (!this.isPseudoField(formatField)) {
                        if (columnType == -1) {
                            s = new String(set.getBytes(i), "ISO-8859-1");
                        }
                        else {
                            s = set.getString(i);
                        }
                    }
                    final Element element2 = document.createElement("value");
                    element2.setAttribute("field", formatField);
                    if (s != null && s != "") {
                        element2.appendChild(document.createTextNode(s));
                    }
                    element.appendChild(element2);
                }
            }
        }
        catch (UnsupportedEncodingException ex) {
            this.logr.fatal((Object)("QueryResponse:Encoding Error in QueryResponse at " + columnTypeName), (Throwable)ex);
            this.reqType = null;
            document = this.createError(-3, "SQL Error in Query", Logging.getCurrentDate() + ":" + ex.getMessage());
        }
        catch (SQLException ex2) {
            this.logr.error((Object)("QueryResponse:SQL Error in QueryResponse at " + columnTypeName), (Throwable)ex2);
            this.reqType = null;
            document = this.createError(-3, "SQL Error in Query", Logging.getCurrentDate() + ":" + ex2.getMessage());
        }
        return document;
    }
    
    @Override
    protected void getQuery(final Connection connection) {
        if (!this.table.equalsIgnoreCase("S_SRV_REQ")) {
            super.getQuery(connection);
        }
        else {
            try {
                this.isQuery = true;
                this.logr.debug((Object)"getQuery:Starting the Query");
                final PreparedStatement prepareStatement = connection.prepareStatement("select * from " + this.table + ", " + "S_SRV_REQ_X" + " where " + this.table + "." + this.matchfield + "= ? and " + this.table + "." + "ROW_ID" + "=" + "S_SRV_REQ_X" + "." + "PAR_ROW_ID");
                prepareStatement.setString(1, this.matchvalue);
                this.outputdoc = this.QueryResponse(prepareStatement.executeQuery());
            }
            catch (SQLException ex) {
                this.logr.error((Object)"SiebelArcBC.getQuery:SQL Error in SiebelArcBCgetQuery", (Throwable)ex);
                this.reqType = null;
                this.outputdoc = this.createError(-3, "SQL Error in Init", Logging.getCurrentDate() + ":" + ex.getMessage());
            }
        }
    }
    
    @Override
    protected void getFields(final Connection connection) {
        if (!this.table.equalsIgnoreCase("S_SRV_REQ")) {
            super.getFields(connection);
        }
        else {
            try {
                this.isQuery = false;
                this.logr.debug((Object)"SiebelArcBC.getInit:Starting the Init");
                this.outputdoc = this.InitResponse(connection.createStatement().executeQuery(this.getSrvReqInit()));
            }
            catch (SQLException ex) {
                this.logr.error((Object)"SiebelArcBC.getFields:SQL Error in SiebelArcBCgetQuery", (Throwable)ex);
                this.reqType = null;
                this.outputdoc = this.createError(-3, "SQL Error in Init", Logging.getCurrentDate() + ":" + ex.getMessage());
            }
        }
    }
    
    private final String getSrvReqQuery() {
        return "select * from " + this.table + ", " + "S_SRV_REQ_X" + " where " + this.table + "." + this.matchfield + "= '" + this.matchvalue + "' and " + this.table + "." + "ROW_ID" + "=" + "S_SRV_REQ_X" + "." + "PAR_ROW_ID";
    }
    
    private final String getSrvReqInit() {
        return "select * from " + this.table + ", " + "S_SRV_REQ_X" + " limit to 1 rows";
    }
    
    private String formatField(final String s) {
        String string;
        if (s.substring(0, 2).equals("$$")) {
            string = "S_SRV_REQ_X" + s.substring(s.indexOf(95));
        }
        else {
            string = s;
        }
        return string;
    }
}