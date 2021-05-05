package com.pst.optim.siebel;

public class SiebelConstants
{
    public static final String COPYRIGHT = "(C) Copyright IBM Corp. 2008";
    public static final String HEADER = "$Header: /users1/aa/cvsroot/com.ibm.optim.aa.siebel.dg.2.5.2/optimsa/java/com/pst/optim/siebel/SiebelConstants.java,v 1.7 2009-07-21 18:02:06 ehan Exp $";
    public static final int COLIND_ATT_FILE_NAME = 24;
    public static final int COLIND_ATT_BLOB = 25;
    public static final String ATT_FILE_NAME = "ATT_FILE_NAME";
    public static final String ATT_BLOB = "ATT_BLOB";
    public static final String ROW_ID = "ROW_ID";
    public static final String CREATED = "CREATED";
    public static final String CREATED_BY = "CREATED_BY";
    public static final String LAST_UPD = "LAST_UPD";
    public static final String LAST_UPD_BY = "LAST_UPD_BY";
    public static final String MODIFICATION_NUM = "MODIFICATION_NUM";
    public static final String CONFLICT_ID = "CONFLICT_ID";
    public static final String PAR_ROW_ID = "PAR_ROW_ID";
    public static final String FILE_AUTO_UPD_FLG = "FILE_AUTO_UPD_FLG";
    public static final String FILE_DEFER_FLG = "FILE_DEFER_FLG";
    public static final String FILE_DOCK_REQ_FLG = "FILE_DOCK_REQ_FLG";
    public static final String FILE_DOCK_STAT_FLG = "FILE_DOCK_STAT_FLG";
    public static final String FILE_NAME = "FILE_NAME";
    public static final String DB_LAST_UPD = "DB_LAST_UPD";
    public static final String FILE_DATE = "FILE_DATE";
    public static final String FILE_SIZE = "FILE_SIZE";
    public static final String COMMENTS = "COMMENTS";
    public static final String DB_LAST_UPD_SRC = "DB_LAST_UPD_SRC";
    public static final String FILE_EXT = "FILE_EXT";
    public static final String FILE_REV_NUM = "FILE_REV_NUM";
    public static final String FILE_SRC_PATH = "FILE_SRC_PATH";
    public static final String FILE_SRC_TYPE = "FILE_SRC_TYPE";
    public static final String ATT_STATUS_TABLE = "CX_RESTORE_ATT";
    public static final String STATUS_COMPLETE = "Restore Completed";
    public static final String DB_OWNER = "SIEBEL";
    public static String STATUS_MESSAGE;
    
    static {
        SiebelConstants.STATUS_MESSAGE = "Restore Completed";
    }
}