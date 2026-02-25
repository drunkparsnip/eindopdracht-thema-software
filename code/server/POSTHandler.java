import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Verwerkt HTTP POST-requests op het /metingen endpoint.
 *
 * Verwacht een JSON body van de ESP32 in dit formaat:
 *   { "sensor_waarde": 1234.5, "meter_waarde": 3.27 }
 *
 * Werkwijze:
 *  1. Lees de JSON body uit de request
 *  2. Parseer de twee waarden naar Java-objecten (SensorMeting, MeterMeting)
 *  3. Sla beide waarden op in de SQLite database via een PreparedStatement
 *
 * @author A.P.A. Slaa
 */
public final class POSTHandler extends BaseHandler {

    // PreparedStatement voorkomt SQL injection: de waarden worden
    // als parameters meegegeven, niet direct in de SQL-string geplakt.
    private static final String INSERT_SQL =
            "INSERT INTO metingen (sensor_waarde, meter_waarde) VALUES (?, ?)";

    public POSTHandler(java.sql.Connection connection) {
        super(connection);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Alleen POST-requests worden geaccepteerd
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            methodNotAllowed(exchange, "POST");
            return;
        }

        // Stap 1: lees de request body uit
        String body;
        try (InputStream is = exchange.getRequestBody()) {
            body = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
        }

        // Stap 2: parseer JSON naar Java-objecten
        // We parsen handmatig zodat er geen externe library nodig is.
        float sensorWaarde;
        float meterWaarde;
        try {
            sensorWaarde = parseJsonFloat(body, "sensor_waarde");
            meterWaarde  = parseJsonFloat(body, "meter_waarde");
        } catch (IllegalArgumentException e) {
            send(exchange, 400,
                    "400 Bad Request: " + e.getMessage() +
                    "\nVerwacht formaat: {\"sensor_waarde\": 1234.5, \"meter_waarde\": 3.27}");
            return;
        }

        // Maak Java-objecten aan (voldoet aan OOP-eis: subclasses van Meting)
        SensorMeting sensorMeting = new SensorMeting(sensorWaarde);
        MeterMeting  meterMeting  = new MeterMeting(meterWaarde);

        // Log naar console zodat je kunt zien wat er binnenkomt
        System.out.println("Ontvangen: " + sensorMeting.omschrijving());
        System.out.println("Ontvangen: " + meterMeting.omschrijving());

        // Stap 3: sla op in de database met een PreparedStatement
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
            ps.setFloat(1, sensorMeting.getSensorWaarde());
            ps.setFloat(2, meterMeting.getMeterWaarde());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database fout: " + e.getMessage());
            send(exchange, 500, "500 Internal Server Error: database fout");
            return;
        }

        send(exchange, 201, "Data opgeslagen.");
    }

    /**
     * Haalt de waarde van een JSON-veld op uit een simpele JSON-string.
     *
     * Voorbeeld: parseJsonFloat("{\"sensor_waarde\": 123.4}", "sensor_waarde") -> 123.4f
     *
     * @param json      de JSON-string
     * @param veldNaam  de naam van het veld dat we zoeken
     * @return          de waarde als float
     * @throws IllegalArgumentException als het veld niet gevonden wordt of geen geldig getal is
     */
    private static float parseJsonFloat(String json, String veldNaam) {
        // Zoek naar "veldNaam": gevolgd door een getal
        String zoekterm = "\"" + veldNaam + "\"";
        int index = json.indexOf(zoekterm);
        if (index == -1) {
            throw new IllegalArgumentException("Veld '" + veldNaam + "' niet gevonden in JSON");
        }

        // Ga na de dubbele punt staan
        int kolonIndex = json.indexOf(':', index + zoekterm.length());
        if (kolonIndex == -1) {
            throw new IllegalArgumentException("Ongeldige JSON bij veld '" + veldNaam + "'");
        }

        // Lees het getal (tot aan de komma, sluitende accolade of spatie)
        int start = kolonIndex + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) {
            start++;
        }
        int end = start;
        while (end < json.length() && "0123456789.-+eE".indexOf(json.charAt(end)) != -1) {
            end++;
        }

        String getalStr = json.substring(start, end).trim();
        if (getalStr.isEmpty()) {
            throw new IllegalArgumentException("Geen getal gevonden voor veld '" + veldNaam + "'");
        }

        try {
            return Float.parseFloat(getalStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ongeldige waarde '" + getalStr + "' voor veld '" + veldNaam + "'");
        }
    }
}
