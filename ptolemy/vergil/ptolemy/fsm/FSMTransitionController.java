/* The edge controller for links between ports and relations.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy.fsm;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import ptolemy.vergil.*;
import ptolemy.vergil.toolbox.*;

import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import diva.util.java2d.Polygon2D;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JPopupMenu;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// FSMTransitionController
/**
This class provides interaction techniques for edges that are to be connected
between ports and relations.  Standard interaction techniques for an
undirected edge are allowed.

@author Steve Neuendorffer
@version $Id$
*/
public class FSMTransitionController extends EdgeController {
    public FSMTransitionController(GraphController controller) {
	super(controller);
	// Create and set up the target for connectors

	SelectionModel sm = controller.getSelectionModel();
	SelectionInteractor interactor =
            (SelectionInteractor) getEdgeInteractor();
	interactor.setSelectionModel(sm);

        // Create and set up the manipulator for connectors
        ConnectorManipulator manipulator = new ConnectorManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.addConnectorListener(new LinkDropper());
        interactor.setPrototypeDecorator(manipulator);

        // The mouse filter needs to accept regular click or control click
        MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        manipulator.setHandleFilter(handleFilter);

	ConnectorTarget ct = new LinkTarget();
	setConnectorTarget(ct);
	setEdgeRenderer(new LinkRenderer());

	_menuCreator = new MenuCreator(null);
	interactor.addInteractor(_menuCreator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the menu factory that will create context menus for this
     *  controller.
     */
    public MenuFactory getMenuFactory() {
        return _menuCreator.getMenuFactory();
    }

    /** Set the menu factory that will create menus for this Entity.
     */
    public void setMenuFactory(MenuFactory factory) {
        _menuCreator.setMenuFactory(factory);
    }

    public class LinkTarget extends PerimeterTarget {
        public boolean acceptHead(Connector c, Figure f) {
            Object object = f.getUserObject();
   	    if(object instanceof Location) {
                Location location = (Location)object;
                if(location.getContainer() instanceof Entity)
                    return true;
                else 
                    return false;
            }
	    return false;
        }

        public boolean acceptTail(Connector c, Figure f) {
            return acceptHead(c, f);
        }
    }

    public class LinkRenderer implements EdgeRenderer {
	/**
         * Render a visual representation of the given edge.
         */
        public Connector render(Object edge, Site tailSite, Site headSite) {
            AbstractConnector c = new ArcConnector(tailSite, headSite);
            c.setHeadEnd(new Arrowhead());
            c.setLineWidth((float)2.0);
            c.setUserObject(edge);
            Arc arc = (Arc) edge;
            Transition transition = (Transition)arc.getRelation();
	    if(transition != null) {
		c.setToolTipText(transition.getName());
                StringBuffer buffer = new StringBuffer();
                if(transition.guardExpression != null &&
                       transition.guardExpression.getExpression() != null) {
                    buffer.append(transition.guardExpression.getExpression());
                }
                String action =  transition.actions.getExpression();
                if (action != null && !action.trim().equals("")) {
                    buffer.append("\n");
                    buffer.append(action);
                }
                LabelFigure label = new LabelFigure(
                        buffer.toString(), _labelFont);
                label.setFillPaint(Color.blue);
                c.setLabelFigure(label);
            }
            return c;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private MenuCreator _menuCreator;

    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);

    ///////////////////////////////////////////////////////////////////
    ////                        inner classes                      ////

    /** An inner class that handles interactive changes to connectivity.
     */
    protected class LinkDropper extends ConnectorAdapter {
        /**
         * Called when a connector end is dropped--attach or
         * detach the edge as appropriate.
         */
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Object edge = c.getUserObject();
            Object node = (f == null) ? null : f.getUserObject();
            FSMGraphModel model =
		(FSMGraphModel) getController().getGraphModel();
	    switch (evt.getEnd()) {
	    case ConnectorEvent.HEAD_END:
		model.getArcModel().setHead(edge, node);
		break;
	    case ConnectorEvent.TAIL_END:
		model.getArcModel().setTail(edge, node);
		break;
	    default:
		throw new IllegalStateException(
                        "Cannot handle both ends of an edge being dragged.");
	    }
            // rerender the edge.  This is necessary for several reasons.
            // First, the edge is only associated with a relation after it
            // is fully connected.  Second, edges that aren't
            // connected should be erased (which this will rather
            // conveniently take care of for us
            // There is a bug in this I need to track down first.
            //  getController().rerenderEdge(edge);
        }
    }
}
