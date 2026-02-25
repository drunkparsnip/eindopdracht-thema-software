import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Verwerkt HTTP GET-requests op het /metingen endpoint.
 *
 * Haalt alle rijen op uit de database en stuurt deze terug als
 * door tabs gescheiden tekst (één meting per regel).
 *
 * @author A.P.A. Slaa
 */
public final class GETHandler extends BaseHandler {

    private static final String SELECT_SQL =
            "SELECT id, sensor_waarde, meter_waarde, created_at FROM metingen ORDER BY id ASC";

    public GETHandler(java.sql.Connection connection) {
        super(connection);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Alleen GET-requests worden geaccepteerd
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            methodNotAllowed(exchange, "GET");
            return;
        }

        // Bouw de response op: één rij per meting
        StringBuilder response = new StringBuilder();
        response.append("id\tsensor_waarde\tmeter_waarde\tcreated_at\n");
        response.append("--\t-------------\t------------\t----------\n");

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(SELECT_SQL)) {

            while (rs.next()) {
                response.append(rs.getInt("id"))           .append('\t')
                        .append(rs.getFloat("sensor_waarde")).append('\t')
                        .append(rs.getFloat("meter_waarde")) .append('\t')
                        .append(rs.getString("created_at"))  .append('\n');
            }

        } catch (SQLException e) {
            System.err.println("Database fout: " + e.getMessage());
            send(exchange, 500, "500 Internal Server Error: database fout");
            return;
        }

        send(exchange, 200, response.toString());
    }
}
