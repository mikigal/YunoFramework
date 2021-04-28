package org.yunoframework.web.routing;

import org.yunoframework.web.http.HttpMethod;

/**
 * Keep information about route
 */
public class RouteInfo {

	private final HttpMethod method;
	private final String path;
	private final Handler routeHandler;

	/**
	 * Creates instance of RouteInfo, it does not automatically register it do Yuno
	 * @param method method of request
	 * @param path path of request
	 * @param routeHandler handler of route {@see RouteHandler}
	 */
	public RouteInfo(HttpMethod method, String path, Handler routeHandler) {
		this.method = method;
		this.path = path;
		this.routeHandler = routeHandler;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public Handler getRouteHandler() {
		return routeHandler;
	}
}
