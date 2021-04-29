package org.yunoframework.web;

import com.jsoniter.output.JsonStream;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.tika.Tika;
import org.yunoframework.web.http.HttpStatus;
import org.yunoframework.web.http.HttpStatusType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/**
 * Representation of HTTP response
 */
public class Response {

	private HttpStatus status;
	private final Map<String, String> headers;
	private byte[] content;

	/**
	 * Creates new instance of response, defines content as 0 length byte array
	 * @param status status of response
	 */
	public Response(HttpStatus status) {
		this.status = status;
		this.headers = new CaseInsensitiveMap<>();
		this.content = new byte[0];
	}

	/**
	 * Serializes object to JSON, writes it to this response, sets Content-Type to application/json
	 * Sets status of response to 200 OK
	 * @param object object which you want to write to response
	 */
	public void json(Object object) {
		this.json(object, HttpStatus.OK);
	}

	/**
	 * Serializes object to JSON, writes it to this response, sets Content-Type to application/json
	 * @param object object which you want to write to response
	 */
	public void json(Object object, HttpStatus status) {
		this.content = JsonStream.serialize(object).getBytes(StandardCharsets.UTF_8);
		this.setHeader("Content-Type", "application/json");
		this.setStatus(status);
	}

	/**
	 * Writes HTML code to this response, sets Content-Type to text/html.
	 * Sets status of response to 200 OK
	 * @param html HTML code which you want to write to response
	 */
	public void html(String html) {
		this.html(html, HttpStatus.OK);
	}

	/**
	 * Writes HTML code to this response, sets Content-Type to text/html
	 * @param html HTML code which you want to write to response
	 * @param status of response
	 */
	public void html(String html, HttpStatus status) {
		this.content = html.getBytes(StandardCharsets.UTF_8);
		this.setHeader("Content-Type", "text/html");
		this.setStatus(status);
	}

	/**
	 * Writes binary data to response, sets given Content-Type.
	 * Sets status of response to 200 OK
	 * @param data binary data
	 * @param contentType Content-Type which you want to set, if null, won't be set
	 */
	public void binary(byte[] data, String contentType) {
		this.binary(data, contentType, HttpStatus.OK);
	}

	/**
	 * Writes binary data to response, sets given Content-Type
	 * @param data binary data
	 * @param contentType Content-Type which you want to set, if null, won't be set
	 * @param status of response
	 */
	public void binary(byte[] data, String contentType, HttpStatus status) {
		this.content = data;

		if (contentType != null) {
			this.setHeader("Content-Type", contentType);
		}

		this.setStatus(status);
	}

	/**
	 * Write content of file to response, sets Content-Type to MIME type of given type, if MIME type is unknown will set application/octet-stream.
	 * Sets status of response to 200 OK
	 * @param file file which you want to write
	 * @throws IOException when something go wrong while reading file
	 */
	public void file(File file) throws IOException {
		this.file(file, HttpStatus.OK);
	}

	/**
	 * Write content of file to response, sets Content-Type to MIME type of given type, if MIME type is unknown will set application/octet-stream
	 * @param file file which you want to write
	 * @param status of response
	 * @throws IOException when something go wrong while reading file
	 */
	public void file(File file, HttpStatus status) throws IOException {
		byte[] bytes = Files.readAllBytes(file.toPath());
		this.binary(bytes, new Tika().detect(bytes), status);
	}

	/**
	 * Write redirection into response
	 * @param target URL where you want to redirect
	 * @param status HTTP status, must be 3xx
	 * @throws IllegalStateException when status is not 3xx
	 */
	public void redirect(String target, HttpStatus status) throws IllegalStateException {
		if (status.getType() != HttpStatusType.REDIRECTION) {
			throw new IllegalStateException("Redirection must have 3xx HTTP status");
		}

		this.setStatus(status);
		this.setHeader("Location", target);
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
	 * Set value of header
	 * @param name name of header
	 * @param value value of header
	 */
	public void setHeader(String name, String value) {
		this.headers.put(name, value);
	}

	/**
	 * Returns map with headers
	 * @return map with headers <HeaderName, HeaderValue>
	 */
	public Map<String, String> headers() {
		return this.headers;
	}

	/**
	 * Sets status of response
	 * @see HttpStatus
	 */
	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	/**
	 * Lets tell client to close connection after receive this response
	 * It sets "Connection" header to "close"
	 */
	public void close() {
		this.setHeader("Connection", "close");
	}

	/**
	 * Returns status of response
	 * @return status of response
	 * @see HttpStatus
	 */
	public HttpStatus getStatus() {
		return this.status;
	}

	/**
	 * Returns content of response as byte array
	 * @return content of response as byte array, if content is not set, returns 0 length byte array
	 */
	public byte[] getContent() {
		return content;
	}
}
