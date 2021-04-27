package org.yunoframework.web.server;

/**
 * Thread wrapper with cached StringBuilder for serializing responses by {@see RequestHandler}
 */
public class RequestHandlerThread extends Thread {

	/**
	 * Cached StringBuilder for serializing response, must be cleared every time before usage
	 */
	private final StringBuilder responseBuilder;

	public RequestHandlerThread(Runnable runnable) {
		super(runnable);
		this.setDaemon(false);
		this.responseBuilder = new StringBuilder();
	}

	/**
	 * Returns response builder of current thread
	 * @return response builder of current thread
	 */
	public StringBuilder getResponseBuilder() {
		return responseBuilder;
	}

	/**
	 * Clears response builder
	 */
	public void clearResponseBuilder() {
		this.responseBuilder.setLength(0);
	}
}
