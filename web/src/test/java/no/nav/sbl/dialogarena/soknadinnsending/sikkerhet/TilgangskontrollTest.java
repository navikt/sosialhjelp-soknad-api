package no.nav.sbl.dialogarena.soknadinnsending.sikkerhet;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.exception.AuthorizationException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.aktor.AktorIdService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollTest {

    @InjectMocks
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private AktorIdService aktorIdService;
    @Mock
    private HenvendelseConnector soknadService;

    @Test
    public void skalGiTilgangForBruker() {
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        StaticSubjectHandler subjectHandler = (StaticSubjectHandler) SubjectHandler.getSubjectHandler();

        when(aktorIdService.hentAktorIdForFno(subjectHandler.getUid())).thenReturn("123");
        when(soknadService.hentSoknadEier(1L)).thenReturn("123");
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(1L);

    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndre() {
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        StaticSubjectHandler subjectHandler = (StaticSubjectHandler) SubjectHandler.getSubjectHandler();

        when(aktorIdService.hentAktorIdForFno(subjectHandler.getUid())).thenReturn("124");
        when(soknadService.hentSoknadEier(1L)).thenReturn("123");
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(1L);

    }

}
