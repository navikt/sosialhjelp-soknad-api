package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

public class AlternativRepresentasjon {
    private String filnavn;
    private String memetype;
    private String uuid;
    private byte[] content;

    public String getFilnavn() {
        return filnavn;
    }

    public AlternativRepresentasjon medFilnavn(String filnavn) {
        this.filnavn = filnavn;
        return this;
    }

    public String getMimetype() {
        return memetype;
    }

    public AlternativRepresentasjon medMemetype(String memetype) {
        this.memetype = memetype;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public AlternativRepresentasjon medUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public byte[] getContent() {
        return content;
    }

    public AlternativRepresentasjon medContent(byte[] content) {
        this.content = content;
        return this;
    }
}
