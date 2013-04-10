package com.acme.example.server;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.impl.RoutePolicySupport;

public class RouteControlPolicy extends RoutePolicySupport
{
	private String routeIDtoControl;
	
	public void setRouteIDtoControl(String routeID)
	{
		routeIDtoControl = routeID;
	}
	
	public RouteControlPolicy(String routeID)
	{
		super();
		routeIDtoControl = routeID;
	}
	
	
		@Override
		public void onExchangeBegin(Route route, Exchange exchange)
		{
			CamelContext context = exchange.getContext();

	        try {
	            if (route.getId().equals("SuspendRoute")) {
	                System.out.println("Suspending/Stopping route " + routeIDtoControl);
	               context.suspendRoute(routeIDtoControl);
	                // context.stopRoute(routeIDtoControl);
	            } else if (route.getId().equals("ResumeRoute")) {
	                System.out.println("Resuming/Starting route " + routeIDtoControl);
	               context.resumeRoute(routeIDtoControl);
	                // context.startRoute(routeIDtoControl);
	            }
	        } catch (Exception e) {
	            getExceptionHandler().handleException(e);
	        }
	    }
}

