/*
 * Copyright (C) 2013   Bob Rutledge
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

package org.lreqpcr.experiment_ui.components;

import java.util.List;
import javax.swing.Action;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.ui_elements.LabelFactory;
import org.lreqpcr.core.ui_elements.LreActionFactory;
import org.lreqpcr.core.ui_elements.LreNode;
import org.lreqpcr.core.ui_elements.LreObjectChildren;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * Displays a list of AverageSampleProfile with replicate SampleProfileNodesSortedByWell as children. 
 *
 * @author Bob Rutledge
 */
public class AvProfileNodesWithSampleProfileChildren extends LreObjectChildren{

    public AvProfileNodesWithSampleProfileChildren(ExplorerManager mgr, DatabaseServices db,
            List<AverageSampleProfile> avSampleProfileList,
            LreActionFactory actionFactory, LabelFactory labelFactory) {
        super(mgr, db, avSampleProfileList, actionFactory, labelFactory);
    }

    /**
     * Displays the AverageSample Profiles with Sample Profile children
     *
     * @param the list of AverageSampleProfiles to be displayed
     * @return
     */
    @SuppressWarnings(value = "unchecked")
    @Override
    protected Node[] createNodes(LreObject lreObject) {
        AverageSampleProfile avSampleProfile = (AverageSampleProfile) lreObject;
        if (nodeActionFactory == null) {
            actions = new Action[]{};//i.e. no Actions have been set
        } else {
            actions = nodeActionFactory.getActions(lreObject.getClass().getSimpleName());
            if (actions == null) {
                actions = new Action[]{};//i.e. there are no Actions for this Node
            }
        }
//SampleProfiles are retrieved from the AverageSampleProfile rather than searching the database
        LreNode node = new LreNode(new SampleProfileNodesSortedByWell(mgr, db, 
                avSampleProfile.getReplicateProfileList(),
                nodeActionFactory, nodeLabelFactory),
                    Lookups.singleton(avSampleProfile), actions);
        node.setExplorerManager(mgr);
        node.setDatabaseService(db);
        node.setName(lreObject.getName());
        if (nodeLabelFactory == null) {
            node.setDisplayName("");
        } else {
            node.setDisplayName(nodeLabelFactory.getNodeLabel(lreObject));
            node.setShortDescription(lreObject.getShortDescription());
        }
        return new Node[]{node};
    }

}
