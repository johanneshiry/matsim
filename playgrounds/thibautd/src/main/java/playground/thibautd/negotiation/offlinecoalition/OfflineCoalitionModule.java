/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.thibautd.negotiation.offlinecoalition;

import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import org.matsim.contrib.socnetsim.framework.replanning.selectors.LogitWeight;
import org.matsim.contrib.socnetsim.framework.replanning.selectors.coalitionselector.CoalitionSelector;
import org.matsim.contrib.socnetsim.framework.replanning.selectors.coalitionselector.ProportionBasedConflictSolver;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.gbl.MatsimRandom;
import playground.thibautd.negotiation.framework.Proposition;

/**
 * @author thibautd
 */
public class OfflineCoalitionModule extends AbstractModule {
	private final Class<? extends Proposition> propositionClass;

	public OfflineCoalitionModule( final Class<? extends Proposition> propositionClass ) {
		this.propositionClass = propositionClass;
	}


	@Override
	public void install() {
		bind( Key.get( Types.newParameterizedType( CoalitionChoiceIterator.class , propositionClass ) ) );
	}

	@Provides
	public CoalitionSelector createSelector( final OfflineCoalitionConfigGroup conf ) {
		return new CoalitionSelector(
				new LogitWeight(
						MatsimRandom.getLocalInstance(),
						conf.getLogitScale() ),
				new ProportionBasedConflictSolver() );
	}
}
