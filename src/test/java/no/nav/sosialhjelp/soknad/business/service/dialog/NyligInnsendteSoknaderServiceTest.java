package no.nav.sosialhjelp.soknad.business.service.dialog;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NyligInnsendteSoknaderServiceTest {

    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @InjectMocks
    private NyligInnsendteSoknaderService nyligInnsendteSoknaderService;

    private final String fnr = "12345";
    private final String fiksForsendelseId = "fiksId";
    private final String navEnhet = "1234";

    @Test
    void skalHenteNyligInnsendteSoknaderForBruker() {
        var innsendtDato = LocalDateTime.now().minusDays(1);
        var soknadMetadata = new SoknadMetadata();
        soknadMetadata.fnr = fnr;
        soknadMetadata.fiksForsendelseId = fiksForsendelseId;
        soknadMetadata.navEnhet = navEnhet;
        soknadMetadata.innsendtDato = innsendtDato;

        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any()))
                .thenReturn(Collections.singletonList(soknadMetadata));

        var dtos = nyligInnsendteSoknaderService.hentNyligInnsendteSoknader(fnr, 3);

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getDigisosId()).isEqualTo(fiksForsendelseId);
        assertThat(dtos.get(0).getEnhetsnr()).isEqualTo(navEnhet);
        assertThat(dtos.get(0).getInnsendtDato()).isEqualTo(innsendtDato.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void skalReturnereTomListeVedNull() {
        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any()))
                .thenReturn(null);

        var dtos = nyligInnsendteSoknaderService.hentNyligInnsendteSoknader(fnr, 3);

        assertThat(dtos).isEmpty();
    }

    @Test
    void skalReturnereTomListeVedTomListe() {
        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any()))
                .thenReturn(Collections.emptyList());

        var dtos = nyligInnsendteSoknaderService.hentNyligInnsendteSoknader(fnr, 3);

        assertThat(dtos).isEmpty();
    }

}