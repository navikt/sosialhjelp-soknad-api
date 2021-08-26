package no.nav.sosialhjelp.soknad.business.service.dialog;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.dialog.dto.NyligInnsendteSoknaderDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NyligInnsendteSoknaderService {

    private final SoknadMetadataRepository soknadMetadataRepository;

    public NyligInnsendteSoknaderService(
            SoknadMetadataRepository soknadMetadataRepository
    ) {
        this.soknadMetadataRepository = soknadMetadataRepository;
    }

    public List<NyligInnsendteSoknaderDto> hentNyligInnsendteSoknader(String fnr, Integer antallManeder) {
        return Optional
                .ofNullable(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(fnr, LocalDateTime.now().minusMonths(antallManeder)))
                .orElse(Collections.emptyList()).stream()
                .map(soknadMetadata -> new NyligInnsendteSoknaderDto(soknadMetadata.fiksForsendelseId, soknadMetadata.navEnhet, soknadMetadata.innsendtDato))
                .collect(Collectors.toList());
    }
}
