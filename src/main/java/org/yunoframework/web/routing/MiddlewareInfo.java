package org.yunoframework.web.routing;

public class MiddlewareInfo {
	private final Handler handler;
	private final int priority;

	public MiddlewareInfo(Handler handler, int priority) {
		this.handler = handler;
		this.priority = priority;
	}

	public Handler getHandler() {
		return handler;
	}

	public int getPriority() {
		return priority;
	}
}
