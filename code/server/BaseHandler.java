import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public abstract class BaseHandler implements HttpHandler {

    
    protected final Connection connection;

    protected BaseHandler(Connection connection) {
        this.connection = connection;
    }

    /**
     * Stuurt een HTTP-response terug naar de client.
     *
     * @param exchange  het HttpExchange-object van de huidige request
     * @param status    HTTP-statuscode (bijv. 200, 201, 400, 500)
     * @param body      de tekst die als response body verstuurd wordt
     */
    protected static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Stuurt een 405 Method Not Allowed response.
     *
     * @param exchange      het HttpExchange-object
     * @param allowedMethod de HTTP-methode die wél toegestaan is
     */
    protected static void methodNotAllowed(HttpExchange exchange, String allowedMethod) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowedMethod);
        send(exchange, 405, "405 Method Not Allowed – gebruik " + allowedMethod);
    }
}
