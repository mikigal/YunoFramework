package org.yunoframework.web.http;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.yunoframework.web.Request;
import org.yunoframework.web.Response;
import org.yunoframework.web.Yuno;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpParser {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	private static final byte[] BODY_PREFIX = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);

	static {
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Parses HTTP request
	 * @param rawRequest HTTP request as byte array
	 * @return Request data, null if raw request is malformed
	 */
	public static Request parseRequest(byte[] rawRequest) {
		try {
			int bodyPosition = -1;
			for (int i = 0; i < rawRequest.length - BODY_PREFIX.length; i++) {
				if (rawRequest[i] == BODY_PREFIX[0] &&
						rawRequest[i + 1] == BODY_PREFIX[1] &&
						rawRequest[i + 2] == BODY_PREFIX[2] &&
						rawRequest[i + 3] == BODY_PREFIX[3]) {
					bodyPosition = i + BODY_PREFIX.length;
				}
			}

			// -4 because we want to cut last "\r\n" from request
			String request = bodyPosition == -1 ? new String(rawRequest) : new String(rawRequest, 0, bodyPosition - 4);
			String[] lines = request.split("\r\n");

			String[] handshake = lines[0].split(" ");

			HttpMethod method = HttpMethod.getByName(handshake[0]);
			if (method == null) {
				return new Request(HttpStatus.BAD_REQUEST,
						null, null, null, null, null, null);
			}

			if (!handshake[2].equalsIgnoreCase("HTTP/1.1")) {
				return new Request(HttpStatus.HTTP_VERSION_NOT_SUPPORTED,
						null, null, null, null, null, null);
			}

			String[] endpoint = handshake[1].split("\\?");
			String path = endpoint[0];
			Map<String, String> params = endpoint.length == 1 ? new CaseInsensitiveMap<>() : parseParams(endpoint[1]);
			Map<String, String> headers = parseHeaders(lines);

			byte[] rawContent = new byte[bodyPosition == -1 ? 0 : (rawRequest.length - bodyPosition)];
			if (bodyPosition != -1) {
				System.arraycopy(rawRequest, bodyPosition, rawContent, 0, rawRequest.length - bodyPosition);
			}

			Object body = null;
			if (bodyPosition != -1 && headers.get("Content-Type") != null &&
					headers.get("Content-Type").equalsIgnoreCase("application/x-www-form-urlencoded")) {
				body = parseParams(new String(rawContent));
			}

			return new Request(HttpStatus.OK, method, path, params, headers, rawContent, body);
		} catch (Exception e) {
			return new Request(HttpStatus.BAD_REQUEST,
					null, null, null, null, null, null);
		}
	}

	/**
	 * Converts parameters as String to <code>Map<ParameterName, ParameterValue></code>
	 * @param raw parameters as String e. g. "foo=bar&abc=def", without "?" at the beginning
	 * @return Map with parsed parameters
	 */
	private static Map<String, String> parseParams(String raw) throws UnsupportedEncodingException {
		Map<String, String> params = new CaseInsensitiveMap<>();
		String[] rawParams = raw.split("&");
		for (String param : rawParams) {
			String[] split = param.split("=");
			params.put(URLDecoder.decode(split[0], "UTF-8"), URLDecoder.decode(split[1]));
		}

		return params;
	}

	/**
	 * Parse headers from HTTP request given as String to to <code>Map<HeaderName, HeaderValue></code>
	 * @param requestLines all lines as request
	 * @return Map with parser parameters
	 */
	private static Map<String, String> parseHeaders(String[] requestLines) {
		Map<String, String> headers = new CaseInsensitiveMap<>();
		for (int i = 1; i < requestLines.length - 1; i++) {
			String currentLine = requestLines[i];
			if (!currentLine.contains(": ")) {
				continue;
			}

			String[] header = requestLines[i].split(": ");
			headers.put(header[0], header[1]);
		}

		return headers;
	}

	/**
	 * Serialize HTTP response to byte[]
	 * @param responseBuilder empty StringBuilder, it should be cached response builder from {@see RequestHandlerThread}
	 * @param response HTTP response
	 * @return serialized response as byte array
	 */
	public static byte[] serializeResponse(StringBuilder responseBuilder, Response response) {
		prepareResponse(response);

		responseBuilder.append("HTTP/1.1 ").append(response.status().getMessage()).append("\r\n");

		for (Map.Entry<String, String> header : response.headers().entrySet()) {
			responseBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
		}
		responseBuilder.append("\n");

		byte[] headersBytes = responseBuilder.toString().getBytes(StandardCharsets.UTF_8);
		byte[] buffer = new byte[headersBytes.length + response.content().length];

		System.arraycopy(headersBytes, 0, buffer, 0, headersBytes.length);
		System.arraycopy(response.content(), 0, buffer, headersBytes.length, response.content().length);
		return buffer;
	}

	/**
	 * Prepared response to send to client, sets required headers to HTTP response
	 * @param response response which you have to prepare to send
	 */
	private static void prepareResponse(Response response) {
		response.setHeader("Server", "Yuno/" + Yuno.VERSION);
		response.setHeader("Date", HttpParser.DATE_FORMAT.format(new Date()));
		response.setHeader("Content-Length", String.valueOf(response.content().length));

		// We won't override "Connection: close"
		if (response.header("Connection") == null) {
			response.setHeader("Connection", "keep-alive");
		}
	}
}
