package org.yunoframework.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class SocketServer {

	private String host;
	private int port;

	private ExecutorService threadPool;
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private ByteBuffer buffer;

	public SocketServer(String host, int port, int threads) {
		this.host = host;
		this.port = port;
		this.buffer = ByteBuffer.allocate(8192);

		this.threadPool = Executors.newFixedThreadPool(threads);
	}

	public void start() throws IOException{
		this.selector = Selector.open();

		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.configureBlocking(false);
		this.serverChannel.socket().bind(new InetSocketAddress(this.host, this.port));
		this.serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

		while (true) {
			this.selector.select();

			Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = keys.next();
				keys.remove();

				if (!key.isValid()) {
					continue;
				}

				if (key.isAcceptable()) {
					accept(key);
				}
				else if (key.isReadable()) {
					read(key);
				}
			}
		}
	}

	public void close() throws IOException {
		this.serverChannel.close();
		this.selector.close();
		this.threadPool.shutdown();
	}

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel channel = serverChannel.accept();
		channel.configureBlocking(false);

		Socket socket = channel.socket();
		SocketAddress remoteAddr = socket.getRemoteSocketAddress();
		System.out.println("Connected to: " + remoteAddr);

		channel.register(this.selector, SelectionKey.OP_READ);
	}

	private void read(SelectionKey key) {
		try {
			SocketChannel channel = (SocketChannel) key.channel();
			this.buffer.clear();

			StringBuilder received = new StringBuilder();
			int read;
			while ((read = channel.read(this.buffer)) > 0) {
				System.out.println(read);
				this.buffer.flip();
				byte[] bytes = new byte[this.buffer.limit()];
				this.buffer.get(bytes);
				received.append(new String(bytes));
				this.buffer.clear();
			}

			if (read < 0) {
				Socket socket = channel.socket();
				SocketAddress remote = socket.getRemoteSocketAddress();
				System.out.println("Connection closed by client: " + remote);
				channel.close();
				key.cancel();
				return;
			}

			threadPool.execute(() -> {
				System.out.println("Got: " + received.toString());

				// handle request
				ByteBuffer response = ByteBuffer.wrap("response".getBytes(StandardCharsets.UTF_8));
				try {
					channel.write(response);
				} catch (IOException e) {
					// TODO: Handle exceptions
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			// TODO: Handle exceptions
			e.printStackTrace();
		}
	}
}
