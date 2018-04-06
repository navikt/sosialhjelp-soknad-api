package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.ks.svarut.servicesv9.Forsendelse;
import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import no.ks.svarut.servicesv9.OrganisasjonDigitalAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksData.DokumentInfo;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
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
        System.setProperty(FiksSender.KRYPTERING_DISABLED, "");

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
        System.setProperty(FiksSender.KRYPTERING_DISABLED, "true");

        fiksSender.sendTilFiks(data);

        ArgumentCaptor<Forsendelse> captor = ArgumentCaptor.forClass(Forsendelse.class);
        verify(forsendelsesService).sendForsendelse(captor.capture());
        Forsendelse sendtForsendelse = captor.getValue();

        assertEquals(false, sendtForsendelse.isKryptert());
        assertEquals(false, sendtForsendelse.isKrevNiva4Innlogging());
        verify(dokumentKrypterer, times(0)).krypterData(any());
    }
}