package org.yunoframework.web.http;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.yunoframework.web.data.MultipartEntry;
import org.yunoframework.web.data.Request;
import org.yunoframework.web.data.Response;
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
			int bodyPosition = findPattern(rawRequest, BODY_PREFIX, 0, rawRequest.length, false);
			String request = bodyPosition == -1 ? new String(rawRequest) : new String(rawRequest, 0, bodyPosition);
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
			Map<String, String> headers = parseHeaders(lines, 1);

			byte[] rawContent = new byte[bodyPosition == -1 ? 0 : (rawRequest.length - bodyPosition)];
			if (bodyPosition != -1) {
				System.arraycopy(rawRequest, bodyPosition, rawContent, 0, rawRequest.length - bodyPosition);
			}

			String contentType = headers.get("Content-Type");
			Object body = null;
			if (bodyPosition != -1 && contentType != null && contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
				body = parseParams(new String(rawContent));
			}
			else if (bodyPosition != -1 && contentType != null && contentType.contains("multipart/form-data")) {
				body = parseMultipart(contentType, rawContent);
			}
			else if (bodyPosition != -1) {
				body = rawContent;
			}

			return new Request(HttpStatus.OK, method, path, params, headers, rawContent, body);
		} catch (Exception e) {
			e.printStackTrace();
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
	 * It stop scanning when found first line without ": "
	 * @param headerLines all lines as request
	 * @param startLine from which line will search for headers
	 * @return Map with parser parameters
	 */
	private static Map<String, String> parseHeaders(String[] headerLines, int startLine) {
		Map<String, String> headers = new CaseInsensitiveMap<>();
		for (int i = startLine; i < headerLines.length; i++) {
			String currentLine = headerLines[i];
			if (!currentLine.contains(": ")) {
				break;
			}

			String[] header = headerLines[i].split(": ");
			headers.put(header[0], header[1]);
		}

		return headers;
	}

	/**
	 * Parses multipart/form-data request's body
	 * @param contentType Content-Type parameter of request
	 * @param rawContent request's body as byte array
	 * @return Map with multipart request entries, with it's name as key
	 */
	private static Map<String, MultipartEntry> parseMultipart(String contentType, byte[] rawContent) {
		String rawBoundary = parseHeaderParameters(contentType).get("boundary");
		if (rawBoundary == null) {
			throw new IllegalStateException("boundary not found (" + contentType + ")");
		}

		byte[] boundary = ("--" + rawBoundary + "\r\n").getBytes(StandardCharsets.UTF_8);

		Map<String, MultipartEntry> entries = new CaseInsensitiveMap<>();
		for (int i = 0; i < rawContent.length; i++) {
			int start = findPattern(rawContent, boundary, i, rawContent.length, false);
			if (start == -1) {
				continue; // Entry does not start here, let's check next byte
			}

			int end = findPattern(rawContent, boundary, start, rawContent.length, true);
			if (end == -1) {
				end = rawContent.length - boundary.length - 4; // Cut additional "--" from closing boundary and "\r\n"
			}

			int bodyStart = findPattern(rawContent, BODY_PREFIX, start, end, false);
			int headersLength = bodyStart - start - 2; // Cut "\r\n"

			boolean needCut = rawContent[end - 1] == 0x0A && rawContent[end - 2] == 0x0D; // Check if body ends with "\r\n"
			byte[] body = new byte[end - bodyStart - (needCut ? 2 : 0)]; // If body ends with "\r\n" we want to cut it
			System.arraycopy(rawContent, bodyStart, body, 0, body.length);

			String rawHeaders = new String(rawContent, start, headersLength);
			Map<String, String> headers = parseHeaders(rawHeaders.split("\r\n"), 0);
			String contentDisposition = headers.get("Content-Disposition");

			if (contentDisposition == null) {
				throw new IllegalStateException("missing Content-Disposition header in multipart entry");
			}

			Map<String, String> dispositionParameters = parseHeaderParameters(contentDisposition);
			if (!dispositionParameters.containsKey("name")) {
				throw new IllegalStateException("missing name parameter in Content-Disposition header");
			}

			entries.put(dispositionParameters.get("name"),
					new MultipartEntry(
						dispositionParameters.get("name"),
						dispositionParameters.get("filename"),
						contentDisposition,
						headers.get("Content-Type"),
						body
					)
			);

			i = end - 1; // Jump to ending of current entry
		}

		return entries;
	}

	/**
	 * Parses parameters from header delimited by ";" (e. g. Content-Disposition: form-data; name="image"; filename="foo.png")
	 * @param header header to parse
	 * @return parsed parameters
	 */
	private static Map<String, String> parseHeaderParameters(String header) {
		Map<String, String> parameters = new CaseInsensitiveMap<>();
		header = header.replace("; ", ";");
		for (String parameter : header.split(";")) {
			if (!parameter.contains("=")) {
				continue;
			}

			String[] split = parameter.split("=");
			parameters.put(split[0], split[1].replace("\"", ""));
		}

		return parameters;
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

	/**
	 * Searches for pattern in given array, returns first position where pattern occurs in array
	 * @param array array to search in
	 * @param pattern pattern to find
	 * @param offset position from which to look for pattern
	 * @param limit position to which to look for pattern
	 * @param findStartPos if true will return index of first pattern's byte, else fire byte after pattern
	 * @return found position, -1 if not found
	 */
	private static int findPattern(byte[] array, byte[] pattern, int offset, int limit, boolean findStartPos) {
		for (int i = offset; i < limit - pattern.length; i++) {

			boolean error = false;
			for (int j = 0; j < pattern.length; j++) {
				if (array[i + j] != pattern[j]) {
					error = true;
					break;
				}
			}

			if (!error) {
				return findStartPos ? i : (i + pattern.length);
			}
		}
		return -1;
	}

	private static void dump(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for(byte b: a)
			sb.append(String.format("%02x", b));
		System.out.println(sb.toString());
	}
}
