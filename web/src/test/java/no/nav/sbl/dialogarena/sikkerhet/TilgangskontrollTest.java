package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.core.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollTest {

    @InjectMocks
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
    }

    @Test
    public void skalGiTilgangForBruker() {
        String userId = SubjectHandler.getUserIdFromToken();
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withEier(userId).withJsonInternalSoknad(createEmptyJsonInternalSoknad(userId));
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.of(soknadUnderArbeid));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndre() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad("other_user"));
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.of(soknadUnderArbeid));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileOmSoknadenIkkeFinnes() {
        when(soknadUnderArbeidRepository.hentSoknadOptional(anyString(), anyString())).thenReturn(Optional.empty());
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test
    public void skalGiTilgangForBrukerMetadata() {
        SoknadMetadata metadata = new SoknadMetadata();
        metadata.fnr = SubjectHandler.getUserIdFromToken();
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
        tilgangskontroll.verifiserTilgangMotPep(null, "");
    }

}
