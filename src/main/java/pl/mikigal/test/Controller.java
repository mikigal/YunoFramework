package pl.mikigal.test;

import org.yunoframework.web.Request;

public class Controller {

	public static void root(Request request) {
		System.out.println("Called route!");
	}
}
