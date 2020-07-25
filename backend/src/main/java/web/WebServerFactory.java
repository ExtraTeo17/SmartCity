package web;

public class WebServerFactory {
    public static WebServer create(int port) {
        var server = new WebServer(port);
        server.setConnectionLostTimeout(100);
        return server;
    }
}
