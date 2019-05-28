package no.nav.sbl.dialogarena.rest.ressurser.informasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AktivitetService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.MaalgrupperService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TjenesterRessursTest {

    String fodselsnummer;
    @InjectMocks
    private TjenesterRessurs ressurs;

    @Mock
    private AktivitetService aktivitetService;

    @Mock
    private MaalgrupperService maalgrupperService;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
        fodselsnummer = OidcFeatureToggleUtils.getUserId();
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void skalHenteAktiviteter() {
        ressurs.hentAktiviteter();
        verify(aktivitetService).hentAktiviteter(fodselsnummer);
    }

    @Test
    public void skalHenteMaalgrupper() {
        ressurs.hentMaalgrupper();
        verify(maalgrupperService).hentMaalgrupper(fodselsnummer);
    }
}