//package no.nav.sosialhjelp.soknad.business.service.dittnav;
//
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto.PabegyntSoknadDto;
//import org.slf4j.Logger;
//import org.springframework.stereotype.Component;
//
//import java.time.ZoneId;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static no.nav.sosialhjelp.soknad.business.util.TimeUtils.toUtc;
//import static org.slf4j.LoggerFactory.getLogger;
//
//@Component
//public class DittNavMetadataService {
//
//    private static final Logger log = getLogger(DittNavMetadataService.class);
//
//    private static final String SOKNAD_TITTEL = "Søknad om økonomisk sosialhjelp";
//    private static final int SIKKERHETSNIVAA_3 = 3;
//    private static final int SIKKERHETSNIVAA_4 = 4;
//
//    private final SoknadMetadataRepository soknadMetadataRepository;
//
//    public DittNavMetadataService(SoknadMetadataRepository soknadMetadataRepository) {
//        this.soknadMetadataRepository = soknadMetadataRepository;
//    }
//
//    public List<PabegyntSoknadDto> hentAktivePabegynteSoknader(String fnr) {
//        return hentPabegynteSoknader(fnr, true);
//    }
//
//    public List<PabegyntSoknadDto> hentInaktivePabegynteSoknader(String fnr) {
//        return hentPabegynteSoknader(fnr, false);
//    }
//
//    private List<PabegyntSoknadDto> hentPabegynteSoknader(String fnr, boolean aktiv) {
//        var pabegynteSoknader = soknadMetadataRepository.hentPabegynteSoknaderForBruker(fnr, !aktiv);
//
//        return pabegynteSoknader.stream()
//                .map(soknadMetadata -> new PabegyntSoknadDto(
//                        toUtc(soknadMetadata.opprettetDato, ZoneId.systemDefault()),
//                        eventId(soknadMetadata.behandlingsId, aktiv),
//                        soknadMetadata.behandlingsId,
//                        SOKNAD_TITTEL,
//                        lenkeTilPabegyntSoknad(soknadMetadata.behandlingsId),
//                        SIKKERHETSNIVAA_3, // todo finn ut hvilken
//                        toUtc(soknadMetadata.sistEndretDato, ZoneId.systemDefault()),
//                        aktiv
//                ))
//                .collect(Collectors.toList());
//    }
//
//    public boolean oppdaterLestDittNavForPabegyntSoknad(String behandlingsId, String fnr) {
//        var soknadMetadata = soknadMetadataRepository.hent(behandlingsId);
//        if (soknadMetadata == null) {
//            log.warn("Fant ingen soknadMetadata med behandlingsId={}", behandlingsId);
//            return false;
//        }
//        soknadMetadata.lestDittNav = true;
//        try {
//            soknadMetadataRepository.oppdaterLestDittNav(soknadMetadata, fnr);
//            return true;
//        } catch (Exception e) {
//            log.warn("Noe feilet ved oppdatering av lestDittNav for soknadMetadata med behandlingsId={}", behandlingsId, e);
//            return false;
//        }
//    }
//
//    private String eventId(String behandlingsId, boolean aktiv) {
//        return behandlingsId + "_" + (aktiv ? "aktiv" : "inaktiv");
//    }
//
//    private String lenkeTilPabegyntSoknad(String behandlingsId) {
//        return lagContextLenke() + "skjema/" + behandlingsId + "/0";
//    }
//
//    private String lagContextLenke() {
//        var miljo = System.getProperty("environment.name", "");
//        var postfix = miljo.contains("q") ? String.format("-%s", miljo) : "";
//        return "https://www" + postfix + ".nav.no/sosialhjelp/soknad/";
//    }
//}
