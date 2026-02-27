import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;

public final class App {

    private static final String DB_URL  = "jdbc:sqlite:metingen.db";
    private static final String HOST    = "localhost";
    private static final int    PORT    = 8000;


    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS metingen (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                sensor_waarde  REAL    NOT NULL,
                meter_waarde   REAL    NOT NULL,
                created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    
    private App() {}

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
  
        Connection conn = DriverManager.getConnection(DB_URL);
        System.out.println("Database verbonden: " + DB_URL);

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(CREATE_TABLE_SQL);
        }
        System.out.println("Tabel 'metingen' gereed.");

       
        HttpServer server = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors())
        ));

        // Koppel handlers aan URL-paden
        server.createContext("/metingen", exchange -> {
    String method = exchange.getRequestMethod().toUpperCase();
    if ("GET".equals(method)) {
        new GETHandler(conn).handle(exchange);
    } else if ("POST".equals(method)) {
        new POSTHandler(conn).handle(exchange);
    } else {
        exchange.getResponseHeaders().set("Allow", "GET, POST");
        byte[] msg = "405 Method Not Allowed".getBytes();
        exchange.sendResponseHeaders(405, msg.length);
        exchange.getResponseBody().write(msg);
        exchange.getResponseBody().close();
    }
});

        server.start();
        System.out.println("Server gestart op http://" + HOST + ":" + PORT);
        System.out.println("  GET  http://" + HOST + ":" + PORT + "/metingen");
        System.out.println("  POST http://" + HOST + ":" + PORT + "/metingen");
    }
}
