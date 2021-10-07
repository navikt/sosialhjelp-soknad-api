package no.nav.sosialhjelp.soknad.consumer.fiks;


public class DigisosApiProperties {

    private final String digisosApiEndpoint;
    private final String integrasjonsidFiks;
    private final String integrasjonpassordFiks;

    public DigisosApiProperties(
            String digisosApiEndpoint,
            String integrasjonsidFiks,
            String integrasjonpassordFiks
    ) {
        this.digisosApiEndpoint = digisosApiEndpoint;
        this.integrasjonsidFiks = integrasjonsidFiks;
        this.integrasjonpassordFiks = integrasjonpassordFiks;
    }

    public String getDigisosApiEndpoint() {
        return digisosApiEndpoint;
    }

    public String getIntegrasjonsidFiks() {
        return integrasjonsidFiks;
    }

    public String getIntegrasjonpassordFiks() {
        return integrasjonpassordFiks;
    }

}
