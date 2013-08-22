package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;
import no.nav.tjeneste.domene.brukerdialog.oppdaterehenvendelsesbehandling.v1.OppdatereHenvendelsesBehandlingPortType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.dokumentinnsending.TestUtils.getBytesFromFile;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class DefaultSoknadServiceIntegrationTest {

    @InjectMocks
    private SoknadService soknadService = new DefaultSoknadService();
    @Mock
    private OppdatereHenvendelsesBehandlingPortType oppdatereHenvendelsesBehandlingPortType;

    private static final String IMAGE_DIR = "/testFiles";

    @Test
    public void lagreDokumentKallerPaaWS() throws IOException {
        List<byte[]> listeMedBytes = new ArrayList<>();
        byte[] imageBytes = getBytesFromFile(IMAGE_DIR + "/skjema.jpg");
        listeMedBytes.add(imageBytes);
        Dokument dokument = lagDokument(listeMedBytes);
        soknadService.oppdaterInnhold(dokument, dokument.getDokumentInnhold());

        verify(oppdatereHenvendelsesBehandlingPortType, times(1)).opprettDokument(any(WSDokument.class), anyLong());
    }

    @Test
    public void slettDokumentKallerPaaWS() {
        String behandlingsId = "1";
        soknadService.slettSoknad(behandlingsId);

        verify(oppdatereHenvendelsesBehandlingPortType, times(1)).avbrytHenvendelse(behandlingsId);
    }

    @Test(expected = ApplicationException.class)
    public void skalKasteApplicationExceptionPaaLagreDokumentDersomKallTilWebServiceKasterSOAPFaultException() throws IOException {
        List<byte[]> listeMedBytes = new ArrayList<>();
        byte[] imageBytes = getBytesFromFile(IMAGE_DIR + "/skjema.jpg");
        listeMedBytes.add(imageBytes);
        Dokument dokument = lagDokument(listeMedBytes);

        SOAPFault faultMock = mock(SOAPFault.class);
        when(faultMock.getFaultString()).thenReturn("There was an exception yo");
        SOAPFaultException exceptionMock = mock(SOAPFaultException.class);
        when(exceptionMock.getFault()).thenReturn(faultMock);

        doThrow(exceptionMock).when(oppdatereHenvendelsesBehandlingPortType).opprettDokument(any(WSDokument.class), anyLong());

        soknadService.oppdaterInnhold(dokument, dokument.getDokumentInnhold());
    }

    @Test
    public void skalKunneSletteEtDokument() {
    	List<byte[]> listeMedBytes = new ArrayList<>();
    	Dokument dokument = lagDokument(listeMedBytes);
    	soknadService.slettInnhold(dokument);
    	verify(oppdatereHenvendelsesBehandlingPortType, times(1)).slettDokument(anyLong());
    }
    
    /***
     * Dette kan for eksempel skje ved dobbeltklikk, eller at to vinduer er åpne samtidig og prøver først å slette dokumentet
     * i det ene vinduet (går bra) og så gjør den samme operasjonen i den andre
     */
    @Test
    public void slettingAvIkkeEksisterendeDokumentSkalIkkeFeile() {
    	List<byte[]> listeMedBytes = new ArrayList<>();
    	Dokument dokument = lagDokument(listeMedBytes);
    	soknadService.slettInnhold(dokument);
    	dokument = null;
    	soknadService.slettInnhold(dokument);
    	verify(oppdatereHenvendelsesBehandlingPortType, times(1)).slettDokument(anyLong());
    }

    private Dokument lagDokument(List<byte[]> listeMedBytes) {
        Dokument dokument = new Dokument(Type.HOVEDSKJEMA);
        dokument.setBehandlingsId("1");
        dokument.setDokumentForventningsId(1L);
        dokument.setDokumentId(1L);
        dokument.setNavn("test.pdf");
        dokument.setDokumentInnhold(new DokumentInnhold());
        dokument.getDokumentInnhold().settOgTransformerInnhold(listeMedBytes);
        return dokument;
    }
}