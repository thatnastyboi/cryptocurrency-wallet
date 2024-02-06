package bg.sofia.uni.fmi.mjt.wallet.crypto.server;

import bg.sofia.uni.fmi.mjt.wallet.crypto.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.wallet.crypto.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.wallet.crypto.database.ServerLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CryptocurrencyWalletServer {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2048;

    private static final String LOG_PATH = "server.log";
    private static final String SHUTTING_DOWN_MESSAGE =
        "Server was shut down";
    private static final String DISCONNECT = "disconnect";
    private static final String DISCONNECTED_SUCCESSFULLY =
        "Disconnected successfully";

    private boolean isServerWorking;

    private final CommandExecutor executor;
    private ByteBuffer buffer;
    private Selector selector;
    private final ServerLogger logger;
    private Set<SocketChannel> connectedClients;

    private static final String SERVER_SOCKET_PROBLEM_MESSAGE =
        "There is a problem with the server socket";
    private static final String CLIENT_DISCONNECTED_FORCEFULLY_MESSAGE =
        "Client disconnected forcefully";

    public CryptocurrencyWalletServer(CommandExecutor executor) {
        this.executor = executor;
        this.logger = new ServerLogger(LOG_PATH);
        this.connectedClients = new HashSet<>();
    }

    public void start() {
        startServer();
    }

    private void startServer() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            this.selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);

            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;
            while (isServerWorking) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    handleKey(key);

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            logger.logError(SERVER_SOCKET_PROBLEM_MESSAGE, e.getStackTrace());
            throw new RuntimeException(SERVER_SOCKET_PROBLEM_MESSAGE, e);
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        if (key.isReadable()) {
            SocketChannel sc = (SocketChannel) key.channel();
            connectedClients.add(sc);
            try {
                String clientInput = getClientInput(sc);
                if (clientInput != null) {
                    if (clientInput.equals(DISCONNECT)) {
                        writeClientOutput(sc, DISCONNECTED_SUCCESSFULLY);
                        handleDisconnect(sc, key);
                        sc.close();
                    } else {
                        String output = executor.execute(CommandCreator.newCommand(clientInput), key);

                        writeClientOutput(sc, output);
                        sc.write(buffer);
                        if (output.equals(SHUTTING_DOWN_MESSAGE)) {
                            shutdownServer();
                        }
                    }
                }
            } catch (SocketException e) {
                logger.logError(CLIENT_DISCONNECTED_FORCEFULLY_MESSAGE, e.getStackTrace());
                System.out.println(CLIENT_DISCONNECTED_FORCEFULLY_MESSAGE);
                handleDisconnect(sc, key);
            }
        } else if (key.isAcceptable()) {
            accept(selector, key);
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel sc) throws IOException {
        buffer.clear();

        int readBytes = sc.read(buffer);
        if (readBytes < 0) {
            sc.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel sc, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        sc.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private void handleDisconnect(SocketChannel sc, SelectionKey key) throws IOException {
        executor.execute(CommandCreator.newCommand(DISCONNECT), key);
        sc.close();
        connectedClients.remove(sc);
        key.cancel();
    }

    private void shutdownServer() throws IOException {
        for (SocketChannel sc : connectedClients) {
            sc.close();
        }

        connectedClients.clear();

        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }
}
