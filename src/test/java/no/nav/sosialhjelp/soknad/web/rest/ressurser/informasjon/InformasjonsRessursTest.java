package no.nav.sosialhjelp.soknad.web.rest.ressurser.informasjon;

import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import no.nav.sosialhjelp.soknad.business.service.InformasjonService;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlService;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(MockitoJUnitRunner.class)
public class InformasjonsRessursTest {

    @Mock
    private PdlService pdlService;
    @Mock
    private InformasjonService informasjonService;
    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private InformasjonRessurs informasjonRessurs;

    @Test
    public void skalReturnereMappetListeOverManueltPakobledeKommuner() {
        List<String> manuelleKommuner = Arrays.asList("1234");
        Map<String, InformasjonRessurs.KommuneInfoFrontend> mappedeKommuner = informasjonRessurs.mapManueltPakobledeKommuner(manuelleKommuner);

        assertNotNull(mappedeKommuner.get("1234"));
        assertTrue(mappedeKommuner.get("1234").kanMottaSoknader);
        assertFalse(mappedeKommuner.get("1234").kanOppdatereStatus);
    }

    @Test
    public void skalReturnereMappetListeOverDigisosKommuner() {
        Map<String, KommuneInfo> digisosKommuner = new HashMap<>();
        digisosKommuner.put("1234", new KommuneInfo("1234", true, true, false, false, null, false, null));
        Map<String, InformasjonRessurs.KommuneInfoFrontend> mappedeKommuner = informasjonRessurs.mapDigisosKommuner(digisosKommuner);

        assertNotNull(mappedeKommuner.get("1234"));
        assertTrue(mappedeKommuner.get("1234").kanMottaSoknader);
        assertTrue(mappedeKommuner.get("1234").kanOppdatereStatus);
    }

    @Test
    public void duplikatIDigisosKommuneSkalOverskriveManuellKommune() {
        List<String> manuelleKommuner = Arrays.asList("1234");
        Map<String, InformasjonRessurs.KommuneInfoFrontend> manueltMappedeKommuner = informasjonRessurs.mapManueltPakobledeKommuner(manuelleKommuner);
        assertFalse(manueltMappedeKommuner.get("1234").kanOppdatereStatus); // Manuelle kommuner får ikke innsyn

        Map<String, KommuneInfo> digisosKommuner = new HashMap<>();
        digisosKommuner.put("1234", new KommuneInfo("1234", true, true, false, false, null, false, null));
        Map<String, InformasjonRessurs.KommuneInfoFrontend> mappedeDigisosKommuner = informasjonRessurs.mapDigisosKommuner(digisosKommuner);

        Map<String, InformasjonRessurs.KommuneInfoFrontend> margedKommuner = informasjonRessurs.mergeManuelleKommunerMedDigisosKommuner(manueltMappedeKommuner, mappedeDigisosKommuner);
        assertEquals(margedKommuner.size(), 1);
        assertTrue(margedKommuner.get("1234").kanOppdatereStatus);
    }

    @Test(expected = AuthorizationException.class)
    public void getMiljovariablerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        informasjonRessurs.hentMiljovariabler();

        verifyNoInteractions(informasjonService);
    }

    @Test(expected = AuthorizationException.class)
    public void getFornavnSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        informasjonRessurs.hentFornavn();

        verifyNoInteractions(pdlService);
    }
}
