package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.ks.svarut.servicesv9.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksData.DokumentInfo;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksSender.ETTERSENDELSE_TIL_NAV;
import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksSender.SOKNAD_TIL_NAV;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FiksSenderTest {

    @Mock
    ForsendelsesServiceV9 forsendelsesService;

    @Mock
    FillagerService fillager;

    @Mock
    DokumentKrypterer dokumentKrypterer;

    @InjectMocks
    FiksSender fiksSender;

    FiksData data;

    @Before
    public void setUp() {
        setProperty(FiksSender.KRYPTERING_DISABLED, "");

        when(forsendelsesService.sendForsendelse(any())).thenReturn("id1234");
        when(fillager.hentFil(any())).thenReturn(new byte[]{1, 2, 3});
        when(dokumentKrypterer.krypterData(any())).thenReturn(new byte[]{3, 2, 1});

        data = new FiksData();
        data.avsenderFodselsnummer = "1234";
        data.mottakerOrgNr = "999999999";

        DokumentInfo etVedlegg = new DokumentInfo("uuid1", "fil1.pdf", "application/pdf");
        DokumentInfo xml = new DokumentInfo("uuid2", "soknad.xml", "application/xml");
        data.dokumentInfoer = asList(etVedlegg, xml);
    }

    @Test
    public void senderMeldingTilFiks() {
        String fiksForsendelsesId = fiksSender.sendTilFiks(data);
        assertEquals("id1234", fiksForsendelsesId);

        ArgumentCaptor<Forsendelse> captor = ArgumentCaptor.forClass(Forsendelse.class);
        verify(forsendelsesService).sendForsendelse(captor.capture());
        Forsendelse sendtForsendelse = captor.getValue();

        assertEquals(data.mottakerOrgNr, ((OrganisasjonDigitalAdresse) sendtForsendelse.getMottaker().getDigitalAdresse()).getOrgnr());
        assertEquals(true, sendtForsendelse.isKryptert());
        assertEquals(true, sendtForsendelse.isKrevNiva4Innlogging());

        assertEquals(2, sendtForsendelse.getDokumenter().size());
        assertEquals("application/pdf", sendtForsendelse.getDokumenter().get(0).getMimetype());
        assertEquals("application/xml", sendtForsendelse.getDokumenter().get(1).getMimetype());

        verify(fillager).hentFil("uuid1");
        verify(fillager).hentFil("uuid2");
        verify(dokumentKrypterer, times(2)).krypterData(any());
    }

    @Test
    public void skalIkkeKryptere() {
        setProperty(FiksSender.KRYPTERING_DISABLED, "true");

        fiksSender.sendTilFiks(data);

        ArgumentCaptor<Forsendelse> captor = ArgumentCaptor.forClass(Forsendelse.class);
        verify(forsendelsesService).sendForsendelse(captor.capture());
        Forsendelse sendtForsendelse = captor.getValue();

        assertEquals(false, sendtForsendelse.isKryptert());
        assertEquals(false, sendtForsendelse.isKrevNiva4Innlogging());
        verify(dokumentKrypterer, times(0)).krypterData(any());
    }

    @Test
    public void opprettForsendelseSetterRiktigTittelForNySoknad() {
        Forsendelse forsendelse = fiksSender.opprettForsendelse(data, new PostAdresse(), true);

        assertThat(forsendelse.getTittel(), is(SOKNAD_TIL_NAV));
    }

    @Test
    public void opprettForsendelseSetterRiktigTittelForEttersendelse() {
        data.ettersendelsePa = "12345";

        Forsendelse forsendelse = fiksSender.opprettForsendelse(data, new PostAdresse(), true);

        assertThat(forsendelse.getTittel(), is(ETTERSENDELSE_TIL_NAV));
    }

    @After
    public void tearDown() {
        clearProperty(FiksSender.KRYPTERING_DISABLED);
    }
}