package org.yunoframework.web.nio;

import org.yunoframework.web.Yuno;
import org.yunoframework.web.nio.RequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Implementation of NIO based server
 */
public class SocketServer {

	private final ExecutorService threadPool;
	private final ByteBuffer buffer;

	private ServerSocketChannel serverChannel;
	private Selector selector;

	private Yuno yuno;

	/**
	 * Creates instance of NIO server
	 *
	 * @param yuno instance of Yuno
	 * @param threads number of threads used for handling connections
	 */
	public SocketServer(Yuno yuno, int threads) {
		this.buffer = ByteBuffer.allocate(8192);
		this.threadPool = Executors.newFixedThreadPool(threads);
		this.yuno = yuno;
	}

	/**
	 * Starts server
	 *
	 * @param address address to bind
	 */
	public void listen(InetSocketAddress address) throws IOException {
		this.selector = Selector.open();

		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.configureBlocking(false);
		this.serverChannel.socket().bind(address);
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
					handleAccept(key);
				} else if (key.isReadable()) {
					handleRead(key);
				}
			}
		}
	}

	/**
	 * Stops server
	 */
	public void close() throws IOException {
		this.serverChannel.close();
		this.selector.close();
		this.threadPool.shutdown();
	}

	private void handleAccept(SelectionKey key) throws IOException {
		SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
		channel.configureBlocking(false);
		channel.register(this.selector, SelectionKey.OP_READ);
	}

	private void handleRead(SelectionKey key) {
		try {
			SocketChannel channel = (SocketChannel) key.channel();
			this.buffer.clear();
			StringBuilder received = new StringBuilder();
			int read;
			while ((read = channel.read(this.buffer)) > 0) {
				this.buffer.flip();
				byte[] bytes = new byte[this.buffer.limit()];
				this.buffer.get(bytes);
				received.append(new String(bytes));
				this.buffer.clear();
			}

			if (read < 0) {
				channel.close();
				key.cancel();
				return;
			}

			threadPool.execute(() -> {
				try {
					new RequestHandler(yuno, channel, received.toString()).handle();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
