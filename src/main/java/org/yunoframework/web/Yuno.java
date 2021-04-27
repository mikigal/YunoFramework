package org.yunoframework.web;

import org.yunoframework.web.http.HttpMethod;
import org.yunoframework.web.server.SocketServer;
import org.yunoframework.web.routing.RouteHandler;
import org.yunoframework.web.routing.RouteInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class Yuno {

	private final int threads;
	private final Set<RouteInfo> routes;
	private final SocketServer socketServer;

	private Yuno(int threads) {
		this.threads = threads;
		this.routes = new HashSet<>();
		this.socketServer = new SocketServer(this, this.threads);
	}

	/**
	 * Starts Yuno server
	 * @param address address with port to bind server, it accepts various host:port formats like ":8080", "[::0]:8080", etc
	 */
	public void listen(String address) throws IOException {
		String[] split = address.split(":");
		if (split.length == 0) {
			throw new IllegalStateException("Invalid address");
		}

		int port = Integer.parseInt(split[split.length - 1]);

		this.listen(address.replaceAll(":" + port + "$", ""), port);
	}

	/**
	 * Starts Yuno server
	 * @param host host to bind server
	 * @param port port to bind server
	 */
	public void listen(String host, int port) throws IOException {
		this.socketServer.listen(new InetSocketAddress(host, port));
	}

	/**
	 * Searches RouteInfo of given data
	 * @param method method of request
	 * @param path path of endpoint
	 * @return it's RouteInfo if found, else null
	 */
	public RouteInfo findRoute(HttpMethod method, String path) {
		path = path.toLowerCase();
		for (RouteInfo routeInfo : this.routes) {
			if (routeInfo.getPath().equals(path) && routeInfo.getMethod() == method) {
				return routeInfo;
			}
		}

		return null;
	}

	/**
	 * Registers route
	 * @param method HTTP method
	 * @param path path of endpoint
	 * @param handler handler of endpoint {@see RouteHandler}
	 */
	public void route(HttpMethod method, String path, RouteHandler handler) {
		this.routes.add(new RouteInfo(method, path, handler));
	}

	/**
	 * Registers route for GET method
	 * @param path path of endpoint
	 * @param handler handler of endpoint {@see RouteHandler}
	 */
	public void get(String path, RouteHandler handler) {
		this.route(HttpMethod.GET, path, handler);
	}

	public static Yuno.Builder builder() {
		return new Yuno.Builder();
	}

	public static final class Builder {
		private int threads = 1;

		/**
		 * Sets amount of threads used to handling connections by Yuno
		 * @param threads amount of threads used to handling connections
		 * @return This builder
		 */
		public Yuno.Builder threads(int threads) {
			this.threads = threads;
			return this;
		}

		/**
		 * Creates instance of Yuno with given parameters
		 * @return new instance of Yuno
		 */
		public Yuno build() {
			return new Yuno(this.threads);
		}
	}
}
