/* *********************************************************************** *
 * project: org.matsim.*
 * FastDijkstraFactory.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package org.matsim.core.router;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LinkToLinkLeastCostPathCalculator;
import org.matsim.core.router.util.LinkToLinkLeastCostPathCalculatorFactory;
import org.matsim.core.router.util.LinkToLinkRoutingNetwork;
import org.matsim.core.router.util.LinkToLinkRoutingNetworkFactory;
import org.matsim.core.router.util.LinkToLinkTravelDisutility;
import org.matsim.core.router.util.LinkToLinkTravelTime;
import org.matsim.core.router.util.PreProcessDijkstra;
import org.matsim.core.router.util.RoutingNetworkLink;
import org.matsim.core.router.util.RoutingNetworkNode;
import org.matsim.lanes.data.Lanes;

@Singleton
public class LinkToLinkFastDijkstraFactory implements LinkToLinkLeastCostPathCalculatorFactory {
	
	private final boolean usePreProcessData;
	private final LinkToLinkRoutingNetworkFactory routingNetworkFactory;
	private final Map<Network, LinkToLinkRoutingNetwork> routingNetworks = new HashMap<>();	// TODO: should also depend on the Lanes
	private final Map<Network, PreProcessDijkstra> preProcessData = new HashMap<>();	// TODO: should also depend on the Lanes

	@Inject
	public LinkToLinkFastDijkstraFactory() {
		this(false);
	}

    public LinkToLinkFastDijkstraFactory(final boolean usePreProcessData) {
    	this.usePreProcessData = usePreProcessData;
    	this.routingNetworkFactory = new LinkToLinkRoutingNetworkFactory();
	}

	@Override
	public synchronized LinkToLinkLeastCostPathCalculator createPathCalculator(final Network network, final Lanes lanes, final LinkToLinkTravelDisutility travelCost, final LinkToLinkTravelTime travelTime) {
		LinkToLinkRoutingNetwork routingNetwork = this.routingNetworks.get(network);
		PreProcessDijkstra preProcessDijkstra = this.preProcessData.get(network);
		
		if (routingNetwork == null) {
			routingNetwork = this.routingNetworkFactory.createRoutingNetwork(network, lanes);
			
			if (this.usePreProcessData) {
				if (preProcessDijkstra == null) {
					preProcessDijkstra = new PreProcessDijkstra();
					preProcessDijkstra.run(network, lanes);
					this.preProcessData.put(network, preProcessDijkstra);
					
					/*
					 * Collect data for nodes that are only present in the routing network. 
					 * Duplicated nodes are identified by links ending at them. We collect
					 * the nodes that they replicate and store the data in a lookup map.
					 */
					Map<Id<Node>, Node> originalNodes = new HashMap<>();
					for (RoutingNetworkLink link : routingNetwork.getLinks().values()) {
						if (!network.getNodes().containsKey(link.getToNode())) {
							Link originalLink = network.getLinks().get(link.getId());
							originalNodes.put(link.getToNode().getId(), originalLink.getToNode());
						}
					}
					
					/*
					 * If the node is not present in the original network, no  pre-process data is found.
					 * As a result, new data is created, which cannot be used for meaningful routing since
					 * it contains default cost values (which are Double.POSITIVE_INFINITY!).
					 * To avoid this, we refer to the data of the original node.
					 */
					for (RoutingNetworkNode node : routingNetwork.getNodes().values()) {
						if (network.getNodes().containsKey(node.getId())) node.setDeadEndData(preProcessDijkstra.getNodeData(node.getNode()));
						else node.setDeadEndData(preProcessDijkstra.getNodeData(originalNodes.get(node.getNode().getId())));
					}
				}
			}
			
			this.routingNetworks.put(network, routingNetwork);
		}
		
		return new LinkToLinkFastDijkstra(routingNetwork, travelCost, travelTime, preProcessDijkstra);
	}
}