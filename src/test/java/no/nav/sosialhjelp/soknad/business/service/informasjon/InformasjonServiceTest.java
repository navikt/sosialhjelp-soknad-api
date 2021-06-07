package no.nav.sosialhjelp.soknad.business.service.informasjon;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class InformasjonServiceTest {

    // ingen pabegynte soknader
    // har 1 eller flere pabegynte

    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @InjectMocks
    private PabegynteSoknaderService informasjonService;

    @Test
    public void brukerHarIngenPabegynteSoknader() {
        when(soknadMetadataRepository.hentPabegynteSoknaderForBruker(anyString())).thenReturn(emptyList());

        assertThat(informasjonService.hentPabegynteSoknaderForBruker("fnr")).isEmpty();
    }

    @Test
    public void brukerHar1PabegyntSoknader() {
        var soknadMetadata = new SoknadMetadata();
        var now = LocalDateTime.now();
        soknadMetadata.sistEndretDato = now;
        soknadMetadata.behandlingsId = "id";

        when(soknadMetadataRepository.hentPabegynteSoknaderForBruker(anyString())).thenReturn(singletonList(soknadMetadata));

        var pabegyntSoknadList = informasjonService.hentPabegynteSoknaderForBruker("fnr");
        assertThat(pabegyntSoknadList).hasSize(1);
        assertThat(pabegyntSoknadList.get(0).getSistOppdatert()).isEqualTo(now);
        assertThat(pabegyntSoknadList.get(0).getBehandlingsId()).isEqualTo("id");
    }
}