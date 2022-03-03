package no.nav.sosialhjelp.soknad.domain;

import java.util.UUID;

public class OpplastetVedlegg {
    private String uuid = UUID.randomUUID().toString();
    private String eier;
    private OpplastetVedleggType vedleggType;
    private byte[] data;
    private Long soknadId;
    private String filnavn;
    private String sha512;

    public String getUuid() {
        return uuid;
    }

    public OpplastetVedlegg withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getEier() {
        return eier;
    }

    public OpplastetVedlegg withEier(String eier) {
        this.eier = eier;
        return this;
    }

    public OpplastetVedleggType getVedleggType() {
        return vedleggType;
    }

    public OpplastetVedlegg withVedleggType(OpplastetVedleggType vedleggType) {
        this.vedleggType = vedleggType;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public OpplastetVedlegg withData(byte[] data) {
        this.data = data;
        return this;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public OpplastetVedlegg withSoknadId(Long soknadId) {
        this.soknadId = soknadId;
        return this;
    }

    public String getFilnavn() {
        return filnavn;
    }

    public OpplastetVedlegg withFilnavn(String filnavn) {
        this.filnavn = filnavn;
        return this;
    }

    public String getSha512() {
        return sha512;
    }

    public OpplastetVedlegg withSha512(String sha512) {
        this.sha512 = sha512;
        return this;
    }
}
