package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.core.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
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
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @Test
    public void skalGiTilgangForBruker() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        StaticSubjectHandlerService subjectHandler = (StaticSubjectHandlerService) SubjectHandler.getSubjectHandlerService();
        when(soknadService.hentSoknad("123", false, false)).thenReturn(new WebSoknad().medAktorId(subjectHandler.getUserIdFromToken()));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndre() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        when(soknadService.hentSoknad("XXX", false, false)).thenReturn(new WebSoknad().medAktorId("other_user"));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileOmSoknadenIkkeFinnes() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        when(soknadService.hentSoknad("123", false, false)).thenReturn(null);
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test
    public void skalGiTilgangForBrukerMetadata() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        StaticSubjectHandlerService subjectHandler = (StaticSubjectHandlerService) SubjectHandler.getSubjectHandlerService();
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = subjectHandler.getUserIdFromToken();
        when(soknadMetadataRepository.hent("123")).thenReturn(metadata);
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndreMetadata() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = "other_user";
        when(soknadMetadataRepository.hent("123")).thenReturn(metadata);
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileHvisEierErNull() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        tilgangskontroll.verifiserTilgangMotPep(null, "");
    }

}
