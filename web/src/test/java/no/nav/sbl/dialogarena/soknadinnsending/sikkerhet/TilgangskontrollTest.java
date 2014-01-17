package no.nav.sbl.dialogarena.soknadinnsending.sikkerhet;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.exception.AuthorizationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.aktor.AktorIdService;
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
    private SoknadService soknadService;

    @Test
    public void skalGiTilgangForBruker() {
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        StaticSubjectHandler subjectHandler = (StaticSubjectHandler) SubjectHandler.getSubjectHandler();

        when(aktorIdService.hentAktorIdForFno(subjectHandler.getUid())).thenReturn("123");
        when(soknadService.hentSoknad(1L)).thenReturn(new WebSoknad().medAktorId(subjectHandler.getUid()));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(1L);

    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndre() {
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        StaticSubjectHandler subjectHandler = (StaticSubjectHandler) SubjectHandler.getSubjectHandler();

        when(aktorIdService.hentAktorIdForFno(subjectHandler.getUid())).thenReturn("124");
        when(soknadService.hentSoknad(1L)).thenReturn(new WebSoknad().medAktorId("123"));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(1L);

    }

}
