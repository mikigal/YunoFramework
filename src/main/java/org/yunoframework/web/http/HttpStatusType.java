package org.yunoframework.web.http;

/**
 * Enumeration of HTTP statuses' types
 */
public enum HttpStatusType {
	INFORMATIONAL(100, 199),
	SUCCESSFUL(200, 299),
	REDIRECTION(300, 399),
	CLIENT_ERROR(400, 499),
	SERVER_ERROR(500, 599);

	private final int min;
	private final int max;

	HttpStatusType(int min, int max) {
		this.min = min;
		this.max = max;
	}

	/**
	 * Returns type of given HTTP status
	 * @param status HTTP status
	 * @return type of given HTTP status, null if status is invalid
	 */
	public static HttpStatusType typeOf(HttpStatus status) {
		return HttpStatusType.typeOf(status.getCode());
	}

	/**
	 * Returns type of given HTTP status code
	 * @param statusCode number of status code
	 * @return type of given HTTP status code, null if status code is invalid
	 */
	public static HttpStatusType typeOf(int statusCode) {
		for (HttpStatusType type : HttpStatusType.values()) {
			if (statusCode >= type.getMin() && statusCode <= type.getMax()) {
				return type;
			}
		}

		return null;
	}

	/**
	 * Returns smallest status code of type
	 * @return smallest status code of type
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Returns biggest status code of type
	 * @return biggest status code of type
	 */
	public int getMax() {
		return max;
	}
}