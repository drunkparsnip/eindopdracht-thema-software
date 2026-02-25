/**
 * Stelt een meting voor die afkomstig is van de sensormodule (ATtiny85).
 * De ATtiny85 stuurt een weerstandswaarde als IEEE 754 float via SPI
 * naar de ESP32, die deze vervolgens in JSON meestuurt naar de server.
 *
 * JSON-veld: "sensor_waarde"
 *
 * @author A.P.A. Slaa
 */
public class SensorMeting extends Meting {

    /** Weerstandswaarde gemeten door de ATtiny85, in ohm. */
    private final float sensorWaarde;

    public SensorMeting(float sensorWaarde) {
        super();
        this.sensorWaarde = sensorWaarde;
    }

    public float getSensorWaarde() {
        return sensorWaarde;
    }

    @Override
    public String omschrijving() {
        return "SensorMeting: sensorWaarde=" + sensorWaarde + " ohm";
    }
}
