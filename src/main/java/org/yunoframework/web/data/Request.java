package org.yunoframework.web.data;

import org.yunoframework.web.http.HttpMethod;
import org.yunoframework.web.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation of HTTP request
 */
public class Request {

	private final HttpStatus parseResult;
	private final HttpMethod method;
	private final String path;
	private final Map<String, String> params;
	private final Map<String, String> headers;
	private final Map<String, Object> locals;
	private final byte[] content;
	private Object body;

	/**
	 * Creates instance of Request, should be used by {@see HttpParser}.
	 * If something is wrong with request and parseResult it not 200 OK all other parameters can be filled with nulls
	 * @param parseResult result of parsing request, 200 (OK) if request is correct
	 * @param method method of request
	 * @param path path of request
	 * @param params params of request
	 * @param headers headers of request
	 * @param content raw content of request
	 */
	public Request(HttpStatus parseResult, HttpMethod method, String path,
				   Map<String, String> params, Map<String, String> headers, byte[] content, Object body) {
		this.parseResult = parseResult;
		this.method = method;
		this.path = path;
		this.params = params;
		this.headers = headers;
		this.locals = new HashMap<>();
		this.content = content;
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
	public HttpMethod method() {
		return method;
	}

	/**
	 * Returns path of request, without parameters
	 * @return path of request, without parameters
	 */
	public String path() {
		return this.path;
	}

	/**
	 * Returns raw content of request, if it didn't have body will return 0 length bytes array
	 * @return raw content of request, if it didn't have body will return 0 length bytes array
	 */
	public byte[] content() {
		return content;
	}

	/**
	 * Returns parsed body
	 * If request body was x-www-form-urlencoded returned Object will be instance of Map<String, String>
	 * If request body was multipart/form-data returned Object will ne instance of Map<String, MultipartEntry>
	 * If request body was other type returned Object will be byte array from request's body
	 * If request didn't have body it will be null
	 * @return parsed body, null if request didn't have body
	 */
	public Object body() {
		return body;
	}

	/**
	 * Sets parsed body, can be used by middlewares
	 * @param body new body
	 */
	public void setBody(Object body) {
		this.body = body;
	}

	/**
	 * Returns result of parsing request, 200 (OK) if request is correct
	 * @return result of parsing request, 200 (OK) if request is correct
	 */
	public HttpStatus getParseResult() {
		return parseResult;
	}

	/**
	 * Returns value from locals with this name
	 * @return value from locals with this name
	 */
	public Object local(String name) {
		return this.locals.get(name);
	}

	/**
	 * Puts data into locals
	 * @param name name of local
	 * @param value value of local
	 */
	public void putLocal(String name, Object value) {
		this.locals.put(name, value);
	}

	/**
	 * Returns container of data from middleware
	 * @return container of data from middleware
	 */
	public Map<String, Object> lLocals() {
		return locals;
	}
}
