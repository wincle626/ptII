/* An actor that converts 32 consecutive BooleanTokens to an IntToken

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

@ProposedRating Red (mikele@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.actor.lib.*;

///////////////////////////////////////////////////////////////
/// BitsToInt
/** This actor takes in 32 consecutive boolean tokens and output
    a single IntToken.

@author Michael Leung
@version $Id$
*/

public class BitsToInt extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BitsToInt(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {

        super(container, name);

        input = (SDFIOPort) newPort("input");
        input.setInput(true);
	_inputRate = 32;
        input.setTokenConsumptionRate(_inputRate);
        input.setTypeEquals(BaseType.BOOLEAN);

        output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTokenProductionRate(1);
        output.setTypeEquals(BaseType.INT);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. This has type BooleanToken. */
    public SDFIOPort input;

    /** The output port. This has type IntToken. */
    public SDFIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume 32 consecutive BooleanTokens on the input.
     *  The first token consumed is the most significant bit.
     *  Output a single IntToken which is representing by the
     *  BooleanTokens.
     *
     *  @exception IllegalActionException If there is no director.
     */

    public final void fire() throws IllegalActionException  {
        int i, j;
        int integer = 0;
        Token[] bits = new BooleanToken[32];

        bits = input.get(0, _inputRate);

        for (i = 0; i < 32; i++) {
            integer = integer << 1;
            if (((BooleanToken)bits[i]).booleanValue())
                integer += 1;
        }

        IntToken value = new IntToken(integer);
        output.send(0, value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _inputRate;
}
