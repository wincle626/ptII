/* A base class for actors that transform an input stream into an output stream.

 Copyright (c) 1998-2005 The Regents of the University of California.
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

 */
package ptolemy.actor.lib.vhdl;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.FixPointQuantization;
import ptolemy.math.Overflow;
import ptolemy.math.Precision;
import ptolemy.math.Quantization;
import ptolemy.math.Rounding;

//////////////////////////////////////////////////////////////////////////
//// FixPointTransformer

/**
 This is an abstract base class for actors that transform an input
 stream into output stream.  It provides an fix point input and an
 fix point output port, and manages the cloning of these ports.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class FixTransformer extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FixTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    
        newFixOutputPort("output");
        output = new TypedIOPort(this, "output", false, true);      
        output.setTypeEquals(BaseType.FIX);
    }

    
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FixTransformer(CompositeEntity container, String name, boolean isQueued)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    
        newFixOutputPort("output");
        if( isQueued ) {
            output = new QueuedTypedIOPort(this, "output", false, true);
        }
        else {
            output = new TypedIOPort(this, "output", false, true);  
        }

        output.setTypeEquals(BaseType.FIX);
    }

    /**
     * Return the precision string of the given port.
     * @param The given port.
     * @return The precision string.
     * @throws IllegalActionException If there is no precision
     *  parameter for the given port. 
     */
    public String getPortPrecision(Port port) 
            throws IllegalActionException {
        Parameter precision = (Parameter) ((Entity) port
                .getContainer()).getAttribute(port.getName() + "Precision");  

        if (precision == null) {
            throw new IllegalActionException(this, port.getName()
                    + " does not have an precision parameter.");
        }
        
        return precision.getExpression();
    }
    
    /**
     * 
     * @param channel
     * @param port
     * @param token
     * @throws NoRoomException
     * @throws IllegalActionException
     */
    public void sendOutput(TypedIOPort port, int channel, Token token) 
            throws NoRoomException, IllegalActionException {
        if (port.getType() == BaseType.FIX && token instanceof FixToken) {
            Precision precision = new Precision(((Parameter) 
                getAttribute(port.getName() + "Precision")).getExpression());
    
            Overflow overflow = Overflow.getName(((Parameter) getAttribute(
                port.getName() + "Overflow")).getExpression().toLowerCase());
    
            Rounding rounding = Rounding.getName(((Parameter) getAttribute(
                port.getName() + "Rounding")).getExpression().toLowerCase());
    
            Quantization quantization = 
                new FixPointQuantization(precision, overflow, rounding);
            
            token = ((FixToken) token).quantize(quantization); 
        }

        port.send(channel, token);
    }
    
    /** Create a new fix point type output port with given the name.
     *  The container of the created port is this actor. This also
     *  create a new precision parameter associated with this port.   
     * @param name The given name of the port.
     * @return The new output port.
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public void newFixOutputPort(String name) throws
            IllegalActionException, NameDuplicationException {
        
        // For each output port, we want to have an assoicated
        // precision parameter.
        Parameter precision = 
            new StringParameter(this, name + "Precision");            
        precision.setExpression("31:0");  

        Parameter overflow = new StringParameter(this, name + "Overflow");
        overflow.setExpression("GROW");
        overflow.addChoice("GROW");
        overflow.addChoice("ROUND");
        overflow.addChoice("WRAP");        
        overflow.addChoice("CLIP");        

        Parameter rounding = new StringParameter(this, name + "Rounding");
        rounding.setExpression("HALF_EVEN");
        rounding.addChoice("HALF_EVEN");
        rounding.addChoice("ROUND");
        rounding.addChoice("WRAP");        
    }
    

    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    
    /** Queued ouput to simulate pipelined add.  The output is fix 
     *  point type.
     */
    public TypedIOPort output;

}
