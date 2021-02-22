package no.nav.sosialhjelp.soknad.domain;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class SoknadUnderArbeid {
    private Long soknadId;
    private Long versjon;
    private String behandlingsId;
    private String tilknyttetBehandlingsId;
    private String eier;
    private JsonInternalSoknad jsonInternalSoknad;
    private SoknadInnsendingStatus innsendingStatus;
    private LocalDateTime opprettetDato;
    private LocalDateTime sistEndretDato;

    public boolean erEttersendelse() {
        return !isEmpty(tilknyttetBehandlingsId);
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public SoknadUnderArbeid withSoknadId(Long soknadId) {
        this.soknadId = soknadId;
        return this;
    }

    public void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public Long getVersjon() {
        return versjon;
    }

    public SoknadUnderArbeid withVersjon(Long versjon) {
        this.versjon = versjon;
        return this;
    }

    public void setVersjon(Long versjon) {
        this.versjon = versjon;
    }

    public String getBehandlingsId() {
        return behandlingsId;
    }

    public SoknadUnderArbeid withBehandlingsId(String behandlingsId) {
        this.behandlingsId = behandlingsId;
        return this;
    }

    public String getTilknyttetBehandlingsId() {
        return tilknyttetBehandlingsId;
    }

    public SoknadUnderArbeid withTilknyttetBehandlingsId(String tilknyttetBehandlingsId) {
        this.tilknyttetBehandlingsId = tilknyttetBehandlingsId;
        return this;
    }

    public String getEier() {
        return eier;
    }

    public SoknadUnderArbeid withEier(String eier) {
        this.eier = eier;
        return this;
    }

    public SoknadInnsendingStatus getInnsendingStatus() {
        return innsendingStatus;
    }

    public SoknadUnderArbeid withInnsendingStatus(SoknadInnsendingStatus innsendingStatus) {
        this.innsendingStatus = innsendingStatus;
        return this;
    }

    public void setInnsendingStatus(SoknadInnsendingStatus innsendingStatus) {
        this.innsendingStatus = innsendingStatus;
    }

    public LocalDateTime getOpprettetDato() {
        return opprettetDato;
    }

    public SoknadUnderArbeid withOpprettetDato(LocalDateTime opprettetDato) {
        this.opprettetDato = opprettetDato;
        return this;
    }

    public LocalDateTime getSistEndretDato() {
        return sistEndretDato;
    }

    public SoknadUnderArbeid withSistEndretDato(LocalDateTime sistEndretDato) {
        this.sistEndretDato = sistEndretDato;
        return this;
    }

    public void setSistEndretDato(LocalDateTime sistEndretDato) {
        this.sistEndretDato = sistEndretDato;
    }

    public JsonInternalSoknad getJsonInternalSoknad() { return jsonInternalSoknad; }

    public SoknadUnderArbeid withJsonInternalSoknad(JsonInternalSoknad jsonInternalSoknad) {
        this.jsonInternalSoknad = jsonInternalSoknad;
        return this;
    }
}
