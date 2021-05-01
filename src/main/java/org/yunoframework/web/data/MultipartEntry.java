package org.yunoframework.web.data;

import java.util.Arrays;

/**
 * Representation of entry in multipart request
 */
public class MultipartEntry {

	private final String name;
	private final String filename;
	private final String contentDisposition;
	private final String contentType;
	private final byte[] content;

	public MultipartEntry(String name, String filename, String contentDisposition, String contentType, byte[] content) {
		this.name = name;
		this.filename = filename;
		this.contentDisposition = contentDisposition;
		this.contentType = contentType;
		this.content = content;
	}

	/**
	 * Returns "name" parameter from Content-Disposition header
	 * @return "name" parameter from Content-Disposition header
	 */
	public String name() {
		return name;
	}

	/**
	 * Returns "filename" parameter from Content-Disposition header, null if parameter does not exist
	 * @return "filename" parameter from Content-Disposition header, null if parameter does not exist
	 */
	public String filename() {
		return filename;
	}

	/**
	 * Returns Content-Disposition header
	 * @return Content-Disposition header
	 */
	public String contentDisposition() {
		return contentDisposition;
	}

	/**
	 * Returns Content-Type header
	 * @return Content-Type header
	 */
	public String contentType() {
		return contentType;
	}

	/**
	 * Returns entries' body as byte array
	 * @return entries' body as byte array
	 */
	public byte[] content() {
		return content;
	}

	@Override
	public String toString() {
		return "MultipartEntry{" +
				"name='" + name + '\'' +
				", filename='" + filename + '\'' +
				", contentDisposition='" + contentDisposition + '\'' +
				", contentType='" + contentType + '\'' +
				", content=" + Arrays.toString(content) +
				'}';
	}
}
