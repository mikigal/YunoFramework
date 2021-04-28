package org.yunoframework.web.routing;

import org.yunoframework.web.Request;
import org.yunoframework.web.Response;

/**
 * Handler of route
 *
 * There's 2 ways to use RouteHandler.
 * 1. Create manually method like <code>public static void endpoint(Request request, Response response)</code>, then register it with lambda <code>MyController::endpoint</code>
 * 2. Create new class which implement RouteHandler, then implement code of your endpoint in <code>apply(Request, Response)</code> method, then register endpoint with instance of your class
 */
public interface RouteHandler {

	/**
	 * This method will be called when user send request to endpoint associated with this handler
	 * @param request object with data of user's request
	 * @param response object with response data, you have to modify it {@see Response}
	 * @throws Exception if any exception is thrown from this method, server will send Error 500 to HTTP client
	 */
	void apply(Request request, Response response) throws Exception;
}
