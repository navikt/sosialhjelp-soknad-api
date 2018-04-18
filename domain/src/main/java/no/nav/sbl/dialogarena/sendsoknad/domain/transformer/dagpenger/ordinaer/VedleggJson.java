package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.dagpenger.ordinaer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;

import java.util.HashMap;
import java.util.Map;

public class VedleggJson {

    private Long vedleggId;
    private Long soknadId;
    private Long faktumId;
    private String skjemaNummer;
    private String skjemanummerTillegg;
    private Vedlegg.Status innsendingsvalg;
    private Vedlegg.Status opprinneligInnsendingsvalg;
    private String navn = "";
    private Long storrelse = 0L;
    private Integer antallSider = 0;
    private Long opprettetDato;
    private String fillagerReferanse;
    private Map<String, String> urls = new HashMap<>();
    private String tittel;
    private String aarsak;
    private String filnavn;
    private String mimetype;

    public VedleggJson medVedleggId(Long vedleggId) {
        this.vedleggId = vedleggId;
        return this;
    }

    public VedleggJson medSoknadId(Long soknadId) {
        this.soknadId = soknadId;
        return this;
    }

    public VedleggJson medFaktumId(Long faktumId) {
        this.faktumId = faktumId;
        return this;
    }

    public VedleggJson medSkjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
        return this;
    }

    VedleggJson medSkjemaNummerTillegg(String skjemanummerTillegg) {
        this.skjemanummerTillegg = skjemanummerTillegg;
        return this;
    }

    public VedleggJson medInnsendingsvalg(Vedlegg.Status innsendingsvalg) {
        this.innsendingsvalg = innsendingsvalg;
        return this;
    }


    VedleggJson medOpprinneligInnsendingsvalg(Vedlegg.Status opprinneligInnsendingsvalg) {
        this.opprinneligInnsendingsvalg = opprinneligInnsendingsvalg;
        return this;
    }

    public VedleggJson medNavn(String navn) {
        this.navn = navn;
        return this;
    }

    public VedleggJson medStorrelse(Long storrelse) {
        this.storrelse = storrelse;
        return this;
    }

    public VedleggJson medAntallSider(Integer antallSider) {
        this.antallSider = antallSider;
        return this;
    }

    public VedleggJson medOpprettetDato(Long opprettetDato) {
        this.opprettetDato = opprettetDato;
        return this;
    }

    public VedleggJson medFillagerReferanse(String fillagerReferanse) {
        this.fillagerReferanse = fillagerReferanse;
        return this;
    }

    VedleggJson medUrls(Map<String, String> urls) {
        this.urls = urls;
        return this;
    }

    VedleggJson medTittel(String tittel) {
        this.tittel = tittel;
        return this;
    }

    VedleggJson medAarsak(String aarsak) {
        this.aarsak = aarsak;
        return this;
    }

    public VedleggJson medFilnavn(String filnavn) {
        this.filnavn = filnavn;
        return this;
    }

    public  VedleggJson medMimetype(String mimetype) {
        this.mimetype = mimetype;
        return this;
    }

    public Long getVedleggId() {
        return vedleggId;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public Long getFaktumId() {
        return faktumId;
    }

    public String getSkjemaNummer() {
        return skjemaNummer;
    }

    public String getSkjemanummerTillegg() {
        return skjemanummerTillegg;
    }

    public Vedlegg.Status getInnsendingsvalg() {
        return innsendingsvalg;
    }

    public Vedlegg.Status getOpprinneligInnsendingsvalg() {
        return opprinneligInnsendingsvalg;
    }

    public String getNavn() {
        return navn;
    }

    public Long getStorrelse() {
        return storrelse;
    }

    Integer getAntallSider() {
        return antallSider;
    }

    public Long getOpprettetDato() {
        return opprettetDato;
    }

    public String getFillagerReferanse() {
        return fillagerReferanse;
    }

    public Map<String, String> getUrls() {
        return urls;
    }

    public String getTittel() {
        return tittel;
    }

    public String getAarsak() {
        return aarsak;
    }

    public String getFilnavn() {
        return filnavn;
    }

    public String getMimetype() {
        return mimetype;
    }

}