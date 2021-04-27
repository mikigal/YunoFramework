package org.yunoframework.web.http;

import org.yunoframework.web.Request;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpParser {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	static {
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Parses HTTP request
	 * @param raw HTTP request as text
	 * @return Request data, null if raw request is malformed
	 */
	public static Request parseRequest(String raw) {
		try {
			raw = raw.replace("\r\n", "\n"); // Just for safety
			String[] lines = raw.split("\n");

			String[] handshake = lines[0].split(" ");

			HttpMethod method = HttpMethod.getByName(handshake[0]);
			if (method == null) {
				throw new IllegalStateException("invalid HTTP method");
			}

			if (!handshake[2].equals("HTTP/1.1")) {
				throw new IllegalStateException("only HTTP/1.1 is supported");
			}

			String[] endpoint = handshake[1].split("\\?");
			String path = endpoint[0];
			Map<String, String> params = endpoint.length == 1 ? new HashMap<>() : parseParams(endpoint[1]);
			Map<String, String> headers = parseHeaders(lines);

			return new Request(method, path, params, headers);
		} catch (Exception e) {
			return null;
		}
	}

	private static Map<String, String> parseParams(String raw) {
		Map<String, String> params = new HashMap<>();
		String[] rawParams = raw.split("&");
		for (String param : rawParams) {
			String[] split = param.split("=");
			params.put(split[0], split[1]);
		}

		return params;
	}

	private static Map<String, String> parseHeaders(String[] requestLines) {
		Map<String, String> headers = new HashMap<>();
		for (int i = 1; i < requestLines.length - 1; i++) {
			String[] header = requestLines[i].split(": ");
			headers.put(header[0], header[1]);
		}

		return headers;
	}

	public static byte[] serializeResponse(StringBuilder responseBuilder, HttpStatus status, Map<String, String> headers, byte[] responseData) {
		responseBuilder.append("HTTP/1.1 ").append(status.getMessage()).append("\r\n");

		for (Map.Entry<String, String> header : headers.entrySet()) {
			responseBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
		}
		responseBuilder.append("\n");

		byte[] headersBytes = responseBuilder.toString().getBytes(StandardCharsets.UTF_8);
		byte[] buffer = new byte[headersBytes.length + responseData.length];

		System.arraycopy(headersBytes, 0, buffer, 0, headersBytes.length);
		System.arraycopy(responseData, 0, buffer, headersBytes.length, responseData.length);
		return buffer;
	}
}
