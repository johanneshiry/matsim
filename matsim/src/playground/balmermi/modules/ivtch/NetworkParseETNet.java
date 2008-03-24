/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkAdaptCHNavtec.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package playground.balmermi.modules.ivtch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.matsim.gbl.Gbl;
import org.matsim.network.NetworkLayer;
import org.matsim.network.algorithms.NetworkAlgorithm;

public class NetworkParseETNet extends NetworkAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final String nodefile;
	private final String linkfile;
	
	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public NetworkParseETNet(String nodefile, String linkfile) {
		super();
		this.nodefile = nodefile;
		this.linkfile = linkfile;
	}

	//////////////////////////////////////////////////////////////////////
	// private methods
	//////////////////////////////////////////////////////////////////////

	private final void parseNodes(NetworkLayer network) {
		try {
			FileReader file_reader = new FileReader(nodefile);
			BufferedReader buffered_reader = new BufferedReader(file_reader);

			// read header
			String curr_line = buffered_reader.readLine();
			while ((curr_line = buffered_reader.readLine()) != null) {
				String[] entries = curr_line.split("\t", -1);
				// ID  X  Y
				// 0   1  2
				network.createNode(entries[0],entries[1],entries[2],null);
			}
			buffered_reader.close();
			file_reader.close();
		} catch (IOException e) {
			Gbl.errorMsg(e);
		}
	}

	private final void parseLinksET(NetworkLayer network) {
		try {
			FileReader file_reader = new FileReader(linkfile);
			BufferedReader buffered_reader = new BufferedReader(file_reader);

			// read header
			String curr_line = buffered_reader.readLine();
			while ((curr_line = buffered_reader.readLine()) != null) {
				String[] entries = curr_line.split("\t", -1);
				// ET_FID  ID  FROMID  TOID  LENGTH  SPEED  CAP  LANES  ORIGID  TYPE  X1  Y1  X2  Y2
				// 0       1   2       3     4       5      6    7      8       9     10  11  12  13
				if (entries.length == 14) {
					double freespeed = Double.parseDouble(entries[5])/3.6;
					network.createLink(entries[1],entries[2],entries[3],
					                   entries[4],String.valueOf(freespeed),
					                   entries[6],entries[7],entries[8],entries[9]);
				}
			}
			buffered_reader.close();
			file_reader.close();
		} catch (IOException e) {
			Gbl.errorMsg(e);
		}
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public void run(NetworkLayer network) {
		System.out.println("    running " + this.getClass().getName() + " algorithm...");
		
		if (!network.getNodes().isEmpty()) { Gbl.errorMsg("links already exist."); }
		if (!network.getLinks().isEmpty()) { Gbl.errorMsg("links already exist."); }
		
		network.setName("created by '"+this.getClass().getName()+"'");
		network.setCapacityPeriod("01:00:00");
		
		this.parseNodes(network);
		this.parseLinksET(network);

		System.out.println("    done.");
	}
}
