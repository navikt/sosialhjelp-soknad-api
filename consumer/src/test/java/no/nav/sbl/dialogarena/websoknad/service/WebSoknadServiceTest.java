package no.nav.sbl.dialogarena.websoknad.service;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Named;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class WebSoknadServiceTest {

    private static final String BEHANDLINGS_ID = "129187212";
    @Mock
    @Named("sendSoknadService")
    SendSoknadPortType webservice;
    @InjectMocks
    HenvendelseConnector service;

    @Before
    public void setUp() {

    }

    @Test
    public void skalKunneStarteSoknad() {
        when(webservice.startSoknad(any(WSStartSoknadRequest.class))).thenReturn(lagResultatFraStartSoknad());
        String behandlingsId = service.startSoknad("***REMOVED***", "");
        assertThat(behandlingsId, equalTo(BEHANDLINGS_ID));
    }

    private WSBehandlingsId lagResultatFraStartSoknad() {
        return new WSBehandlingsId().withBehandlingsId(BEHANDLINGS_ID);
    }


}
