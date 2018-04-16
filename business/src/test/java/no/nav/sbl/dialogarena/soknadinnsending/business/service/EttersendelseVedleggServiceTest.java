package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.EttersendelseVedleggService.EttersendelseVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EttersendelseVedleggServiceTest {

    @Mock
    SoknadService soknadService;

    @Mock
    VedleggService vedleggService;

    @Mock
    VedleggRepository vedleggRepository;

    @Mock
    FillagerService fillagerService;

    @Mock
    VedleggOriginalFilerService vedleggOriginalFilerService;

    @Captor
    ArgumentCaptor<Vedlegg> captor;

    @InjectMocks
    EttersendelseVedleggService ettersendelseVedleggService;

    WebSoknad webSoknad = new WebSoknad().medId(999L);

    @Before
    public void setup() {
        when(soknadService.hentSoknad(any(), anyBoolean(), anyBoolean())).thenReturn(webSoknad);
    }

    @Test
    public void slarSammenVedlegg() {
        Vedlegg v1 = new Vedlegg()
                .medVedleggId(111L)
                .medFilnavn("fil1.jpg")
                .medSkjemaNummer("skjema1")
                .medSkjemanummerTillegg("tillegg1")
                .medInnsendingsvalg(Status.LastetOpp);
        Vedlegg v2 = new Vedlegg()
                .medVedleggId(222L)
                .medFilnavn("fil2.jpg")
                .medSkjemaNummer("skjema1")
                .medSkjemanummerTillegg("tillegg1")
                .medInnsendingsvalg(Status.LastetOpp);
        Vedlegg v3 = new Vedlegg()
                .medVedleggId(333L)
                .medSkjemaNummer("skjema2")
                .medSkjemanummerTillegg("tillegg2")
                .medInnsendingsvalg(Status.VedleggKreves);

        webSoknad.medVedlegg(asList(v1, v2, v3));

        List<EttersendelseVedlegg> resultat = ettersendelseVedleggService.hentVedleggForSoknad("1234beh");

        assertEquals(2, resultat.size());

        EttersendelseVedlegg vedlegg1 = resultat.get(0);
        EttersendelseVedlegg vedlegg2 = resultat.get(1);

        assertEquals(Status.LastetOpp, vedlegg1.innsendingsvalg);
        assertEquals(111L, vedlegg1.vedleggId);
        assertEquals("skjema1", vedlegg1.skjemaNummer);
        assertEquals("tillegg1", vedlegg1.skjemanummerTillegg);
        assertEquals(2, vedlegg1.filer.size());
        assertEquals(111L, vedlegg1.filer.get(0).filId);
        assertEquals("fil1.jpg", vedlegg1.filer.get(0).filnavn);
        assertEquals(222L, vedlegg1.filer.get(1).filId);
        assertEquals("fil2.jpg", vedlegg1.filer.get(1).filnavn);

        assertEquals(Status.VedleggKreves, vedlegg2.innsendingsvalg);
        assertEquals("skjema2", vedlegg2.skjemaNummer);
        assertEquals(0, vedlegg2.filer.size());
    }

    @Test
    public void endrerVedleggVedForsteOpplasting() {
        Vedlegg v1 = new Vedlegg()
                .medVedleggId(111L)
                .medSoknadId(222L)
                .medInnsendingsvalg(Status.VedleggKreves)
                .medSkjemaNummer("skjema1")
                .medSkjemanummerTillegg("skjema2");

        webSoknad.medVedlegg(asList(v1));

        ettersendelseVedleggService.lastOppVedlegg(111L, null, "filnavn.txt");

        verify(vedleggOriginalFilerService).leggTilOriginalVedlegg(captor.capture(), any(), eq("filnavn.txt"), eq(webSoknad));

        Vedlegg nyttVedlegg = captor.getValue();
        assertEquals(v1, nyttVedlegg);
    }

    @Test
    public void nyttVedleggVedEndaEnOpplasting() {
        Vedlegg v1 = new Vedlegg()
                .medVedleggId(111L)
                .medSoknadId(222L)
                .medInnsendingsvalg(Status.LastetOpp)
                .medSkjemaNummer("skjema1")
                .medSkjemanummerTillegg("skjema2");

        webSoknad.medVedlegg(asList(v1));

        ettersendelseVedleggService.lastOppVedlegg(111L, null, "filnavn.txt");

        verify(vedleggRepository).opprettEllerEndreVedlegg(any(), any());
        verify(vedleggOriginalFilerService).leggTilOriginalVedlegg(captor.capture(), any(), eq("filnavn.txt"), eq(webSoknad));

        Vedlegg nyttVedlegg = captor.getValue();
        assertNotEquals(v1, nyttVedlegg);
    }
}