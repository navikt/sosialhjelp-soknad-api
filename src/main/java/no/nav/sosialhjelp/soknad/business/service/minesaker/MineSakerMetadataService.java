package no.nav.sosialhjelp.soknad.business.service.minesaker;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto.InnsendtSoknadDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Component
public class MineSakerMetadataService {

    private static final String TEMA_NAVN = "Økonomisk sosialhjelp";
    private static final String TEMA_KODE_KOM = "KOM";

    private final SoknadMetadataRepository soknadMetadataRepository;

    public MineSakerMetadataService(
            SoknadMetadataRepository soknadMetadataRepository
    ) {
        this.soknadMetadataRepository = soknadMetadataRepository;
    }

    public List<InnsendtSoknadDto> hentInnsendteSoknader(String fnr) {
        var innsendteSoknader = soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr).stream().flatMap(Stream::ofNullable);

        return innsendteSoknader.findFirst()
                .map(soknadMetadata -> singletonList(new InnsendtSoknadDto(TEMA_NAVN, TEMA_KODE_KOM, soknadMetadata.innsendtDato)))
                .orElse(emptyList());
    }
}
