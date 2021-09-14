package no.nav.sosialhjelp.soknad.business.service.dialog;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.dialog.dto.SistInnsendteSoknadDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;

@Component
public class SistInnsendteSoknadService {

    private final SoknadMetadataRepository soknadMetadataRepository;

    public SistInnsendteSoknadService(
            SoknadMetadataRepository soknadMetadataRepository
    ) {
        this.soknadMetadataRepository = soknadMetadataRepository;
    }

    public Optional<SistInnsendteSoknadDto> hentSistInnsendteSoknad(String fnr) {
        return Optional.ofNullable(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr)).orElse(emptyList())
                .stream()
                .max(comparing(o -> o.innsendtDato))
                .map(metadata -> new SistInnsendteSoknadDto(metadata.fiksForsendelseId, metadata.navEnhet, metadata.innsendtDato));
    }
}
