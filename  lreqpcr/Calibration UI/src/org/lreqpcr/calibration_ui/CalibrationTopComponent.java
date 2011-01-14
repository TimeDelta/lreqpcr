/*
 * Copyright (C) 2010  Bob Rutledge
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * and open the template in the editor.
 */
package org.lreqpcr.calibration_ui;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.lreqpcr.core.data_objects.Amplicon;
import org.lreqpcr.core.data_objects.AverageCalibrationProfile;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.ReactionSetupImpl;
import org.lreqpcr.core.data_objects.Sample;
import org.lreqpcr.core.database_services.DatabaseProvider;
import org.lreqpcr.core.database_services.DatabaseServiceFactory;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.database_services.SettingsServices;
import org.lreqpcr.core.ui_elements.AmpliconNode;
import org.lreqpcr.core.ui_elements.SampleNode;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.lreqpcr.core.utilities.UniversalLookupListener;
import org.lreqpcr.data_export_services.DataExportServices;
import org.lreqpcr.ui_components.PanelMessages;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.lreqpcr.calibration_ui//Calibration//EN",
autostore = false)
public final class CalibrationTopComponent extends TopComponent
        implements ExplorerManager.Provider, UniversalLookupListener,
        DatabaseProvider, LookupListener, PropertyChangeListener {

    private static CalibrationTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "CalibrationTopComponent";
    private UniversalLookup universalLookup = UniversalLookup.getDefault();
    private SettingsServices settingsDB;
    private DatabaseServices calibrationDB;
    private ExplorerManager mgr = new ExplorerManager();
    private DataExportServices exportServices;
    private final Result<AmpliconNode> ampliconNodeResult;
    private final Result<SampleNode> sampleNodeResult;

    @SuppressWarnings(value = "unchecked")
    public CalibrationTopComponent() {
        initComponents();
//        setName(NbBundle.getMessage(CalibrationTopComponent.class, "CTL_CalibrationTopComponent"));
        setToolTipText(NbBundle.getMessage(CalibrationTopComponent.class, "HINT_CalibrationTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_SLIDING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);

        setName("Calibration");
        associateLookup(ExplorerUtils.createLookup(mgr, this.getActionMap()));
        mgr.addPropertyChangeListener(this);
        ampliconNodeResult = Utilities.actionsGlobalContext().lookupResult(AmpliconNode.class);
        ampliconNodeResult.allItems();
        ampliconNodeResult.addLookupListener(this);
        sampleNodeResult = Utilities.actionsGlobalContext().lookupResult(SampleNode.class);
        sampleNodeResult.allItems();
        sampleNodeResult.addLookupListener(this);
        //This is to avoid problems with updating dataInfo in the Calibn window
        initServices();
        universalLookup.addListner(PanelMessages.UPDATE_CALIBRATION_PANELS, this);
        calbnTree.initCalbnTree(mgr, calibrationDB);
    }

    private void initServices() {
        // TODO Could throw an exception of any of the services are null??
        Lookup servicesLookup = Lookup.getDefault();
        settingsDB = servicesLookup.lookup(SettingsServices.class);
        exportServices = servicesLookup.lookup(DataExportServices.class);
        calibrationDB = servicesLookup.lookup(DatabaseServiceFactory.class).createDatabaseService(DatabaseType.CALIBRATION);
        if (calibrationDB != null) {
            UniversalLookup.getDefault().add(DatabaseType.CALIBRATION, calibrationDB);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        calbnTree = new org.lreqpcr.calibration_ui.components.CalbnTree();
        openDBbutton = new javax.swing.JButton();
        newDBbutton = new javax.swing.JButton();
        lastDBbutton = new javax.swing.JButton();
        closeDBbutton = new javax.swing.JButton();
        exportProfilesButton = new javax.swing.JButton();

        openDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/calibration_ui/Open24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(openDBbutton, org.openide.util.NbBundle.getMessage(CalibrationTopComponent.class, "CalibrationTopComponent.openDBbutton.text")); // NOI18N
        openDBbutton.setToolTipText(org.openide.util.NbBundle.getMessage(CalibrationTopComponent.class, "CalibrationTopComponent.openDBbutton.toolTipText")); // NOI18N
        openDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDBbuttonActionPerformed(evt);
            }
        });

        newDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/calibration_ui/New24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(newDBbutton, org.openide.util.NbBundle.getMessage(CalibrationTopComponent.class, "CalibrationTopComponent.newDBbutton.text")); // NOI18N
        newDBbutton.setToolTipText(org.openide.util.NbBundle.getMessage(CalibrationTopComponent.class, "CalibrationTopComponent.newDBbutton.toolTipText")); // NOI18N
        newDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDBbuttonActionPerformed(evt);
            }
        });

        lastDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/calibration_ui/Back24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lastDBbutton, org.openide.util.NbBundle.getMessage(CalibrationTopComponent.class, "CalibrationTopComponent.lastDBbutton.text")); // NOI18N
        lastDBbutton.setToolTipText(org.openide.util.NbBundle.getMessage(CalibrationTopComponent.class, "CalibrationTopComponent.lastDBbutton.toolTipText")); // NOI18N
        lastDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lastDBbuttonActionPerformed(evt);
            }
        });

        closeDBbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/lreqpcr/calibration_ui/Stop24.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(closeDBbutton, org.openide.util.NbBundle.getMessage(CalibrationTopComponent.class, "CalibrationTopComponent.closeDBbutton.text")); // NOI18N
        closeDBbutton.setToolTipText(org.openide.util.NbBundle.getMessage(CalibrationTopComponent.class, "CalibrationTopComponent.closeDBbutton.toolTipText")); // NOI18N
        closeDBbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeDBbuttonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(exportProfilesButton, org.openide.util.NbBundle.getMessage(CalibrationTopComponent.class, "CalibrationTopComponent.exportProfilesButton.text")); // NOI18N
        exportProfilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportProfilesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(calbnTree, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lastDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportProfilesButton)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closeDBbutton, lastDBbutton, newDBbutton, openDBbutton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lastDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closeDBbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exportProfilesButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(calbnTree, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    @SuppressWarnings(value = "unchecked")
    private void openDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDBbuttonActionPerformed
        boolean wasNewFileOpened = calibrationDB.openDatabase();
        //False if the action was cancelled
        if (wasNewFileOpened) {
            calbnTree.createTree();
            calbnTree.calcAverageOCF();
            UniversalLookup.getDefault().addSingleton(PanelMessages.DATABASE_FILE_CHANGED, calibrationDB);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.DATABASE_FILE_CHANGED);
        }
    }//GEN-LAST:event_openDBbuttonActionPerformed

    @SuppressWarnings(value = "unchecked")
    private void newDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDBbuttonActionPerformed
        boolean wasNewFileCreated = calibrationDB.createNewDatabase();
        //False if the action was cancelled
        if (wasNewFileCreated) {
            //All profile DBs must contain a lre window parameters object
            calibrationDB.saveObject(new LreWindowSelectionParameters());
            //Create a single reaction setup and save to the calibration database
            ReactionSetupImpl rxnSetup = new ReactionSetupImpl();
            rxnSetup.setName("Reaction Setup");
            rxnSetup.setShortDescription("A Reaction Setup holding a calbration profile dataset");
            calibrationDB.saveObject(rxnSetup);
            calibrationDB.commitChanges();
            calbnTree.createTree();
            calbnTree.calcAverageOCF();
            UniversalLookup.getDefault().addSingleton(PanelMessages.DATABASE_FILE_CHANGED, calibrationDB);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.DATABASE_FILE_CHANGED);
        }
    }//GEN-LAST:event_newDBbuttonActionPerformed

    private void closeDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeDBbuttonActionPerformed
        if (calibrationDB.isDatabaseOpen()) {
            settingsDB.setLastCalibrationDatabaseFile(calibrationDB.getDatabaseFile());
            calibrationDB.closeDatabase();
            calbnTree.createTree();
            UniversalLookup.getDefault().addSingleton(PanelMessages.DATABASE_FILE_CHANGED, null);
            UniversalLookup.getDefault().fireChangeEvent(PanelMessages.DATABASE_FILE_CHANGED);
        }
    }//GEN-LAST:event_closeDBbuttonActionPerformed

    @SuppressWarnings(value = "unchecked")
    private void lastDBbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastDBbuttonActionPerformed
        if (calibrationDB != null) {
            File previousFile = calibrationDB.getDatabaseFile();
            File lastDBfile = settingsDB.getLastCalibrationDatabaseFile();
            if (lastDBfile != null) {
                if (lastDBfile.exists()) {
                    if (previousFile != null) {
                        settingsDB.setLastCalibrationDatabaseFile(previousFile);
                    }
                    calibrationDB.openDatabase(lastDBfile);
                    calbnTree.createTree();
                    UniversalLookup.getDefault().addSingleton(PanelMessages.DATABASE_FILE_CHANGED, calibrationDB);
                    UniversalLookup.getDefault().fireChangeEvent(PanelMessages.DATABASE_FILE_CHANGED);
                }
            }
        } else {
            // TODO present an error dialog...this might not be necessary
        }
    }//GEN-LAST:event_lastDBbuttonActionPerformed

    @SuppressWarnings("unchecked")
    private void exportProfilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportProfilesButtonActionPerformed
        if (!calibrationDB.isDatabaseOpen()) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                    "A calibration database is not open",
                    "",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<AverageCalibrationProfile> profileList = (List<AverageCalibrationProfile>) calibrationDB.getAllObjects(AverageCalibrationProfile.class);
        //Confirm that this is a list of RunImpl nodes
        if (!profileList.isEmpty()) {
            Lookup.getDefault().lookup(DataExportServices.class).exportAverageCalibrationProfiles(profileList);
            return;
        } else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                    "No calibration profiles were retrieved",
                    "",
                    JOptionPane.ERROR_MESSAGE);
        }
}//GEN-LAST:event_exportProfilesButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.lreqpcr.calibration_ui.components.CalbnTree calbnTree;
    private javax.swing.JButton closeDBbutton;
    private javax.swing.JButton exportProfilesButton;
    private javax.swing.JButton lastDBbutton;
    private javax.swing.JButton newDBbutton;
    private javax.swing.JButton openDBbutton;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized CalibrationTopComponent getDefault() {
        if (instance == null) {
            instance = new CalibrationTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the CalibrationTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized CalibrationTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(CalibrationTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof CalibrationTopComponent) {
            return (CalibrationTopComponent) win;
        }
        Logger.getLogger(CalibrationTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void universalLookupChangeEvent(Object key) {
        if (key == PanelMessages.UPDATE_CALIBRATION_PANELS) {
            calbnTree.createTree();
            calbnTree.calcAverageOCF();
        }
    }

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    public DatabaseServices getDatabase() {
        return calibrationDB;
    }

    public void resultChanged(LookupEvent ev) {
        Lookup.Result r = (Result) ev.getSource();
        Object[] c = r.allInstances().toArray();
        if (c.length > 0) {
            if (c[0] instanceof AmpliconNode) {
                AmpliconNode ampNode = (AmpliconNode) c[0];
                //Test to be sure that this node was derived from this window's database
                if (ampNode.getDatabase() != calibrationDB) {
                    return;
                }
                Amplicon amplicon = ampNode.getLookup().lookup(Amplicon.class);
                calbnTree.creatAmpliconTree(amplicon.getName());
                return;
            }
            if (c[0] instanceof SampleNode) {
                SampleNode sampleNode = (SampleNode) c[0];
                //Test to be sure that this node was derived from this window's database
                if (sampleNode.getDatabase() != calibrationDB) {
                    return;
                }
                Sample sample = sampleNode.getLookup().lookup(Sample.class);
                calbnTree.createSampleTree(sample.getName());
                return;
            }
        }
    }

    /**
     * Listens for changes in node selection via Explorer Manager
     *
     * @param evt the PropertyChangeEvent
     */
    @SuppressWarnings("static-access")
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(mgr.PROP_SELECTED_NODES)) {
        }
        if (evt.getPropertyName().equals(mgr.PROP_NODE_CHANGE)) {
            //OCF has changed, signified via a node label change
            calbnTree.calcAverageOCF();
        }
    }
}
