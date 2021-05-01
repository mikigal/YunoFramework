package org.yunoframework.web.server;

import org.yunoframework.web.data.Request;
import org.yunoframework.web.data.Response;
import org.yunoframework.web.Yuno;
import org.yunoframework.web.http.HttpStatus;
import org.yunoframework.web.routing.MiddlewareInfo;
import org.yunoframework.web.routing.RouteInfo;
import org.yunoframework.web.http.HttpParser;

import java.io.IOException;

/**
 * It handles every request received by NIO server
 */
public class RequestHandler {

	private final Yuno yuno;
	private final byte[] rawRequest;
	private final HttpStatus handlingError;
	private final ClientConnection connection;

	/**
	 * Creates new instance of RequestHandler, it does not automatically start handling request.
	 * It must be called from {@see RequestHandlerThread)
	 * @param yuno instance of Yuno
	 * @param channel connection's channel
	 * @param rawRequest received request as unparsed String
	 * @param handlingError error status which occured while handling request by NIO server, null if everything is good
	 * @param connection instance of client's connection which this handler will handle
	 * @throws IllegalStateException when constructor is called from another thread than {@see RequestHandlerThread)
	 */
	public RequestHandler(Yuno yuno, byte[] rawRequest, HttpStatus handlingError, ClientConnection connection) throws IllegalStateException {
		this.yuno = yuno;
		this.rawRequest = rawRequest;
		this.handlingError = handlingError;
		this.connection = connection;
	}

	/**
	 * Start handling request given in constructor.
	 * It must be called from the same thread as constructor of this instance {@see RequestHandlerThread}
	 * @throws IllegalStateException when this method is called from other thread than constructor of this instance
	 * @throws IOException when networking exception occurs
	 */
	public void handle() throws IllegalStateException, IOException {
		try {
			if (this.handlingError != null && this.handlingError != HttpStatus.OK) {
				this.connection.send(this.generateErrorResponse(this.handlingError));
				return;
			}

			Request request = HttpParser.parseRequest(rawRequest);
			if (request.getParseResult() != null && request.getParseResult() != HttpStatus.OK) {
				this.connection.send(this.generateErrorResponse(request.getParseResult()));
				return;
			}

			if (!request.method().isSupported()) {
				this.connection.send(this.generateErrorResponse(HttpStatus.NOT_IMPLEMENTED));
				return;
			}

			RouteInfo routeInfo = yuno.findRoute(request.path());
			if (routeInfo == null) {
				this.connection.send(this.generateErrorResponse(HttpStatus.NOT_FOUND));
				return;
			}

			if (routeInfo.getMethod() != request.method()) {
				this.connection.send(this.generateErrorResponse(HttpStatus.METHOD_NOT_ALLOWED));
				return;
			}

			Response response = new Response(HttpStatus.OK);
			if (request.header("Connection") != null &&
					request.header("Connection").equalsIgnoreCase("close")) {
				response.markToClose();
			}

			for (MiddlewareInfo middleware : this.yuno.getMiddlewares()) {
				middleware.getHandler().apply(request, response);
			}

			routeInfo.getHandler().apply(request, response);
			this.connection.send(response);
		} catch (Exception e) {
			this.connection.send(this.generateErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR));
			throw new RuntimeException("An exception occurred while processing request", e);
		}
	}

	private Response generateErrorResponse(HttpStatus status) {
		Response response = new Response(status);
		response.html("<html><head><title>" + status.getMessage() + "</title></head>" +
				"<body><h1>" + status.getMessage() + "</h1><hr /><h3>Yuno/1.0</h3></body></html>", status);
		response.markToClose();

		return response;
	}
}
