/* Three Dimensional (3D) domain director 

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.dd3d.kernel;

import ptolemy.graph.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.actor.util.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.data.expr.Parameter;
import javax.media.j3d.*;

import java.util.*;

public class DD3DDirector extends SDFDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public DD3DDirector() {
    	super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace of this object.
     */
    public DD3DDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument is null, a NullPointerException will 
     *  be thrown. If the name argument is null, then the name is set 
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     */
    public DD3DDirector(TypedCompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
   
    /** Clone the director into the specified workspace. This calls the
     *  base class and then copies the parameter of this director.  The new
     *  actor will have the same parameter values as the old.
     *  The period parameter is explicitly cloned in this method.
     *  @param ws The workspace for the new object.
     *  @return A new object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        _reset();
        DD3DDirector newobj = (DD3DDirector)(super.clone(ws));
        return newobj;
    }
    
    
     
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {
        //debug.prompt("fireAt");
        setCurrentTime(time);
    }



    public double getNextIterationTime() {
        //debug.prompt("getNext");
        return Double.MAX_VALUE;
    }
    
    /** Go through the schedule and iterate every actor with calls to
     *  prefire() , fire() , and postfire().
     *  
     *  @exception IllegalActionException If an actor executed by this
     *  director return false in its prefire().
     */
    public void fire() throws IllegalActionException {
    //  -fire-
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = _getOutsideDirector();
        
        if (container == null) {
            throw new InvalidStateException("3DDirector " + getName() +
                    " fired, but it has no container!");
        } else {
                     
            Scheduler s = getScheduler();
            if (s == null)
                throw new IllegalActionException("Attempted to fire " +
                        "3D system with no scheduler");
            Enumeration allactors = s.schedule();
            int i=1;
            while (allactors.hasMoreElements()) {
            	i++;
                
                Actor actor = (Actor)allactors.nextElement();
                if(!actor.prefire()) {
                    throw new IllegalActionException(this,
                            (ComponentEntity) actor, "Actor " +
                            "is not ready to fire.");
                }

                if(_debugging)
                    _debug("Firing " + ((Nameable)actor).getFullName());
                //debug.println("Firing " + ((Nameable)actor).getFullName());
                actor.fire();
                
                _postfirereturns = actor.postfire();
            }
        }
        if ((outsideDirector != null) && _shouldDoInternalTransferOutputs) {
            _issueTransferOutputs();
        } 
    }

    
    
    /** Initialize all the actors associated with this director by calling
     *  super.initialize().  Create a cached table of all the actors 
     *  associated with this director.  Determine which actors need to generate
     *  initial tokens for causality. All actors with nonhomogeneous input 
     *  ports will need to generate initial tokens for all its output ports. 
     *  For example, if actor A has a nonhomogeneous input port and an output
     *  port with production rate 'm' then actor A needs to produce 'm' initial
     *  tokens on the output port.  The director will handle the production of 
     *  initial tokens if the actor does not have a parameter 'initialOutputs'
     *  on its output ports. 
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
    //  -initialize-
       super.initialize();
       _buildActorTable();
       _buildScene();
    }
    
    
   
    /** Process the mutation that occurred.  Reset this director
     *  to an uninitialized state.  Notify parent class about
     *  invalidated schedule.  This method is called when an entity
     *  is instantiated under this director. This method is also 
     *  called when a link is made between ports and/or relations.
     *  see also other mutation methods:
     *    <p><UL>
     *    <LI> void attributeChanged(Attribute)
     *    <LI> void attributeTypeChanged(Attribute)
     *    </UL></p>
     */
    public void invalidateSchedule() {
    //  -invalidateSchedule-
        _reset();
        super.invalidateSchedule();
    }
    
    /** Return a new receiver consistent with the DT domain.
     *  This function is called when a connection between an output port
     *  of an actor and an input port of another actor is made in Vergil.
     *  This function is also called during the preinitialize() stage of
     *  a toplevel director.  This function may also be called prior to
     *  the preinitialize() stage of a non-toplevel director.
     *  
     *  @return A new DD3DReceiver.
     */
    public Receiver newReceiver() {
    
        DD3DReceiver currentReceiver = new DD3DReceiver();
        _receiverTable.add(currentReceiver);
        return currentReceiver;
    }
    
    
    /** Set current time to zero. Invoke the preinitialize() methods of
     *  all actors deeply contained by the container by calling
     *  super.preinitialize(). This method is invoked once per execution,
     *  before any iteration; i.e. every time the GO button is pressed.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method 
     *   of the container or one of the deeply contained actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
    //  -preinitialize-
        super.preinitialize();
    }
    
    
    /** Request the outside director to fire this director's container
     *  again for the next period. 
     * 
     *  @return true if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If the parent class throws
     *  it.
     */
    public boolean postfire() throws IllegalActionException {
    //  -postfire-

        boolean returnValue = super.postfire();
        return returnValue;
    }

          
    
    /** This is called by the outside director to get tokens 
     *  from the inside director. 
     *  Return true if transfers data from an output port of the
     *  container to the ports it is connected to on the outside.
     *  This method differs from the base class method in that this
     *  method will transfer all available tokens in the receivers,
     *  while the base class method will transfer at most one token.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  output port.  If any channel of the output port has no data,
     *  then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
    //  -transferOutputs-
        return super.transferOutputs(port);
    }
    

    
   /** Reset this director to an uninitialized state.
    *  @exception IllegalActionException If the parent class 
    *  throws it
    */
    public void wrapup() throws IllegalActionException {
    //  -wrapup-
        super.wrapup();
        _reset();
    }





    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////
    
    /**Get the number of times an actor repeats in the schedule of an 
     * SDF graph.  If the actor does not exist, throw an exception.
     * @param a The actor whose firing count is needed
     * @exception IllegalActionException If actor does not exist.
     */
    protected int getRepeats(Actor actor) throws IllegalActionException {
        ListIterator actorIterator = _actorTable.listIterator();
        int repeats = 0;
        
        foundRepeatValue:
        while(actorIterator.hasNext()) {
            _DD3DActor currentActor = (_DD3DActor) actorIterator.next();
            if (actor.equals(currentActor._actor)) {
                repeats = currentActor._repeats;
                break foundRepeatValue;
            }
        }
        
        if (repeats == 0) {
            throw new IllegalActionException(
                      "internal DT error: actor with zero firing count");
        }
    	return repeats;
    }
    
    

    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Create an actor table that caches all the actors directed by this
     *  director.  This method is called once at initialize();
     *  @exception IllegalActionException If the scheduler is null 
     */
    private void _buildActorTable() throws IllegalActionException {
        Scheduler currentScheduler = getScheduler();
        if (currentScheduler== null) 
            throw new IllegalActionException("Attempted to fire " +
                    "DT system with no scheduler");
        Enumeration allActorsScheduled = currentScheduler.schedule();
        
        
        int actorsInSchedule = 0;                    
        while (allActorsScheduled.hasMoreElements()) {
            Actor actor = (Actor) allActorsScheduled.nextElement();
            String name = ((Nameable)actor).getFullName();
            _DD3DActor dtActor = (_DD3DActor) _allActorsTable.get(actor);
            if (dtActor==null) {
              _allActorsTable.put(actor, new _DD3DActor(actor));
              dtActor = (_DD3DActor) _allActorsTable.get(actor);
              _actorTable.add(dtActor);
            }
            dtActor._repeats++;
            actorsInSchedule++;
        }
        
        // include the container as an actor.  This is needed for TypedCompositeActors
        String name = getContainer().getFullName();
        Actor actor = (Actor) getContainer();
        _allActorsTable.put(actor, new _DD3DActor((Actor)getContainer()));
        _DD3DActor dtActor = (_DD3DActor) _allActorsTable.get(actor);
        dtActor._repeats = 1;
        _actorTable.add(dtActor);
        
        _displayActorTable();
        ListIterator receiverIterator = _receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DD3DReceiver currentReceiver = (DD3DReceiver) receiverIterator.next();
            currentReceiver.determineEnds(this);
        }
        
        receiverIterator = _receiverTable.listIterator();
        
        
        _displayActorTable();
        _displayReceiverTable();
    }
    
    
    private void _buildOutputPortTable() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        
        Iterator outports = container.outputPortList().iterator();
        while(outports.hasNext()) {
            IOPort port = (IOPort)outports.next();
            
            _outputPortTable.add(new DD3DIOPort(port));
        }
        
    }
    
 


    /** For debugging purposes.  Display the list of contained actors
     *  and other pertinent information about them.
     *
     *  @exception IllegalActionException if there is a problem in 
     *   obtaining the number of initial token for delay actors
     */
    private void _displayActorTable() throws IllegalActionException {
         debug.println("\nACTOR TABLE with "+_actorTable.size()+" unique actors");
         debug.println("---------------------------------------");
         ListIterator actorIterator = _actorTable.listIterator();
         while(actorIterator.hasNext()) {
            _DD3DActor currentActor = (_DD3DActor) actorIterator.next();
            String actorName = ((Nameable) currentActor._actor).getName();
            
            debug.print(actorName+" repeats:"+currentActor._repeats);
            debug.print(" initial_tokens? "+currentActor._shouldGenerateInitialTokens);
            
            if ( !((ComponentEntity) currentActor._actor).isAtomic() ) {
                debug.print(" **COMPOSITE** ");
            }
            debug.println(" ");
         }
    }
    
    /** For debugging purposes.  Display the list of contained receivers
     *  and other pertinent information about them.
     */
    private void _displayReceiverTable() { 
    //  -displayReceiverTable-
        debug.print("\nARC RECEIVER table with "+_receiverTable.size());
        debug.println(" unique receivers");
        
        ListIterator receiverIterator = _receiverTable.listIterator();
        
        while(receiverIterator.hasNext()) {
            DD3DReceiver currentReceiver = (DD3DReceiver) receiverIterator.next();
            currentReceiver.displayReceiverInfo();
        }
        debug.println("\n");
    }
 
   /** For debugging purposes.  Display the list of attributes
     *  inside a given named object
     *  @param obj The named object that has a list of attributes
     */
    private void _displayAttributesList(NamedObj obj)
    {
    	List list = obj.attributeList();
    	Iterator listIterator = list.iterator();
    	
    	debug.println("attribute List:");
    	while(listIterator.hasNext()) {
    	    Attribute attribute = (Attribute) listIterator.next();
    	    debug.println(attribute);
    	}
    }
 
 
    /** For debugging purposes.  Display the list of contained entities
     *  inside the composite object
     *  @param obj The composite entity with a list of contained entities.
     */
    private void _displayEntityList(CompositeEntity obj) {
    
        List list = obj.entityList();
    	Iterator listIterator = list.iterator();
    	
    	debug.println("\nentity List:");
    	while(listIterator.hasNext()) {
    	    Entity entity = (Entity) listIterator.next();
    	    debug.println(entity);
    	}
    	debug.println("\n");
    }
    
    private void _displayContainerOutputPorts() throws IllegalActionException {
    
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();
        
        debug.println("\ndirector container output port list:");
        while(listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            debug.println(" ->"+port);
            _displayPortInsideReceivers(port);
        }
        debug.println("\n");
    }
    
    private void _displayPortRemoteReceivers(IOPort port) {
        Receiver[][] remoteReceivers = port.getRemoteReceivers();
    		    
    	for(int i=0;i<port.getWidth();i++) {
    	    for(int j=0;j<remoteReceivers[i].length;j++) {
    	        debug.println("  -->"+remoteReceivers[i][j]);
    	        debug.println("  ==>"+remoteReceivers[i][j].getContainer());
    	    }
    	}
    }
    
    private void _displayPortInsideReceivers(IOPort port) throws IllegalActionException {
        Receiver[][] portReceivers = port.getInsideReceivers();
    		    
    	for(int i=0;i<port.getWidth();i++) {
    	    for(int j=0;j<portReceivers[i].length;j++) {
    	        debug.println("  ->"+portReceivers[i][j]);
    	        ((DD3DReceiver)portReceivers[i][j]).displayReceiverInfo();
    	    }
    	}
    }
    
    private void _issueTransferOutputs() throws IllegalActionException {
        Director outsideDirector = _getOutsideDirector();
        
        Iterator outputPorts = _outputPortTable.iterator();
        while(outputPorts.hasNext()) {
            DD3DIOPort dtport = (DD3DIOPort) outputPorts.next();
        
            if (dtport._shouldTransferOutputs) {
                outsideDirector.transferOutputs(dtport._port);
            }
        }
    }
   
    
    protected void _buildScene() throws IllegalActionException {
        
       
        ListIterator receiverIterator = _receiverTable.listIterator();
        
        while(receiverIterator.hasNext()) {
            DD3DReceiver currentReceiver = (DD3DReceiver) receiverIterator.next();

            TypedIOPort currentPort = (TypedIOPort) currentReceiver.getContainer();
            Actor toActor = (Actor) currentPort.getContainer();
            TypedIOPort fromPort = currentReceiver.getSourcePort();
            Actor fromActor = (Actor) fromPort.getContainer();
            
            /*if ((toActor instanceof DD3DActor) && (fromActor instanceof DD3DActor)) {
                ((DD3DActor)toActor).addChild(((DD3DActor)fromActor).getNodeObject());
            }*/ 
            
            if (toActor instanceof DD3DActor) {
                DD3DActor parent = (DD3DActor) toActor;
            
                if (fromActor instanceof DD3DActor) {
                    
                    DD3DActor child = (DD3DActor) fromActor;
                    parent.addChild(child.getNodeObject());
                    
                } else if (fromActor instanceof TypedCompositeActor) {
                    TypedCompositeActor insideContainer = (TypedCompositeActor) fromActor;
                    Director director= insideContainer.getDirector();
                    
                    if (director instanceof DD3DDirector) {
                        DD3DDirector director3d = (DD3DDirector) director;
                        
    
                        List list = insideContainer.outputPortList();
                        Iterator listIterator = list.iterator();
        
                        while(listIterator.hasNext()) {
                            // FIXME there should only be one output port for NOW!
                            IOPort port = (IOPort) listIterator.next();
                            debug.println(" ->"+port);
                            if (port == fromPort) {
                                Receiver[][] portReceivers = port.getInsideReceivers();
                                DD3DReceiver receiver;
    		    
    		                    // FIXME there should be width 1 only
                        	    if (portReceivers[0][0] != null) {
                        	        receiver = (DD3DReceiver) portReceivers[0][0];
                        	        parent.addChild(receiver.group);
                        	    } 
                        	    break;
                        	}
                        }
                    }
                }
            } 
            
            if (toActor instanceof TypedCompositeActor) {
                TypedCompositeActor container = (TypedCompositeActor) toActor;
                _displayContainerOutputPorts();
                
                if (fromActor instanceof DD3DActor) {
                    DD3DActor child = (DD3DActor) fromActor;

                    if (currentReceiver.group == null) {
                        currentReceiver.group = new BranchGroup();
                    }
                    currentReceiver.group.addChild(child.getNodeObject());
                }
            }
        }
        
        ListIterator actorIterator = _actorTable.listIterator();
        
        while(actorIterator.hasNext()) {
            _DD3DActor currentActor = (_DD3DActor) actorIterator.next();
            if (currentActor._actor instanceof DD3DActor) {
                DD3DActor dd3DActor = (DD3DActor) currentActor._actor;
                dd3DActor.makeLive();
            }
        }
    }

    
    
    /** Convenience method for getting the director of the container that 
     *  holds this director.  If this director is inside a toplevel 
     *  container, then the returned value is null.
     *  @returns The executive director
     */
    private Director _getOutsideDirector() {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();
        
        return outsideDirector;
    }

    
    /** Convenience method for getting the token consumption rate of a 
     *  specified port. If the port does not have the attribute 
     *  "tokenConsumptionRate" then return a rate of 1.
     *  @param ioport The port to be queried
     *  @returns The token consumption rate of the port.
     *  @exception IllegalActionException If getting an attribute from
     *  this port fails.
     */
    private int _getTokenConsumptionRate(IOPort ioport) throws IllegalActionException {
        int rate;
        Parameter param = (Parameter) ioport.getAttribute("tokenConsumptionRate");
    	if (param != null) {
            rate = ((IntToken)param.getToken()).intValue();
        } else rate = 1;
        
        return rate;
    }
    
   
    /** Most of the constructor initialization is relegated to this method.
     *  Initialization process includes :
     *    - create a new actor table to cache all actors contained
     *    - create a new receiver table to cache all receivers contained
     *    - set default number of iterations
     *    - set period value
     */
    private void _init() {
    	try {
            //period = new Parameter(this,"period",new DoubleToken(1.0));
            _reset();
            iterations.setToken(new IntToken(0));
            debug = new DD3DDebug(false);
    	} catch (Exception e) {
    	    throw new InternalErrorException(
                    "unable to initialize DT Director:\n" +
                    e.getMessage());
    	}
    }
    
    private void _reset() {
        _actorTable = new ArrayList();
        _receiverTable = new ArrayList();
        _outputPortTable = new ArrayList();
        _allActorsTable = new Hashtable(); 
        _currentTime = 0.0;
        _formerTimeFired = 0.0;
        _formerValidTimeFired = 0.0;
        _shouldDoInternalTransferOutputs = false;
        //if (thebg!= null) thebg.detach();
    }
    
       
    
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // ArrayList to keep track of all actors scheduled by DTDirector
    private ArrayList _actorTable;
    
    // ArrayList used to cache all receivers managed by DTDirector
    private ArrayList _receiverTable;
    
    // Hashtable for keeping track of actor information
    private Hashtable _allActorsTable;
    
    // The time when the previous valid prefire() was called
    private double _formerValidTimeFired;
    
    // The time when the previous valid or invalid prefire() was called
    private double _formerTimeFired;

    // ArrayList to keep track of all container output ports     
    private ArrayList _outputPortTable;
    
    // used to determine whether the director should call transferOutputs() 
    private boolean _shouldDoInternalTransferOutputs;
    
    // display for debugging purposes
    private DD3DDebug debug;
    
    
    private static final double TOLERANCE = 0.0000000001;
    

    
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    // Inner class to cache important variables for contained actors 
    private class _DD3DActor {
    	private Actor    _actor;
    	private int      _repeats;
        private boolean  _shouldGenerateInitialTokens;
   
    	/* Construct the information on the contained Actor
    	 * @param a The actor  
    	 */	
    	public _DD3DActor(Actor actor) {
    		_actor = actor;
    		_repeats = 0;
            _shouldGenerateInitialTokens = false;
    	}
    }
    
    // Inner class to cache important variables for container output ports 
    private class DD3DIOPort {
        private IOPort _port;
        private boolean _shouldTransferOutputs;
        
        /*  Construct the information on the output port
         *  @param p The port
         */
        public DD3DIOPort(IOPort port) {
            _port = port;
            _shouldTransferOutputs = false;
        }
    }
}
