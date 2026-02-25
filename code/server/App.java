import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;

/**
 * Startpunt van de server.
 *
 * Doet drie dingen bij het opstarten:
 *  1. Maakt verbinding met de SQLite database (of maakt hem aan als hij nog niet bestaat)
 *  2. Maakt de tabel 'metingen' aan als die er nog niet is
 *  3. Start de HTTP server op poort 8000 met twee endpoints:
 *       GET  /metingen  -> alle opgeslagen metingen ophalen
 *       POST /metingen  -> een nieuwe meting opslaan
 *
 * @author A.P.A. Slaa
 */
public final class App {

    private static final String DB_URL  = "jdbc:sqlite:metingen.db";
    private static final String HOST    = "localhost";
    private static final int    PORT    = 8000;

    // SQL om de tabel aan te maken als die nog niet bestaat.
    // REAL is het SQLite-type voor floating-point getallen.
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS metingen (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                sensor_waarde  REAL    NOT NULL,
                meter_waarde   REAL    NOT NULL,
                created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    // Private constructor: deze klasse wordt nooit geïnstantieerd
    private App() {}

    public static void main(String[] args) throws IOException, SQLException {

        // Stap 1: verbind met de database
        Connection conn = DriverManager.getConnection(DB_URL);
        System.out.println("Database verbonden: " + DB_URL);

        // Stap 2: maak de tabel aan (als die er nog niet is)
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(CREATE_TABLE_SQL);
        }
        System.out.println("Tabel 'metingen' gereed.");

        // Stap 3: start de HTTP server
        // newFixedThreadPool zorgt dat meerdere requests tegelijk verwerkt kunnen worden
        HttpServer server = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors())
        ));

        // Koppel de handlers aan hun URL-paden
        server.createContext("/metingen", new GETHandler(conn));   // GET /metingen
        server.createContext("/metingen", new POSTHandler(conn));  // POST /metingen

        server.start();
        System.out.println("Server gestart op http://" + HOST + ":" + PORT);
        System.out.println("  GET  http://" + HOST + ":" + PORT + "/metingen");
        System.out.println("  POST http://" + HOST + ":" + PORT + "/metingen");
    }
}
