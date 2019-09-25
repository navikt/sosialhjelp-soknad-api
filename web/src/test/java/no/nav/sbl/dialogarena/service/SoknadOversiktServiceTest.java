package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.rest.ressurser.soknadoversikt.SoknadOversiktRessurs.SoknadOversikt;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.service.SoknadOversiktService.DEFAULT_TITTEL;
import static no.nav.sbl.dialogarena.service.SoknadOversiktService.KILDE_SOKNAD_API;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
        when(soknadMetadataRepository.hentInnsendteSoknaderForBruker("12345"))
                .thenReturn(singletonList(soknadMetadata));

        List<SoknadOversikt> resultat = service.hentAlleSoknaderFor("12345");

        assertEquals(1, resultat.size());
        SoknadOversikt soknad = resultat.get(0);
        assertNull(soknad.getFiksDigisosId());
        assertEquals(DEFAULT_TITTEL, soknad.getSoknadTittel());
        assertEquals(UNDER_ARBEID.toString(), soknad.getStatus());
        assertEquals(LocalDateTime.of(2018, 4, 11, 13, 30, 0), soknad.getSistOppdatert());
        assertNull(soknad.getAntallNyeOppgaver());
        assertEquals(KILDE_SOKNAD_API, soknad.getKilde());
    }
}