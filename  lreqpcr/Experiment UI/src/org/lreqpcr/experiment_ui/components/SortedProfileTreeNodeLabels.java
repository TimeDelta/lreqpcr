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
package org.lreqpcr.experiment_ui.components;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import org.lreqpcr.core.data_objects.AverageSampleProfile;
import org.lreqpcr.core.data_objects.LreObject;
import org.lreqpcr.core.data_objects.SampleProfile;
import org.lreqpcr.core.ui_elements.LabelFactory;

/**
 * Provides node labels for Profiles sorted by either Amplicon or Sample
 * as specified by the Amplicon Overview and Sample Overview windows
 * respectively.
 *
 * @author Bob Rutledge
 */
public class SortedProfileTreeNodeLabels implements LabelFactory {

    private SimpleDateFormat sdf = new SimpleDateFormat("dMMMyy");
    private DecimalFormat df = new DecimalFormat();

    public String getNodeLabel(LreObject member) {
        SampleProfile profile = (SampleProfile) member;
        profile.setShortDescription("");
        //Label madeup of three components
        String rundate = sdf.format(profile.getRunDate());
        String profileName = rundate + ": " + profile.getAmpliconName() + "@ " + profile.getSampleName() + " ";
        //If excluded no Emax or No is displayed
        if (profile.isExcluded()) {
            if (profile instanceof AverageSampleProfile) {
                profile.setShortDescription("This Profile has been excluded by the user");
            } else {//Must be a SampleProfile
                profile.setShortDescription("This Sample Profile has been excluded by the user and will not be included in the Average Profile");
            }
            return profileName + "<PROFILE IS EXCLUDED>";
        }
        String emax;
        String no;
        //Determine what to display for Emax
        if (profile.isEmaxFixedTo100() && profile.hasAnLreWindowBeenFound()) {
            df.applyPattern("#0.0");
            profile.setShortDescription("Emax overridden");
            emax = "<100%> " ;
        } else {
            if (!profile.hasAnLreWindowBeenFound()) {
                emax = "<LRE window not found>";
                profile.setShortDescription("An LRE window could not be found, likely due to being a flat profile" 
                        + " or the Min Fc being set too high");
                return profileName + emax;
            } else {
                df.applyPattern("#0.0");
                emax = "(" + df.format(profile.getEmax() * 100) + "%) ";
            }
        }
        if (profile instanceof AverageSampleProfile) {
            AverageSampleProfile avPrf = (AverageSampleProfile) profile;
            if (avPrf.isReplicateAverageNoLessThan10Molecules()
                    && !avPrf.isExcluded()
                    && avPrf.numberOfActiveReplicateProfiles() > 1) {
                df.applyPattern("0.00");
                profile.setShortDescription("Less than 10 molecules requires averaging the replicate profiles quantities");
                return profileName + "avRep: " + df.format(avPrf.getNo());
            }
        }
        //Determine what to display for No
        if (profile.getNo() == Double.POSITIVE_INFINITY){
                //Produced because an OCF has not been applied
                if (profile.getAmpliconSize() == 0){
                    profile.setShortDescription("Target quantity could not be determined because an amplicon size has not been provided");
                return profileName + emax + "n.d. (no amplicon size)";
                }
                if (!(profile.getOCF() > 0)){
                    profile.setShortDescription("Target quantity could not be determined because an OCF has not been applied");
                return profileName + emax + "n.d. (no OCF)";
                }
                profile.setShortDescription("For unkown reasons, a target quantity could not be determined");
                return profileName + emax + "n.d.";
            }
        if (profile.getNo() < 10) {
            df.applyPattern("0.00");
        } else {
            df.applyPattern("###,###");
        }
        if (!profile.hasAnLreWindowBeenFound()) {
            no = "0";
        } else {
            no = df.format(profile.getNo());
        }
        return profileName + emax + no;
    }
}
