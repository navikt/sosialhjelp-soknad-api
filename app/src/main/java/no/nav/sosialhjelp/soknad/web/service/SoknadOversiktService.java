package no.nav.sosialhjelp.soknad.web.service;

import no.nav.sosialhjelp.soknad.web.rest.ressurser.soknadoversikt.SoknadOversiktRessurs.SoknadOversikt;
import no.nav.sosialhjelp.soknad.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.sosialhjelp.soknad.web.service.SaksoversiktMetadataService.lagEttersendelseLenke;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SoknadOversiktService {

    private static final Logger logger = getLogger(SoknadOversiktService.class);

    static final String KILDE_SOKNAD_API = "soknad-api";

    static final String DEFAULT_TITTEL = "Økonomisk sosialhjelp";

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    public List<SoknadOversikt> hentSvarUtSoknaderFor(String fnr) {

        List<SoknadMetadata> soknader = soknadMetadataRepository.hentSvarUtInnsendteSoknaderForBruker(fnr);

        return soknader.stream()
                .map(soknadMetadata -> new SoknadOversikt()
                        .withFiksDigisosId(null)
                        .withSoknadTittel(String.format(DEFAULT_TITTEL + " (%s)", soknadMetadata.behandlingsId))
                        .withStatus(soknadMetadata.status.toString())
                        .withSistOppdatert(Timestamp.valueOf(soknadMetadata.sistEndretDato))
                        .withAntallNyeOppgaver(null)
                        .withKilde(KILDE_SOKNAD_API)
                        .withUrl(lagEttersendelseLenke(soknadMetadata.behandlingsId)))
                .collect(toList());
    }
}