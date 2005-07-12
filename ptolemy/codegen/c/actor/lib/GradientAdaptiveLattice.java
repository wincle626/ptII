/* A helper class for actor.lib.GradientAdaptiveLattice

@Copyright (c) 2005 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.codegen.c.actor.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.GradientAdaptiveLattice
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class GradientAdaptiveLattice extends CCodeGeneratorHelper {

    /**
     * Constructor method for the GradientAdaptiveLattice helper
     * @param actor the associated actor
     */
    public GradientAdaptiveLattice(
            ptolemy.actor.lib.GradientAdaptiveLattice actor) {
        super(actor);
    }

    /**
     * Generate fire code
     * The method reads in <code>fireBlock</code> from 
     * GradientAdaptiveLattice.c and puts into the given stream buffer
     * @param stream the given buffer to append the code to
     */
    public void  generateFireCode(StringBuffer stream)
        throws IllegalActionException {
        ptolemy.actor.lib.GradientAdaptiveLattice actor = 
            (ptolemy.actor.lib.GradientAdaptiveLattice) getComponent();
        
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("fireBlock");

        stream.append(processCode(tmpStream.toString()));
    }

    /** Generate initialization code.
     *  This method reads the <code>initBlock</code> from 
     *  GradientAdaptiveLattice.c, replaces macros with their values and 
     *  returns the results.
     *  @return The processed code block.
     */
    public String generateInitializeCode()
        throws IllegalActionException {
        super.generateInitializeCode();

        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("initBlock");

        return processCode(tmpStream.toString());
    }
    
    /** Generate preinitialization code.
     *  This method reads the <code>preinitBlock</code> from 
     *  GradientAdaptiveLattice.c, replaces macros with their values and 
     *  returns the results.
     *  @return The processed code block.
     */
    public String generatePreinitializeCode() 
        throws IllegalActionException {
        super.generatePreinitializeCode();

        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("preinitBlock");

        return processCode(tmpStream.toString());
    }

    /** Generate wrap up code.
     *  This method reads the <code>wrapupBlock</code> 
     *  from GradientAdaptiveLattice.c, 
     *  replaces macros with their values and
     *  put the processed code block into the given stream buffer
     */
    public void generateWrapupCode(StringBuffer stream)
        throws IllegalActionException {
        ptolemy.actor.lib.GradientAdaptiveLattice actor = 
            (ptolemy.actor.lib.GradientAdaptiveLattice) getComponent();

        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("wrapupBlock");

        stream.append(processCode(tmpStream.toString()));
    }

    /** Get the files needed by the code generated for the
     *  GradientAdaptiveLattice actor.
     *  @return A set of strings that are names of the files
     *   needed by the code generated for the GradientAdaptiveLattice actor.
     */
    public Set getIncludingFiles() {
        Set files = new HashSet();
        files.add("\"math.h\"");

        return files;
    }
}
