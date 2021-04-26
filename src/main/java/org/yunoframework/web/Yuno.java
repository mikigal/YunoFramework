package org.yunoframework.web;

import org.yunoframework.web.nio.SocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Yuno {

	private final int threads;
	private final SocketServer socketServer;

	private Yuno(int threads) {
		this.threads = threads;
		this.socketServer = new SocketServer(this.threads);
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
