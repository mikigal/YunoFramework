package org.yunoframework.web.server;

import org.yunoframework.web.Request;
import org.yunoframework.web.Yuno;
import org.yunoframework.web.http.HttpStatus;
import org.yunoframework.web.routing.RouteInfo;
import org.yunoframework.web.http.HttpParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * It handles every request received by NIO server
 */
public class RequestHandler {

	private final Yuno yuno;
	private final SocketChannel channel;
	private final String rawRequest;
	private final RequestHandlerThread thread;

	/**
	 * Creates new instance of RequestHandler, it does not automatically start handling request.
	 * It must be called from {@see RequestHandlerThread)
	 * @param yuno instance of Yuno
	 * @param channel connection's channel
	 * @param rawRequest received request as unparsed String
	 * @throws IllegalStateException when constructor is called from another thread than {@see RequestHandlerThread)
	 */
	public RequestHandler(Yuno yuno, SocketChannel channel, String rawRequest) throws IllegalStateException {
		this.yuno = yuno;
		this.channel = channel;
		this.rawRequest = rawRequest;

		// Let's get current thread, we will need it later to access cached response builder
		Thread thread = Thread.currentThread();
		if (!(thread instanceof RequestHandlerThread)) {
			throw new IllegalStateException("RequestHandler must be called from RequestHandlerThread");
		}

		this.thread = (RequestHandlerThread) thread;

		// Remove old data from current thread's response builder, we want to use it again
		this.thread.clearResponseBuilder();
	}

	/**
	 * Start handling request given in constructor.
	 * It must be called from the same thread as constructor of this instance {@see RequestHandlerThread}
	 * @throws IllegalStateException when this method is called from other thread than constructor of this instance
	 * @throws IOException when networking exception occurs
	 */
	public void handle() throws IllegalStateException, IOException {
		if (!this.thread.equals(Thread.currentThread())) {
			throw new IllegalStateException("handle() must be called from the same thread which created this instance of RequestHandler");
		}

		Request request = HttpParser.parseRequest(rawRequest);
		if (request == null) {
			//this.send("malformed request 400");
			this.channel.close();
			return;
		}

		if (!request.getMethod().isSupported()) {
			//this.send("not implemented");
			this.channel.close();
			return;
		}

		RouteInfo routeInfo = yuno.findRoute(request.getMethod(), request.getPath());
		if (routeInfo == null) {
			//this.send("not found 404");
			this.channel.close();
			return;
		}

		Map<String, String> headers = new HashMap<>();
		headers.put("Date", HttpParser.DATE_FORMAT.format(new Date()));
		headers.put("Content-Type", "application/json");
		headers.put("Content-Length", "2");
		headers.put("Server", "Yuno/1.0");
		headers.put("Connection", "keep-alive");

		routeInfo.getRouteHandler().apply(request);
		this.send(HttpParser.serializeResponse(
				this.thread.getResponseBuilder(), HttpStatus.OK, headers, "{}".getBytes(StandardCharsets.UTF_8)));
	}

	private void send(byte[] data) throws IOException {
		this.channel.write(ByteBuffer.wrap(data));
	}
}
