/* Repeat, CGC domain: CGCRepeat.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCRepeat.pl by ptlang
*/
/*
Copyright (c) 1990-2005 The Regents of the University of California.
All rights reserved.
See the file $PTOLEMY/copyright for copyright notice,
limitation of liability, and disclaimer of warranty provisions.
 */
package ptolemy.codegen.lib;

import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.codegen.kernel.ClassicCGCActor;
import ptolemy.codegen.kernel.ClassicPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCRepeat
/**
Repeats each input sample the specified number of times.
<p>
Repeat repeats each input Particle the specified number of times
(<i>numTimes</i>) on the output.  Note that this is a sample rate
change, and hence affects the number of invocations of downstream
stars.

 @Author S. Ha
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCRepeat.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCRepeat extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCRepeat(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        output = new ClassicPort(this, "output", false, true);

        // Repetition factor. IntState
        numTimes = new Parameter(this, "numTimes");
        numTimes.setExpression("2");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type anytype.
     */
    public ClassicPort input;

    /**
     * output of type anytype.
     */
    public ClassicPort output;

    /**
     *  Repetition factor. parameter with initial value "2".
     */
     public Parameter numTimes;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return ((IntToken)((numTimes).getToken())).intValue();
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

output.setSDFParams(((IntToken)((numTimes).getToken())).intValue(),((IntToken)((numTimes).getToken())).intValue()-1);
     }

    /**
     */
    public void  generateFireCode() {

addCode(out);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String out =
        "        int i;\n"
        + "        for (i = 0; i < $val(numTimes); i++) {\n"
        + "                $ref(output, i) = $ref(input);\n"
        + "        }\n";
}
