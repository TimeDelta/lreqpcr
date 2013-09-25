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
package org.lreqpcr.analysis.rutledge;

import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileInitializer;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.lreqpcr.core.utilities.LREmath;
import org.lreqpcr.core.utilities.MathFunctions;

/**
 * Static functions used for automated LRE window selection
 * 
 * @author Bob Rutledge
 */
public class LreWindowSelector {

    /**
     * Automated selection of the LRE window start cycle. This is initiated by
     * identifying the first cycle in which the LRE plot r2 value derived from 
     * a window encompassing the two preceeding and two following Cycles
     * (5 cycles total), is above the "r2 tolerance".  <p>
     * 
     * As of version 0.8.5 inclusion of an Emax threshold, which must be >40%, was found to greatly 
     * increase the accuracy of LRE window selection by prevent false identification 
     * of the start of the profile. <p>
     *
     * The LRE window is then set to 3 cycles and the StartCycle adjusted
     * to the first cycle below half of Fmax, generating an LRE window in the
     * center of the Profle. The upper limit of the LRE window is then
     * expanded based on a default Fo tolerance of 6%.
     *
     * @param prfSum the ProfileSummary to process
     */
    public static void findLreWindowUsingDefaultParameters(ProfileSummary prfSum) {
        double r2Tolerance = 0.95; //The tolerance of the LRE r2 to determine the start of the profile
        double emaxThreshold = 0.4;//Emax > 40%
        double foThreshold = 0.06;
        if (prfSum.getZeroCycle() == null) {
            //There is no Fc data in this profile
            return;
        }
        Profile profile = prfSum.getProfile();
        //Reset to no LRE window found in preparation for reanalysis
        profile.setHasAnLreWindowBeenFound(false);
        //Find the start of the profile by running a 5 cycle window up
        //the profile until an LRE linear region is found based on the r2
        Cycle cycZero = prfSum.getZeroCycle();
        Cycle runner = cycZero.getNextCycle().getNextCycle(); //Go to cycle 2
        //Calculate and set the LRE parameters across all Cycles within the profile
        while (runner.getNextCycle().getNextCycle() != null) {
            double[][] fcEcArray = new double[2][5];
            fcEcArray[0][0] = runner.getPrevCycle().getPrevCycle().getFc();
            fcEcArray[1][0] = runner.getPrevCycle().getPrevCycle().getEc();
            fcEcArray[0][1] = runner.getPrevCycle().getFc();
            fcEcArray[1][1] = runner.getPrevCycle().getEc();
            fcEcArray[0][2] = runner.getFc();
            fcEcArray[1][2] = runner.getEc();
            fcEcArray[0][3] = runner.getNextCycle().getFc();
            fcEcArray[1][3] = runner.getNextCycle().getEc();
            fcEcArray[0][4] = runner.getNextCycle().getNextCycle().getFc();
            fcEcArray[1][4] = runner.getNextCycle().getNextCycle().getEc();

            //Calc cycle LRE paramaters [dE, Emax, r2]
            double[] regressionValues;
            runner.setCycLREparam(MathFunctions.linearRegressionAnalysis(fcEcArray));
            int cycNum = runner.getCycNum();
            double fc = runner.getFc();
            regressionValues = runner.getCycLREparam();
            //Calculate Fo
            runner.setFo(LREmath.calcFo(
                    cycNum,
                    fc,
                    regressionValues[0],
                    regressionValues[1]));
            runner = runner.getNextCycle();
        }
        double fb = profile.getFb();
        /*-----Finds start cycle based on the Cycle LRE r2-----*/
        Cycle strCycle;
        runner = cycZero.getNextCycle().getNextCycle().getNextCycle(); //Start at cycle 3
        //Limit the analysis to 3 cycles before the end of the profile
        while (runner.getNextCycle().getNextCycle().getNextCycle() != null) {

  //Test for the minimum r2 >r2 tolerance across 1 cycle before and after the target cycle
            //Testing Emax was found to greatly increase the accuracy of the analysis ver 0.8.5
            //[slope, intercept, r2]
            if (runner.getPrevCycle().getCycLREparam()[2] > r2Tolerance
                    && runner.getCycLREparam()[2] > r2Tolerance
                    && runner.getNextCycle().getCycLREparam()[2] > r2Tolerance
                    && runner.getCycLREparam()[1] > emaxThreshold) {
                    strCycle = runner;
                    profile.setStrCycleInt(strCycle.getCycNum()); //Sets the integer start cycle
                    prfSum.setStrCycle(strCycle);
                    profile.setHasAnLreWindowBeenFound(true);
            }
            if (runner.getNextCycle() == null) {
                profile.setHasAnLreWindowBeenFound(false);
                processFailedProfile(profile);
                return;
            }
            runner = runner.getNextCycle(); //Advances to the next cycle
        }
        //Set the initiate LRE window size to a default size
        int defaultLREwinSize = 3;
        profile.setLreWinSize(defaultLREwinSize);
        //Need a preliminary C1/2 value
        ProfileInitializer.updateProfileSummary(prfSum);
        optimizeLreWin(prfSum, foThreshold);
        double halfFmax = (profile.getEmax()/-profile.getDeltaE())/2;
        runner = prfSum.getZeroCycle();
        if (runner == null) {
            //Must have reached the end of the profile
            //Is likely a flat profile
            profile.setHasAnLreWindowBeenFound(false);
            return;
        }
        //Move to the cycle with an Fc just > 1/2 Fmax
        try {
           while (halfFmax > runner.getFc() && runner.getNextCycle() != null){
            runner = runner.getNextCycle();
        } 
        } catch (Exception e) {
            int i = 0;
        }
        if (runner == null) {
            //Have reached the end of the profile and thus most certainly is a flat profile
            profile.setHasAnLreWindowBeenFound(false);
            return;
        }
        //Go back one cycle
        runner = runner.getPrevCycle();
        try {
            profile.setStrCycleInt(runner.getCycNum());
        } catch (Exception e) {
            //null point exception
//This is likely a flat profile, which has been found to be processed correctly
         processFailedProfile(profile);
            return;   
        }
        prfSum.setStrCycle(runner);
        profile.setLreWinSize(3);
//Reoptimize the LRE window which tries to add cycles to the top of the LRE window
        ProfileInitializer.updateProfileSummary(prfSum);
        optimizeLreWin(prfSum, foThreshold);
    }

