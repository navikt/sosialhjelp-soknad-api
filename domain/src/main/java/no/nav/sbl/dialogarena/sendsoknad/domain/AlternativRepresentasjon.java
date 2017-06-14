package no.nav.sbl.dialogarena.sendsoknad.domain;

import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;

public class AlternativRepresentasjon {
    private AlternativRepresentasjonType representasjonsType;
    private String filnavn;
    private String mimetype;
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
        return mimetype;
    }

    public AlternativRepresentasjon medMimetype(String memetype) {
        this.mimetype = memetype;
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

    public AlternativRepresentasjonType getRepresentasjonsType() {
        return representasjonsType;
    }
    public AlternativRepresentasjon medRepresentasjonsType(AlternativRepresentasjonType type) {
        this.representasjonsType = type;
        return this;
    }
}
