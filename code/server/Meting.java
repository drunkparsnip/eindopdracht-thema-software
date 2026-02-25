/**
 * Abstracte basisklasse voor alle soorten metingen.
 * SensorMeting en MeterMeting zijn subclasses van deze klasse.
 *
 * @author A.P.A. Slaa
 */
public abstract class Meting {

    /** Unix-timestamp (ms) waarop de meting ontvangen werd door de server. */
    private final long tijdstip;

    protected Meting() {
        this.tijdstip = System.currentTimeMillis();
    }

    /** Geeft het tijdstip terug waarop de meting aangemaakt werd. */
    public long getTijdstip() {
        return tijdstip;
    }

    /**
     * Geeft een voor mensen leesbare omschrijving van de meting.
     * Elke subklasse implementeert dit op zijn eigen manier.
     */
    public abstract String omschrijving();
}
