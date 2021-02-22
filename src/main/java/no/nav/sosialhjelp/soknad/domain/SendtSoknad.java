package no.nav.sosialhjelp.soknad.domain;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class SendtSoknad {
    private Long sendtSoknadId;
    private String behandlingsId;
    private String tilknyttetBehandlingsId;
    private String eier;
    private String fiksforsendelseId;
    private String orgnummer;
    private String navEnhetsnavn;
    private LocalDateTime brukerOpprettetDato;
    private LocalDateTime brukerFerdigDato;
    private LocalDateTime sendtDato;

    public boolean erEttersendelse() {
        return !isEmpty(tilknyttetBehandlingsId);
    }

    public Long getSendtSoknadId() {
        return sendtSoknadId;
    }

    public String getBehandlingsId() {
        return behandlingsId;
    }

    public String getTilknyttetBehandlingsId() {
        return tilknyttetBehandlingsId;
    }

    public String getEier() {
        return eier;
    }

    public String getFiksforsendelseId() {
        return fiksforsendelseId;
    }

    public String getOrgnummer() {
        return orgnummer;
    }

    public String getNavEnhetsnavn() {
        return navEnhetsnavn;
    }

    public LocalDateTime getBrukerOpprettetDato() {
        return brukerOpprettetDato;
    }

    public LocalDateTime getBrukerFerdigDato() {
        return brukerFerdigDato;
    }

    public LocalDateTime getSendtDato() {
        return sendtDato;
    }

    public void setSendtSoknadId(Long sendtSoknadId) {
        this.sendtSoknadId = sendtSoknadId;
    }

    public SendtSoknad withSendtSoknadId(Long sendtSoknadId) {
        this.sendtSoknadId = sendtSoknadId;
        return this;
    }

    public SendtSoknad withBehandlingsId(String behandlingsId) {
        this.behandlingsId = behandlingsId;
        return this;
    }

    public SendtSoknad withTilknyttetBehandlingsId(String tilknyttetBehandlingsId) {
        this.tilknyttetBehandlingsId = tilknyttetBehandlingsId;
        return this;
    }

    public SendtSoknad withEier(String eier) {
        this.eier = eier;
        return this;
    }

    public SendtSoknad withFiksforsendelseId(String fiksforsendelseId) {
        this.fiksforsendelseId = fiksforsendelseId;
        return this;
    }

    public SendtSoknad withOrgnummer(String orgnummer) {
        this.orgnummer = orgnummer;
        return this;
    }

    public SendtSoknad withNavEnhetsnavn(String navEnhetsnavn) {
        this.navEnhetsnavn = navEnhetsnavn;
        return this;
    }

    public SendtSoknad withBrukerOpprettetDato(LocalDateTime brukerOpprettetDato) {
        this.brukerOpprettetDato = brukerOpprettetDato;
        return this;
    }

    public SendtSoknad withBrukerFerdigDato(LocalDateTime brukerFerdigDato) {
        this.brukerFerdigDato = brukerFerdigDato;
        return this;
    }

    public SendtSoknad withSendtDato(LocalDateTime sendtDato) {
        this.sendtDato = sendtDato;
        return this;
    }
}
