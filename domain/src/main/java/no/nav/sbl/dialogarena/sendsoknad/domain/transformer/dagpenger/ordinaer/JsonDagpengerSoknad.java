package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.dagpenger.ordinaer;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import org.joda.time.DateTime;

import java.util.List;

public class JsonDagpengerSoknad {

    private Long soknadId;


    private String soknadsType;
    private String skjemaNummer;
    private Integer versjon;
    private String uuid;
    private String brukerBehandlingId;
    private String behandlingskjedeId;
    private List<JsonDagpengerFaktum> fakta;
    private SoknadInnsendingStatus status;
    private String aktoerId;
    private DateTime opprettetDato;
    private DateTime sistLagret;
    private DelstegStatus delstegStatus;
    private List<VedleggJson> vedlegg;
    private String journalforendeEnhet;
    private String soknadPrefix;
    private String soknadUrl;
    private String fortsettSoknadUrl;

    public JsonDagpengerSoknad medSoknadsType(String soknadsType) {
        this.soknadsType = soknadsType;
        return this;
    }


    public JsonDagpengerSoknad medSoknadId(Long soknadId){
       this.soknadId = soknadId;
        return this;
    }

    public JsonDagpengerSoknad medSkjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
        return this;
    }

    JsonDagpengerSoknad medVersjon(Integer versjon) {
        this.versjon = versjon;
        return this;
    }

    public JsonDagpengerSoknad medUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    JsonDagpengerSoknad medBrukerBehandlingId(String brukerBehandlingId) {
        this.brukerBehandlingId = brukerBehandlingId;
        return this;
    }

    JsonDagpengerSoknad medBehandlingskjedeId(String behandlingskjedeId) {
        this.behandlingskjedeId = behandlingskjedeId;
        return this;
    }

    JsonDagpengerSoknad medFakta(List<JsonDagpengerFaktum> jsonDagpengerFaktums) {
        this.fakta = jsonDagpengerFaktums;
        return this;
    }

    JsonDagpengerSoknad medStatus(SoknadInnsendingStatus status) {
        this.status = status;
        return this;
    }

    JsonDagpengerSoknad medAktoerId(String aktoerId) {
        this.aktoerId = aktoerId;
        return this;
    }

    JsonDagpengerSoknad medOpprettetDato(DateTime opprettetDato) {
        this.opprettetDato = opprettetDato;
        return this;
    }


    JsonDagpengerSoknad medSistLagret(DateTime sistLagret) {
        this.sistLagret = sistLagret;
        return this;
    }

    JsonDagpengerSoknad medDelstegStatus(DelstegStatus delstegStatus) {
        this.delstegStatus = delstegStatus;
        return this;
    }

    JsonDagpengerSoknad medJournalforendeEnhet(String journalforendeEnhet) {
        this.journalforendeEnhet = journalforendeEnhet;
        return this;
    }


    public JsonDagpengerSoknad medVedlegg(List<VedleggJson> vedlegg) {
        this.vedlegg = vedlegg;
        return this;
    }

    public JsonDagpengerSoknad medSoknadPrefix(String soknadPrefix) {
        this.soknadPrefix = soknadPrefix;
        return this;
    }

    JsonDagpengerSoknad medSoknadUrl(String soknadUrl) {
        this.soknadUrl = soknadUrl;
        return this;
    }

    JsonDagpengerSoknad medFortsettSoknadUrl(String fortsettSoknadUrl) {
        this.fortsettSoknadUrl = fortsettSoknadUrl;
        return this;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public String getSkjemaNummer() {
        return skjemaNummer;
    }

    public Integer getVersjon() {
        return versjon;
    }

    public String getUuid() {
        return uuid;
    }

    public String getBrukerBehandlingId() {
        return brukerBehandlingId;
    }

    public String getBehandlingskjedeId() {
        return behandlingskjedeId;
    }

    public List<JsonDagpengerFaktum> getFakta() {
        return fakta;
    }

    public SoknadInnsendingStatus getStatus() {
        return status;
    }

    public String getAktoerId() {
        return aktoerId;
    }

    public DateTime getOpprettetDato() {
        return opprettetDato;
    }

    public DateTime getSistLagret() {
        return sistLagret;
    }

    public DelstegStatus getDelstegStatus() {
        return delstegStatus;
    }

    public List<VedleggJson> getVedlegg() {
        return vedlegg;
    }

    public String getJournalforendeEnhet() {
        return journalforendeEnhet;
    }

    public String getSoknadPrefix() {
        return soknadPrefix;
    }

    public String getSoknadUrl() {
        return soknadUrl;
    }

    public String getFortsettSoknadUrl() {
        return fortsettSoknadUrl;
    }

    public String getSoknadsType() {
        return soknadsType;
    }

}
