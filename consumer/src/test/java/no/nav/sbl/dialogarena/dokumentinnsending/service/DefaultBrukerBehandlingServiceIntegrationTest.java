package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.convert.PdfGenerator;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingOppsummering;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentInnhold;
import no.nav.tjeneste.domene.brukerdialog.henvendelsesbehandling.v1.HenvendelsesBehandlingPortType;
import no.nav.tjeneste.domene.brukerdialog.oppdaterehenvendelsesbehandling.v1.OppdatereHenvendelsesBehandlingPortType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.ws.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkClient.KVITTERING_KODEVERKSID;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType.DOKUMENT_BEHANDLING;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType.DOKUMENT_ETTERSENDING;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg.INNSENDT;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class DefaultBrukerBehandlingServiceIntegrationTest {

    @InjectMocks
    private BrukerBehandlingServiceIntegration brukerBehandlingServiceIntegration = new DefaultBrukerBehandlingServiceIntegration();

    @Mock
    private HenvendelsesBehandlingPortType henvendelsesBehandlingPortType;

    @Mock
    private OppdatereHenvendelsesBehandlingPortType oppdatereService;

    @Mock
    private PdfGenerator pdfGenerator;

    @Mock
    private SoknadService soknadService;

    @Captor
    private ArgumentCaptor<List<WSDokumentForventning>> argumentCaptor;


    private String brukerbehandlingsId = "12345";
    DokumentSoknad soknad;

    @Before()
    public void before() {
        soknad = new DokumentSoknad("ident", "sId0001");
        soknad.brukerBehandlingType = BrukerBehandlingType.DOKUMENT_BEHANDLING;
        when(soknadService.hentSoknad(eq(brukerbehandlingsId))).thenReturn(soknad);
    }

    @Test
    public void skalOppretteDokumentBehandling() {
        brukerBehandlingServiceIntegration.opprettDokumentBehandling(anyString(), anyListOf(String.class), false);
        verify(oppdatereService, times(1)).opprettDokumentBehandling(anyListOf(WSDokumentForventning.class), eq(DOKUMENT_BEHANDLING));
    }

    @Test
    public void skalOppretteDokumentBehandlingEttersending() {
        brukerBehandlingServiceIntegration.opprettDokumentBehandling(anyString(), anyListOf(String.class), true);
        verify(oppdatereService, times(1)).opprettDokumentBehandling(anyListOf(WSDokumentForventning.class), eq(DOKUMENT_ETTERSENDING));
    }

    @Test
    public void skalSetteHovedskjemaTilInnsendtDersomEttersending() {
        brukerBehandlingServiceIntegration.opprettDokumentBehandling(anyString(), anyListOf(String.class), true);
        verify(oppdatereService, times(1)).opprettDokumentBehandling(argumentCaptor.capture(), eq(DOKUMENT_ETTERSENDING));
        for (WSDokumentForventning forventning : argumentCaptor.getValue()) {
            if (forventning.isHovedskjema()) {
                assertThat(forventning.getInnsendingsValg(), is(equalTo(INNSENDT)));
                return;
            }
        }
        fail("Hovedskjema ikke funnet");
    }

    @Test
    public void skalOppdatereBrukerBehandling() {
        brukerBehandlingServiceIntegration.oppdaterBrukerBehandling("1", brukerbehandlingsId);
        verify(oppdatereService, times(1)).identifiserAktor(anyString(), anyString());
    }

    @Test(expected = ApplicationException.class)
    public void skalKasteApplicationExceptionPaaOpprettDokumentBehandlingDersomKallTilWebServiceKasterSOAPFaultException() {
        doThrow(SOAPFaultException.class).when(oppdatereService).opprettDokumentBehandling(anyList(), eq(DOKUMENT_BEHANDLING));
        brukerBehandlingServiceIntegration.opprettDokumentBehandling(anyString(), anyListOf(String.class), false);
    }

    @Test(expected = ApplicationException.class)
    public void skalKasteApplicationExceptionPaaOppdaterBrukerBehandlingDersomKallTilWebServiceKasterSOAPFaultException() {
        doThrow(SOAPFaultException.class).when(oppdatereService).identifiserAktor(anyString(), anyString());
        brukerBehandlingServiceIntegration.oppdaterBrukerBehandling("1", brukerbehandlingsId);
    }

    @Test(expected = ApplicationException.class)
    public void skalKasteApplicationExceptionPaaHentBrukerBehandlingIderDersomKallTilWebServiceKasterSOAPFaultException() {
        doThrow(SOAPFaultException.class).when(henvendelsesBehandlingPortType).hentBrukerBehandlingListe(anyString());

        brukerBehandlingServiceIntegration.hentBrukerBehandlingIder(anyString());
    }

    @Test
    public void skalHenteBrukerBehandlingIder() {
        List<WSBrukerBehandlingOppsummering> brukerBehandlinger = lagBrukerBehandlinger();
        when(henvendelsesBehandlingPortType.hentBrukerBehandlingListe(anyString())).thenReturn(brukerBehandlinger);

        List<String> behandlingsIder = brukerBehandlingServiceIntegration.hentBrukerBehandlingIder("11111111111");
        assertEquals(asList("500", "600"), behandlingsIder);
    }

    @Test
    public void skalOppretteDokumentBehandlingDersomDetGisInnGyldigeKodeverksIder() {
        brukerBehandlingServiceIntegration.opprettDokumentBehandling("S7", Arrays.asList("S8"), false);

        verify(oppdatereService, times(1)).opprettDokumentBehandling(anyList(), eq(DOKUMENT_BEHANDLING));
    }

    @Test
    public void sendBrukerBehandlingSkalLeggeTilKvittering() {
        Long dokId = 101L;
        when(oppdatereService.opprettDokumentForventning(any(WSDokumentForventning.class), eq(brukerbehandlingsId))).thenReturn(dokId);
        brukerBehandlingServiceIntegration.sendBrukerBehandling(brukerbehandlingsId, "");
        verify(pdfGenerator).lagKvitteringsSide(eq(soknad));
        verify(oppdatereService).opprettDokumentForventning(argThat(new ArgumentMatcher<WSDokumentForventning>() {
            @Override
            public boolean matches(Object argument) {
                return ((WSDokumentForventning) argument).getKodeverkId().equals(KVITTERING_KODEVERKSID);
            }
        }), eq(brukerbehandlingsId));
        verify(oppdatereService).opprettDokument(any(WSDokumentInnhold.class), eq(dokId));
    }

    @Test
    public void sendBrukerBehandlingSkalKalleOpprettElektroniskSamtykke() {
        brukerBehandlingServiceIntegration.sendBrukerBehandling(brukerbehandlingsId, "");
        verify(oppdatereService, times(1)).opprettElektroniskSamtykke(brukerbehandlingsId);
    }

    @Test
    public void sendBrukerBehandlingSkalKalleSendHenvendelse() {
        brukerBehandlingServiceIntegration.sendBrukerBehandling(brukerbehandlingsId, "");
        verify(oppdatereService, times(1)).opprettElektroniskSamtykke(brukerbehandlingsId);
        verify(oppdatereService, times(1)).sendHenvendelse(brukerbehandlingsId, "");
    }

    @Test(expected = ApplicationException.class)
    public void sendBrukerBehandlingSkalFaa() {
        doThrow(SOAPFaultException.class).when(oppdatereService).opprettElektroniskSamtykke(brukerbehandlingsId);

        try {
            brukerBehandlingServiceIntegration.sendBrukerBehandling(brukerbehandlingsId, "");
        } catch (SOAPFaultException e) {
            verify(oppdatereService, never()).sendHenvendelse(brukerbehandlingsId, "");
            throw e;
        }
    }

    @Test
    public void skalKallePaaPingTjeneste() {
        brukerBehandlingServiceIntegration.ping();
        verify(henvendelsesBehandlingPortType, times(1)).ping();
    }

    private List<WSBrukerBehandlingOppsummering> lagBrukerBehandlinger() {
        List<WSBrukerBehandlingOppsummering> brukerBehandlinger = new ArrayList<>();

        brukerBehandlinger.add(lagBrukerBehandling("500"));
        brukerBehandlinger.add(lagBrukerBehandling("600"));

        return brukerBehandlinger;
    }

    private WSBrukerBehandlingOppsummering lagBrukerBehandling(String behandlingsId) {
        WSBrukerBehandlingOppsummering brukerBehandling1 = new WSBrukerBehandlingOppsummering();
        brukerBehandling1.setBehandlingsId(behandlingsId);
        return brukerBehandling1;
    }
}
