package org.yunoframework.web;

import org.yunoframework.web.http.HttpMethod;
import org.yunoframework.web.routing.MiddlewareInfo;
import org.yunoframework.web.server.SocketServer;
import org.yunoframework.web.routing.Handler;
import org.yunoframework.web.routing.RouteInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class Yuno {

	/**
	 * Current version of Yuno
	 */
	public static final String VERSION = "1.0.0";

	private final Set<RouteInfo> routes;
	private List<MiddlewareInfo> middlewares;

	private final SocketServer socketServer;

	/**
	 * Creates new instance of Yuno, can be called only by builder
	 * @param threads number of used by NIO server
	 * @see Yuno.Builder
	 */
	private Yuno(int threads) {
		this.routes = new HashSet<>();
		this.middlewares = new ArrayList<>();
		this.socketServer = new SocketServer(this, threads);
	}

	/**
	 * Starts Yuno server
	 * @param address address with port to bind server, it accepts various host:port formats like ":8080", "[::0]:8080", etc
	 * @throws IOException when network exception occurs
	 * @throws IllegalStateException when address is invalid
	 */
	public void listen(String address) throws IOException, IllegalStateException {
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
	 * @param path path of endpoint
	 * @return it's RouteInfo if found, else null
	 */
	public RouteInfo findRoute(String path) {
		path = path.toLowerCase();
		for (RouteInfo routeInfo : this.routes) {
			if (routeInfo.getPath().equals(path)) {
				return routeInfo;
			}
		}

		return null;
	}

	/**
	 * Registers middleware, middleware with lower priority will be called first
	 * @param handler Handler of middleware {@see Handler}
	 * @param priority priority of middleware
	 */
	public void middleware(Handler handler, int priority) {
		this.middlewares.add(new MiddlewareInfo(handler, priority));
		this.middlewares = this.middlewares.stream()
				.sorted(Comparator.comparingInt(MiddlewareInfo::getPriority))
				.collect(Collectors.toList());
	}

	/**
	 * Returns list of middlewares sorted by priority (from lowest to highest)
	 * @return list of middlewares sorted by priority (from lowest to highest)
	 */
	public List<MiddlewareInfo> getMiddlewares() {
		return middlewares;
	}

	/**
	 * Registers route
	 * @param method HTTP method
	 * @param path path of endpoint
	 * @param handler handler of endpoint {@see Handler}
	 */
	public void route(HttpMethod method, String path, Handler handler) {
		this.routes.add(new RouteInfo(method, path, handler));
	}

	/**
	 * Registers route for GET method
	 * @param path path of endpoint
	 * @param handler handler of endpoint {@see Handler}
	 */
	public void get(String path, Handler handler) {
		this.route(HttpMethod.GET, path, handler);
	}

	/**
	 * Returns new instance of Yuno's builder
	 * @return new instance of Yuno's builder
	 */
	public static Yuno.Builder builder() {
		return new Yuno.Builder();
	}

	/**
	 * Yuno's builder
	 */
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
