package no.nav.sosialhjelp.soknad.business.service.minesaker;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.MineSakerMetadataRessurs;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto.InnsendtSoknadDto;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.business.util.TimeUtils.toUtc;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class MineSakerMetadataService {

    private static final Logger log = getLogger(MineSakerMetadataRessurs.class);

    private static final String TEMA_NAVN = "Økonomisk sosialhjelp";
    private static final String TEMA_KODE_KOM = "KOM";

    private final SoknadMetadataRepository soknadMetadataRepository;

    public MineSakerMetadataService(
            SoknadMetadataRepository soknadMetadataRepository
    ) {
        this.soknadMetadataRepository = soknadMetadataRepository;
    }

    public List<InnsendtSoknadDto> hentInnsendteSoknader(String fnr) {
        var innsendteSoknader = Optional.ofNullable(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr)).orElse(emptyList());
        log.debug("Fant {} innsendte soknader", innsendteSoknader.size());
        return innsendteSoknader.stream().findFirst()
                .map(soknadMetadata -> singletonList(new InnsendtSoknadDto(TEMA_NAVN, TEMA_KODE_KOM, toUtc(soknadMetadata.innsendtDato, ZoneId.systemDefault()))))
                .orElse(emptyList());
    }
}
