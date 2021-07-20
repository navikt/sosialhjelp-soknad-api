package no.nav.sosialhjelp.soknad.business.service.minesaker;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MineSakerMetadataServiceTest {

    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @InjectMocks
    private MineSakerMetadataService mineSakerMetadataService;

    @Test
    void skalHenteInnsendteSoknaderForBruker() {
        var soknadMetadata = new SoknadMetadata();
        soknadMetadata.fnr = "12345";
        soknadMetadata.behandlingsId = "beh123";
        soknadMetadata.type = SoknadType.SEND_SOKNAD_KOMMUNAL;
        soknadMetadata.innsendtDato = LocalDateTime.now();

        when(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345"))
                .thenReturn(Collections.singletonList(soknadMetadata));

        var dtos = mineSakerMetadataService.hentInnsendteSoknader("12345");

        assertThat(dtos).hasSize(1);
    }

    @Test
    void skalReturnereTomListeVedNull() {
        when(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345"))
                .thenReturn(null);

        var dtos = mineSakerMetadataService.hentInnsendteSoknader("12345");

        assertThat(dtos).isEmpty();
    }

    @Test
    void skalReturnereTomListeVedTomListe() {
        when(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345"))
                .thenReturn(Collections.emptyList());

        var dtos = mineSakerMetadataService.hentInnsendteSoknader("12345");

        assertThat(dtos).isEmpty();
    }
}