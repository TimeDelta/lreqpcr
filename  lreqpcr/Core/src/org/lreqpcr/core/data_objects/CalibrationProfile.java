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
package org.lreqpcr.core.data_objects;

/**
 * A lambda gDNA derived profile generated by a single amplification reaction in
 * which the primary output is an optical calibration profile (OCF). This is
 * based on the concept of optical calibration in which amplification of a known
 * quantity of lambda gDNA is used to determine the optical intensity of an
 * assay. This then requires that the mass of lambda used in the amplification
 * to be provided.
 *
 * Note that the default target strandedness for a Calibration Profile is double
 * stranded so it is not necessary to set the target strandedness for a
 * lambda-derived calibration profile.
 *
 * @author Bob Rutledge
 */
public class CalibrationProfile extends Profile {

    private double lambdaMass;//The mass of the lamdba gDNA in nanograms
    private double mo;//Mo calculated from the lambdaMass
    private double ocf;//OCF calculated using the LRE-derived Emax
    private double ocfCF = 0;//OCF calculated using the curve fitting Fo
    private boolean isOcfNormalizedToFmax;//Normalize the OCF to the Run's average Fmax

    /**
     * Note that the default target strandedness for a Profile is double
     * stranded so it is not necessary to set the target strandedness for a
     * lambda-derived calibration profile.
     *
     * @param run the Run that generated this CalibrationProfile
     */
    public CalibrationProfile() {
    }

    /**
     * Retrieves the quantity of the lambda standard that was amplified
     * expressed in nanograms
     *
     * @return nanograms of lambda gDNA used to generate this profile
     */
    public double getLambdaMass() {
        return lambdaMass;
    }

    /**
     * Sets the quantity of the lambda standard that was amplified. Note that it
     * is imperative that the provided mass is expressed in femtograms of lambda
     * genomic DNA.
     *
     * @param lambdaMass femtograms of lambda gDNA used to generate this
     * calibration profile
     */
    public void setLambdaMass(double lambdaMass) {
        this.lambdaMass = lambdaMass / 1000000;//converted to nanograms
    }

//Override all setters for all paramaters that change No values in order to recalculate
//the OCF values via calling updateCalibrationProfile()@Override
    @Override
    public void setAmpliconSize(int ampliconSize) {
        super.setAmpliconSize(ampliconSize);
        mo = (lambdaMass * getAmpliconSize()) / 48502;//Mo for OCF determination expressed in nanograms
        updateCalibrationProfile();
    }

    @Override
    public void setAvFo(double averageFo) {
        super.setAvFo(averageFo);
        updateCalibrationProfile();
    }

    @Override
    public void setTargetStrandedness(TargetStrandedness targetStrandedness) {
        super.setTargetStrandedness(targetStrandedness);
        updateCalibrationProfile();
    }

    /**
     * Calculates the optical calibration factor (OCF) based on the nanograms of
     * lambda gDNA (Mo) and the average Fo.
     */
    private void updateCalibrationProfile() {
        ocf = getAvFo() / mo;
        ocfCF = getNrFo() / mo;
    }

    /**
     * The optical calibration factor is the quantitative entity derived from an
     * CalibrationProfile.
     *
     * @return the optical calibration factor expressed as fluorescence units
     * per
     */
    public double getOCF() {
        if (isOcfNormalizedToFmax) {
            return getOcfAdjustedToFmax();
        }
        return ocf;
    }

    protected double getOcfAdjustedToFmax() {
        double avFmax = super.getRun().getAverageFmax();
//Note that using the Run avFmax is solely for correcting variances in well to well flourescence calibration
        double fmax = getFmax();
        if (fmax <= 0 || avFmax <= 0) {
            return 0;
        }
        double correctionFactor = fmax / avFmax;
        return ocf / correctionFactor;
    }

    public double getOcfCF() {
        return ocfCF;
    }

    public boolean isOcfNormalizedToFmax() {
        return isOcfNormalizedToFmax;
    }

    public void setIsOcfNormalizedToFmax(boolean isOcfNormalizedToFmax) {
        this.isOcfNormalizedToFmax = isOcfNormalizedToFmax;
    }

    /**
     * Sort to put excluded profiles at the bottom of the list
     *
     * @param o the Profile to compare to
     * @return the comparator integer
     */
    @Override
    public int compareTo(Object o) {
        Profile profile = (Profile) o;
        if (!profile.isExcluded()) {
            if (isExcluded()) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }
}
