package no.nav.sosialhjelp.soknad.web.service;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.soknadoversikt.SoknadOversiktRessurs.SoknadOversikt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.UNDER_ARBEID;
import static no.nav.sosialhjelp.soknad.web.service.SoknadOversiktService.DEFAULT_TITTEL;
import static no.nav.sosialhjelp.soknad.web.service.SoknadOversiktService.KILDE_SOKNAD_API;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadOversiktServiceTest {

    @Mock
    SoknadMetadataRepository soknadMetadataRepository;

    @InjectMocks
    private SoknadOversiktService service;

    private SoknadMetadata soknadMetadata;

    @Before
    public void setUp() {
        soknadMetadata = new SoknadMetadata();
        soknadMetadata.fnr = "12345";
        soknadMetadata.behandlingsId = "beh123";
        soknadMetadata.type = SoknadType.SEND_SOKNAD_KOMMUNAL;
        soknadMetadata.innsendtDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0);
        soknadMetadata.sistEndretDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0);
        soknadMetadata.status = UNDER_ARBEID;
    }

    @Test
    public void hentAlleSoknaderForBruker() {
        when(soknadMetadataRepository.hentSvarUtInnsendteSoknaderForBruker("12345"))
                .thenReturn(singletonList(soknadMetadata));

        List<SoknadOversikt> resultat = service.hentSvarUtSoknaderFor("12345");

        assertThat(resultat).hasSize(1);
        SoknadOversikt soknad = resultat.get(0);
        assertThat(soknad.getFiksDigisosId()).isNull();
        assertThat(soknad.getSoknadTittel()).contains(DEFAULT_TITTEL);
        assertThat(soknad.getSoknadTittel()).contains(soknadMetadata.behandlingsId);
        assertThat(soknad.getStatus()).isEqualTo(UNDER_ARBEID.toString());
        assertThat(soknad.getSistOppdatert()).isEqualTo(Timestamp.valueOf(soknadMetadata.innsendtDato));
        assertThat(soknad.getAntallNyeOppgaver()).isNull();
        assertThat(soknad.getKilde()).isEqualTo(KILDE_SOKNAD_API);
        assertThat(soknad.getUrl()).contains(soknadMetadata.behandlingsId);
    }
}