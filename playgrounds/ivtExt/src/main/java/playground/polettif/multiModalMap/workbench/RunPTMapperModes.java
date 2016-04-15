/* *********************************************************************** *
 * project: org.matsim.*
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


package playground.polettif.multiModalMap.workbench;

import playground.polettif.multiModalMap.mapping.PTMapperModes;

public class RunPTMapperModes {

	public static void main(String[] args) {


<<<<<<< b1b7094614681fac2e0579491657bb3f28927df4
		String base = "E:/"; //"C:/Users/polettif/Desktop/";
=======
		String base = "C:/Users/polettif/Desktop/";
>>>>>>> setup default config, new mode specific ptmapper
		String outbase = base+"output/mtsMapping_modes/";

		// input
//		final String mtsFile = base + "data/mts/zvv/zvv_unmappedSchedule_LV1903+.xml";
//		final String mtsFile = base + "data/mts/uri/schedule_unmapped_cut.xml";
//		final String mtsFile = base + "data/mts/uri/debug.xml";
//		final String mtsFile = base + "data/mts/zvv_69er.xml";
<<<<<<< b1b7094614681fac2e0579491657bb3f28927df4
		final String mtsFile = base + "data/mts/unmapped/fromHafas/uri.xml";
//		final String mtsFile = base + "data/mts/unmapped/fromHafas/zurich.xml";
=======
//		final String mtsFile = base + "data/mts/unmapped/fromHafas/uri.xml";
		final String mtsFile = base + "data/mts/unmapped/fromHafas/zurich.xml";
>>>>>>> setup default config, new mode specific ptmapper
//		final String mtsFile = base + "data/mts/unmapped/fromGtfs/zvv.xml";
//		final String mtsFile = base + "data/mts/unmapped/fromHafas/debug-pseudo.xml";

//		final String networkFile = base + "data/network/uri.xml.gz";
//		final String networkFile = base + "data/network/zurich-city.xml.gz";
//		final String networkFile = base + "data/network/zurich-plus.xml.gz";
<<<<<<< b1b7094614681fac2e0579491657bb3f28927df4
//		final String networkFile = base + "data/network/multimodal/zurich-plus-mm.xml.gz";
		final String networkFile = base + "data/network/multimodal/uri-mm.xml.gz";
=======
		final String networkFile = base + "data/network/mm/zurich-plus-mm.xml";
>>>>>>> setup default config, new mode specific ptmapper
//		final String networkFile = base + "data/network/uri.xml.gz";

		PTMapperModes.main(new String[]{mtsFile, networkFile, outbase+"schedule.xml", outbase+"network.xml"});
	}

}
