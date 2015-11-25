/*
 * *********************************************************************** *
 * project: org.matsim.*                                                   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package playground.boescpa.projects.topdad.postprocessing;

import playground.boescpa.analysis.trips.tripCreation.TripCreator;
import playground.boescpa.analysis.trips.tripCreation.TripProcessor;
import playground.boescpa.analysis.trips.tripCreation.spatialCuttings.CirclePointCutting;
import playground.boescpa.analysis.trips.tripCreation.spatialCuttings.SpatialCuttingStrategy;

/**
 * Creates and evaluates trips for topdad-postprocessing.
 *
 * @author boescpa
 */
public class TopdadTripCreator {

	public static void main(String[] args) {
		String eventsFile = args[0]; // Path to an events-File, e.g. "run.combined.150.events.xml.gz"
		String networkFile = args[1]; // Path to the network-File used for the simulation resulting in the above events-File, e.g. "multimodalNetwork2030final.xml.gz"
		String tripFile = args[2]; // Path to the trip-File produced as output, e.g. "trips2030combined.txt"
		String valueFile = args[3]; // Path to the value-File produced as output, e.g. "vals2030combined.txt"
		int zoneRadius = Integer.valueOf(args[4]);

		SpatialCuttingStrategy circleBellevueCutting = new CirclePointCutting(zoneRadius, 683518.0, 246836.0);
		TripProcessor tripProcessor = new TopdadTripProcessor(tripFile, valueFile, circleBellevueCutting);
		TripCreator tripCreator = new TripCreator(eventsFile, networkFile, tripProcessor);
		tripCreator.createTrips();
	}

}