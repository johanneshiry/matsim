/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

package playground.singapore.ptsim.pt;

import java.util.Iterator;
import java.util.ListIterator;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.pt.TransitStopAgentTracker;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.utils.misc.PopulationUtils;
import org.matsim.pt.PtConstants;
import org.matsim.pt.Umlauf;
import org.matsim.pt.UmlaufStueckI;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;

/**
 * @author michaz
 */
public class UmlaufDriver extends AbstractTransitDriver {

	private static class PlanBuilder {

		PlanImpl plan = new PlanImpl();

		String activityType = PtConstants.TRANSIT_ACTIVITY_TYPE;

		public void addTrip(NetworkRoute networkRoute, String transportMode) {
			Activity lastActivity;
			if (!plan.getPlanElements().isEmpty()) {
				lastActivity = (Activity) plan.getPlanElements().get(plan.getPlanElements().size()-1);
				assert lastActivity.getLinkId().equals(networkRoute.getStartLinkId());
			} else {
				lastActivity = new ActivityImpl(activityType, networkRoute.getStartLinkId());
				plan.addActivity(lastActivity);
			}
			Leg leg = new LegImpl(transportMode);
			leg.setRoute(networkRoute);
			plan.addLeg(leg);
			Activity activity = new ActivityImpl(activityType, networkRoute.getEndLinkId());
			plan.addActivity(activity);
		}

		public PlanImpl build() {
			return plan;
		}

	}

	private final Umlauf umlauf;
	private Iterator<UmlaufStueckI> iUmlaufStueck;
	private ListIterator<PlanElement> iPlanElement;
	private NetworkRoute carRoute;
	private double departureTime;
	private PlanElement currentPlanElement;
	private TransitLine transitLine;
	private TransitRoute transitRoute;
	private Departure departure;
	
	public UmlaufDriver(Umlauf umlauf,
			String transportMode,
			TransitStopAgentTracker thisAgentTracker, InternalInterface internalInterface) {
		super(internalInterface, thisAgentTracker);
		this.umlauf = umlauf;
		this.iUmlaufStueck = this.umlauf.getUmlaufStuecke().iterator();
		PersonImpl driverPerson = new PersonImpl(new IdImpl("pt_"+umlauf.getId())); // we use the non-wrapped route for efficiency, but the leg has to return the wrapped one.
		PlanBuilder planBuilder = new PlanBuilder();
		for (UmlaufStueckI umlaufStueck : umlauf.getUmlaufStuecke()) {
			planBuilder.addTrip(getWrappedCarRoute(umlaufStueck.getCarRoute()), transportMode);
		}
		Plan plan = planBuilder.build();
		driverPerson.addPlan(plan);
		driverPerson.setSelectedPlan(plan);
		setDriver(driverPerson);
		iPlanElement = plan.getPlanElements().listIterator();
		this.currentPlanElement = iPlanElement.next();
		setNextLeg();
	}

	@Override
	public void endActivityAndComputeNextState(final double now) {
		this.currentPlanElement = iPlanElement.next();
		sendTransitDriverStartsEvent(now);	
		
//		this.sim.arrangeAgentDeparture(this);
		this.state = MobsimAgent.State.LEG ;
//		this.sim.reInsertAgentIntoMobsim(this) ;
		// yyyyyy 000000

	}

	@Override
	public void endLegAndComputeNextState(final double now) {
		this.getSimulation().getEventsManager().processEvent(
				this.getSimulation().getEventsManager().getFactory().createAgentArrivalEvent(
						now, this.getId(), this.getDestinationLinkId(), this.getCurrentLeg().getMode()));
		this.currentPlanElement = iPlanElement.next();
		if (this.iUmlaufStueck.hasNext()) {
			setNextLeg();
			if (this.departureTime < now) {
				this.departureTime = now;
			}

//			this.sim.arrangeActivityStart(this);
			this.state = MobsimAgent.State.ACTIVITY ;
//			this.sim.reInsertAgentIntoMobsim(this) ;
			// yyyyyy 000000


		} else {
			// inserting an activity with end time infinity.  one can debate if this is a hack:
			// * in general, a MobsimAgent can construct its path through the day on the fly
			// * in this particular instance, the agent pretends to have a plan
			// kai, mar'12
			
			this.state = MobsimAgent.State.ACTIVITY ;
			this.departureTime = Double.POSITIVE_INFINITY ;
			
		}
	}

	private void setNextLeg() {
		UmlaufStueckI umlaufStueck = this.iUmlaufStueck.next();
		if (umlaufStueck.isFahrt()) {
			setLeg(umlaufStueck.getLine(), umlaufStueck.getRoute(), umlaufStueck.getDeparture());
		} else {
			setWenden(umlaufStueck.getCarRoute());
		}
		init();
	}

	private void setWenden(NetworkRoute carRoute) {
		this.transitLine = null;
		this.transitRoute = null;
		this.departure = null;
		this.carRoute = carRoute;
	}

	private void setLeg(final TransitLine line, final TransitRoute route, final Departure departure) {
		this.transitLine = line;
		this.transitRoute = route;
		this.departure = departure;
		this.departureTime = departure.getDepartureTime();
		this.carRoute = route.getRoute();
	}

	@Override
	Leg getCurrentLeg() {
		return (Leg) this.currentPlanElement;
	}
	
	@Override
	public Double getExpectedTravelTime() {
		return ((Leg) this.currentPlanElement).getTravelTime() ;
	}
	
	@Override
	public String getMode() {
		return ((Leg)this.currentPlanElement).getMode();
	}
	
	@Override
	public Id getPlannedVehicleId() {
		Route route = ((Leg)this.currentPlanElement).getRoute() ;
		return ((NetworkRoute)route).getVehicleId() ; 
	}

//	@Override
//	public Activity getCurrentActivity() {
//		return (Activity) this.currentPlanElement;
//	}

	@Override
	public PlanElement getCurrentPlanElement() {
		return this.currentPlanElement; 
	}

	@Override
	public PlanElement getNextPlanElement() {
		if (iPlanElement.hasNext()) {
			PlanElement next = iPlanElement.next(); // peek at the next element, but...
			iPlanElement.previous(); // ...rewind iterator by one step
			return next;
		} else {
			return null ;
		}
	}

	@Override
	public Id getDestinationLinkId() {
		return getCurrentLeg().getRoute().getEndLinkId();
	}

	@Override
	public NetworkRoute getCarRoute() {
		return this.carRoute;
	}

	@Override
	public TransitLine getTransitLine() {
		return this.transitLine;
	}

	@Override
	public TransitRoute getTransitRoute() {
		return this.transitRoute;
	}

	@Override
	public Departure getDeparture() {
		return this.departure;
	}

	@Override
	public double getActivityEndTime() {
		return this.departureTime;
	}
	
	@Override
	public Plan getSelectedPlan() {
		return PopulationUtils.unmodifiablePlan(this.getPerson().getSelectedPlan());
	}

}
