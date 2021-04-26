package org.yunoframework.web.nio;

import org.yunoframework.web.Request;
import org.yunoframework.web.routing.RouteInfo;
import org.yunoframework.web.Yuno;
import org.yunoframework.web.http.HttpParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class RequestHandler {

	private final Yuno yuno;
	private final SocketChannel channel;
	private final String rawRequest;

	public RequestHandler(Yuno yuno, SocketChannel channel, String rawRequest) {
		this.yuno = yuno;
		this.channel = channel;
		this.rawRequest = rawRequest;
	}

	public void handle() throws IOException {
		Request request = HttpParser.parse(rawRequest);
		if (request == null) {
			this.send("malformed request 400");
			this.channel.close();
			return;
		}

		if (!request.getMethod().isSupported()) {
			this.send("not implemented");
			this.channel.close();
			return;
		}

		RouteInfo routeInfo = yuno.findRoute(request.getMethod(), request.getPath());
		if (routeInfo == null) {
			this.send("not found 404");
			this.channel.close();
			return;
		}

		System.out.println(request);
		routeInfo.getRouteHandler().apply(request);
	}

	private void send(String message) throws IOException {
		ByteBuffer response = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
		this.channel.write(response);
	}
}
