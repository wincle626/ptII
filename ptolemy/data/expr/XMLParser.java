/* Generated By:JJTree&JavaCC: Do not edit this line. PtParser.java */
/*
 Copyright (c) 2003-2004 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;

import org.w3c.dom.*;



//////////////////////////////////////////////////////////////////////
//// XmlParser.java
/**
This class parses a XML string token to a DOM document.


@author Yang Zhao
@version $Id$
@since Ptolemy II 4.0

*/
public class XMLParser {

    public XMLParser() throws Exception {

        if ( _documentBuilderFactory==null )
            _documentBuilderFactory = DocumentBuilderFactory.newInstance();
            _documentBuilder = _documentBuilderFactory.newDocumentBuilder();
    }

        /** Generate the document tree for the specified XML string. The Document
         *  (the root of the document tree) is
         *  returned. An exception will be thrown if the parse fails.
         *  @param str The XML string to be parsed.
     *  @exception Exception If the parse fails.
     *  @return The document for the parse tree.
         */
    public Document parser(String str) throws Exception {
        InputStream is = (InputStream) new StringBufferInputStream(str);
        //System.out.println("--- the inputStream of the XmlToken is: " + is.toString() + "\n");
        return parser(is);
    }

        /** Generate the document tree for the specified input stream.
         * The Document (the root of the document tree) is
         *  returned. An exception will be thrown if the parse fails.
         *  @param is The input steam to be parsed.
         *  @exception Exception If the parse fails.
         *  @return The document for the parse tree.
         */
    public Document parser(InputStream is) throws Exception {
        return _documentBuilder.parse(is);
    }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////
        private DocumentBuilderFactory _documentBuilderFactory;
    private DocumentBuilder _documentBuilder;
}
