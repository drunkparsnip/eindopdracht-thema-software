import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                
                if ("GET".equals(method)) {
                    handleGet(exchange);
                } else if ("POST".equals(method)) {
                    handlePost(exchange);
                } else {
                    sendResponse(exchange, 405, "Method Not Allowed");
                }
            }
        });
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:8080");
    }
    
    private static void handleGet(HttpExchange exchange) throws IOException {
        String response = "GET request received";
        sendResponse(exchange, 200, response);
    }
    
    private static void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        String response = "POST request received: " + body;
        sendResponse(exchange, 200, response);
    }
    
    private static void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        exchange.sendResponseHeaders(status, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}