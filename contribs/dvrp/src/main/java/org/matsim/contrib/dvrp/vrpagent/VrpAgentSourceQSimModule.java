/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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
 * *********************************************************************** *
 */

package org.matsim.contrib.dvrp.vrpagent;

import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.QSim;

import com.google.inject.Inject;

public class VrpAgentSourceQSimModule extends AbstractQSimModule {
	private final String mode;

	public VrpAgentSourceQSimModule(String mode) {
		this.mode = mode;
	}

	@Override
	protected void configureQSim() {
		bindComponent(VrpAgentSource.class, DvrpModes.mode(mode)).toProvider(
				new ModalProviders.AbstractProvider<VrpAgentSource>(mode) {
					@Inject
					private QSim qSim;

					@Override
					public VrpAgentSource get() {
						return new VrpAgentSource(getModalInstance(VrpAgentLogic.DynActionCreator.class),
								getModalInstance(Fleet.class), getModalInstance(VrpOptimizer.class), qSim);
					}
				}).asEagerSingleton();
	}
}
