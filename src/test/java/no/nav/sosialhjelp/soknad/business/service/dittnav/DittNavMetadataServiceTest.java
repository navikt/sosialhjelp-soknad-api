package no.nav.sosialhjelp.soknad.business.service.dittnav;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DittNavMetadataServiceTest {

    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @InjectMocks
    private DittNavMetadataService dittNavMetadataService;

    @Test
    public void skalHentePabegynteSoknaderForBruker() {
        var soknadMetadata = new SoknadMetadata();
        soknadMetadata.fnr = "12345";
        soknadMetadata.behandlingsId = "beh123";
        soknadMetadata.status = SoknadMetadataInnsendingStatus.UNDER_ARBEID;
        soknadMetadata.type = SoknadType.SEND_SOKNAD_KOMMUNAL;
        soknadMetadata.opprettetDato = LocalDateTime.now().minusDays(10);
        soknadMetadata.sistEndretDato = LocalDateTime.now().minusDays(2);

        when(soknadMetadataRepository.hentPabegynteSoknaderForBruker(anyString()))
                .thenReturn(Collections.singletonList(soknadMetadata));

        var dtos = dittNavMetadataService.hentPabegynteSoknader("12345");

        assertThat(dtos).hasSize(1);
    }

    @Test
    public void markerPabegyntSoknadSomLest_skalGiFalse_hvisRepositoryReturnererNull() {
        when(soknadMetadataRepository.hent(anyString()))
                .thenReturn(null);
        var markert = dittNavMetadataService.markerPabegyntSoknadSomLest("behandlingsId", "12345");

        assertThat(markert).isFalse();
    }

    @Test
    public void markerPabegyntSoknadSomLest_skalGiFalse_hvisNoeFeiler() {
        var soknadMetadata = new SoknadMetadata();
        soknadMetadata.fnr = "12345";
        soknadMetadata.behandlingsId = "beh123";
        soknadMetadata.status = SoknadMetadataInnsendingStatus.UNDER_ARBEID;
        soknadMetadata.type = SoknadType.SEND_SOKNAD_KOMMUNAL;
        soknadMetadata.innsendtDato = LocalDateTime.now();
        soknadMetadata.lestDittNav = false;

        when(soknadMetadataRepository.hent(anyString()))
                .thenReturn(soknadMetadata);
        doThrow(new RuntimeException("Noe feilet")).when(soknadMetadataRepository).oppdaterLestDittNav(any(SoknadMetadata.class), anyString());

        var markert = dittNavMetadataService.markerPabegyntSoknadSomLest("behandlingsId", "12345");

        assertThat(markert).isFalse();
    }
}