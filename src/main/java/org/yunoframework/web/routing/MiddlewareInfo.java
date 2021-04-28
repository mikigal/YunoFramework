package org.yunoframework.web.routing;

/**
 * Keep information about middleware
 */
public class MiddlewareInfo {

	private final Handler handler;
	private final int priority;

	/**
	 * Creates instance of RouteInfo, it does not automatically register it do Yuno
	 * @param handler handler of middleware {@see Handler}
	 * @param priority priority of middleware
	 */
	public MiddlewareInfo(Handler handler, int priority) {
		this.handler = handler;
		this.priority = priority;
	}

	/**
	 * Returns handler of middleware
	 * @return handler of middleware
	 */
	public Handler getHandler() {
		return handler;
	}

	/**
	 * Returns priority of middleware
	 * @return priority of middleware
	 */
	public int getPriority() {
		return priority;
	}
}
