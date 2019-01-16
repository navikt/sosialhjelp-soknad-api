package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.core.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.StaticOidcSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler.OIDC_SUBJECT_HANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler.getSubjectHandler;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollTest {

    @InjectMocks
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private SoknadService soknadService;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @Test
    public void skalGiTilgangForBruker() {
        System.setProperty(OIDC_SUBJECT_HANDLER_KEY, StaticOidcSubjectHandler.class.getName());
        StaticOidcSubjectHandler subjectHandler = (StaticOidcSubjectHandler) getSubjectHandler();
        when(soknadService.hentSoknad("123", false, false)).thenReturn(new WebSoknad().medAktorId(subjectHandler.getUserIdFromToken()));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndre() {
        System.setProperty(OIDC_SUBJECT_HANDLER_KEY, StaticOidcSubjectHandler.class.getName());
        when(soknadService.hentSoknad("XXX", false, false)).thenReturn(new WebSoknad().medAktorId("other_user"));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileOmSoknadenIkkeFinnes() {
        System.setProperty(OIDC_SUBJECT_HANDLER_KEY, StaticOidcSubjectHandler.class.getName());
        when(soknadService.hentSoknad("123", false, false)).thenReturn(null);
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test
    public void skalGiTilgangForBrukerMetadata() {
        System.setProperty(OIDC_SUBJECT_HANDLER_KEY, StaticOidcSubjectHandler.class.getName());
        StaticOidcSubjectHandler subjectHandler = (StaticOidcSubjectHandler) getSubjectHandler();
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = subjectHandler.getUserIdFromToken();
        when(soknadMetadataRepository.hent("123")).thenReturn(metadata);
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndreMetadata() {
        System.setProperty(OIDC_SUBJECT_HANDLER_KEY, StaticOidcSubjectHandler.class.getName());
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = "other_user";
        when(soknadMetadataRepository.hent("123")).thenReturn(metadata);
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileHvisEierErNull() {
        System.setProperty(OIDC_SUBJECT_HANDLER_KEY, StaticOidcSubjectHandler.class.getName());
        tilgangskontroll.verifiserTilgangMotPep(null, "");
    }

}
