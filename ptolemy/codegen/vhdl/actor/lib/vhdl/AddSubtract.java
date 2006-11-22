/* A helper class for actor.lib.FixConst

 @Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.vhdl.actor.lib.vhdl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.vhdl.kernel.VHDLCodeGeneratorHelper;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Precision;

/**
 * A helper class for ptolemy.actor.lib.Uniform.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class AddSubtract extends VHDLCodeGeneratorHelper {
    /**
     * Construct a FixConst helper.
     * @param actor the associated actor
     */
    public AddSubtract(ptolemy.actor.lib.vhdl.AddSubtract actor) {
        super(actor);
    }

    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();
        _codeStream.clear();

        ptolemy.actor.lib.vhdl.AddSubtract actor = 
            (ptolemy.actor.lib.vhdl.AddSubtract) getComponent();
        
        boolean isAdd = 
            actor.operation.getExpression().endsWith("ADD");

        boolean signed = 
            actor.operation.getExpression().startsWith("SIGNED");
            
        ArrayList args = new ArrayList();
        if (isAdd && signed) {
            args.add("pt_sfixed_add2");
        
        } else if (isAdd && !signed) {
            args.add("pt_ufixed_add2");
        
        } else if (!isAdd && signed) {
            args.add("pt_sfixed_sub2");
            
        } else {
            args.add("pt_ufixed_sub2");
        }
       
        int latencyValue = ((IntToken) actor.latency.getToken()).intValue();

        if (latencyValue == 0) {
            _codeStream.appendCodeBlock("sharedBlock_lat0", args);
        } else {
            _codeStream.appendCodeBlock("sharedBlock", args);            
        }
        
        sharedCode.add(processCode(_codeStream.toString()));
        return sharedCode;
    }    
    /**
     * Generate fire code.
     * The method reads in the <code>fireBlock</code> from FixConst.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters
     *  an error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.vhdl.AddSubtract actor = 
            (ptolemy.actor.lib.vhdl.AddSubtract) getComponent();

        boolean isAdd = 
            actor.operation.getExpression().endsWith("ADD");

        boolean signed = 
            actor.operation.getExpression().startsWith("SIGNED");
            
        ArrayList args = new ArrayList();
        if (isAdd && signed) {
            args.add("pt_sfixed_add2");
        
        } else if (isAdd && !signed) {
            args.add("pt_ufixed_add2");
        
        } else if (!isAdd && signed) {
            args.add("pt_sfixed_sub2");
            
        } else {
            args.add("pt_ufixed_sub2");
        }


        TypedIOPort source1 = (TypedIOPort) actor.A.sourcePortList().get(0);
        TypedAtomicActor sourceActor1 = (TypedAtomicActor) source1.getContainer();
        
        TypedIOPort source2 = (TypedIOPort) actor.B.sourcePortList().get(0);
        TypedAtomicActor sourceActor2 = (TypedAtomicActor) source2.getContainer();
                 
        Precision precision1 = 
            new Precision(((Parameter) sourceActor1.getAttribute(
                        source1.getName() + "Precision")).getExpression());
        
        Precision precision2 = 
            new Precision(((Parameter) sourceActor2.getAttribute(
                        source2.getName() + "Precision")).getExpression());
        
        int highA = precision1.getIntegerBitLength() - 1;
        int lowA = -precision1.getFractionBitLength();
        
        int highB = precision2.getIntegerBitLength() - 1;
        int lowB = -precision2.getFractionBitLength();

        Precision outputPrecision = 
            new Precision(((Parameter) actor.getAttribute("outputPrecision"))
                    .getExpression());
        
        int highO = outputPrecision.getIntegerBitLength() - 1;
        int lowO = -outputPrecision.getFractionBitLength();
        
        args.add("" + highA);
        args.add("" + lowA);
        args.add("" + highB);
        args.add("" + lowB);
        args.add("" + highO);
        args.add("" + lowO);
        if (((IntToken) actor.latency.getToken()).intValue() > 0) {
            args.add(", " + actor.latency.getExpression()); 
            args.add(", $ref(clk)");
        } else {
            args.add("");
            args.add("");            
        }
        
        _codeStream.appendCodeBlock("fireBlock", args);

        return processCode(_codeStream.toString());
    }

    /** Get the files needed by the code generated for the FixConst actor.
     *  @return A set of strings that are names of the library and package.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        
        files.add("ieee.std_logic_1164.all");
        files.add("ieee.numeric_std.all");
        files.add("ieee_proposed.math_utility_pkg.all");
        files.add("ieee_proposed.fixed_pkg.all");
        return files;
    }
}
