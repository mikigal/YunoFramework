package org.yunoframework.web.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class RequestHandler {

	public void handle(SocketChannel channel, String request) {
		System.out.println(Thread.currentThread());
		System.out.println(request);

		ByteBuffer response = ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));

		try {
			channel.write(response);
			System.out.println("sent");
		} catch (IOException e) {
			// TODO: 26/04/2021 Error handling
			e.printStackTrace();
		}
	}
}
