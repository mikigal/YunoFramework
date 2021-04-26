package org.yunoframework.web.routing;

import org.yunoframework.web.http.HttpMethod;

public class RouteInfo {

	private final HttpMethod method;
	private final String path;
	private final RouteHandler routeHandler;

	public RouteInfo(HttpMethod method, String path, RouteHandler routeHandler) {
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

	public RouteHandler getRouteHandler() {
		return routeHandler;
	}
}
