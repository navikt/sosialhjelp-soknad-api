package no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi;

public class FilMetadata {

    public String filnavn;
    public String mimetype;
    public Long storrelse;

    public FilMetadata withFilnavn(String filnavn) {
        this.filnavn = filnavn;
        return this;
    }

    public FilMetadata withMimetype(String mimetype) {
        this.mimetype = mimetype;
        return this;
    }

    public FilMetadata withStorrelse(Long storrelse) {
        this.storrelse = storrelse;
        return this;
    }
}
