package no.nav.sosialhjelp.soknad.business.service.dialog;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.dialog.dto.NyligInnsendteSoknaderDto;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class NyligInnsendteSoknaderService {

    private final SoknadMetadataRepository soknadMetadataRepository;

    public NyligInnsendteSoknaderService(
            SoknadMetadataRepository soknadMetadataRepository
    ) {
        this.soknadMetadataRepository = soknadMetadataRepository;
    }

    public Optional<NyligInnsendteSoknaderDto> hentNyligInnsendteSoknader(String fnr) {
        List<SoknadMetadata> soknadMetadataListe = soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr);
        if (soknadMetadataListe == null)
            return Optional.empty();
        return
                soknadMetadataListe
                        .stream().max(Comparator.comparing(o -> o.innsendtDato))
                        .map(soknadMetadata -> new NyligInnsendteSoknaderDto(soknadMetadata.fiksForsendelseId, soknadMetadata.navEnhet, soknadMetadata.innsendtDato));
    }
}
