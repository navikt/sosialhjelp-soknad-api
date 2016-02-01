package no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HenvendelseServiceTest {

    public static final String BEHANDLINGSKJEDE_ID = "A";
    public static final String BEHANDLINGS_ID = "B";

    @Mock
    SendSoknadPortType sendSoknadEndpoint;

    @InjectMocks
    HenvendelseService service;

    @Before
    public void setUp() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(sendSoknadEndpoint.startSoknad(any(WSStartSoknadRequest.class))).thenReturn(new WSBehandlingsId());
    }

    @Test
    public void testEttersendingBrukerBehandlingskjedeIdDersomDenErSatt() {
        WSHentSoknadResponse respons = new WSHentSoknadResponse();
        respons.setBehandlingskjedeId(BEHANDLINGSKJEDE_ID);
        respons.setBehandlingsId(BEHANDLINGS_ID);

        ArgumentCaptor<WSStartSoknadRequest> argument = ArgumentCaptor.forClass(WSStartSoknadRequest.class);
        service.startEttersending(respons);

        verify(sendSoknadEndpoint).startSoknad(argument.capture());
        assertEquals(BEHANDLINGSKJEDE_ID, argument.getValue().getBehandlingskjedeId());
    }

    @Test
    public void testEttersendingBrukerBehandlingsIdDersomBehandlingskjedeIdIkkeErSatt() {
        WSHentSoknadResponse respons = new WSHentSoknadResponse();
        respons.setBehandlingsId(BEHANDLINGS_ID);

        ArgumentCaptor<WSStartSoknadRequest> argument = ArgumentCaptor.forClass(WSStartSoknadRequest.class);
        service.startEttersending(respons);

        verify(sendSoknadEndpoint).startSoknad(argument.capture());
        assertEquals(BEHANDLINGS_ID, argument.getValue().getBehandlingskjedeId());
    }
}
