package org.yunoframework.web;

import com.jsoniter.output.JsonStream;
import org.apache.tika.Tika;
import org.yunoframework.web.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of HTTP response
 */
public class Response {

	private final HttpStatus status;
	private final Map<String, String> headers;
	private byte[] content;

	public Response(HttpStatus status) {
		this.status = status;
		this.headers = new HashMap<>();
		this.content = new byte[0];
	}

	/**
	 * Serializes object to JSON, writes it to this response, sets Content-Type to application/json
	 * @param object object which you want to write to response
	 */
	public void json(Object object) {
		this.content = JsonStream.serialize(object).getBytes(StandardCharsets.UTF_8);
		this.setHeader("Content-Type", "application/json");
	}

	/**
	 * Writes HTML code to this response, sets Content-Type to text/html
	 * @param html HTML code which you want to write to response
	 */
	public void html(String html) {
		this.content = html.getBytes(StandardCharsets.UTF_8);
		this.setHeader("Content-Type", "text/html");
	}

	/**
	 * Writes binary data to response, sets given Content-Type
	 * @param data binary data
	 * @param contentType Content-Type which you want to set, if null, won't be set
	 */
	public void binary(byte[] data, String contentType) {
		this.content = data;

		if (contentType != null) {
			this.setHeader("Content-Type", contentType);
		}
	}

	/**
	 * Write content of file to response, sets Content-Type to MIME type of given type, if MIME type is unknown will set application/octet-stream
	 * @param file file which you want to write
	 * @throws IOException when something go wrong while reading file
	 */
	public void file(File file) throws IOException {
		byte[] bytes = Files.readAllBytes(file.toPath());
		this.binary(bytes, new Tika().detect(bytes));
	}

	/**
	 * Returns value of given header
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
