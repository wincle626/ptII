/* A port supporting clustered graphs.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel;

import ptolemy.kernel.util.*;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ComponentPort
/**
A port supporting hierarchy. A component port can have "inside"
links as well as the usual "outside" links supported by the base
class. An inside link is a link to a relation that is within the
container of the port.
<p>
A ComponentPort may be transparent or opaque.  If it is transparent,
then "deep" accesses of the topology see through the port.
Methods that read the topology come in two versions, shallow and deep.
The deep versions pass through transparent ports. This is
done with a simple rule. If a transparent port is encountered from
inside, then the traversal continues with its outside links. If it
is encountered from outside, then the traversal continues with its
inside links.  A ComponentPort is opaque if its container is opaque.
(its isOpaque() method returns true).  Derived classes may use other
strategies to specify whether a port is opaque.
<p>
Normally, links to a transparent port from the outside are to
relations contained by the container of the container of the port.
Links from the inside are to relations contained by the container
of the port.  That is, levels of the hierarchy are not crossed.
For a few applications, links that cross levels of the hierarchy
are needed. The links in these connections are created
using the liberalLink() method. The link() method
prohibits such links, throwing an exception if they are attempted
(most applications will prohibit level-crossing connections by using
only the link() method).
<p>
A ComponentPort can link to any instance of ComponentRelation.
An attempt to link to an instance of Relation will trigger an exception.
Derived classes may wish to further constrain links to a subclass
of ComponentRelation.  To do this, subclasses should override the
protected methods _link() and _linkInside() to throw an exception
if their arguments are relations that are not of the appropriate
subclass.  Similarly, a ComponentPort can be contained by a
ComponentEntity, and an attempt to set the container to an instance
of Entity will trigger an exception.  If a subclass wishes to
constrain the containers of the port to be of a subclass of
ComponentEntity, they should override setContainer().

@author Edward A. Lee, Xiaojun Liu
@version $Id$
*/
public class ComponentPort extends Port {

