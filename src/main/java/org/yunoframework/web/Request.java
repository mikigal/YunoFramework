package org.yunoframework.web;

import org.yunoframework.web.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class Request {

	private final HttpMethod method;
	private final String path;
	private final Map<String, String> params;
	private final Map<String, String> headers;

	public Request(HttpMethod method, String path, Map<String, String> params, Map<String, String> headers) {
		this.method = method;
		this.path = path;
		this.params = params;
		this.headers = headers;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getPath() {
		return this.path;
	}

	public Map<String, String> params() {
		return this.params;
	}

	public String param(String name) {
		return this.params.get(name);
	}

	public Map<String, String> headers() {
		return this.headers;
	}

	public String header(String name) {
		return this.headers.get(name);
	}

	@Override
	public String toString() {
		return "Request{" +
				"method=" + method +
				", path='" + path + '\'' +
				", params=" + params +
				", headers=" + headers +
				'}';
	}
}
