package com.pst.optim.siebel;

import com.pst.datasource.PSTConnector;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.Statement;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.ResultSetMetaData;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.NamingException;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.io.IOException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.util.ArrayList;
import org.apache.log4j.Logger;

public class SiebelRestore
{
    public static final String COPYRIGHT = "(C)Copyright IBM Corp. 2008";
    public static final String SCCS = "$Header: /users1/aa/cvsroot/com.ibm.optim.aa.siebel.dg.2.5.2/optimsa/java/com/pst/optim/siebel/SiebelRestore.java,v 1.12 2009-07-21 18:02:06 ehan Exp $";
    private String guid;
    private String tempPath;
    private String restoreRowId;
    private String table;
    private String row_id;
    private String match_field;
    private String match_value;
    private String dbOwner;
    public static final String[] PREFIXES;
    private Logger logr;
    private int ret_value;
    private ArrayList<String> columnNames;
    
    public String getTempPath() {
        return this.tempPath;
    }
    
    public void setTempPath(final String tempPath) {
        this.tempPath = tempPath;
    }
    
    public String getRestoreRowId() {
        return this.restoreRowId;
    }
    
    public void setRestoreRowId(final String restoreRowId) {
        this.restoreRowId = restoreRowId;
    }
    
    public String getMatch_field() {
        return this.match_field;
    }
    
    public void setMatch_field(final String match_field) {
        this.match_field = match_field;
    }
    
    public SiebelRestore() {
        this.logr = Logger.getLogger((Class)SiebelRestore.class);
        this.columnNames = new ArrayList<String>();
    }
    
