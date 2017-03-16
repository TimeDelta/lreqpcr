package org.lreqpcr.analysis.rutledge;

import org.lreqpcr.core.data_objects.Profile;
import org.lreqpcr.core.data_processing.Cycle;
import org.lreqpcr.core.data_processing.ProfileSummary;

public class LREWindowEvaluator {

    public static final double INVALID = -1;

    private static final double MAX_DISTANCE_FROM_100_EFFICIENCY = 1;
    private static final double MAX_ACTUAL_VS_PREDICTED_FLUORESCENCE = .5;

    /**
     * @return A number between 0 and 1 representing the fitness of the current
     * LRE window with a higher number symbolizing a more fit score. If the current
     * LRE window either does not existent or is determined to be unusable then a
     * value of {@link #INVALID} will be returned.
     */
    public double evaluateLREWindow(ProfileSummary profileSummary) {
        Profile profile = profileSummary.getProfile();
        if (isLreWindowInvalid(profileSummary)) {
            return INVALID;
        }
        //double[] evaluations = new double[2];
        //evaluations[0] = evaluateMaxEfficiency(profile);
        //evaluations[1] = evaluateActualVersusPredictedFluorescence(profileSummary);
        //return getAverageEvaluation(evaluations);
        return evaluateMaxEfficiency(profile);
    }

    private boolean isLreWindowInvalid(ProfileSummary profileSummary) {
        Profile profile = profileSummary.getProfile();
        return !profile.hasAnLreWindowBeenFound() || isMaxEfficiencyInvalid(profile) || isActualVersusPredictedFluorescenceInvalid(profileSummary);
    }

    private boolean isMaxEfficiencyInvalid(Profile profile) {
        return Math.abs(1 - profile.getMaxEfficiency()) > MAX_DISTANCE_FROM_100_EFFICIENCY;
    }

    private double evaluateMaxEfficiency(Profile profile) {
        return 1 - Math.abs(profile.getMaxEfficiency() - 1);
    }

    private boolean isActualVersusPredictedFluorescenceInvalid(ProfileSummary profileSummary) {
        return evaluateActualVersusPredictedFluorescence(profileSummary) > MAX_ACTUAL_VS_PREDICTED_FLUORESCENCE;
    }

    /**
     * Get the average normalized (between 0 and 1) distance between the actual and
     * predicted fluorescence values of the cycles included in the LRE window.
     */
    private double evaluateActualVersusPredictedFluorescence(ProfileSummary profileSummary) {
        double aggregateFitnessScore = 0;
        int totalNumberOfCycles = 0;

        // note that we only care about cycles that are included in the LRE window
        Profile profile = profileSummary.getProfile();
        Cycle cycle = profileSummary.getLreWindowStartCycle();
        for (int i = 0; i < profile.getLreWinSize() && cycle != null; ++i) {
            double actual = cycle.getCurrentCycleFluorescence();
            double predicted = cycle.getPredictedCyclecFluorescence();
            if (actual < 0 && predicted > 0) {
                actual = Math.abs(actual);
                predicted += 2 * actual;
            }
            else if (predicted < 0 && actual > 0) {
                predicted = Math.abs(predicted);
                actual += 2 * predicted;
            }
            // have to divide by max of the two values in order to ensure that the value is between 0 and 1
            aggregateFitnessScore += 1 - Math.abs(actual - predicted) / Math.max(Math.abs(predicted), Math.abs(actual));
            ++totalNumberOfCycles;
            cycle = cycle.getNextCycle();
        }
        return aggregateFitnessScore / totalNumberOfCycles;
    }

    private double getAverageEvaluation(double[] evaluations) {
        double totalEvaluation = 0;
        for (double evaluation : evaluations) {
            totalEvaluation += evaluation;
        }
        return totalEvaluation / evaluations.length;
    }
}
