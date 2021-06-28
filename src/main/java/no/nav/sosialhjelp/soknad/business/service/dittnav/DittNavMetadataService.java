package no.nav.sosialhjelp.soknad.business.service.dittnav;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto.PabegyntSoknadDto;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sosialhjelp.soknad.business.util.TimeUtils.toUtc;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DittNavMetadataService {

    private static final Logger log = getLogger(DittNavMetadataService.class);

    private static final String SOKNAD_TITTEL = "Søknad om økonomisk sosialhjelp";
    private static final int SIKKERHETSNIVAA_3 = 3;
    private static final int SIKKERHETSNIVAA_4 = 4;

    private final SoknadMetadataRepository soknadMetadataRepository;

    public DittNavMetadataService(SoknadMetadataRepository soknadMetadataRepository) {
        this.soknadMetadataRepository = soknadMetadataRepository;
    }

    public List<PabegyntSoknadDto> hentPabegynteSoknader(String fnr) {
        var pabegynteSoknader = soknadMetadataRepository.hentPabegynteSoknaderForBruker(fnr);

        return pabegynteSoknader.stream()
                .map(soknadMetadata -> new PabegyntSoknadDto(
                        toUtc(soknadMetadata.sistEndretDato, ZoneId.systemDefault()),
                        soknadMetadata.behandlingsId,
                        SOKNAD_TITTEL,
                        lenkeTilPabegyntSoknad(soknadMetadata.behandlingsId),
                        SIKKERHETSNIVAA_3, // todo finn ut hvilken
                        toUtc(soknadMetadata.opprettetDato.plusDays(14), ZoneId.systemDefault())
                ))
                .collect(Collectors.toList());
    }

    public boolean markerPabegyntSoknadSomLest(String behandlingsId, String fnr) {
        var soknadMetadata = soknadMetadataRepository.hent(behandlingsId);
        if (soknadMetadata == null) {
            log.warn("Fant ingen soknadMetadata med behandlingsId={}", behandlingsId);
            return false;
        }
        soknadMetadata.lestDittNav = true;
        try {
            soknadMetadataRepository.oppdaterLestDittNav(soknadMetadata, fnr);
            return true;
        } catch (Exception e) {
            log.warn("Noe feilet ved markering av soknadMetadata {} som lest i DittNav", behandlingsId, e);
            return false;
        }
    }

    private String lenkeTilPabegyntSoknad(String behandlingsId) {
        return lagContextLenke() + "skjema/" + behandlingsId + "/0";
    }

    private String lagContextLenke() {
        var miljo = System.getProperty("environment.name", "");
        var postfix = miljo.contains("q") ? String.format("-%s", miljo) : "";
        return "https://www" + postfix + ".nav.no/sosialhjelp/soknad/";
    }
}