    /**
     * This method sets the Start Cycle as the first cycle with an Fc reading 
     * above the minimum fluorescence (Min Fc). Once a start cycle has 
     * been found, a default 3 cycle LRE window
     * is then applied, and the upper limit of the LRE window expanded until the 
     * Fo threshold is exceeded.
     *
     * @param prfSum the ProfileSummary to be processed
     * @param minFc the minimum fluorescence for setting the Start Cycle
     * @param threshold maximum fractional difference (%) between cycle Fo and the average Fo
     */
    public static void selectLreWindowUsingMinFc(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        Profile profile = prfSum.getProfile();
//See if an LRE window can be found
        findLreWindowUsingDefaultParameters(prfSum);
//If not, abort looking for a minFc
//This is needed to avoid artifacts generated by late partial profiles which reach min Fc
        if (!profile.hasAnLreWindowBeenFound()) {
//LRE window not found using automated selection and thus abort using a min Fc
            processFailedProfile(profile);
            return;
        }
        //Reselect the LRE window using the min Fc
        double minFc = parameters.getMinFc();
        double foThreshold = parameters.getFoThreshold();
        if (minFc == 0) {//Zero signifies that no minimum Fc has been set
//The profile has already been initialized without a minFc...so just abort looking for a min Fc
            return;
        }
        Cycle cycZero = prfSum.getZeroCycle();
        //Run to the first cycle above the minFc and set it as the start cycle
        Cycle runner = cycZero.getNextCycle().getNextCycle(); //Go to cycle 2
        while (runner.getFc() < minFc) {
            if (runner.getNextCycle() == null
                    || runner.getNextCycle().getNextCycle() == null) {
                //Reached the end of the profile...
                //This should never happen unless the min Fc is set too high
//A user warning of too high min Fc is generated by the Selection Parameter Panel
                profile.setHasAnLreWindowBeenFound(false);
                processFailedProfile(profile);
                return;
            }
            //Move up one cycle
            runner = runner.getNextCycle();
        }
//The start cycle is set to the next cycle, because minFc should be applied to the denominator of Ec
        prfSum.setStrCycle(runner.getNextCycle());
        profile.setStrCycleInt(runner.getNextCycle().getCycNum());
        profile.setLreWinSize(3);
        optimizeLreWin(prfSum, foThreshold);
    }

    /**
     * Expands the upper LRE window boundary based upon the
     * difference between the average LRE window Fo and
     * the Fo value derived from the first cycle above
     * the LRE window. If this difference is smaller than the Fo threshold,
     * this next cycle is added to the LRE window and the analysis repeated. 
     * Note however, that the upper limit of this expansion is limited to the 
     * cycle Fc less than 95% of Fmax, eliminating the possibility of includeing 
     * plateau phase cycles into the LRe window. 
     *
     * @param prfSum the ProfileSummary to be processed
     * @param foThreshold the Fo threshold
     * @return returns true if an LRE window was optimized or false if optimized LRE window selection failed
     */
    private static boolean optimizeLreWin(ProfileSummary prfSum, Double foThreshold) {
        Profile profile = prfSum.getProfile();
        //Go to the first cycle of the LRE window
        Cycle runner = prfSum.getStrCycle();
        //Run to the last cycle of the LRE window
        for (int i = 1; i < profile.getLreWinSize(); i++) {
            runner = runner.getNextCycle();
            if (runner == null) {
                return false;//Runner is at the end of the Profile
            }
        }
        //Set the initial upper boundary of the LRE window based on the Fo threshold
        if (runner.getNextCycle() == null) {//This is most certainly redundant
            return false;//Have reached the last cycle of the profile
        }
        //This also limits the top of the LRE window to 95% of Fmax
        while (Math.abs(runner.getNextCycle().getFoFracFoAv()) < foThreshold 
                && runner.getNextCycle().getFc() < profile.getFmax() * 0.95) {
            //Increase and set the LRE window size by 1 cycle
            profile.setLreWinSize(profile.getLreWinSize() + 1);
            ProfileInitializer.updateProfileSummary(prfSum);
            if (runner.getNextCycle().getNextCycle() == null) {
                return true;//Odd situation in which the second to last cycle was reached
            }
            runner = runner.getNextCycle();
        }
        return true;
    }

    /**
     * Process a Profile for which an LRE window could not be found by
     * resetting all LRE parameters to zero.
     *
     * @param failedProfile
     */
    private static void processFailedProfile(Profile failedProfile) {
        failedProfile.setStrCycleInt(0);
        failedProfile.setLreWinSize(0);
        failedProfile.setEmax(0);
        failedProfile.setDeltaE(0);
        failedProfile.setR2(0);
        failedProfile.setMidC(0);
        failedProfile.setAvFo(0);
        failedProfile.setAvFoCV(0);
    }
}
