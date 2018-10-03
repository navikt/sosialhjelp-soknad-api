package no.nav.sbl.sosialhjelp.domain;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;

import java.time.LocalDateTime;

public class SoknadUnderArbeid {
    private Long soknadId;
    private Long versjon;
    private String behandlingsId;
    private String tilknyttetBehandlingsId;
    private String eier;
    private byte[] data;
    private String orgnummer;
    private SoknadInnsendingStatus innsendingStatus;
    private LocalDateTime opprettetDato;
    private LocalDateTime sistEndretDato;

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

    public byte[] getData() {
        return data;
    }

    public SoknadUnderArbeid withData(byte[] data) {
        this.data = data;
        return this;
    }

    public String getOrgnummer() {
        return orgnummer;
    }

    public SoknadUnderArbeid withOrgnummer(String orgnummer) {
        this.orgnummer = orgnummer;
        return this;
    }

    public SoknadInnsendingStatus getInnsendingStatus() {
        return innsendingStatus;
    }

    public SoknadUnderArbeid withInnsendingStatus(SoknadInnsendingStatus innsendingStatus) {
        this.innsendingStatus = innsendingStatus;
        return this;
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
}
