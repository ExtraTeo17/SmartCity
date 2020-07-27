package web;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.ExtendedProperties;
import web.message.MessageHandler;

import java.net.InetSocketAddress;
import java.util.HashSet;

public class WebServer extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private final HashSet<WebSocket> sockets;
    private final MessageHandler messageHandler;

    @Inject
    WebServer(MessageHandler messageHandler, ExtendedProperties properties) {
        super(getSocketAddress(properties));
        this.messageHandler = messageHandler;
        sockets = new HashSet<>();
    }

    private static InetSocketAddress getSocketAddress(ExtendedProperties properties){
        var port = properties.getOrDefault("port", 8000);
        return new InetSocketAddress(port);
    }

    /**
     * Called after an opening handshake has been performed and the given websocket is ready to be written on.
     *
     * @param socket    The <tt>WebSocket</tt> instance this event is occuring on.
     * @param handshake The handshake of the websocket instance
     */
    @Override
    public void onOpen(WebSocket socket, ClientHandshake handshake) {
        sockets.add(socket);
        logger.info("Connection established from: " + getSocketAddress(socket));
    }

    /**
     * Called after the websocket connection has been closed.
     *
     * @param socket The <tt>WebSocket</tt> instance this event is occurring on.
     * @param code   The codes can be looked up here: {@link CloseFrame}
     * @param reason Additional information string
     * @param remote If remote
     **/
    @Override
    public void onClose(WebSocket socket, int code, String reason, boolean remote) {
        if (code == CloseFrame.NORMAL || code == CloseFrame.GOING_AWAY) {
            logger.info("Gracefully closed connection.\n"
                    + "Address: " + getSocketAddress(socket));
        }
        else {
            logger.warn("Connection closed unexpectedly.\n"
                    + "Address: " + getSocketAddress(socket) + "\n"
                    + "Error code: " + code
                    + "Reason: " + reason);
        }
        sockets.remove(socket);
    }

    /**
     * Callback for string messages received from the remote host
     *
     * @param socket  The <tt>WebSocket</tt> instance this event is occurring on.
     * @param message The UTF-8 decoded message that was received.
     **/
    @Override
    public void onMessage(WebSocket socket, String message) {
        messageHandler.Handle(message);
    }

    public void broadcastMessage(String message) {
        for (var socket : sockets) {
            socket.send(message);
        }
    }

    /**
     * Called when errors occurs. If an error causes the websocket connection to fail {@link #onClose(WebSocket, int, String, boolean)}
     * will be called additionally. <br>
     * This method will be called primarily because of IO or protocol errors.<br>
     * If the given exception is an RuntimeException that probably means that you encountered a bug.<br>
     *
     * @param socket Can be null if there error does not belong to one specific websocket. For example if the servers port could not be bound.
     * @param ex     The exception causing this error
     **/
    @Override
    public void onError(WebSocket socket, Exception ex) {
        logger.error("Error in connection to " + getSocketAddress(socket), ex);
        sockets.remove(socket);
    }

    /**
     * Called when the server started up successfully.
     * <p>
     * If any error occurred, onError is called instead.
     */
    @Override
    public void onStart() {
        logger.info("Successfully started on port: " + getPort());
    }

    private static String getSocketAddress(WebSocket socket) {
        return socket.getRemoteSocketAddress().getHostString();
    }
}
