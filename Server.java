import java.util.*;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.*;


public class Server implements Runnable{
	private int port;
	private boolean running = false;
	private Selector selector;
	private ServerSocket serverSocket;
        public static ArrayList<MainConnection> allConnections = new ArrayList<>();


	public Server (int port){
		this.port = port;

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start(){
		new Thread(this).start();
	}

	public void run(){
		running = true;
		while (running) {
			try {
				/*int client = selector.select();
				if (client == 0) {
					continue;
				}
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = keys.iterator();

				while (keyIterator.hasNext()) {
					SelectionKey key = (SelectionKey)keyIterator.next();
					if ((key.readyOps() & SelectionKey.OP_ACCEPT)==SelectionKey.OP_ACCEPT) {
						Socket socket = serverSocket.accept();
						SocketChannel channel = socket.getChannel();
						channel.configureBlocking(false);
						channel.register(selector, SelectionKey.OP_READ);
						System.out.println("connection" + socket);
					} else if ((key.readyOps() & SelectionKey.OP_READ)==SelectionKey.OP_READ){
						SocketChannel channel = null;
						channel = (SocketChannel) key.channel();
						boolean connection = readData(channel, buffer);
						if (!connection) {
							key.cancel();
							Socket socket = null;
							socket = channel.socket();
							socket.close();
						}
					}
				}
				keys.clear();*/
				Socket socket = serverSocket.accept();
				initSocket(socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		shutdown();
	}

	public static ArrayList<MainConnection> getAllConnections(){
            return allConnections;
        }
        
        public void initSocket(Socket socket) {
		MainConnection board = new MainConnection(socket);
                allConnections.add(board);
		new Thread(board).start();
	}

	public void shutdown(){
		running = false;
		try {
			serverSocket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/*public void open(){
		ServerSocketChannel serverChannel;
		try {
			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);

			serverSocket = serverChannel.socket();

			InetSocketAddress address = new InetSocketAddress(port);
			serverSocket.bind(address);

			selector = Selector.open();

			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("server created" + address+", "+port);
		} catch (IOException e){
			e.printStackTrace();
		}
	}*/

	/*public boolean readData(SocketChannel channel, ByteBuffer buffer) {
		return true;
	}*/

	public static void main(String[] args) {
		Server s = new Server(50000);
		s.start();
	}

}