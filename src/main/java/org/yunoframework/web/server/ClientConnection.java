package org.yunoframework.web.server;

import org.yunoframework.web.Response;
import org.yunoframework.web.http.HttpParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Representation of HTTP Client's connection to NIO server
 */
public class ClientConnection {

	private final SocketChannel channel;
	private final RequestHandlerThread thread;

	/**
	 * Creates new instance of ClientConnection which represent connection of HTTP client
	 * It must be called from {@see RequestHandlerThread)
	 * @param channel client's socket channel
	 */
	public ClientConnection(SocketChannel channel) {
		this.channel = channel;

		// Let's get current thread, we will need it later to access cached response builder
		Thread thread = Thread.currentThread();
		if (!(thread instanceof RequestHandlerThread)) {
			throw new IllegalStateException("RequestHandler must be called from RequestHandlerThread");
		}

		this.thread = (RequestHandlerThread) thread;

		// Remove old data from current thread's response builder, we want to use it again
		this.thread.clearResponseBuilder();
	}

	public void send(Response response) throws IOException {
		if (!this.thread.equals(Thread.currentThread())) {
			throw new IllegalStateException("handle() must be called from the same thread which created this instance of RequestHandler");
		}

		this.channel.write(ByteBuffer.wrap(HttpParser.serializeResponse(this.thread.getResponseBuilder(), response)));
		if (response.header("Connection").equalsIgnoreCase("close")) {
			System.out.println("Closed connection");
			this.channel.close();
		}
	}
}
