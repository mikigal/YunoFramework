package org.yunoframework.web.routing;

import org.yunoframework.web.http.HttpMethod;

/**
 * Keep information about route
 */
public class RouteInfo {

	private final HttpMethod method;
	private final String path;
	private final Handler handler;

	/**
	 * Creates instance of RouteInfo, it does not automatically register it do Yuno
	 * @param method method of request
	 * @param path path of request
	 * @param handler handler of route {@see Handler}
	 */
	public RouteInfo(HttpMethod method, String path, Handler handler) {
		this.method = method;
		this.path = path;
		this.handler = handler;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public Handler getHandler() {
		return handler;
	}
}
