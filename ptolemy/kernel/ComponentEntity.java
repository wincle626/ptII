/* Generated By:JavaScope: Do not edit this line. ComponentEntity.java */
/* A ComponentEntity is a vertex in a clustered graph.

 Copyright (c) 1997-2001 The Regents of the University of California.
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
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator; import COM.sun.suntest.javascope.database.js$;import COM.sun.suntest.javascope.database.CoverageUnit; 


//////////////////////////////////////////////////////////////////////////
//// ComponentEntity
/**
A ComponentEntity is a component in a CompositeEntity.
It might itself be composite, but in this base class it is assumed to
be atomic (meaning that it cannot contain components).
<p>
Derived classes may further constrain the container to be
a subclass of CompositeEntity.  To do this, they should override
the protected method _checkContainer() to throw an exception.
<p>
A ComponentEntity can contain instances of ComponentPort.  Derived
classes may further constrain to a subclass of ComponentPort.
To do this, they should override the public method newPort() to create
a port of the appropriate subclass, and the protected method _addPort()
to throw an exception if its argument is a port that is not of the
appropriate subclass.

@author John S. Davis II, Edward A. Lee
@version $Id$
*/
public class ComponentEntity extends Entity { static private int js$t0 = js$.setDatabase("/home/eecs/cxh/jsdatabase");static private String[] js$p={"ptolemy","kernel",};static private CoverageUnit js$c=js$.c(js$p,"ComponentEntity","/export/maury/maury2/cxh/tmp/ptII/ptolemy/kernel/jsoriginal/ComponentEntity.java",976122022828L,js$n());  static final int[] js$a = js$c.counters; 

    /** Construct an entity in the default workspace with an empty string
     *  The object is added to the workspace directory.
     *  as its name. Increment the version number of the workspace.
     */
    public ComponentEntity() {
	super(); try{  js$.g(ComponentEntity.js$a,1); 
	 js$.g(ComponentEntity.js$a,0);/*$js$*/ _addIcon(); }finally{js$.flush(ComponentEntity.js$c);} 
    }

