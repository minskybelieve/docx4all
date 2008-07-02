/*
 *  Copyright 2008, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4all.

    Docx4all is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4all is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4all.  If not, see <http://www.gnu.org/licenses/>.
    
 */
package org.plutext.client.state;

import java.io.StringReader;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.wml.Id;
import org.docx4j.wml.SdtBlock;

/* Represent the state of a chunk.
 * 
 * Intent is to be able to compare the
 * state at a time we might save it to the
 * server with some previously known "clean" state.
 * 
 * We never directly look at the content
 * control itself 
 */ 
public class StateChunk 
{
	
	// TODO - have to be able to update the SdtBlock,
	// I expect.
	

	private static Logger log = Logger.getLogger(StateChunk.class);

    public StateChunk(SdtBlock cc) {
		this.cc = cc;
		xml = getContentControlXML(cc);    	
    }
    
	SdtBlock cc;
    public SdtBlock getSdt() {
    	return cc; 
    }
    

	public String getIdAsString() {
		return cc.getSdtPr().getId().getVal().toString();
	}
	
	public Id getId() {
		return cc.getSdtPr().getId();
	}

	
    // The tag of the wrapped SDT
    protected String tag;
//        public String Tag
//        {
//            get { return tag; }
//        }

    private String xml = null;
    public String getXml() {
    	return xml; 
    }
            
//            set { xml = value; }
//        }
	/*
	 * cc.Range.WordOpenXML returns an XML document which contains all
	 * associates Parts eg style.xml etc. But all we want is the XML for the
	 * content control itself!
	 */
	public static String getContentControlXML(SdtBlock cc) {

		boolean suppressDeclaration = true;
		return org.docx4j.XmlUtils.marshaltoString(cc, suppressDeclaration);

		// return "<w:sdt
		// xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
		// + node.InnerXml + "</w:sdt>";
	}
    

    private Boolean transformUpdatesExist = false;
        // TODO - reset this to false each time thread starts

    public Boolean containsTrackedChanges()
    {
        if (xml.contains("w:del")
            || xml.contains("w:delText")
            || xml.contains("w:ins"))
            return true;
        else
            return false;

    }

    public void acceptTrackedChanges()
    {
        transform("ChangesAccept.xslt");
    }
    public void rejectTrackedChanges()
    {
        transform("ChangesReject.xslt");
    }

    private void transform(String stylesheet)
    {
        log.debug("In: " + xml);
        
        
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(sw); 
        
		try {
			StreamSource src = new StreamSource(new StringReader(xml));
			java.io.InputStream xslt = 
				org.docx4j.utils.ResourceUtils.getResource("org/plutext/client/state/" + stylesheet);
				//org.docx4j.utils.ResourceUtils.getResource("org/docx4j/diff/diffx2html.xslt");
			Map<String, Object> transformParameters = new java.util.HashMap<String, Object>();
			XmlUtils.transform(src, xslt, transformParameters, result);
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}
        


        // Ouptut
        xml = sw.toString();

        log.debug("Transformed (" + stylesheet + "): " + xml);
    }



//    public Word.ContentControl getContentControl()
//    {
//        foreach (Word.ContentControl ctrl in Globals.ThisAddIn.Application.ActiveDocument.ContentControls)
//        {
//            if (ctrl.ID.Equals(id) )
//            {
//                //diagnostics("DEBUG - Got control");
//                return ctrl;
//            }
//        }
//        return null;
//    }

}
