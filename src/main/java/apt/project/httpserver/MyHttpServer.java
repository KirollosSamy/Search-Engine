package apt.project.httpserver;

import apt.project.handlers.PostHandler;
import com.sun.net.httpserver.HttpServer;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MyHttpServer {
    public static void main(String []args) {

        HttpServer httpServer = null;

        Jedis jedis = new Jedis("localhost");
        jedis.select(9);

        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Setting the handler for the GET method.
        httpServer.createContext("/", new PostHandler(jedis));

        httpServer.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        httpServer.start();
    }
}
