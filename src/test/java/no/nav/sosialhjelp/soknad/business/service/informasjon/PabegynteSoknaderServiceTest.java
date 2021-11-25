package no.nav.sosialhjelp.soknad.business.service.informasjon;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PabegynteSoknaderServiceTest {

    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @InjectMocks
    private PabegynteSoknaderService pabegynteSoknaderService;

    @Test
    void brukerHarIngenPabegynteSoknader() {
        when(soknadMetadataRepository.hentPabegynteSoknaderForBruker(anyString())).thenReturn(emptyList());

        assertThat(pabegynteSoknaderService.hentPabegynteSoknaderForBruker("fnr")).isEmpty();
    }

    @Test
    void brukerHar1PabegyntSoknad() {
        var now = LocalDateTime.now();
        var soknadMetadata = new SoknadMetadata();
        soknadMetadata.sistEndretDato = now;
        soknadMetadata.behandlingsId = "id";

        when(soknadMetadataRepository.hentPabegynteSoknaderForBruker(anyString())).thenReturn(singletonList(soknadMetadata));

        var pabegyntSoknadList = pabegynteSoknaderService.hentPabegynteSoknaderForBruker("fnr");
        assertThat(pabegyntSoknadList).hasSize(1);
        assertThat(pabegyntSoknadList.get(0).getSistOppdatert()).isEqualTo(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(pabegyntSoknadList.get(0).getBehandlingsId()).isEqualTo("id");
    }
}