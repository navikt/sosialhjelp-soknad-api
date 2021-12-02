//package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
///**
// * Response-objekt for endepunkt som skal hente informasjon om påbegynte søknader for DittNAV.
// * https://navikt.github.io/brukernotifikasjon-docs/eventtyper/beskjed/felter/
// */
//public class PabegyntSoknadDto {
//
//    private final LocalDateTime eventTidspunkt;
//    private final String eventId;
//    private final String grupperingsId;
//    private final String tekst;
//    private final String link;
//    private final Integer sikkerhetsnivaa;
//    private final LocalDateTime sistOppdatert;
//    private final boolean aktiv;
//
//    public PabegyntSoknadDto(
//            LocalDateTime eventTidspunkt,
//            String eventId,
//            String grupperingsId,
//            String tekst,
//            String link,
//            Integer sikkerhetsnivaa,
//            LocalDateTime sistOppdatert,
//            boolean aktiv
//    ) {
//        this.eventTidspunkt = eventTidspunkt;
//        this.eventId = eventId;
//        this.grupperingsId = grupperingsId;
//        this.tekst = tekst;
//        this.link = link;
//        this.sikkerhetsnivaa = sikkerhetsnivaa;
//        this.sistOppdatert = sistOppdatert;
//        this.aktiv = aktiv;
//    }
//
//    public String getEventTidspunkt() {
//        return eventTidspunkt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//    }
//
//    public String getEventId() {
//        return eventId;
//    }
//
//    public String getGrupperingsId() {
//        return grupperingsId;
//    }
//
//    public String getTekst() {
//        return tekst;
//    }
//
//    public String getLink() {
//        return link;
//    }
//
//    public Integer getSikkerhetsnivaa() {
//        return sikkerhetsnivaa;
//    }
//
//    public String getSistOppdatert() {
//        return sistOppdatert.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//    }
//
//    public boolean isAktiv() {
//        return aktiv;
//    }
//}
