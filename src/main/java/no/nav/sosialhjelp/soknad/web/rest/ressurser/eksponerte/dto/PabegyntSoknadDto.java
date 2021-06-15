package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Response-objekt for endepunkt som skal hente informasjon om påbegynte søknader for DittNAV.
 * https://navikt.github.io/brukernotifikasjon-docs/eventtyper/beskjed/felter/
 */
public class PabegyntSoknadDto {

    private final LocalDateTime tidspunkt;
    private final String grupperingsId;
    private final String tekst;
    private final String link;
    private final Integer sikkerhetsnivaa;
    private final LocalDateTime synligFremTil; // 14 dager frem i tid fra siste endring
    private final boolean eksternVarsling; // Settes alltid til false

    public PabegyntSoknadDto(
            LocalDateTime tidspunkt,
            String grupperingsId,
            String tekst,
            String link,
            Integer sikkerhetsnivaa,
            LocalDateTime synligFremTil
    ) {
        this.tidspunkt = tidspunkt;
        this.grupperingsId = grupperingsId;
        this.tekst = tekst;
        this.link = link;
        this.sikkerhetsnivaa = sikkerhetsnivaa;
        this.synligFremTil = synligFremTil;
        this.eksternVarsling = false;
    }

    public String getTidspunkt() {
        return tidspunkt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String getGrupperingsId() {
        return grupperingsId;
    }

    public String getTekst() {
        return tekst;
    }

    public String getLink() {
        return link;
    }

    public Integer getSikkerhetsnivaa() {
        return sikkerhetsnivaa;
    }

    public String getSynligFremTil() {
        return synligFremTil.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public boolean isEksternVarsling() {
        return eksternVarsling;
    }
}
