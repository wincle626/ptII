/* An actor that performs a specified logic operation on the input.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Green (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.logic;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

// NOTE: If you update the list of functions, then you will want
// to update the list in actor/lib/logic/logic.xml.

//////////////////////////////////////////////////////////////////////////
//// LogicFunction
/**
Produce an output token on each firing with a value that is
equal to the specified logic operator of the input(s).
The functions are:
<ul>
<li> <b>and</b>: The logical and operator.
This is the default function for this actor.
<li> <b>or</b>: The logical or operator.
<li> <b>xor</b>: The logical xor operator.
<li> <b>nand</b>: The logical nand operator.
Equivalent to the negation of <i>and</i>.
<li> <b>nor</b>: The logical nor operator.
Equivalent to the negation of <i>or</i>.
<li> <b>xnor</b>: The logical xnor operator.
Equivalent to the negation of <i>xor</i>.
</ul>
<p>
NOTE: All operators have
a single input port, which is a multiport, and a single output port, which
is not a multiport.  All ports have type boolean.
<p>
This actor is not strict.  That is, it does not require that each input
channel have a token upon firing.  As long as one channel contains a
token, output will be produced.  If no input tokens are available at
all, then no output is produced.  At most one token is consumed
on each input channel.

@author Paul Whitaker
@version $Id$
*/
public class LogicFunction extends Transformer {

    /** Construct an actor with the given container and name.  Set the
     *  logic function to the default ("and").  Set the types of the ports
     *  to boolean.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LogicFunction(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Parameters
        function = new StringAttribute(this, "function");
        function.setExpression("and");
        _function = _AND;
        _negate = false;

        // Ports
        input.setMultiport(true);
        output.setMultiport(false);
        input.setTypeEquals(BaseType.BOOLEAN);
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The function to compute.  This is a string-valued attribute
     *  that defaults to "and".
     */
    public StringAttribute function;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Override the base class to determine which function is being
     *  specified.  Read the value of the function attribute and set
     *  the cached value appropriately.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws  IllegalActionException {

        if (attribute == function) {
            String functionName = 
                function.getExpression().trim().toLowerCase();

            if (functionName.equals("and")) {
                _function = _AND;
                _negate = false;
            } else if (functionName.equals("or")) {
                _function = _OR;
                _negate = false;
            } else if (functionName.equals("xor")) {
                _function = _XOR;
                _negate = false;
            } else if (functionName.equals("nand")) {
                _function = _AND;
                _negate = true;
            } else if (functionName.equals("nor")) {
                _function = _OR;
                _negate = true;
            } else if (functionName.equals("xnor")) {
                _function = _XOR;
                _negate = true;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized logic function: " + functionName
                        + ".  Valid functions are 'and', 'or', 'xor', "
                        + "'nand', 'nor', and 'xnor'.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume at most one input token from each input channel, 
     *  and produce a token on the output port.  If there is no 
     *  input on any channel, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        BooleanToken value = null;
        BooleanToken in = null;
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                in = (BooleanToken)(input.get(i));
                if (in != null) value = _updateFunction(in, value);
            }
        }
        if (value != null) {
            if (_negate) value = value.not();
            output.send(0,(BooleanToken)value);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Calculate the function on the given arguments.
     *  @param in The new input value.  Should never be null.
     *  @param old The old result value, or null if there is none.
     *  @return The result of applying the function.
     *  @exception IllegalActionException Possibly thrown by BooleanToken.
     */
    private BooleanToken _updateFunction(BooleanToken in, BooleanToken old)
            throws IllegalActionException {
        Token result;
        if (old == null) {
            result = in;
        } else {
            switch(_function) {
            case _AND:
                result = old.multiply(in);
                break;
            case _OR:
                BooleanToken negatedResult =
                    (BooleanToken)((old.not()).multiply(in.not()));
                result = negatedResult.not();
                break;
            case _XOR:
                result = old.add(in);
                break;
            default:
                throw new InternalErrorException(
                        "Invalid value for _function private variable. "
                        + "LogicFunction actor (" + getFullName()
                        + ")"
                        + " on function type " + _function);
            }
        }
        return (BooleanToken)result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // An indicator for the function to compute.  
    // Valid values are _AND, _OR, and _XOR.
    private int _function;

    // An indicator for negating the final result.
    private boolean _negate;

    // Constants used for more efficient execution.
    private static final int _AND = 0;
    private static final int _OR  = 1;
    private static final int _XOR = 2;
}

