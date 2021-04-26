package org.yunoframework.web.http;

import org.yunoframework.web.Request;

import java.util.HashMap;
import java.util.Map;

public class HttpParser {

	/**
	 * Parses HTTP request
	 * @param raw HTTP request as text
	 * @return Request data, null if raw request is malformed
	 */
	public static Request parse(String raw) {
		try {
			String[] lines = raw.split("\r\n");

			String[] handshake = lines[0].split(" ");

			HttpMethod method = HttpMethod.getByName(handshake[0]);
			if (method == null) {
				throw new IllegalStateException("invalid HTTP method");
			}

			if (!handshake[2].equals("HTTP/1.1")) {
				throw new IllegalStateException("only HTTP/1.1 is supported");
			}

			String[] fullPath = handshake[1].split("\\?");
			String path = fullPath[0];
			Map<String, String> params = fullPath.length == 1 ? new HashMap<>() : parseParams(fullPath[1]);
			Map<String, String> headers = parseHeaders(lines);

			return new Request(method, path, params, headers);
		} catch (Exception e) {
			return null;
		}
	}

	private static Map<String, String> parseParams(String raw) {
		Map<String, String> params = new HashMap<>();
		raw = raw.substring(1); // Cut first "?" from params string
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
}
