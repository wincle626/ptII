/* A parser for MoML (model markup language)

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.moml;

// Ptolemy imports.
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.expr.Variable;

// Java imports.
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.io.InputStream;
import java.io.FileInputStream;

// XML imports.
import com.microstar.xml.*;


//////////////////////////////////////////////////////////////////////////
//// MoMLParser
/**
This class constructs Ptolemy II models from specifications
in MoML (model markup language), which is based on XML.
The class contains an instance of the Microstar &AElig;lfred XML
parser and implements callback methods to interpret the parsed XML.
The way to use this class is to call its parse() method.
The returned value is top-level composite entity of the model.

@author Edward A. Lee, Steve Neuendorffer, John Reekie
@version $Id$
*/
public class MoMLParser extends HandlerBase {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle an attribute assignment that is part of an XML element.
     *  This method is called prior to the corresponding startElement()
     *  call, so it simply accumulates attributes in a hashtable for
     *  use by startElement().
     *  @param name The name of the attribute.
     *  @param value The value of the attribute, or null if the attribute
     *   is <code>#IMPLIED</code> and not specified.
     *  @param specified True if the value is specified, false if the
     *   value comes from the default value in the DTD rather than from
     *   the XML file.
     *  @exception XmlException If the name or value is null.
     */
    public void attribute(String name, String value, boolean specified)
            throws XmlException {
        if(DEBUG) {
            System.out.println(
                    "Attribute name = " + name + 
                    ", value = " + value + 
                    ", specified = " + specified);
        }
        if(name == null) throw new XmlException("Attribute has no name",
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
        if(value == null) throw new XmlException("Attribute with name " + 
                name + " has no value",
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
        _attributes.put(name, value);
    }

    /** Handle character data.  In this implementation, the
     *  character data is accumulated in a buffer until the
     *  end element.  Character data appears only in doc elements.
     *  &AElig;lfred will call this method once for each chunk of
     *  character data found in the contents of elements.  Note that
     *  the parser may break up a long sequence of characters into
     *  smaller chunks and call this method once for each chunk.
     *  @param chars The character data.
     *  @param offset The starting position in the array.
     *  @param length The number of characters available.
     */
    public void charData(char[] chars, int offset, int length) {
        _currentCharData.append(chars, offset, length);
    }

    /** End the document.  In this implementation, do nothing.
     *  &AElig;lfred will call this method once, when it has
     *  finished parsing the XML document.
     *  It is guaranteed that this will be the last method called.
     */
    public void endDocument() throws Exception {
    }

    /** End an element. This method pops the current container from
     *  the stack, if appropriate, and also adds specialized attributes
     *  to the container, such as <i>_doc</i>, if appropriate.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {
        if(DEBUG) System.out.println("Ending Element:" + elementName);
        if (elementName.equals("doc")) {
            // Use the special attribute name "_doc" to contain the text.
            // If the attribute already exists, remove it firts.
            DocAttribute doc
                    = new DocAttribute(_current, _currentCharData.toString());
        } else if (elementName.equals("director")
                || elementName.equals("actor")) {
            _current = (NamedObj)_containers.pop();
        } else if (elementName.equals("connection")) {
            _currentConnection = null;
        }
    }

    /**
     * Implement com.microstr.xml.XMLHandler.endExternalEntity
     * move up one leve in the entity tree.
     */
    public void endExternalEntity(String URI) throws Exception {
        String current = _currentExternalEntity();
        if(DEBUG)
            System.out.println("endExternalEntity: URI=\"" + URI + "\"\n");
/* FIXME
        if(!current.equals(URI))
            throw new XmlException("Entities out of order",
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        sysids.removeFirst();
*/
    }

    /**
     * Implement com.microstar.xml.XMLHandler.error
     * @throws XmlException if called.
     */
    public void error(String message, String sysid,
            int line, int column) throws XmlException {
        if (DEBUG) {
            System.out.println("XML error at line " + line + ", column "
            + column + " of " + sysid);
        }
        throw new XmlException(message, sysid, line, column);
    }

    /** Parse the MoML file with the given URL.
     *  @param url The URL for an a MoML file.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @throws Exception if the parser fails.
     */
    public TypedCompositeActor parse(String url) throws Exception {
        _parser.setHandler(this);
        _parser.parse(url, null, (String)null);
        return _toplevel;
    }

    /** Parse the given stream, using the specified url as the context
     *  to expand any external references within the MoML file.
     *  For example, the context might be the document base of an
     *  applet.
     *  @param url The context URL.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @throws Exception if the parser fails.
     */
    public TypedCompositeActor parse(String url, InputStream input)
            throws Exception {
        _parser.setHandler(this);
        _parser.parse(url, null, input, null);
        return _toplevel;
    }

    /**
     * Implement com.microstar.xml.XMLHandler.resolveEntity
     * If no public ID is given, then return the system ID.
     * Otherwise, construct a local absolute URL by appending the
     * public ID to the location of the XML files.
     */
    public Object resolveEntity(String pubID, String sysID)
            throws Exception {
        if (DEBUG) {
            System.out.println("resolveEntity: " + pubID + " : " + sysID);
        }
        String result;
        StringBuffer dtdPath = new StringBuffer();
        // Use System ID if the public one is unknown.
        if(pubID == null) {
            result = sysID;
        } else {

            // Construct the path to the DTD file. The PTII root MUST be
            // defined as a system property (this can be done by using
            // the -D option to java.
            dtdPath = new StringBuffer(System.getProperty("PTII"));
            System.out.println("dtdPath = " + dtdPath);
            
            //// FIXME FIXME
            //// StringBuffer dtdPath = new StringBuffer(DomainLibrary.getPTIIRoot());
            // StringBuffer dtdPath = new StringBuffer("/users/ptII");
            
            // Use System ID if there's no PTII environment variable
            if(dtdPath.toString().equals("UNKNOWN")) {
                result = sysID;
            } else {

                // Always use slashes as file separator, since this is a URL
                //String fileSep = java.lang.System.getProperty("file.separator");
                String fileSep = "/";
                
                // Construct the URL
                int last = dtdPath.length()-1;
                if (dtdPath.charAt(last) != fileSep.charAt(0)) {
                    dtdPath.append(fileSep);
                }
                dtdPath.append("ptolemy" + fileSep + "schematic" + fileSep);
                dtdPath.append("lib" + fileSep + pubID);
                
                // Windows is special. Very special.
                if (System.getProperty("os.name").equals("Windows NT")) {
                    result = "file:/" + dtdPath;
                } else {
                    result = "file:" + dtdPath;
                }
            }
        }
        if (DEBUG) System.out.println("resolveEntity result: " + dtdPath);
        return result;
    }

    /** Start a document.  This method is called just before the parser
     *  attempts to read the first entity (the root of the document).
     *  It is guaranteed that this will be the first method called.
     */
    public void startDocument() {
        if(DEBUG) System.out.println("-- Starting Document.");
        _attributes = new HashMap();
        _toplevel = null;
    }

    /** Start an element.
     *  This is called at the beginning of each XML
     *  element.  By the time it is called, all of the attributes
     *  for the element will already have been reported using the
     *  attribute() method.  Unrecognized elements are ignored.
     *  @param elementName The element type name.
     *  @exception XmlException If the element produces an error
     *   in constructing the model.
     */
    public void startElement(String elementName)
            throws XmlException {
        if(DEBUG) System.out.println("Starting Element:" + elementName);
        try {
            if (elementName.equals("model")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"model\"");
                String modelName = (String)_attributes.get("name");
                _checkForNull(modelName, "No name for element \"model\"");
                // NOTE: Workspace has no name.
                _workspace = new Workspace();
                Class toplevelClass = Class.forName(className);
                Class[] argTypes = new Class[1];
                argTypes[0] = Workspace.class;
                Constructor toplevelConstructor
                        = toplevelClass.getConstructor(argTypes);
                Object[] arguments = new Object[1];
                arguments[0] = _workspace;
                _toplevel = (TypedCompositeActor)
                        toplevelConstructor.newInstance(arguments);
                _manager = new Manager(_workspace, "manager");
                _toplevel.setName(modelName);
                _toplevel.setManager(_manager);
                _current = _toplevel;
            } else if (elementName.equals("director")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"director\"");
                String dirName = (String)_attributes.get("name");
                _checkForNull(dirName, "No name for element \"director\"");
                Class dirClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = CompositeActor.class;
                argTypes[1] = String.class;
                Constructor dirConstructor
                        = dirClass.getConstructor(argTypes);
                Object[] arguments = new Object[2];
                arguments[0] = _current;
                arguments[1] = dirName;
                _containers.push(_current);
                _current = (NamedObj)dirConstructor.newInstance(arguments);
            } else if (elementName.equals("actor")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"actor\"");
                String actorName = (String)_attributes.get("name");
                _checkForNull(actorName, "No name for element \"actor\"");

                // Get a constructor for the actor.
                Class actorClass = Class.forName(className);
                Class[] argTypes = new Class[2];
                argTypes[0] = TypedCompositeActor.class;
                argTypes[1] = String.class;
                Constructor actorConstructor
                        = actorClass.getConstructor(argTypes);

                // Invoke the constructor.
                Object[] arguments = new Object[2];
                _checkClass(_current, TypedCompositeActor.class,
                        "Element \"actor\" found inside an element that "
                        + "is not a TypedCompositeActor.");
                arguments[0] = (TypedCompositeActor)_current;
                arguments[1] = actorName;
                _containers.push(_current);
                _current = (NamedObj)actorConstructor.newInstance(arguments);
            } else if (elementName.equals("parameter")) {
                String paramName = (String)_attributes.get("name");
                _checkForNull(paramName, "No name for element \"parameter\"");
                String paramValue = (String)_attributes.get("value");
                _checkForNull(paramValue, "No value for element \"parameter\"");
                Attribute attribute
                        = (Attribute)_current.getAttribute(paramName);
                _checkForNull(attribute, "No such parameter: \"" + paramName
                        + "\" in class: " + _current.getClass().toString());
                _checkClass(attribute, Variable.class,
                        "Element \"parameter\" named \"" + paramName
                        + "\" is not an instance of Variable.");
                Variable param = (Variable)attribute;
                param.setExpression(paramValue);
            } else if (elementName.equals("connection")) {
                String port1Name = (String)_attributes.get("port1");
                _checkForNull(port1Name, "No port1 for element \"connection\"");
                String port2Name = (String)_attributes.get("port2");
                _checkForNull(port2Name, "No port2 for element \"connection\"");
                String name = (String)_attributes.get("name");

                _checkClass(_current, CompositeEntity.class,
                        "Element \"connection\" found inside a container that"
                        + " is not an instance of CompositeEntity.");
                CompositeEntity context = (CompositeEntity)_current;

                // Parse port1
                int point = _positionOfDot(port1Name);
                String portname = port1Name.substring(point+1);
                String actorname = port1Name.substring(0, point);
                ComponentEntity actor = context.getEntity(actorname);
                _checkForNull(actor, "No actor named \"" + actorname
                        + "\" in " + context.getFullName());
                Port port = actor.getPort(portname);
                _checkForNull(port, "No port named \"" + portname
                        + "\" in " + actor.getFullName());
                ComponentPort port1 = (ComponentPort)port;

                // Parse port2
                point = _positionOfDot(port2Name);
                portname = port2Name.substring(point+1);
                actorname = port2Name.substring(0, point);
                actor = context.getEntity(actorname);
                port = actor.getPort(portname);
                _checkForNull(port, "No port named \"" + portname
                        + "\" in " + actor.getFullName());
                ComponentPort port2 = (ComponentPort)port;

                if (name == null) {
                    _currentConnection = context.connect(port1, port2);
                } else {
                    _currentConnection = context.connect(port1, port2, name);
                }
            } else if (elementName.equals("link")) {
                String portName = (String)_attributes.get("port");
                _checkForNull(portName, "No name for element \"link\"");
                String connectionName = (String)_attributes.get("connection");
                _checkForNull(connectionName,
                        "No connection for element \"link\"");

                _checkClass(_current, CompositeEntity.class,
                        "Element \"link\" found inside an element that "
                        + "is not a CompositeEntity.");
                CompositeEntity context = (CompositeEntity)_current;

                // Parse port
                int point = _positionOfDot(portName);
                String portname = portName.substring(point+1);
                String actorname = portName.substring(0, point);
                ComponentEntity actor = context.getEntity(actorname);
                _checkForNull(actor, "No actor named \"" + actorname
                        + "\" in " + context.getFullName());
                Port tmpPort = actor.getPort(portname);
                _checkForNull(tmpPort, "No port named \"" + portname
                        + "\" in " + actor.getFullName());
                ComponentPort port = (ComponentPort)tmpPort;

                // Get relation
                Relation tmpRelation = context.getRelation(connectionName);
                _checkForNull(tmpRelation, "No relation named \"" +
                        connectionName + "\" in " + context.getFullName());
                ComponentRelation relation = (ComponentRelation)tmpRelation;

                port.link(relation);
            } else if (elementName.equals("doc")) {
                _currentCharData = new StringBuffer();
            }
        } catch (Exception ex) {
            String msg = "XML element \"" + elementName
                   + "\" triggers exception:\n  " + ex.toString();
            throw new XmlException(msg,
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
        _attributes.clear();
    }

    /**
     * implement com.microstar.xml.XMLHandler.startExternalEntity
     * move down one level in the entity tree.
     */
    public void startExternalEntity(String URI) throws Exception {
        if(DEBUG)
            System.out.println("startExternalEntity: URI=\"" + URI + "\"\n");
        sysids.addFirst(URI);
    }

    protected String _currentExternalEntity() {
        if(DEBUG)
            System.out.println("currentExternalEntity: URI=\"" +
                    (String)sysids.getFirst() + "\"\n");
        return (String)sysids.getFirst();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // If the first argument is not an instance of the second,
    // throw an exception with the given message.
    private void _checkClass(Object object, Class correctClass, String msg)
            throws XmlException {
        if(!correctClass.isInstance(object)) {
            throw new XmlException(msg,
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
    }

    // If the argument is null, throw an exception with the given message.
    private void _checkForNull(Object object, String message)
            throws XmlException {
        if(object == null) {
            throw new XmlException(message,
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
    }

    // Return the position of the last dot (period) in the specified
    // port name.  Throw an exception if there is none, or if it is the
    // first or last character.
    private int _positionOfDot(String portname) throws XmlException {
        int position = portname.lastIndexOf(".");
        if ((position <= 0) || (position == portname.length() - 1)) {
            throw new XmlException("Invalid port name: \"" + portname + "\"",
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
        return position;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Attributes associated with an entity.
    private Map _attributes;

    // The stack of objects that contain the current one.
    Stack _containers = new Stack();

    // The current object in the hierarchy.
    NamedObj _current;

    // The current character data for the current element.
    StringBuffer _currentCharData;

    // The relation for the currently active connection.
    ComponentRelation _currentConnection;

    // The manager for this model.
    Manager _manager;

    // The parser.
    private XmlParser _parser = new XmlParser();

    // Top-level entity.
    private TypedCompositeActor _toplevel = null;

    // The workspace for this model.
    Workspace _workspace;

// FIXME...

    /* this linkedlist contains the current path in the tree of
     * entities being parsed.  The leaf is first in the list.
     */
    private LinkedList sysids = new LinkedList();

    private static final boolean DEBUG = false;
    private String _dtdlocation = null;
}
