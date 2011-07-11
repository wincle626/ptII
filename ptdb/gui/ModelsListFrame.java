/*
@Copyright (c) 2010 The Regents of the University of California.
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
package ptdb.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import ptdb.common.dto.XMLDBModel;
import ptdb.kernel.bl.load.LoadManager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
////ModelsListFrame

/**
 * Frame to display a list of all models in the database.
 * @author Ashwini Bijwe
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (abijwe)
 * @Pt.AcceptedRating red (abijwe)
 */
public class ModelsListFrame extends javax.swing.JFrame {

    /**
     * Create new form ModelsListFrame.
     */
    public ModelsListFrame() {

    }

    /** Create new form ModelsListFrame.
     * @param configuration The configuration under which Ptolemy is running.
     * */
    public ModelsListFrame(Configuration configuration) {
        setTitle("List of All Models in the Database");
        _initModelsList();
        initComponents();
        _configuration = configuration;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        _modelsListTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        _totalModelsLabel = new java.awt.Label();
        _numberOfModelsLabel = new java.awt.Label();
        _previousPageButton = new java.awt.Button();
        _pageNumberCombo = new javax.swing.JComboBox();
        _nextPageButton = new java.awt.Button();
        _hintLabel = new java.awt.Label();

        _hintLabel.setFont(new java.awt.Font("Dialog", 1, 12));
        _hintLabel.setText("Hint: Double click a model name to open it.");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setResizable(false);

        _setTableData();
        _setPageNumbers();
        jScrollPane1.setViewportView(_modelsListTable);

        //        _modelsListTable.getColumnModel().getColumn(0).setPreferredWidth(20);

        _modelsListTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    javax.swing.JTable target = (javax.swing.JTable) e
                            .getSource();
                    int row = target.getSelectedRow();
                    //int column = target.getSelectedColumn();
                    _loadModel((String) target.getValueAt(row, 1));
                }
            }
        });
        _totalModelsLabel.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        _totalModelsLabel.setName(""); // NOI18N
        _totalModelsLabel.setText("Total number of models:");

        _numberOfModelsLabel.setName("noOfModels"); // NOI18N
        _numberOfModelsLabel.setText(Integer.toString(_noOfModels));

        _previousPageButton.setLabel("<< ");
        _previousPageButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        _gotToPreviousPage(evt);
                    }
                });

        _pageNumberCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _gotoPage(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
                jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout
                .setHorizontalGroup(jPanel1Layout
                        .createParallelGroup(
                                javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                jPanel1Layout
                                        .createSequentialGroup()
                                        .addComponent(
                                                _totalModelsLabel,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(
                                                _numberOfModelsLabel,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                50,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                148, Short.MAX_VALUE)
                                        .addComponent(
                                                _previousPageButton,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(
                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(
                                                _pageNumberCombo,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                41,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)));
        jPanel1Layout
                .setVerticalGroup(jPanel1Layout
                        .createParallelGroup(
                                javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                jPanel1Layout
                                        .createSequentialGroup()
                                        .addGroup(
                                                jPanel1Layout
                                                        .createParallelGroup(
                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(
                                                                _totalModelsLabel,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(
                                                                _numberOfModelsLabel,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(
                                                                _pageNumberCombo,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(
                                                                _previousPageButton,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap(
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)));

        _nextPageButton.setLabel(">>");
        _nextPageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _goToNextPage(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
                getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        javax.swing.GroupLayout.Alignment.TRAILING,
                        layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(
                                                        _hintLabel,
                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(
                                                        jScrollPane1,
                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        380, Short.MAX_VALUE)
                                                .addGroup(
                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                        layout.createSequentialGroup()
                                                                .addComponent(
                                                                        jPanel1,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(
                                                                        _nextPageButton,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        layout.setVerticalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        layout.createSequentialGroup()
                                .addComponent(_hintLabel)
                                .addComponent(jScrollPane1,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        265,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(
                                                        jPanel1,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(
                                                        _nextPageButton,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Check the selected page number and enable disable the next and previous
     * page buttons accordingly.
     */
    private void _checkButtonsAndPageNumber() {

        if (_currentPageNumber == 1) {
            _previousPageButton.setEnabled(false);
        } else {
            _previousPageButton.setEnabled(true);
        }

        if (_currentPageNumber == _noOfPages) {
            _nextPageButton.setEnabled(false);
        } else {
            _nextPageButton.setEnabled(true);
        }
        try {
            _pageNumberCombo.setSelectedIndex(_currentPageNumber - 1);
        } catch (Exception e) {

        }
    }

    /**
     * Paint the previous page.
     * @param evt Click event for the button.
     */
    private void _gotToPreviousPage(java.awt.event.ActionEvent evt) {
        _currentPageNumber--;
        _repaintTable();
    }

    /**
     * Paint the next page.
     * @param evt Click event for the button.
     */
    private void _goToNextPage(java.awt.event.ActionEvent evt) {
        _currentPageNumber++;
        _repaintTable();
    }

    /**
     * Paint the selected page.
     * @param evt Onchange event for the pages combo.
     */
    private void _gotoPage(java.awt.event.ActionEvent evt) {
        _currentPageNumber = (Integer) _pageNumberCombo.getSelectedItem();
        _repaintTable();
    }

    /**
     * Initialize the models list in the load manager.
     */
    private void _initModelsList() {
        _currentPageNumber = 1;
        _modelsList = new ArrayList<XMLDBModel>();
        try {
            _loadManager.getAllModelsFromDatabase();
            _noOfPages = _loadManager.getNoOfPages();
            _noOfModels = _loadManager.getTotalNumberOfModels();

        } catch (Exception e) {
            MessageHandler.error("Could not load the models list. ", e);
        }
    }

    /**
     * Create an effigy of the model and open it in a new editing frame.
     */
    private void _loadModel(String modelName) {

        try {

            PtolemyEffigy effigy = LoadManager.loadModel(modelName,
                    _configuration);

            if (effigy != null) {

                effigy.showTableaux();

            } else {

                JOptionPane.showMessageDialog(this,
                        "The specified model could "
                                + "not be found in the database.",
                        "Load Error", JOptionPane.INFORMATION_MESSAGE, null);

            }

        } catch (Exception e) {

            MessageHandler.error("Cannot load the specified model. ", e);

        }

    }

    /**
     * Paint the table with the new data.
     */
    private void _repaintTable() {
        _setTableData();
        _modelsListTable.repaint();
    }

    /**
     * Set the table data to the models list for the selected page.
     */
    private void _setTableData() {
        try {
            _modelsList = _loadManager
                    .getAllModelsFromDatabase(_currentPageNumber);

            String[][] modelData = new String[_modelsList.size()][2];
            int i = 0;
            int serialNumber = LoadManager.NO_OF_ITEMS_PER_PAGE
                    * (_currentPageNumber - 1);

            for (XMLDBModel model : _modelsList) {
                modelData[i][0] = Integer.toString(++serialNumber);
                modelData[i++][1] = model.getModelName();
            }

            _modelsListTable.setModel(new javax.swing.table.DefaultTableModel(
                    modelData, new String[] { " # ", "Model Name" }) {
                Class[] types = new Class[] { java.lang.Long.class,
                        java.lang.String.class };
                boolean[] canEdit = new boolean[] { false, false };

                public Class getColumnClass(int columnIndex) {
                    return types[columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });

            _modelsListTable.getColumn(" # ").setMinWidth(40);
            _modelsListTable.getColumn(" # ").setMaxWidth(40);
            _modelsListTable.getColumn(" # ").setPreferredWidth(40);
            _checkButtonsAndPageNumber();
        } catch (Exception e) {
            MessageHandler.error("Could not load the models list. ", e);
        }
    }

    /**
     * Set the page numbers combo.
     */
    private void _setPageNumbers() {
        Integer[] pages = new Integer[_noOfPages];
        for (int page = 1; page <= _noOfPages; page++) {
            pages[page - 1] = page;
        }
        _pageNumberCombo.setModel(new javax.swing.DefaultComboBoxModel(pages));
    }

    //    /*
    //    /**
    //    * @param args the command line arguments
    //    */
    //    public static void main(String args[]) {
    //        java.awt.EventQueue.invokeLater(new Runnable() {
    //            public void run() {
    //                new ModelsListFrame(null).setVisible(true);
    //            }
    //        });
    //    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Button _previousPageButton;
    private java.awt.Button _nextPageButton;
    private javax.swing.JComboBox _pageNumberCombo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable _modelsListTable;
    private java.awt.Label _totalModelsLabel;
    private java.awt.Label _numberOfModelsLabel;
    private java.awt.Label _hintLabel;
    // End of variables declaration//GEN-END:variables

    /** List of models on the selected page. */
    private List<XMLDBModel> _modelsList = null;
    /** Instance of LoadManager that maintains the entire models list. */
    private LoadManager _loadManager = new LoadManager();
    /** Current page. */
    private int _currentPageNumber;
    /** Total number of pages. */
    private int _noOfPages;
    /** Total number of models. */
    private int _noOfModels;
    /** Configuration to open the model. */
    private Configuration _configuration;
}
