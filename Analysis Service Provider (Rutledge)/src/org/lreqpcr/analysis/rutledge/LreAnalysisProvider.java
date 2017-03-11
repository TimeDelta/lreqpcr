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

import org.lreqpcr.analysis_services.LreAnalysisService;
import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.ProfileSummary;
import org.openide.util.lookup.ServiceProvider;

/**
 * Rutledge implementation of LRE window selection and optimization.
 *
 * @author Bob Rutledge
 */
@ServiceProvider(service = LreAnalysisService.class)
public class LreAnalysisProvider implements LreAnalysisService {

    private static final int MAX_CYCLES_FROM_GUESS_TO_SEARCH = 4;

    private Profile profile;
    private ProfileSummary profileSummary;
    private LreWindowSelectionParameters parameters;
    private final NonlinearRegressionImplementation nrAnalysis = new NonlinearRegressionImplementation();

    public LreAnalysisProvider() {
    }

    @Override
    public boolean lreWindowInitialization(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        this.profileSummary = prfSum;
        this.parameters = parameters;
        this.profile = prfSum.getProfile();

        // this avoids having to search all possible windows
        if (!makeInitialGuessAtLreWindow()) {
            return false;
        }

        // look at all possible windows near the initial
        // guess and evaluate each one to find the best
        LREWindowEvaluator evaluator = new LREWindowEvaluator();
        final int initialStartCycleIndex = profile.getStartingCycleIndex();
        final int initialWindowSize = profile.getLreWinSize();
        int bestStartCycleIndex = initialStartCycleIndex;
        int bestWindowSize = initialWindowSize;
        double bestFitnessScore = evaluator.evaluateLREWindow(prfSum);

        for (int startCycleOffset = -MAX_CYCLES_FROM_GUESS_TO_SEARCH; startCycleOffset < MAX_CYCLES_FROM_GUESS_TO_SEARCH; ++startCycleOffset) {
            int newStartCycleIndex = initialStartCycleIndex + startCycleOffset;
            if (!isValidStartCycleIndex(newStartCycleIndex)) {
                break;
            }
            profile.setStartingCycleIndex(newStartCycleIndex);

            for (int endCycleOffset = -MAX_CYCLES_FROM_GUESS_TO_SEARCH; endCycleOffset < MAX_CYCLES_FROM_GUESS_TO_SEARCH; ++endCycleOffset) {
                int newWindowSize = initialWindowSize - startCycleOffset + endCycleOffset;
                if (!isValidWindowSize(newWindowSize)) {
                    break;
                }

                profile.setLreWinSize(newWindowSize);
                profileSummary.update();
                double fitnessScore = evaluator.evaluateLREWindow(prfSum);
                if (fitnessScore > bestFitnessScore) {
                    bestFitnessScore = fitnessScore;
                    bestStartCycleIndex = newStartCycleIndex;
                    bestWindowSize = newWindowSize;
                }
            }
        }

        if (bestFitnessScore != LREWindowEvaluator.INVALID) {
            profile.setStartingCycleIndex(bestStartCycleIndex);
            profile.setLreWinSize(bestWindowSize);
            profileSummary.update();
            return true;
        }

        return false;
    }

    @Override
    public boolean optimizeLreWindowUsingNonlinearRegression(ProfileSummary prfSum, LreWindowSelectionParameters parameters) {
        profile = prfSum.getProfile();
        this.parameters = parameters;
        //Determine if a valid LRE window has been established
        if (!profile.hasAnLreWindowBeenFound()) {
            //Try to reinitialize the LRE window
            lreWindowInitialization(prfSum, parameters);
            if (!profile.hasAnLreWindowBeenFound()) {
                //If a window has not been found, abort
                prfSum.update();
                return false;
            }
        }
        //Attempt to optimize the window using nonlinear regression
        LreWindowSelector.optimizeLreWindowUsingNR(prfSum, parameters.getFoThreshold());
        prfSum.update();
        return prfSum.getProfile().didNonlinearRegressionSucceed();
    }

    @Override
    public boolean lreWindowUpdateUsingNR(ProfileSummary prfSum) {
        nrAnalysis.generateOptimizedFcDatasetUsingNonliearRegression(prfSum);
        prfSum.update();
        return prfSum.getProfile().didNonlinearRegressionSucceed();
    }

    private boolean makeInitialGuessAtLreWindow() {
        //Reset the Profile to remove any previous LRE or NR analysis derived values
        profile.setLreVariablesToZero();
        //Be sure that the working Fc dataset is derived using an average Fb;
        //that is, to reverse any NR modification of the Fc dataset
        LreWindowSelector.substractBackgroundUsingAvFc(profile);
        profileSummary.update();
        //Selecting a start cycle also removes any previously determined LRE parameters
        if (parameters.getMinFc() == 0) {
            //No user selected minimum Fc, so need to scan the profile for a  LRE window
            LreWindowSelector.selectLreStartCycleViaScanning(profileSummary);
        } else {//A user-selected minFc has be set, so use it
            LreWindowSelector.selectLreStartCycleUsingMinFc(profileSummary, parameters.getMinFc());
        }
        if (!profile.hasAnLreWindowBeenFound()) {
            // Failed to find a window, thus return as updating the LRE parameters is irrelevant
            profileSummary.update();
            return false;
        }
        //Attempt to expand the upper limit of the LRE window
        LreWindowSelector.expandLreWindowWithoutNR(profileSummary, parameters.getFoThreshold());
        profileSummary.update();
        return profile.hasAnLreWindowBeenFound();
    }

    private boolean isValidStartCycleIndex(int startCycleIndex) {
        return startCycleIndex >= 0 && startCycleIndex < profile.getTotalNumberOfCycles() - 1;
    }

    private boolean isValidWindowSize(int windowSize) {
        return windowSize > 0 && windowSize <= profile.getTotalNumberOfCycles() - profile.getStartingCycleIndex() - 1;
    }
}
