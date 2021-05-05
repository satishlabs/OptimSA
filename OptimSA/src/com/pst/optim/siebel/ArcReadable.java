package com.pst.optim.siebel;

import java.sql.Connection;
import java.io.OutputStream;
import java.io.IOException;
import org.w3c.dom.Document;
import java.io.Writer;
import java.io.InputStream;

public interface ArcReadable
{
    public static final String COPYRIGHT = "(C)Copyright IBM Corp. 2007";
    public static final String HEADER = "$Header: /users1/aa/cvsroot/com.ibm.optim.aa.siebel.dg.2.5.2/optimsa/java/com/pst/optim/siebel/ArcReadable.java,v 1.5 2009-06-29 18:46:31 ehan Exp $";
    
    String initArc(final InputStream p0, final String p1, final String p2, final String p3, final String p4, final String p5, final boolean p6, final boolean p7);
    
    void getXML(final Writer p0, final Document p1) throws IOException;
    
    void getXML(final OutputStream p0, final Document p1) throws IOException;
    
    Document getOutputDocument();
    
    String getDataSource();
    
    String getGUID();
    
    String getTable();
    
    Connection getConnection(final String p0, final String p1, final String p2, final String p3, final String p4, final boolean p5, final boolean p6);
}