    /** Construct a port in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
     *  The object is added to the workspace directory.
     */
    public ComponentPort() {
	super();
        // Ignore exception because "this" cannot be null.
        try {
            _insideLinks = new CrossRefList(this);
        } catch (IllegalActionException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                    "Internal error in ComponentPort constructor!"
                    + ex.getMessage());
        }
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public ComponentPort(Workspace workspace) {
	super(workspace);
        // Ignore exception because "this" cannot be null.
        try {
            _insideLinks = new CrossRefList(this);
        } catch (IllegalActionException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                    "Internal error in ComponentPort constructor!"
                    + ex.getMessage());
        }
    }

    /** Construct a port with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This port will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.  Increment the version of the workspace.
     *  @param container The container entity.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public ComponentPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
        // Ignore exception because "this" cannot be null.
        try {
            _insideLinks = new CrossRefList(this);
        } catch (IllegalActionException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                    "Internal error in ComponentPort constructor!"
                    + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the
     *   attributes cannot be cloned.
     *  @return A new ComponentPort.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        ComponentPort newobj = (ComponentPort)super.clone(ws);
        try {
            newobj._insideLinks = new CrossRefList(newobj);
        } catch (IllegalActionException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                    "Internal error in ComponentPort clone() method!"
                    + ex.getMessage());
        }
        return newobj;
    }

    /** Deeply enumerate the ports connected to this port on the outside.
     *  Begin by enumerating the ports that are connected to this port.
     *  If any of those are transparent ports that we are connected to
     *  from the inside, then enumerate all the ports deeply connected
     *  on the outside to that transparent port.  Note that a port may
     *  be listed more than once. This method read synchronized on the
     *  workspace.
     *  @return An enumeration of ComponentPort objects.
     */
    public Enumeration deepConnectedPorts() {
        try {
            _workspace.getReadAccess();
            return _deepConnectedPorts(null);
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this port is transparent, then deeply enumerate the ports
     *  connected on the inside.  Otherwise, enumerate
     *  just this port. All ports enumerated are opaque. Note that
     *  the returned enumeration could conceivably be empty, for
     *  example if this port is transparent but has no inside links.
     *  Also, a port may be listed more than once if more than one
     *  inside connection to it has been established.
     *  @return An enumeration of ComponentPort objects.
     */
    public Enumeration deepInsidePorts() {
        try {
            _workspace.getReadAccess();
            return _deepInsidePorts(null);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the ports connected on the inside to this port. Note that
     *  a port may be listed more than once if more than one inside connection
     *  has been established to it.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of ComponentPort objects.
     */
    public Enumeration insidePorts() {
        try {
            _workspace.getReadAccess();
            LinkedList result = new LinkedList();
            Enumeration relations = insideRelations();
            while (relations.hasMoreElements()) {
                Relation relation = (Relation)relations.nextElement();
                result.appendElements(relation.linkedPorts(this));
            }
            return result.elements();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the relations linked on the inside to this port.
     *  Note that a relation may be listed more than once if more than link
     *  to it has been established.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of ComponentRelation objects.
     */
    public Enumeration insideRelations() {
        try {
            _workspace.getReadAccess();
            return _insideLinks.getContainers();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true the the given port is deeply connected with this port.
     *  This method is read-synchronized on the workspace.
     *  @return True if the given port is deeply connected.
     */
    public boolean isDeeplyConnected(ComponentPort port) {
        if(port == null) return false;
        try {
            _workspace.getReadAccess();
            // Call deepConnectedPort to refresh the cache.
            Enumeration dummy = deepConnectedPorts();
            return _deeplinkedports.includes(port);
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if the given relation is linked from inside.
     *  @return True if the given relation is linked from inside.
     */
    public boolean isInsideLinked(Relation relation) {
        return _insideLinks.isLinked(relation);
    }

    /** Return true if the container entity is opaque.
     *  @return True if the container entity is opaque.
     */
    public boolean isOpaque() {
        ComponentEntity ent = (ComponentEntity)getContainer();
        if (ent == null) return true;
        return ent.isOpaque();
    }

    /** Link this port with a relation.  The only constraints are
     *  that the port and the relation share the same workspace, and
     *  that the relation be of a compatible type (ComponentRelation).
     *  They are not required to be at the same level of the hierarchy.
     *  To prohibit links across levels of the hierarchy, use link().
     *  Note that generally it is a bad idea to allow level-crossing
     *  links, since it breaks modularity.  This loss of modularity
     *  means, among other things, that the composite within which this
     *  port exists cannot be cloned.
     *  Nonetheless, this capability is provided for the benefit of users
     *  that feel they just must have it, and who are willing to sacrifice
     *  clonability and modularity.
     *  Both inside and outside links are supported.
     *  Note that a port may
     *  be linked to the same relation more than once, in which case
     *  the link will be reported more than once by the linkedRelations()
     *  method.
     *  If the relation argument is null, do nothing.
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If the relation does not share
     *   the same workspace, or the port has no container.
     */
    public void liberalLink(ComponentRelation relation)
            throws IllegalActionException {
        if (relation == null) return;
        if (_workspace != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            if (_outside(relation.getContainer())) {
                // An inside link
                _linkInside(relation);
            } else {
                // An outside link
                _link(relation);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Link this port with a relation.  This method calls liberalLink()
     *  if the proposed link does not cross levels of the hierarchy and
     *  the proposed relation is of class ComponentRelation, and
     *  otherwise throws an exception.  Note that a port may
     *  be linked to the same relation more than once, in which case
     *  the link will be reported more than once by the linkedRelations()
     *  method.  If the argument is null, do nothing.
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If the link crosses levels of
     *   the hierarchy, or the port has no container, or the relation
     *   is not a ComponentRelation.
     */
    public void link(Relation relation)
            throws IllegalActionException {
        if (relation == null) return;
        if (!(relation instanceof ComponentRelation)) {
            throw new IllegalActionException(this, relation,
                    "ComponentPort can only link to instances "+
                    "of ComponentRelation.");
        }
        if (_workspace != relation.workspace()) {
            throw new IllegalActionException(this, relation,
                    "Cannot link because workspaces are different.");
        }
        try {
            _workspace.getWriteAccess();
            Nameable container = getContainer();
            if (container != null) {
                Nameable relcont = relation.getContainer();
                if (container != relcont &&
                        container.getContainer() != relcont) {
                    throw new IllegalActionException(this, relation,
                            "Link crosses levels of the hierarchy");
                }
            }
            liberalLink((ComponentRelation)relation);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return the number of inside links.
     *  This method is read-synchronized on the workspace.
     *  @return The number of inside links.
     */
    public int numInsideLinks() {
        try {
            _workspace.getReadAccess();
            return _insideLinks.size();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Override the base class to ensure that the proposed container is a
     *  ComponentEntity.
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the container is not a
     *   ComponentEntity, or it has no name,
     *   or the port and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    public void setContainer(Entity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof ComponentEntity) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "ComponentPort can only be contained by ComponentEntity");
        }
        super.setContainer(container);
    }

    /** Unlink the specified Relation. If the Relation
     *  is not linked to this port, do nothing.
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param relation The relation to unlink.
     */
    public void unlink(Relation relation) {
        try {
            _workspace.getWriteAccess();
            // Not sure whether it's an inside link, so unlink both.
            super.unlink(relation);
            _insideLinks.unlink(relation);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink all relations, inside and out.
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     */
    public void unlinkAll() {
        try {
            _workspace.getWriteAccess();
            super.unlinkAll();
            _insideLinks.unlinkAll();
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Deeply enumerate the ports connected to this port on the outside.
     *  Begin by enumerating the ports that are connected to this port.
     *  If any of those are transparent ports that we are connected to
     *  from the inside, then enumerate all the ports deeply connected
     *  on the outside to that transparent port.  Note that a port may
     *  be listed more than once. The path argument is the path from
     *  the port that originally calls this method to this port.
     *  If this port is already on the list of ports on the path to this
     *  port in deeply traversing the topology, then there is a loop in
     *  the topology, and an InvalidStateException is thrown.
     *  This method not synchronized on the workspace, so the
     *  caller should.
     *  @param path The list of ports on the path to this port in deeply
     *   traversing the topology.
     *  @return An enumeration of ComponentPort objects.
     */
    protected Enumeration _deepConnectedPorts(LinkedList path) {
        if (_deeplinkedportsversion == _workspace.getVersion()) {
            // Cache is valid.  Use it.
            return _deeplinkedports.elements();
        }
        if(path == null) {
            path = new LinkedList();
        } else {
            if (path.firstIndexOf(this) >= 0) {
                throw new InvalidStateException( path.elements(),
                        "loop in topology!");
            }
        }
        path.insertFirst(this);
        Enumeration nearrelations = linkedRelations();
        LinkedList result = new LinkedList();

        while( nearrelations.hasMoreElements() ) {
            ComponentRelation relation =
                (ComponentRelation)nearrelations.nextElement();

            Enumeration connectedports =
                relation.linkedPorts(this);
            while (connectedports.hasMoreElements()) {
                ComponentPort port =
                    (ComponentPort)connectedports.nextElement();
                // NOTE: If level-crossing transitions are not allowed,
                // then a simpler test than that of the following
                // would work.
                if (port._outside(relation.getContainer())) {
                    // We are coming at the port from the inside.
                    if (port.isOpaque()) {
                        result.insertLast(port);
                    } else {
                        // Port is transparent
                        result.appendElements(
                                port._deepConnectedPorts(path));
                    }
                } else {
                    // We are coming at the port from the outside.
                    if (port.isOpaque()) {
                        result.insertLast(port);
                    } else {
                        // It is transparent.
                        result.appendElements(
                                port._deepInsidePorts(path));
                    }
                }
            }
        }
        _deeplinkedports = result;
        _deeplinkedportsversion = _workspace.getVersion();
        path.removeFirst();
        return _deeplinkedports.elements();
    }

    /** If this port is transparent, then deeply enumerate the ports
     *  connected on the inside.  Otherwise, enumerate
     *  just this port. All ports enumerated are opaque. Note that
     *  the returned enumeration could conceivably be empty, for
     *  example if this port is transparent but has no inside links.
     *  Also, a port may be listed more than once if more than one
     *  inside connection to it has been established.
     *  The path argument is the path from
     *  the port that originally calls this method to this port.
     *  If this port is already on the list of ports on the path to this
     *  port in deeply traversing the topology, then there is a loop in
     *  the topology, and an InvalidStateException is thrown.
     *  This method is read-synchronized on the workspace.
     *  @param path The list of ports on the path to this port in deeply
     *   traversing the topology.
     *  @return An enumeration of ComponentPort objects.
     */
    protected Enumeration _deepInsidePorts(LinkedList path) {
        if (_deeplinkedinportsversion == _workspace.getVersion()) {
            // Cache is valid.  Use it.
            return _deeplinkedinports.elements();
        }
        if(path == null) {
            path = new LinkedList();
        } else {
            if (path.firstIndexOf(this) >= 0) {
                throw new InvalidStateException( path.elements(),
                        "loop in topology!");
            }
        }
        path.insertFirst(this);
        LinkedList result = new LinkedList();
        if (isOpaque()) {
            // Port is opaque.
            result.insertLast(this);
        } else {
            // Port is transparent.
            Enumeration relations = insideRelations();
            while (relations.hasMoreElements()) {
                Relation relation = (Relation)relations.nextElement();
                Enumeration insideports =
                    relation.linkedPorts(this);
                while (insideports.hasMoreElements()) {
                    ComponentPort downport =
                        (ComponentPort)insideports.nextElement();
                    // The inside port may not be actually inside,
                    // in which case we want to look through it
                    // from the inside (this supports transparent
                    // entities).
                    if (downport._outside(relation.getContainer())) {
                        // The inside port is not truly inside.
                        // Check to see whether it is transparent.
                        if (downport.isOpaque()) {
                            result.insertLast(downport);
                        } else {
                            result.appendElements(
                                    downport._deepConnectedPorts(path));
                        }
                    } else {
                        // The inside port is truly inside.
                        result.appendElements(
                                downport._deepInsidePorts(path));
                    }
                }
            }
        }
        _deeplinkedinports = result;
        _deeplinkedinportsversion = _workspace.getVersion();
        path.removeFirst();
        return _deeplinkedinports.elements();
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            if ((detail & LINKS) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                // To avoid infinite loop, turn off the LINKS flag
                // when querying the Ports.
                detail &= ~LINKS;
                result += "insidelinks {\n";
                Enumeration enum = insideRelations();
                while (enum.hasMoreElements()) {
                    Relation rel = (Relation)enum.nextElement();
                    result += rel._description(detail, indent+1, 2) + "\n";
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Override the base class to throw an exception if the relation is
     *  not a ComponentRelation.  If it is, then invoke the base class method.
     *  This method should not be used
     *  directly.  Use the public version instead.
     *  This method <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not a ComponentRelation.
     */
    protected void _link(Relation relation)
            throws IllegalActionException {
        if (!(relation instanceof ComponentRelation)) {
            throw new IllegalActionException(this,
                    "Attempt to link to an incompatible relation.");
        }
        super._link(relation);
    }

    /** Link this port on the inside with a relation. This method should
     *  not be used directly.  Use the public version instead.
     *  The argument is a
     *  ComponentRelation, but derived classes may constrain the relation
     *  further. If this port has no container, throw an exception.
     *  Level-crossing links are allowed.
     *  This port and the relation are assumed to be in the same workspace,
     *  but this not checked here.  The caller should check.
     *  This method <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container or
     *   is not a ComponentPort.
     */
    protected void _linkInside(ComponentRelation relation)
            throws IllegalActionException {
        if (!(relation instanceof ComponentRelation)) {
            throw new IllegalActionException(this,
                    "Attempt to link to an incompatible relation.");
        }
        Entity container = (Entity)getContainer();
        if (container == null) {
            throw new IllegalActionException(this, relation,
                    "Port must have a container to establish a link.");
        }
        // Throw an exception if this port is not of an acceptable
        // class for the relation.
        relation._checkPort(this);
        // It is acceptable.
        _insideLinks.link( relation._getPortList() );
        container.connectionsChanged(this);
    }

    /** Return true if the port is either a port of the specified entity,
     *  or a port of an entity that contains the specified entity.
     *  This method is read-synchronized on the workspace.
     *  @param entity A possible container.
     *  @return True if this port is outside the entity.
     */
    protected boolean _outside(Nameable entity) {
        try {
            _workspace.getReadAccess();
            Nameable portcontainer = getContainer();
            while (entity != null) {
                if (portcontainer == entity) return true;
                entity = entity.getContainer();
            }
            return false;
        } finally {
            _workspace.doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The list of inside relations for this port. */
    private CrossRefList _insideLinks;

    // A cache of the deeply linked ports, and the version used to
    // construct it.
    // 'transient' means that the variable will not be serialized.
    private transient LinkedList _deeplinkedports;
    private transient long _deeplinkedportsversion = -1;
    private transient LinkedList _deeplinkedinports;
    private transient long _deeplinkedinportsversion = -1;
}
