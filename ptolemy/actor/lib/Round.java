/* An actor that outputs a specified rounding function of the input.

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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

// NOTE: If you update the list of functions, then you will want
// to update the list in actor/lib/math.xml.

//////////////////////////////////////////////////////////////////////////
//// Round
/**
Produce an output token on each firing with a value that is
equal to the specified rounding function of the input.
The input and output types are DoubleToken.  The functions
are exactly those in the java.lang.Math class.  They are:
<ul>
<li> <b>ceil</b>: Round towards positive infinity.
If the argument is NaN, then the result is NaN.
<li> <b>fix</b>: Round towards zero.
If the argument is NaN, then the result is NaN.
<li> <b>floor</b>: Round towards negative infinity.
If the argument is NaN, then the result is NaN.
<li> <b>round</b>: Round towards nearest integer.
If the argument is NaN, then the result is NaN.
</ul>

@author C. Fong
@version $Id$
@see AbsoluteValue
@see TrigFunction
@see Scale
*/

public class Round extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Round(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // parameters
        function = new StringAttribute(this, "function");
        function.setExpression("floor");
        _function = FLOOR;

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The function to compute.  This is a string-valued parameter
     *  that defaults to "floor".
     */
    public StringAttribute function;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

            
    /** Override the base class to determine which function is being
     *  specified.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == function) {
            String spec = function.getExpression();
            if (spec.equals("ceil")) {
                _function = CEIL;
            } else if (spec.equals("fix")) {
                _function = FIX;
            } else if (spec.equals("floor")) {
                _function = FLOOR;
            } else if (spec.equals("round")) {
                _function = ROUND;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized rounding function: " + spec);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Compute the specified rounding function of the input.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            double in = ((DoubleToken)input.get(0)).doubleValue();
            output.send(0, new DoubleToken(_doFunction(in)));
        }
    }

    /** Invoke a specified number of iterations of this actor. Each
     *  iteration computes the rounding function specified by the
     *  <i>function</i> attribute on a single token. An invocation
     *  of this method therefore applies the function to <i>count</i>
     *  successive input tokens.
     *  <p>
     *  This method should be called instead of the usual prefire(),
     *  fire(), postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.  This leads to more
     *  efficient execution.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, return NOT_READY, and do
     *   not consume any input tokens.
     *  @exception IllegalActionException If iterating cannot be
     *  performed.
     */
    public int iterate(int count) throws IllegalActionException {
	// Check whether we need to reallocate the output token array.
	    if (count > _resultArray.length) {
	        _resultArray = new DoubleToken[count];
	    }

        if (input.hasToken(0, count)) {
	    // NOTE: inArray.length may be > count, in which case
	    // only the first count tokens are valid.
            Token[] inArray = input.get(0, count);
	        for (int i = 0; i < count; i++) {
		        double input = ((DoubleToken)(inArray[i])).doubleValue();
		        _resultArray[i] = new DoubleToken(_doFunction(input));
	        }
            output.send(0, _resultArray, count);
            return COMPLETED;
        } else {
            return NOT_READY;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Calculate the function on the given argument.
     *  @param in The input value.
     *  @return The result of applying the function.
     */
    private double _doFunction(double in) {
        double result;
        switch(_function) {
        case CEIL:
            result = Math.ceil(in);
            break;
        case FIX:
            if ( in > 0) {
                result = Math.floor(in);
            } else {
                result = Math.ceil(in);
            }
            break;
        case FLOOR:
            result = Math.floor(in);
            break;
        case ROUND:
            result = Math.round(in);
            break;
        default:
            throw new InternalErrorException(
                    "Invalid value for _function private variable. "
                    + "Round actor (" + getFullName()
                    + ")");
        }
        return result;
    }
    


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private DoubleToken[] _resultArray = new DoubleToken[1];

    // An indicator for the function to compute.
    private int _function;

    // Constants used for more efficient execution.
    private static final int CEIL = 0;
    private static final int FIX = 1;
    private static final int FLOOR = 2;
    private static final int ROUND = 3;
}
