package no.nav.sosialhjelp.soknad.consumer.fiks;


public class DigisosApiProperties {

    private final String digisosApiEndpoint;
    private final String idPortenTokenUrl;
    private final String idPortenClientId;
    private final String idPortenScope;
    private final String idPortenConfigUrl;
    private final String integrasjonsidFiks;
    private final String integrasjonpassordFiks;
    private final String virksomhetssertifikatPath;

    public DigisosApiProperties(
            String digisosApiEndpoint,
            String idPortenTokenUrl,
            String idPortenClientId,
            String idPortenScope,
            String idPortenConfigUrl,
            String integrasjonsidFiks,
            String integrasjonpassordFiks,
            String virksomhetssertifikatPath
    ) {
        this.digisosApiEndpoint = digisosApiEndpoint;
        this.idPortenTokenUrl = idPortenTokenUrl;
        this.idPortenClientId = idPortenClientId;
        this.idPortenScope = idPortenScope;
        this.idPortenConfigUrl = idPortenConfigUrl;
        this.integrasjonsidFiks = integrasjonsidFiks;
        this.integrasjonpassordFiks = integrasjonpassordFiks;
        this.virksomhetssertifikatPath = virksomhetssertifikatPath;
    }

    public String getDigisosApiEndpoint() {
        return digisosApiEndpoint;
    }

    public String getIdPortenTokenUrl() {
        return idPortenTokenUrl;
    }

    public String getIdPortenClientId() {
        return idPortenClientId;
    }

    public String getIdPortenScope() {
        return idPortenScope;
    }

    public String getIdPortenConfigUrl() {
        return idPortenConfigUrl;
    }

    public String getIntegrasjonsidFiks() {
        return integrasjonsidFiks;
    }

    public String getIntegrasjonpassordFiks() {
        return integrasjonpassordFiks;
    }

    public String getVirksomhetssertifikatPath() {
        return virksomhetssertifikatPath;
    }
}
