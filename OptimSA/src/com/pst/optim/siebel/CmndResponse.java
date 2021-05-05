package com.pst.optim.siebel;

import java.io.OutputStream;
import java.io.IOException;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import java.io.Writer;
import org.w3c.dom.Text;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.DOMImplementation;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Document;

public class CmndResponse
{
    public static final String COPYRIGHT = "(C)Copyright IBM Corp. 2007";
    private String table_name;
    private String guid;
    private String row_id;
    private String par_row_id;
    private String status;
    private static final String DOCROOT = "ListOfPstAttachmentRestore";
    private static final String RESTORE = "PstRestoreAttachment";
    private static final String ID = "Id";
    private static final String CREATED = "Created";
    private static final String UPDATED = "Updated";
    private static final String CONFLICTID = "ConflictId";
    private static final String MODID = "ModId";
    private static final String GUID = "Guid";
    private static final String PARROWID = "ParRowId";
    private static final String RESTROREBCNAME = "RestoreBCName";
    private static final String STATUS = "Status";
    private Document out;
    public static final String SCCS = "$Header: /users1/aa/cvsroot/com.ibm.optim.aa.siebel.dg.2.5.2/optimsa/java/com/pst/optim/siebel/CmndResponse.java,v 1.5 2009-06-29 18:46:31 ehan Exp $";
    
    public CmndResponse(final String table_name, final String guid, final String par_row_id, final String row_id, final String status) {
        this.table_name = "";
        this.guid = "";
        this.row_id = "";
        this.par_row_id = "";
        this.status = "";
        this.out = null;
        this.table_name = table_name;
        this.guid = guid;
        this.par_row_id = par_row_id;
        this.row_id = row_id;
        this.status = status;
        this.out = this.createDocument();
        this.buildDocument();
    }
    
    private Document createDocument() {
        final DOMImplementation domImplementation = DOMImplementationImpl.getDOMImplementation();
        return domImplementation.createDocument(null, "ListOfPstAttachmentRestore", domImplementation.createDocumentType("XML", null, null));
    }
    
    private void buildDocument() {
        final Element documentElement = this.out.getDocumentElement();
        final Element element = this.out.createElement("PstRestoreAttachment");
        element.setAttribute("operation", "Upsert");
        element.setAttribute("searchspec", "String");
        documentElement.appendChild(element);
        final Element element2 = this.out.createElement("Id");
        final Element element3 = this.out.createElement("Created");
        final Element element4 = this.out.createElement("Updated");
        final Element element5 = this.out.createElement("ConflictId");
        final Element element6 = this.out.createElement("ModId");
        final Element element7 = this.out.createElement("Guid");
        final Element element8 = this.out.createElement("ParRowId");
        final Element element9 = this.out.createElement("RestoreBCName");
        final Element element10 = this.out.createElement("Status");
        final Text textNode = this.out.createTextNode("Text");
        element.appendChild(element2);
        element2.appendChild(this.out.createTextNode(this.row_id));
        element.appendChild(element3);
        element3.appendChild(textNode);
        element.appendChild(element4);
        element4.appendChild(textNode.cloneNode(false));
        element.appendChild(element5);
        element5.appendChild(textNode.cloneNode(false));
        element.appendChild(element6);
        element6.appendChild(textNode.cloneNode(false));
        element.appendChild(element7);
        element7.appendChild(this.out.createTextNode(this.guid));
        element.appendChild(element8);
        element8.appendChild(this.out.createTextNode(this.par_row_id));
        element.appendChild(element9);
        element9.appendChild(this.out.createTextNode(this.table_name));
        element.appendChild(element10);
        element10.appendChild(this.out.createTextNode(this.status));
    }
    
    public void getXML(final Writer writer, final Document document) throws IOException {
        final XMLSerializer xmlSerializer = new XMLSerializer(writer, new OutputFormat(document));
        xmlSerializer.asDOMSerializer();
        xmlSerializer.serialize(document);
    }
    
    public void getXML(final OutputStream outputStream, final Document document) throws IOException {
        final XMLSerializer xmlSerializer = new XMLSerializer(outputStream, new OutputFormat(document));
        xmlSerializer.asDOMSerializer();
        xmlSerializer.serialize(document);
    }
    
    public Document getDocument() {
        return this.out;
    }
}