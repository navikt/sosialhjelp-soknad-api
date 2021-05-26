package no.nav.sosialhjelp.soknad.business.service.minesaker;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto.InnsendtSoknadDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MineSakerMetadataService {

    private final SoknadMetadataRepository soknadMetadataRepository;

    public MineSakerMetadataService(
            SoknadMetadataRepository soknadMetadataRepository
    ) {
        this.soknadMetadataRepository = soknadMetadataRepository;
    }

    public List<InnsendtSoknadDto> hentInnsendteSoknader(String fnr) {
        var pabegynteSoknaderForBruker = soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr);

        if (pabegynteSoknaderForBruker.isEmpty()) {
            return Collections.singletonList(new InnsendtSoknadDto("KOM", null, null));
        }
        return pabegynteSoknaderForBruker.stream()
                .map(soknadMetadata -> new InnsendtSoknadDto("KOM", soknadMetadata.innsendtDato, "url"))
                .collect(Collectors.toList());
    }
}