    /** Construct an entity in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public ComponentEntity(Workspace workspace) {
	super(workspace); try{  js$.g(ComponentEntity.js$a,3); 
	 js$.g(ComponentEntity.js$a,2);/*$js$*/ _addIcon(); }finally{js$.flush(ComponentEntity.js$c);} 
    }

    /** Construct an entity with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ComponentEntity(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container.workspace(), name); try{  js$.g(ComponentEntity.js$a,5); 
         js$.g(ComponentEntity.js$a,4);/*$js$*/ setContainer(container);
	_addIcon(); }finally{js$.flush(ComponentEntity.js$c);} 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new entity with the same ports as the original, but
     *  no connections.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ComponentEntity.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {try  { js$.g(ComponentEntity.js$a,7); 
         js$.g(ComponentEntity.js$a,6);/*$js$*/ ComponentEntity newObject = (ComponentEntity)super.clone(workspace);
        newObject._container = null;
        return newObject;
    } finally{js$.flush(ComponentEntity.js$c);}} 

    /** Get the container entity.
     *  @return The container, which is an instance of CompositeEntity.
     */
    public Nameable getContainer() {try  { js$.g(ComponentEntity.js$a,9); 
         js$.g(ComponentEntity.js$a,8);/*$js$*/ return _container;
    } finally{js$.flush(ComponentEntity.js$c);}} 

    /** Return true if the entity is atomic.
     *  An atomic entity is one that cannot have components.
     *  Instances of this base class are always atomic.
     *  Derived classes that return false are assumed to be instances of
     *  CompositeEntity or a class derived from that.
     *  @return True if the entity is atomic.
     *  @see ptolemy.kernel.CompositeEntity
     */
    public boolean isAtomic() {try  { js$.g(ComponentEntity.js$a,11); 
	 js$.g(ComponentEntity.js$a,10);/*$js$*/ return true;
    } finally{js$.flush(ComponentEntity.js$c);}} 

    /** Return true if the entity is opaque.
     *  An opaque entity is one that either is atomic or hides
     *  its components behind opaque ports.
     *  Instances of this base class are always opaque.
     *  Derived classes may be transparent, in which case they return false
     *  to this method and to isAtomic().
     *  @return True if the entity is opaque.
     *  @see ptolemy.kernel.CompositeEntity
     */
    public boolean isOpaque() {try  { js$.g(ComponentEntity.js$a,13); 
	 js$.g(ComponentEntity.js$a,12);/*$js$*/ return true;
    } finally{js$.flush(ComponentEntity.js$c);}} 

    /** Create a new port with the specified name.
     *  The container of the port is set to this entity.
     *  This overrides the base class to create an instance of ComponentPort.
     *  Derived classes may override this to further constrain the ports.
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param name The new port name.
     *  @return The new port
     *  @exception IllegalActionException If the argument is null.
     *  @exception NameDuplicationException If this entity already has a
     *   port with the specified name.
     */
    public Port newPort(String name)
            throws IllegalActionException, NameDuplicationException {try  { js$.g(ComponentEntity.js$a,16); 
         js$.g(ComponentEntity.js$a,14);/*$js$*/ try {
            _workspace.getWriteAccess();
            Port port = new ComponentPort(this, name);
            return port;
        } finally {
             js$.g(ComponentEntity.js$a,15);/*$js$*/ _workspace.doneWriting();
        }
    } finally{js$.flush(ComponentEntity.js$c);}} 

    /** Specify the container, adding the entity to the list
     *  of entities in the container.  If the container already contains
     *  an entity with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this entity, throw an exception.
     *  If the entity is already contained by the container, do nothing.
     *  If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then unlink the ports of the entity
     *  from any relations and remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this entity being garbage collected.
     *  Derived classes may further constrain the container
     *  to subclasses of CompositeEntity by overriding the protected
     *  method _checkContainer(). This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {try  { js$.g(ComponentEntity.js$a,27); 

        if ( ((( container != null && _workspace != container.workspace() ) && ++ComponentEntity.js$a[28] != 0) || ++ComponentEntity.js$a[29] == 0) ) {
             js$.g(ComponentEntity.js$a,17);/*$js$*/ throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }
         js$.g(ComponentEntity.js$a,18);/*$js$*/ try {
            _workspace.getWriteAccess();
            _checkContainer(container);
            // NOTE: The following code is quite tricky.  It is very careful
            // to leave a consistent state even in the face of unexpected
            // exceptions.  Be very careful if modifying it.
            CompositeEntity previousContainer =
                (CompositeEntity)getContainer();
            if ( ((( previousContainer == container ) && ++ComponentEntity.js$a[30] != 0) || ++ComponentEntity.js$a[31] == 0) ) {   js$.g(ComponentEntity.js$a,19);/*$js$*/ return; } 

            // Do this first, because it may throw an exception, and we have
            // not yet changed any state.
            if ( ((( container != null ) && ++ComponentEntity.js$a[32] != 0) || ++ComponentEntity.js$a[33] == 0) ) {
                 js$.g(ComponentEntity.js$a,20);/*$js$*/ container._addEntity(this);
                if ( ((( previousContainer == null ) && ++ComponentEntity.js$a[34] != 0) || ++ComponentEntity.js$a[35] == 0) ) {
                     js$.g(ComponentEntity.js$a,21);/*$js$*/ _workspace.remove(this);
                }
            }
             js$.g(ComponentEntity.js$a,22);/*$js$*/ _container = container;
            if ( ((( previousContainer != null ) && ++ComponentEntity.js$a[36] != 0) || ++ComponentEntity.js$a[37] == 0) ) {
                // This is safe now because it does not throw an exception.
                 js$.g(ComponentEntity.js$a,23);/*$js$*/ previousContainer._removeEntity(this);
            }
            if ( ((( container == null ) && ++ComponentEntity.js$a[38] != 0) || ++ComponentEntity.js$a[39] == 0) ) {
                 js$.g(ComponentEntity.js$a,24);/*$js$*/ Iterator ports = portList().iterator();
                while ( ((( ports.hasNext() ) && ++ComponentEntity.js$a[40] != 0) || ++ComponentEntity.js$a[41] == 0) ) {
                     js$.g(ComponentEntity.js$a,25);/*$js$*/ Port port = (Port)ports.next();
                    port.unlinkAll();
                }
            }
        } finally {
             js$.g(ComponentEntity.js$a,26);/*$js$*/ _workspace.doneWriting();
        }
    } finally{js$.flush(ComponentEntity.js$c);}} 

    /** Set the name of the ComponentEntity. If there is already
     *  a ComponentEntity of the container with the same name, throw an
     *  exception.
     *  @exception IllegalActionException If the name has a period.
     *  @exception NameDuplicationException If there already is an entity
     *   in the container with the same name.
     */
    public void setName(String name)
            throws IllegalActionException, NameDuplicationException {try  { js$.g(ComponentEntity.js$a,47); 
        if ( ((( name == null ) && ++ComponentEntity.js$a[48] != 0) || ++ComponentEntity.js$a[49] == 0) ) {
             js$.g(ComponentEntity.js$a,42);/*$js$*/ name = new String("");
        }
         js$.g(ComponentEntity.js$a,43);/*$js$*/ CompositeEntity container = (CompositeEntity) getContainer();
        if( ((( (container != null) ) && ++ComponentEntity.js$a[50] != 0) || ++ComponentEntity.js$a[51] == 0) ) {
             js$.g(ComponentEntity.js$a,44);/*$js$*/ ComponentEntity another = (ComponentEntity)
                container.getEntity(name);
            if( ((( (another != null) && (another != this) ) && ++ComponentEntity.js$a[52] != 0) || ++ComponentEntity.js$a[53] == 0) ) {
                 js$.g(ComponentEntity.js$a,45);/*$js$*/ throw new NameDuplicationException(container,
                        "already contains an entity with the name "+name+".");
            }
        }
         js$.g(ComponentEntity.js$a,46);/*$js$*/ super.setName(name);
    } finally{js$.flush(ComponentEntity.js$c);}} 

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a port to this entity. This overrides the base class to
     *  throw an exception if the added port is not an instance of
     *  ComponentPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set
     *  the container of the port to point to this entity.
     *  It assumes that the port is in the same workspace as this
     *  entity, but does not check.  The caller should check.
     *  Derived classes may override this method to further constrain to
     *  a subclass of ComponentPort.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param port The port to add to this entity.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this entity, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the entity.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {try  { js$.g(ComponentEntity.js$a,56); 
        if ( ((( !(port instanceof ComponentPort) ) && ++ComponentEntity.js$a[57] != 0) || ++ComponentEntity.js$a[58] == 0) ) {
             js$.g(ComponentEntity.js$a,54);/*$js$*/ throw new IllegalActionException(this, port,
                    "Incompatible port class for this entity.");
        }
         js$.g(ComponentEntity.js$a,55);/*$js$*/ super._addPort(port);
    } finally{js$.flush(ComponentEntity.js$c);}} 

    /** Check that the specified container is of a suitable class for
     *  this entity.  In this base class, this method returns immediately
     *  without doing anything.  Derived classes may override it to constrain
     *  the container.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.  Not thrown in this base class.
     */
    protected void _checkContainer(CompositeEntity container)
            throws IllegalActionException {try  { js$.g(ComponentEntity.js$a,59); } finally{js$.flush(ComponentEntity.js$c);}} 

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addIcon() {try  { js$.g(ComponentEntity.js$a,61); 
	 js$.g(ComponentEntity.js$a,60);/*$js$*/ _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"0\" y=\"0\" width=\"60\" " +
                "height=\"40\" style=\"fill:white\"/>\n" +
                "<polygon points=\"10,10 50,20 10,30\" " +
                "style=\"fill:blue\"/>\n" +
                "</svg>\n");
    } finally{js$.flush(ComponentEntity.js$c);}} 

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The entity that contains this entity. */
    private CompositeEntity _container; static private int js$n() {return 62;}  static private int js$t1=js$.flush(ComponentEntity.js$c);private int js$t2=js$.flush(ComponentEntity.js$c); 
