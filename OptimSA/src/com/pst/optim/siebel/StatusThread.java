package com.pst.optim.siebel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class StatusThread extends Thread
{
    public static final String COPYRIGHT = "(C) Copyright IBM Corp. 2008";
    public static final String HEADER = "$Header: /users1/aa/cvsroot/com.ibm.optim.aa.siebel.dg.2.5.2/optimsa/java/com/pst/optim/siebel/StatusThread.java,v 1.7 2009-07-21 18:02:06 ehan Exp $";
    private String rowID;
    private long sleepTime;
    private String dbOwner;
    
    public StatusThread(final String rowID, final long sleepTime, final String dbOwner) {
        this.rowID = "";
        this.sleepTime = 0L;
        this.dbOwner = "SIEBEL";
        this.rowID = rowID;
        this.sleepTime = sleepTime;
        this.dbOwner = dbOwner;
    }
    
    @Override
    public void run() {
        PreparedStatement prepareStatement = null;
        Connection connection = null;
        try {
            Thread.currentThread();
            Thread.sleep(this.sleepTime);
            connection = ((DataSource)new InitialContext().lookup("java:comp/env/jdbc/SiebelDataSource")).getConnection();
            prepareStatement = connection.prepareStatement("UPDATE " + this.dbOwner + "." + "CX_RESTORE_ATT" + " SET STATUS = ? WHERE ROW_ID = ?");
            prepareStatement.setString(1, SiebelConstants.STATUS_MESSAGE);
            prepareStatement.setString(2, this.rowID);
            prepareStatement.executeUpdate();
            connection.commit();
        }
        catch (Exception ex) {}
        finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (prepareStatement != null) {
                    prepareStatement.close();
                }
            }
            catch (Exception ex2) {}
        }
    }
}