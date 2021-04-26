package org.yunoframework.web.routing;

import org.yunoframework.web.Request;

/**
 * Handler of route
 *
 * There's 2 ways to use RouteHandler.
 * 1. Create manually method like <code>public static void endpoint(Request request)</code>, then register it with lambda <code>MyController::endpoint</code>
 * 2. Create new class which implement RouteHandler, then implement code of your endpoint in <code>apply(Request)</code> method, then register endpoint with instance of your class
 */
public interface RouteHandler {

	/**
	 * This method will be called when user send request to endpoint associated with this handler
	 * @param request object with data of user's request
	 */
	void apply(Request request);
}