    public void parseParams(final Element element) throws IOException {
        this.tempPath = element.getAttribute("temppath");
        this.logr.info((Object)("Got the temp path to resotre : " + this.tempPath));
        final NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            final Node item = childNodes.item(i);
            if (item.getNodeName().equals("filter")) {
                this.guid = ((Element)item).getAttribute("GUID");
                final NodeList childNodes2 = item.getChildNodes();
                for (int j = 0; j < childNodes2.getLength(); ++j) {
                    final Node item2 = childNodes2.item(j);
                    if (item2.getNodeName().equals("table")) {
                        this.table = ((Element)item2).getAttribute("name");
                        this.row_id = ((Element)item2).getAttribute("index");
                        final NodeList childNodes3 = item2.getChildNodes();
                        for (int k = 0; k < childNodes3.getLength(); ++k) {
                            final Node item3 = childNodes3.item(k);
                            if (item3.getNodeName().equals("field")) {
                                this.match_field = ((Element)item3).getAttribute("name");
                                this.match_value = "";
                                if (((Element)item3).getAttribute("operator").equals("=")) {
                                    this.match_value = ((Element)item3).getAttribute("value");
                                }
                                if (this.match_field.equals("ROW_ID")) {
                                    this.restoreRowId = this.match_value;
                                    this.logr.info((Object)("Got the restore row_id : " + this.restoreRowId));
                                }
                                this.logr.info((Object)(this.match_field + " 1-1 " + this.match_value + " 2-2 " + this.table));
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void restore(final String s, final String s2, final String s3, final Element element, final boolean b, final String s4, final boolean b2) throws FileNotFoundException, IOException, NamingException, SQLException, Exception {
        this.logr.info((Object)"Creating SiebelRestore");
        try {
            SiebelConstants.STATUS_MESSAGE = "Restore Completed";
            this.ret_value = 0;
            this.parseParams(element);
        }
        catch (IOException ex) {
            this.ret_value = -1;
            SiebelConstants.STATUS_MESSAGE = "Restore Failed";
            this.logr.error((Object)"Error parsing parameters for SiebelRestore", (Throwable)ex);
        }
        final Connection connection = this.getConnection(s3, s, this.guid, s2, b2);
        if (connection != null) {
            this.logr.info((Object)"Connected to Archive file");
        }
        else {
            this.logr.info((Object)"Connection to Archive file failed");
        }
        if (this.runQueryRowID(this.match_value, this.dbOwner, this.table).equals("")) {
            this.logr.info((Object)"Row ID is not present at siebel attachment table ");
            this.restoreFile(this.runQuery(connection), b, s4);
        }
        else {
            this.logr.info((Object)"Row ID is present at siebel attachment table ");
            this.ret_value = -1;
            SiebelConstants.STATUS_MESSAGE = "Restore Failed";
        }
    }
    
    private ResultSet runQuery(final Connection connection) {
        ResultSet executeQuery = null;
        try {
            final String string = "select * from " + this.table + " where " + this.match_field + " = '" + this.match_value + "'";
            this.logr.info((Object)("Running SQL Query : " + string));
            executeQuery = connection.createStatement().executeQuery(string);
            final DatabaseMetaData metaData = connection.getMetaData();
            this.columnNames.clear();
            final ResultSet columns = metaData.getColumns(null, null, this.table, null);
            while (columns.next()) {
                final String string2 = columns.getString("COLUMN_NAME");
                if (!this.isPseudoField(string2)) {
                    this.columnNames.add(string2);
                }
            }
        }
        catch (SQLException ex) {
            this.ret_value = -1;
            SiebelConstants.STATUS_MESSAGE = "Restore Failed";
            this.logr.error((Object)"OSA-02: SQL Error on Siebel Attachment table", (Throwable)ex);
        }
        return executeQuery;
    }
    
    private void restoreFile(final ResultSet set, final boolean b, String str) throws FileNotFoundException, IOException, NamingException, SQLException, Exception {
        InputStream binaryStream = null;
        OutputStream outputStream = null;
        String substring = "";
        final ResultSetMetaData metaData = set.getMetaData();
        final int columnCount = metaData.getColumnCount();
        int n = 24;
        int n2 = 25;
        for (int i = 1; i <= columnCount; ++i) {
            final String columnName = metaData.getColumnName(i);
            if (columnName.endsWith("ATT_FILE_NAME")) {
                n = i;
            }
            if (columnName.endsWith("ATT_BLOB")) {
                n2 = i;
            }
        }
        try {
            this.logr.info((Object)"Fetching data from Archive file.");
            while (set.next()) {
                if (set.getString("FILE_SRC_TYPE").equalsIgnoreCase("FILE")) {
                    final String name = new File(set.getString(n)).getName();
                    try {
                        substring = System.getProperty("os.name").substring(0, 7);
                    }
                    catch (Exception ex4) {}
                    if (substring.equalsIgnoreCase("Windows")) {
                        str = this.tempPath + "\\" + this.restoreRowId + "\\Att\\";
                    }
                    else {
                        str = this.tempPath + "//" + this.restoreRowId + "//Att//";
                    }
                    final String string = str + name;
                    this.logr.info((Object)("Restore Path : " + string));
                    final File file = new File(string);
                    if (!file.getParentFile().mkdirs()) {
                        throw new IllegalArgumentException("Invalid Path");
                    }
                    binaryStream = set.getBinaryStream(n2);
                    outputStream = new FileOutputStream(file);
                    final byte[] array = new byte[1024];
                    int read;
                    while ((read = binaryStream.read(array)) > 0) {
                        outputStream.write(array, 0, read);
                    }
                }
                this.updateSiebel(set);
            }
        }
        catch (SQLException ex) {
            this.ret_value = -1;
            SiebelConstants.STATUS_MESSAGE = "Restore Failed";
            this.logr.error((Object)"OSA-02: SQL Error on Siebel Attachment table", (Throwable)ex);
        }
        catch (IllegalArgumentException ex2) {
            this.ret_value = -1;
            SiebelConstants.STATUS_MESSAGE = "Restore Failed";
            this.logr.error((Object)"OSA-06: Invalid Siebel Attachment path", (Throwable)ex2);
        }
        catch (Exception ex3) {
            this.ret_value = -1;
            SiebelConstants.STATUS_MESSAGE = "Restore Failed";
            this.logr.error((Object)"OSA-09: Invalid Attachment restore location", (Throwable)ex3);
            ex3.printStackTrace();
        }
        finally {
            if (set != null) {
                set.close();
            }
            if (binaryStream != null) {
                binaryStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
    
    public void updateRestoreStatus(final long n) throws Exception {
        this.logr.info((Object)("Updating Restore Status to " + SiebelConstants.STATUS_MESSAGE));
        new StatusThread(this.row_id, n, this.getDbOwner()).start();
    }
    
    private void updateSiebel(final ResultSet set) throws NamingException, SQLException, Exception {
        Connection connection = null;
        final Statement statement = null;
        final DataSource dataSource = (DataSource)new InitialContext().lookup("java:comp/env/jdbc/SiebelDataSource");
        try {
            connection = dataSource.getConnection();
            final String generateStatement = this.generateStatement();
            final PreparedStatement setInsertValues = this.setInsertValues(set, connection.prepareStatement(generateStatement));
            this.logr.info((Object)("SQL: " + generateStatement));
            setInsertValues.executeUpdate();
            connection.commit();
        }
        catch (Exception ex) {
            this.ret_value = -1;
            SiebelConstants.STATUS_MESSAGE = "Restore Failed";
            this.logr.error((Object)"OSA-04: SQL Error on insert query for Siebel Attachment table");
            ex.printStackTrace();
        }
        finally {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    
    private String generateStatement() throws SQLException {
        this.logr.info((Object)"Creating SQL Insert statement.");
        final StringBuffer sb = new StringBuffer("INSERT INTO ");
        sb.append(this.getDbOwner()).append(".").append(this.table).append(" (");
        int n = 0;
        for (int i = 0; i < this.columnNames.size(); ++i) {
            sb.append(this.columnNames.get(i)).append(", ");
            ++n;
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(") VALUES (");
        for (int j = 0; j < n; ++j) {
            if (j < n - 1) {
                sb.append("?, ");
            }
            else {
                sb.append("?)");
            }
        }
        return sb.toString();
    }
    
    private PreparedStatement setInsertValues(final ResultSet set, final PreparedStatement preparedStatement) throws SQLException {
        this.logr.info((Object)"Binding values for Insert query");
        final ResultSetMetaData metaData = set.getMetaData();
        int n = 1;
        for (int i = 0; i < this.columnNames.size(); ++i) {
            switch (metaData.getColumnType(n)) {
                case 12: {
                    preparedStatement.setString(n, set.getString(this.columnNames.get(i)));
                    break;
                }
                case 1: {
                    preparedStatement.setString(n, set.getString(this.columnNames.get(i)));
                    break;
                }
                case -1: {
                    preparedStatement.setString(n, set.getString(this.columnNames.get(i)));
                    break;
                }
                case 93: {
                    if (set.getString(this.columnNames.get(i)) == null) {
                        preparedStatement.setString(n, null);
                        break;
                    }
                    preparedStatement.setTimestamp(n, Timestamp.valueOf(set.getString(this.columnNames.get(i))));
                    break;
                }
                case 91: {
                    if (set.getString(this.columnNames.get(i)) == null) {
                        preparedStatement.setString(n, null);
                        break;
                    }
                    preparedStatement.setTimestamp(n, Timestamp.valueOf(set.getString(this.columnNames.get(i))));
                    break;
                }
                case 92: {
                    if (set.getString(this.columnNames.get(i)) == null) {
                        preparedStatement.setString(n, null);
                        break;
                    }
                    preparedStatement.setTimestamp(n, Timestamp.valueOf(set.getString(this.columnNames.get(i))));
                    break;
                }
                default: {
                    preparedStatement.setInt(n, set.getInt(this.columnNames.get(i)));
                    break;
                }
            }
            ++n;
        }
        return preparedStatement;
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
        boolean equalsIgnoreCase10 = false;
        try {
            equalsIgnoreCase = s.substring(0, SiebelRestore.PREFIXES[0].length()).equalsIgnoreCase(SiebelRestore.PREFIXES[0]);
        }
        catch (StringIndexOutOfBoundsException ex) {}
        try {
            equalsIgnoreCase2 = s.substring(0, SiebelRestore.PREFIXES[1].length()).equalsIgnoreCase(SiebelRestore.PREFIXES[1]);
        }
        catch (StringIndexOutOfBoundsException ex2) {}
        try {
            equalsIgnoreCase3 = s.substring(0, SiebelRestore.PREFIXES[2].length()).equalsIgnoreCase(SiebelRestore.PREFIXES[2]);
        }
        catch (StringIndexOutOfBoundsException ex3) {}
        try {
            equalsIgnoreCase4 = s.substring(0, SiebelRestore.PREFIXES[3].length()).equalsIgnoreCase(SiebelRestore.PREFIXES[3]);
        }
        catch (StringIndexOutOfBoundsException ex4) {}
        try {
            equalsIgnoreCase5 = s.substring(0, SiebelRestore.PREFIXES[4].length()).equalsIgnoreCase(SiebelRestore.PREFIXES[4]);
        }
        catch (StringIndexOutOfBoundsException ex5) {}
        try {
            equalsIgnoreCase6 = s.substring(0, SiebelRestore.PREFIXES[5].length()).equalsIgnoreCase(SiebelRestore.PREFIXES[5]);
        }
        catch (StringIndexOutOfBoundsException ex6) {}
        try {
            equalsIgnoreCase7 = s.substring(0, SiebelRestore.PREFIXES[6].length()).equalsIgnoreCase(SiebelRestore.PREFIXES[6]);
        }
        catch (StringIndexOutOfBoundsException ex7) {}
        try {
            equalsIgnoreCase8 = s.substring(0, SiebelRestore.PREFIXES[7].length()).equalsIgnoreCase(SiebelRestore.PREFIXES[7]);
        }
        catch (StringIndexOutOfBoundsException ex8) {}
        try {
            equalsIgnoreCase9 = s.substring(0, SiebelRestore.PREFIXES[8].length()).equalsIgnoreCase(SiebelRestore.PREFIXES[8]);
        }
        catch (StringIndexOutOfBoundsException ex9) {}
        try {
            equalsIgnoreCase10 = s.substring(0, SiebelRestore.PREFIXES[9].length()).equalsIgnoreCase(SiebelRestore.PREFIXES[9]);
        }
        catch (StringIndexOutOfBoundsException ex10) {}
        return equalsIgnoreCase | equalsIgnoreCase2 | equalsIgnoreCase3 | equalsIgnoreCase4 | equalsIgnoreCase5 | equalsIgnoreCase6 | equalsIgnoreCase7 | equalsIgnoreCase8 | equalsIgnoreCase9 | equalsIgnoreCase10;
    }
    
    public Connection getConnection(final String s, final String s2, final String s3, final String s4, final boolean b) {
        this.logr.info((Object)"Establishing connection with Archive File");
        return PSTConnector.getConnector(s).getConnection(s2, (String)null, s3, s4, b);
    }
    
    private String runQueryRowID(final String str, final String str2, final String str3) {
        String string = "";
        try {
            final Connection connection = ((DataSource)new InitialContext().lookup("java:comp/env/jdbc/SiebelDataSource")).getConnection();
            final String string2 = "select ROW_ID from " + str2 + "." + str3 + " where ROW_ID = '" + str + "'";
            this.logr.info((Object)("Running SQL Query : " + string2));
            final ResultSet executeQuery = connection.createStatement().executeQuery(string2);
            while (executeQuery.next()) {
                string = executeQuery.getString("ROW_ID");
            }
            connection.close();
        }
        catch (Exception ex) {
            this.logr.info((Object)"OSA-02: SQL Error on Siebel attachment table for rowid exist : ", (Throwable)ex);
        }
        this.logr.info((Object)("Row ID from Siebel attachment table : " + string));
        return string;
    }
    
    public String getGuid() {
        return this.guid;
    }
    
    public void setGuid(final String guid) {
        this.guid = guid;
    }
    
    public String getTable() {
        return this.table;
    }
    
    public void setTable(final String table) {
        this.table = table;
    }
    
    public String getRow_id() {
        return this.row_id;
    }
    
    public void setRow_id(final String row_id) {
        this.row_id = row_id;
    }
    
    public String getMatch_value() {
        return this.match_value;
    }
    
    public void setMatch_value(final String match_value) {
        this.match_value = match_value;
    }
    
    public String getDbOwner() {
        return this.dbOwner;
    }
    
    public void setDbOwner(final String dbOwner) {
        this.dbOwner = dbOwner;
        this.logr.info((Object)("DBOwner name: " + this.dbOwner));
    }
    
    public int getReturnValue() {
        return this.ret_value;
    }
    
    static {
        PREFIXES = new String[] { "SRATT", "OPTYATT", "OPPTY_ATT", "OPTY_ATT", "ACTATT", "PRDATT", "ORDATT", "QTEATT", "IBMATT", "PST" };
    }
}