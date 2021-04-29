package org.yunoframework.web;

import org.yunoframework.web.http.HttpMethod;

import java.util.Map;

/**
 * Representation of HTTP request
 */
public class Request {

	private final HttpMethod method;
	private final String path;
	private final Map<String, String> params;
	private final Map<String, String> headers;
	private final byte[] body;

	public Request(HttpMethod method, String path, Map<String, String> params, Map<String, String> headers, byte[] body) {
		this.method = method;
		this.path = path;
		this.params = params;
		this.headers = headers;
		this.body = body;
	}

	/**
	 * Returns value of given parameter, name is case insensitive
	 * @param name name of parameter
	 * @return value of parameter, null if parameter does not exist
	 */
	public String param(String name) {
		return this.params.get(name);
	}

	/**
	 * Returns map with params
	 * @return map with params <ParamName, ParamValue>
	 */
	public Map<String, String> params() {
		return this.params;
	}

	/**
	 * Returns value of given header, name is case insensitive
	 * @param name name of header
	 * @return value of header, null if header does not exist
	 */
	public String header(String name) {
		return this.headers.get(name);
	}

	/**
	 * Returns map with headers
	 * @return map with headers <HeaderName, HeaderValue>
	 */
	public Map<String, String> headers() {
		return this.headers;
	}

	/**
	 * Returns method of request
	 * @return method of request
	 * @see HttpMethod
	 */
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * Returns path of request, without parameters
	 * @return path of request, without parameters
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Returns body of request, if it didn't have body will return 0 length bytes array
	 * @return body of request, if it didn't have body will return 0 length bytes array
	 */
	public byte[] getBody() {
		return body;
	}
}
