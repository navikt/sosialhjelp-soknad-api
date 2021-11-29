package no.nav.sosialhjelp.soknad.web.service;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.soknadoversikt.SoknadOversiktRessurs.SoknadOversikt;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.sosialhjelp.soknad.web.service.SaksoversiktMetadataService.lagEttersendelseLenke;

@Service
public class SoknadOversiktService {

    static final String KILDE_SOKNAD_API = "soknad-api";
    static final String DEFAULT_TITTEL = "Økonomisk sosialhjelp";

    private final SoknadMetadataRepository soknadMetadataRepository;

    public SoknadOversiktService(SoknadMetadataRepository soknadMetadataRepository) {
        this.soknadMetadataRepository = soknadMetadataRepository;
    }

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