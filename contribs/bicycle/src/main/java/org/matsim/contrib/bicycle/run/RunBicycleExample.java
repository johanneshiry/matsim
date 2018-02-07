/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package org.matsim.contrib.bicycle.run;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

/**
 * @author dziemke
 */
public class RunBicycleExample {

	public static void main(String[] args) {
		// This works when the data is stored under "/matsim/contribs/bicycle/src/main/resources/bicycle_example"
		boolean considerMotorizedInteraction = false;
		Config config = ConfigUtils.loadConfig("bicycle_example/config.xml", new BicycleConfigGroup());
		config.controler().setLastIteration(0);
		new RunBicycleExample().run(config, considerMotorizedInteraction);
	}

	public void run(Config config, boolean considerMotorizedInteraction) {
//		config.plansCalcRoute().setInsertingAccessEgressWalk(true);
		config.global().setNumberOfThreads(1);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		
		// New, yet to be applied
		config.plansCalcRoute().setRoutingRandomness(0.2);
		//
				
		Scenario scenario = ScenarioUtils.loadScenario(config);

		VehicleType car = VehicleUtils.getFactory().createVehicleType(Id.create(TransportMode.car, VehicleType.class));
		scenario.getVehicles().addVehicleType(car);

		VehicleType bicycle = VehicleUtils.getFactory().createVehicleType(Id.create("bicycle", VehicleType.class));
		bicycle.setMaximumVelocity(15.0/3.6);
		bicycle.setPcuEquivalents(0.25);
		scenario.getVehicles().addVehicleType(bicycle);

		scenario.getConfig().qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);

		Controler controler = new Controler(scenario);
		BicycleModule bicycleModule = new BicycleModule();
		if (considerMotorizedInteraction) {
			bicycleModule.setConsiderMotorizedInteraction(true);
		}
		controler.addOverridingModule(bicycleModule);
		
		controler.run();
	}
}