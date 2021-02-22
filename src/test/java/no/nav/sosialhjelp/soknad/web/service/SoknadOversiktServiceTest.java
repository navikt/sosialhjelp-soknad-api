package no.nav.sosialhjelp.soknad.web.service;

import no.nav.sosialhjelp.soknad.business.db.soknadmetadata.SoknadMetadataRepository;
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
import static no.nav.sosialhjelp.soknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sosialhjelp.soknad.web.service.SoknadOversiktService.DEFAULT_TITTEL;
import static no.nav.sosialhjelp.soknad.web.service.SoknadOversiktService.KILDE_SOKNAD_API;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

        assertEquals(1, resultat.size());
        SoknadOversikt soknad = resultat.get(0);
        assertNull(soknad.getFiksDigisosId());
        assertTrue(soknad.getSoknadTittel().contains(DEFAULT_TITTEL));
        assertTrue(soknad.getSoknadTittel().contains(soknadMetadata.behandlingsId));
        assertEquals(UNDER_ARBEID.toString(), soknad.getStatus());
        assertEquals(Timestamp.valueOf(soknadMetadata.innsendtDato), soknad.getSistOppdatert());
        assertNull(soknad.getAntallNyeOppgaver());
        assertEquals(KILDE_SOKNAD_API, soknad.getKilde());
        assertTrue(soknad.getUrl().contains(soknadMetadata.behandlingsId));
    }
}