package org.yunoframework.web.server;

import org.yunoframework.web.Yuno;
import org.yunoframework.web.http.HttpStatus;

import java.io.ByteArrayOutputStream;
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

	private final ThreadPoolExecutor threadPool;
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
		this.threadPool = new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),  RequestHandlerThread::new);
		this.threadPool.prestartAllCoreThreads();
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
	public void stop() throws IOException {
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
		ByteArrayOutputStream requestStream = new ByteArrayOutputStream();
		SocketChannel channel = (SocketChannel) key.channel();
		this.buffer.clear();

		try {
			int read;
			while ((read = channel.read(this.buffer)) > 0) {
				this.buffer.flip();
				byte[] bytes = new byte[this.buffer.limit()];
				this.buffer.get(bytes);

				requestStream.write(bytes);
				this.buffer.clear();

				if (this.yuno.getMaxRequestSize() > 0 && requestStream.size() > this.yuno.getMaxRequestSize()) {
					this.handleRequest(requestStream, channel, HttpStatus.PAYLOAD_TOO_LARGE);
					return;
				}
			}

			if (read < 0) {
				this.close(channel, requestStream);
				return;
			}

			this.handleRequest(requestStream, channel, null);
		} catch (IOException e) {
			this.close(channel, requestStream);
		}
	}

	private void handleRequest(ByteArrayOutputStream stream, SocketChannel channel, HttpStatus handleError) {
		this.threadPool.execute(() -> {
			try {
				byte[] rawRequest = stream.toByteArray();
				stream.close();

				new RequestHandler(yuno, rawRequest, handleError, new ClientConnection(channel)).handle();
			} catch (IOException e) {
				this.close(channel, stream);
			}
		});
	}

	private void close(SocketChannel channel, ByteArrayOutputStream stream) {
		try {
			channel.close();
			stream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
