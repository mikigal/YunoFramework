package org.yunoframework.web.http;

import java.util.Locale;

public enum HttpMethod {
	GET(true),
	HEAD(false),
	POST(false),
	PUT(false),
	DELETE(false),
	CONNECT(false),
	OPTIONS(false),
	TRACE(false),
	PATCH(false);

	private final boolean supported;
	HttpMethod(boolean supported) {
		this.supported = supported;
	}

	/**
	 * Does Yuno support this HTTP method?
	 * @return true if method is supported, else false
	 */
	public boolean isSupported() {
		return supported;
	}

	/**
	 * Returns HttpMethod by it's name
	 * @param name name of method, it can be lower case
	 * @return HttpMethod if name is valid, else null
	 */
	public static HttpMethod getByName(String name) {
		for (HttpMethod method : HttpMethod.values()) {
			if (method.toString().equals(name.toUpperCase())) {
				return method;
			}
		}

		return null;
	}
}
