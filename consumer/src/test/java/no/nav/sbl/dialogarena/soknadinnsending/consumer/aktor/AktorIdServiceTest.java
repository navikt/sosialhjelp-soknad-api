package no.nav.sbl.dialogarena.soknadinnsending.consumer.aktor;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.tjeneste.virksomhet.aktoer.v1.AktoerPortType;
import no.nav.tjeneste.virksomhet.aktoer.v1.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v1.meldinger.HentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v1.meldinger.HentAktoerIdForIdentResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.soap.SOAPFactory;
import javax.xml.ws.soap.SOAPFaultException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AktorIdServiceTest {

    @InjectMocks
    private AktorIdService aktorIdService;
    @Mock
    private AktoerPortType aktoerPortType;

    @Test
    public void skalKalleHentMedKorrektArgumentOgReturnereSvar() throws Exception {
        HentAktoerIdForIdentResponse serviceRes = new HentAktoerIdForIdentResponse("321");
        when(aktoerPortType.hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class))).thenReturn(serviceRes);
        String res = aktorIdService.hentAktorIdForFno("123");
        verify(aktoerPortType).hentAktoerIdForIdent(argThat(HENT_AKTOR_MATCHER));
        assertThat(res, equalTo("321"));
    }

    @Test(expected = ApplicationException.class)
    public void skalWrappeExceptionsPaIkkeFunnet() throws Exception {
        when(aktoerPortType.hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class))).thenThrow(new HentAktoerIdForIdentPersonIkkeFunnet());
        aktorIdService.hentAktorIdForFno("123");
    }

    @Test(expected = ApplicationException.class)
    public void skalWrappeExceptionsPaSoapExceptions() throws Exception {
        when(aktoerPortType.hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class))).thenThrow(new SOAPFaultException(SOAPFactory.newInstance().createFault()));
        aktorIdService.hentAktorIdForFno("123");
    }

    @Test
    public void skalKallePing() throws Exception {
        aktorIdService.ping();
        verify(aktoerPortType).ping();
    }

    @Test(expected = ApplicationException.class)
    public void skalWrappeExceptionsPaSoapExceptionsPåPing() throws Exception {
        doThrow(new SOAPFaultException(SOAPFactory.newInstance().createFault())).when(aktoerPortType).ping();
        aktorIdService.ping();
    }

    private static ArgumentMatcher<HentAktoerIdForIdentRequest> HENT_AKTOR_MATCHER = new ArgumentMatcher<HentAktoerIdForIdentRequest>() {
        @Override
        public boolean matches(Object item) {
            assertThat(((HentAktoerIdForIdentRequest) item).getIdent(), equalTo("123"));
            return true;
        }
    };
}
