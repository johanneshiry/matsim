/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * QSimProvider.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2015 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package org.matsim.core.mobsim.qsim;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.qsim.interfaces.ActivityHandler;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

public class QSimProvider implements Provider<QSim> {
	private static final Logger log = Logger.getLogger( QSimProvider.class ) ;
	
	private Injector injector;
	private Collection<AbstractQSimPlugin> plugins;

	@Inject
    QSimProvider(Injector injector, Collection<AbstractQSimPlugin> plugins) {
        this.injector = injector;
		this.plugins = plugins;
    }

    @Override
    public QSim get() {
	    com.google.inject.AbstractModule module = new com.google.inject.AbstractModule() {
			@Override
			protected void configure() {
				for (AbstractQSimPlugin plugin : plugins) {
					// install each plugin's modules:
					for (Module module1 : plugin.modules()) {
						install(module1);
					}
				}
				bind(QSim.class).asEagerSingleton();
				bind(Netsim.class).to(QSim.class);
			}
		};
        Injector qSimLocalInjector = injector.createChildInjector(module);
        org.matsim.core.controler.Injector.printInjector( qSimLocalInjector, log ) ;
        QSim qSim = qSimLocalInjector.getInstance(QSim.class);
//        qSim.setChildInjector( qSimLocalInjector ) ;
        
        ComponentRegistry<MobsimEngine> mobsimEngineRegistry = new ComponentRegistry<>();
        ComponentRegistry<ActivityHandler> activityHandlerRegister = new ComponentRegistry<>();
        ComponentRegistry<DepartureHandler> departureHandlerRegistry = new ComponentRegistry<>();
        ComponentRegistry<AgentSource> agentSourceRegistry = new ComponentRegistry<>();
        
        for (AbstractQSimPlugin plugin : plugins) {
        	plugin.engines().forEach(mobsimEngineRegistry::register);
        	plugin.activityHandlers().forEach(activityHandlerRegister::register);
        	plugin.departureHandlers().forEach(departureHandlerRegistry::register);
        	plugin.agentSources().forEach(agentSourceRegistry::register);
        }
        
        List<String> mobsimEngineOrdering = new LinkedList<>();
        List<String> activityHandlerOrdering = new LinkedList<>();
        List<String> departureHandlerOrdering = new LinkedList<>();
        List<String> agentSourceOrdering = new LinkedList<>();
        
        mobsimEngineRegistry.getOrderedComponents(mobsimEngineOrdering).stream().map(qSimLocalInjector::getInstance).forEach(qSim::addMobsimEngine);
        activityHandlerRegister.getOrderedComponents(activityHandlerOrdering).stream().map(qSimLocalInjector::getInstance).forEach(qSim::addActivityHandler);
        departureHandlerRegistry.getOrderedComponents(departureHandlerOrdering).stream().map(qSimLocalInjector::getInstance).forEach(qSim::addDepartureHandler);
        agentSourceRegistry.getOrderedComponents(agentSourceOrdering).stream().map(qSimLocalInjector::getInstance).forEach(qSim::addAgentSource);
        
        return qSim;
    }

}
