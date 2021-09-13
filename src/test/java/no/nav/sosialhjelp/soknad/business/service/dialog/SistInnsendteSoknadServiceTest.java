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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SistInnsendteSoknadServiceTest {

    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @InjectMocks
    private SistInnsendteSoknadService sistInnsendteSoknadService;

    private final String fnr = "12345";
    private final String fiksForsendelseId = "fiksId";
    private final String navEnhet = "1234";

    @Test
    void skalHenteSistInnsendteSoknadForBruker() {
        var innsendtDato = LocalDateTime.now().minusDays(1);
        var soknadMetadata = new SoknadMetadata();
        soknadMetadata.fnr = fnr;
        soknadMetadata.fiksForsendelseId = fiksForsendelseId;
        soknadMetadata.navEnhet = navEnhet;
        soknadMetadata.innsendtDato = innsendtDato;

        when(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(any()))
                .thenReturn(Collections.singletonList(soknadMetadata));

        var dto = sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr);

        assertThat(dto).isPresent();
        assertThat(dto.get().getDigisosId()).isEqualTo(fiksForsendelseId);
        assertThat(dto.get().getEnhetsnr()).isEqualTo(navEnhet);
        assertThat(dto.get().getInnsendtDato()).isEqualTo(innsendtDato.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void skalHenteSistInnsendteSoknadForBruker_siste() {
        var innsendtDatoNyest = LocalDateTime.now().minusDays(1);
        var innsendtDatoElst = LocalDateTime.now().minusDays(2);

        var soknadMetadata1 = new SoknadMetadata();
        soknadMetadata1.fnr = fnr;
        soknadMetadata1.fiksForsendelseId = fiksForsendelseId;
        soknadMetadata1.navEnhet = navEnhet;
        soknadMetadata1.innsendtDato = innsendtDatoNyest;

        var soknadMetadata2 = new SoknadMetadata();
        soknadMetadata2.fnr = fnr;
        soknadMetadata2.fiksForsendelseId = "fiksId2";
        soknadMetadata2.navEnhet = navEnhet;
        soknadMetadata2.innsendtDato = innsendtDatoElst;

        when(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(any()))
                .thenReturn(List.of(soknadMetadata1, soknadMetadata2));

        var dto = sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr);

        assertThat(dto).isPresent();
        assertThat(dto.get().getDigisosId()).isEqualTo(fiksForsendelseId);
        assertThat(dto.get().getEnhetsnr()).isEqualTo(navEnhet);
        assertThat(dto.get().getInnsendtDato()).isEqualTo(innsendtDatoNyest.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void skalReturnereEmptyOptionalVedNull() {
        when(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(any()))
                .thenReturn(null);

        var dto = sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr);

        assertThat(dto).isEmpty();
    }

    @Test
    void skalReturnereEmptyOptionalVedTomListe() {
        when(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(any()))
                .thenReturn(Collections.emptyList());

        var dto = sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr);

        assertThat(dto).isEmpty();
    }

}
