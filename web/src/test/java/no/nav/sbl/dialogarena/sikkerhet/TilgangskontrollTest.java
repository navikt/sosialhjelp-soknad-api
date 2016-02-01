package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
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
    private SoknadService soknadService;

    @Test
    public void skalGiTilgangForBruker() {
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        StaticSubjectHandler subjectHandler = (StaticSubjectHandler) SubjectHandler.getSubjectHandler();
        when(soknadService.hentSoknad("123", false, false)).thenReturn(new WebSoknad().medAktorId(subjectHandler.getUid()));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndre() {
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(soknadService.hentSoknad("XXX", false, false)).thenReturn(new WebSoknad().medAktorId("other_user"));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX");
    }

}
