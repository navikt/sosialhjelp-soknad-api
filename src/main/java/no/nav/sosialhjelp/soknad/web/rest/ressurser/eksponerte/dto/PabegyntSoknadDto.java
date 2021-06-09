package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PabegyntSoknadDto {

    private final LocalDateTime tidspunkt;
    private final String grupperingsId;
    private final String tekst;
    private final String link;
    private final Integer sikkerhetsnivaa;
    private final LocalDateTime synligFremTil;
    private final boolean eksternVarsling;

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
