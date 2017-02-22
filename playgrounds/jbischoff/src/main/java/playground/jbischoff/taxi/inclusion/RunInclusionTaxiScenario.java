/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package playground.jbischoff.taxi.inclusion;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.file.VehicleReader;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;
import org.matsim.contrib.dvrp.run.*;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.contrib.taxi.optimizer.TaxiOptimizer;
import org.matsim.contrib.taxi.passenger.TaxiRequestCreator;
import org.matsim.contrib.taxi.run.*;
import org.matsim.contrib.taxi.vrpagent.TaxiActionCreator;
import org.matsim.core.config.*;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import com.google.inject.AbstractModule;

import playground.jbischoff.taxi.setup.*;


public class RunInclusionTaxiScenario
{
    public static void run(String configFile, boolean otfvis)
    {
    	
        Config config = ConfigUtils.loadConfig(configFile, new TaxiConfigGroup(),
                new OTFVisConfigGroup());
        createControler(config, otfvis).run();
    
    }
    public static void runMany(String configFile)
    {
    	for (int i = 50; i<=1000; i=i+50){
    	
        Config config = ConfigUtils.loadConfig(configFile, new TaxiConfigGroup(), new DvrpConfigGroup(),
                new OTFVisConfigGroup());
        config.controler().setOutputDirectory("D:/runs-svn/barrierFreeTaxi/1500_run_"+i+"/");
        config.controler().setRunId("taxis_"+i);
        DvrpConfigGroup.get(config).setMode(TaxiModule.TAXI_MODE);
        TaxiConfigGroup taxi = (TaxiConfigGroup) config.getModules().get(TaxiConfigGroup.GROUP_NAME);
        taxi.setTaxisFile("hc_vehicles"+i+".xml.gz");
        createControler(config, false).run();
    	}
    }


    public static Controler createControler(Config config, boolean otfvis)
    {
        TaxiConfigGroup taxiCfg = TaxiConfigGroup.get(config);
        config.addConfigConsistencyChecker(new TaxiConfigConsistencyChecker());
        config.checkConsistency();

        Scenario scenario = ScenarioUtils.loadScenario(config);
        FleetImpl fleet = new FleetImpl();
        new VehicleReader(scenario.getNetwork(), fleet).readFile(taxiCfg.getTaxisFileUrl(config.getContext()).getFile());

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new JbTaxiModule(fleet));
        controler.addOverridingModule(new DvrpModule(fleet, new AbstractModule() {
			@Override
			protected void configure() {
				bind(TaxiOptimizer.class).toProvider(JbTaxiOptimizerProvider.class).asEagerSingleton();
				bind(VrpOptimizer.class).to(TaxiOptimizer.class);
				bind(DynActionCreator.class).to(TaxiActionCreator.class).asEagerSingleton();
				bind(PassengerRequestCreator.class).to(TaxiRequestCreator.class).asEagerSingleton();
			}
		}, TaxiOptimizer.class));

        if (otfvis) {
            controler.addOverridingModule(new OTFVisLiveModule());
        }

        return controler;
    }


    public static void main(String[] args)
    {
//        RunInclusionTaxiScenario.run("C:/Users/Joschka/Documents/shared-svn/projects/sustainability-w-michal-and-dlr/data/scenarios/inclusion/config.xml", false);
        RunInclusionTaxiScenario.runMany("C:/Users/Joschka/Documents/shared-svn/projects/sustainability-w-michal-and-dlr/data/scenarios/inclusion/config.xml");
    }
}
