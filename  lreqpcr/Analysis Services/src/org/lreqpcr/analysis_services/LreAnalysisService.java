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

package org.lreqpcr.analysis_services;

import org.lreqpcr.core.data_objects.LreWindowSelectionParameters;
import org.lreqpcr.core.data_processing.ProfileSummary;

/**
 * Identifies and optimizes LRE window selection for the Profile encapsulated within 
 * the supplied ProfileSummary. 
 * <p>
 * 
 *
 * @author Bob Rutledge
 */
public abstract interface LreAnalysisService {

    /**
     * LRE window selection and expansion using fluorescence background 
     * derived from the Fc readings within early cycles, with no application 
     * of nonlinear regression analysis. 
     * 
     * Note that this entails removing any 
     * previous LRE analysis, so that the Profile is reinitialized 
     * and automated LRE window selection conducted using the 
     * LreWindowSelectionParameters but without nonlinear regression.
     * <p>
     * Note also that this function must save the modified to Profile to the 
     * corresponding database, e.g. via ProfileSummary.update().
     *
     * @param prfSum the ProfileSummary encapsulating the Profile
     * @param parameters the LRE window selection parameters or null if default values are to be used
     * @return true if a window was found or false if window selection failed
     */
    public abstract boolean lreWindowInitialization(ProfileSummary prfSum, LreWindowSelectionParameters parameters);
    
    /**
     * Provides automated LRE window optimization using nonlinear regression 
     * based on the existing start cycle.
     * <p>
     * Window size is set to a default 3 cycles and is 
     * expanded it by attempting to add cycles to the top of the window based on  
     * the LRE window selection parameters. This involves  
     * using nonlinear regression-derived Fb and Fb-slope to generate an 
     * optimized working Fc dataset after each cycle is added to the top of the window. 
     * Note that if a LRE window has not been set, the LRE window is reinitialized before 
     * attempting nonlinear regression analysis; otherwise the existing start cycle is used. 
     * <p>
     * Note also that this function must save the 
     * modified Profile to the corresponding database, e.g. via ProfileSummary.update().
     * 
     * @param prfSum the ProfileSummary encapsulating the Profile to be initialized
     * @param parameters the LRE window selection parameters or null if default values are to be used
     * @return true if an LRE window was found or false if window selection failed
     */
    public abstract boolean optimizeLreWindowUsingNonlinearRegression(ProfileSummary prfSum, LreWindowSelectionParameters parameters);
    
    /**
     * Updates the LRE analysis using nonlinear regression analysis 
     * within the supplied Profile without modifying the LRE window. 
     * The primary intent is to process manual adjustments to the LRE window.
     * <p>
     * Note that this replaces the ProfileSummary update, which provides LRE 
     * window updating without nonlinear regression. 
     * <p>
     * Note also that this function must save the modified to Profile to the 
     * corresponding database, e.g. via ProfileSummary.update().
     * 
     * @param prfSum the ProfileSummary encapsulating the Profile to be updated
     * @param parameters the LRE window selection parameters
     * @return whether the LRE window was updated successfully
     */
    public abstract boolean lreWindowUpdateUsingNR(ProfileSummary prfSum, LreWindowSelectionParameters parameters);
}
