package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollTest {

    @InjectMocks
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private SoknadService soknadService;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void skalGiTilgangForBruker() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.empty());
        when(soknadService.hentSoknad("123", false, false)).thenReturn(new WebSoknad().medAktorId(OidcFeatureToggleUtils.getUserId()));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndre() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.empty());
        when(soknadService.hentSoknad("XXX", false, false)).thenReturn(new WebSoknad().medAktorId("other_user"));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileOmSoknadenIkkeFinnes() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.empty());
        when(soknadService.hentSoknad("123", false, false)).thenReturn(null);
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test
    public void skalGiTilgangForBrukerMetadata() {
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = OidcFeatureToggleUtils.getUserId();
        when(soknadMetadataRepository.hent("123")).thenReturn(metadata);
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndreMetadata() {
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = "other_user";
        when(soknadMetadataRepository.hent("123")).thenReturn(metadata);
        tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileHvisEierErNull() {
        tilgangskontroll.verifiserTilgangMotPep(null);
    }

}
