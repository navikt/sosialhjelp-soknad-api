package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlSubjectHandler.SUBJECTHANDLER_KEY;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollUtenOidcTest {

    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @InjectMocks
    private Tilgangskontroll tilgangskontroll = spy(new Tilgangskontroll());

    @InjectMocks
    private TilgangskontrollTest tilgangskontrollTest;

    @Before
    public void setUp() {
        System.setProperty(SUBJECTHANDLER_KEY, SamlStaticSubjectHandler.class.getName());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @After
    public void tearDown() {
        System.clearProperty(SUBJECTHANDLER_KEY);
    }

    @Test
    public void skalGiTilgangForBruker() {
        tilgangskontrollTest.skalGiTilgangForBruker();
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndre() {
        tilgangskontrollTest.skalFeileForAndre();
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileOmSoknadenIkkeFinnes() {
        tilgangskontrollTest.skalFeileOmSoknadenIkkeFinnes();
    }

    @Test
    public void skalGiTilgangForBrukerMetadata() {
        tilgangskontrollTest.skalGiTilgangForBrukerMetadata();
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndreMetadata() {
        tilgangskontrollTest.skalFeileForAndreMetadata();
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileHvisEierErNull() {
        tilgangskontrollTest.skalFeileHvisEierErNull();
    }

}
