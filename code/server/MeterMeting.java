/**
 * Stelt een meting voor die afkomstig is van de metermodule (ESP32).
 * De ESP32 leest zelf ook een analoge waarde van een spanningsdeler
 * en stuurt deze samen met de sensorwaarde via HTTP POST naar de server.
 *
 * JSON-veld: "meter_waarde"
 *
 * @author A.P.A. Slaa
 */
public class MeterMeting extends Meting {

    /** Analoge waarde gemeten door de ESP32 zelf, in volt. */
    private final float meterWaarde;

    public MeterMeting(float meterWaarde) {
        super();
        this.meterWaarde = meterWaarde;
    }

    public float getMeterWaarde() {
        return meterWaarde;
    }

    @Override
    public String omschrijving() {
        return "MeterMeting: meterWaarde=" + meterWaarde + " V";
    }
}
