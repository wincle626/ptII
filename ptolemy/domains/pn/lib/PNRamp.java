/* This generates a stream of integer beginning with the seed

 Copyright (c) 1997- The Regents of the University of California.
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

package ptolemy.domains.pn.lib;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.data.*;
import ptolemy.actor.*;
//import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// PNRamp
/** 

@author Mudit Goel
@version $Id$
*/
public class PNRamp extends PNActor {
    
    /** Constructor Adds ports to the star
     * @param initValue is the initial token that the star puts in the stream
     * @exception NameDuplicationException indicates that an attempt to add
     *  two ports with the same name has been made
     */
    public PNRamp(CompositeActor container, String name)
            throws NameDuplicationException {
        super(container, name);
        _output = newOutPort(this, "output");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    public void setInitState(int seed) {
        _seed = seed;
    }
    
    /** Writes successive integers to the output
     */
    public void run() {
        int i;
        IntToken data;
        setCycles(((PNCompositeActor)getContainer()).getCycles());
        try {
            for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
                data = new IntToken(_seed);
                writeTo(_output, data);
                _seed++;
            }
            System.out.println("Terminating at al "+this.getName());
            ((PNDirector)getDirector()).processStopped();
        } catch (NoSuchItemException e) {
	    System.out.println("Terminating "+this.getName());
            return;
        }
    }
    
    public void setParam(String name, double value) 
            throws IllegalActionException {
        System.out.println(name + ": " + value);

        if (name.equals("seed")) {
            _seed = (int) value;
        } else {
            throw new IllegalActionException("Unknown parameter: " + name);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /* This is the initial value that the star puts in the stream */
    private int _seed;
    /* Output port */
    private PNOutPort _output;
}

