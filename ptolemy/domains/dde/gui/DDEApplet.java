/* A base class for applets that use the DDE domain.

 Copyright (c) 1999-2000 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDDED HEREUNDDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (davisj@eecs.berkeley.edu)
*/

package ptolemy.domains.dde.gui;

import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.domains.dde.kernel.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//////////////////////////////////////////////////////////////////////////
//// DDEApplet
/**
A base class for applets that use the DDE domain. This is provided
for convenience, in order to promote certain common elements among
DDE applets. It is by no means required in order to create an applet
that uses the DDE domain. In particular, it creates and configures a
director. If the applet's "stopTime" parameter has been set, then it
uses that parameter to define the duration of the execution of the
model. Otherwise, it creates an entry box in the applet for specifying
the stop time. If the applet parameter "defaultStopTime" has been set,
then the entry box is initialized with the value given by that parameter.

@author Edward A. Lee, John S. Davis II
@version $Id$
*/
public class DDEApplet extends PtolemyApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Describe the applet parameters.
     *  @return An array describing the applet parameters.
     */
    public String[][] getParameterInfo() {
	String newinfo[][] = {
	    {"stopTime", "", "when to stop"},
	    {"defaultStopTime", "100.0", "default value for when to stop"}
	};
        return _concatStringArrays(super.getParameterInfo(), newinfo);
    }

    /** Initialize the applet. After calling the base class init() method,
     *  this method creates a director which is accessible to derived
     *  classes via a protected member. If the applet "stopTime" parameter
     *  is given, then set the director stop time to its value. If this
     *  parameter is not given, then create an entry box on screen to query
     *  the user for the stop time.
     */
    public void init() {
        super.init();

        // Process the stopTime parameter.
        double stopTime = 100.0;
        try {
            String stopSpec = getParameter("stopTime");
            if (stopSpec != null) {
                stopTime = (new Double(stopSpec)).doubleValue();
                _stopTimeGiven = true;
            }
        } catch (Exception ex) {
            report("Warning: stop time parameter failed: ", ex);
        }

        try {
            // Initialization
            _director = new DDEDirector(_toplevel, "DDEDirector");
	    _director.stopTime.setToken( new DoubleToken(stopTime) );
        } catch (Exception ex) {
            report("Failed to setup director:\n", ex);
            _setupOK = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** In addition to creating the buttons provided by the base
     *  class, if the stop time has not been specified by the
     *  applet parameter "stopTime," then create an entry box for
     *  that number to be entered. If the "showStopTime" parameter
     *  is set to true, then display a text entry box for specifying
     *  the stop time of the model. The panel containing the buttons
     *  and the entry box is returned.
     * @param numbuttons The number of buttons to create.
     * @param showStopTime A flag indicating whether stop time
     *  selection box should be displayed.
     * @return The panel containing the controls.
     */
    protected JPanel _createRunControls(int numbuttons) {
        JPanel panel = super._createRunControls(numbuttons);

        if( !_stopTimeGiven ) {
            // To keep the label and entry box together,
            // put them in a new panel.
            JPanel stopTimePanel = new JPanel();
            stopTimePanel.add(new JLabel("Stop time:"));

            // Process the default iterations parameter.
            String defaultStopSpec =
                getParameter("defaultStopTime");
            // getSingleParameter("defaultStopTime");
            if (defaultStopSpec == null) {
                defaultStopSpec = "100.0";
            }

            _stopTimeBox = new JTextField(defaultStopSpec, 10);
            _stopTimeBox.addActionListener(new StopTimeBoxListener());
            stopTimePanel.add(_stopTimeBox);
            panel.add(stopTimePanel);
        }
        return panel;
    }

    /** Get the stop time from the entry box, if there is one,
     *  or from the director, if not.
     * @return The stop time.
     */
    protected double _getStopTime() {
	double result = -5.0;

	if( _director != null ) {
            try {
            	result =
                    ((DoubleToken)_director.stopTime.getToken()).doubleValue();
            } catch (IllegalActionException ex) {
                report("Error in stop time:\n", ex);
            }
	}

	if( _stopTimeBox != null ) {
	    try {
		result = (new Double(_stopTimeBox.getText())).doubleValue();
            } catch (NumberFormatException ex) {
                report("Error in stop time:\n", ex);
	    }

	}
	return result;
    }

    /** Execute the system until the stop time given by the
     *  _getStopTime() method.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected void _go() throws IllegalActionException {
	_director.stopTime.setToken( new DoubleToken(_getStopTime()) );
        super._go();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The director for the top-level composite actor, created in the
     *  init() method.
     */
    protected DDEDirector _director;

    /** True if the stop time has been given via an applet
     *  parameter.  Note that this is set by the init() method.
     */
    protected boolean _stopTimeGiven = false;

    /** The entry box containing the stop time, or null if
     *  there is none.
     */
    protected JTextField _stopTimeBox;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for the stop time box.  When the applet user hits
     *  return, the model executes.
     */
    class StopTimeBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            start();
        }
    }
}
