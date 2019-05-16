package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.rest.ressurser.VedleggRessurs.MAKS_TOTAL_FILSTORRELSE;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VedleggRessursTest {

    public static final long VEDLEGGSID = 1;
    public static final String BEHANDLINGSID = "123";
    @Mock
    SoknadService soknadService;
    @Mock
    VedleggService vedleggService;

    @InjectMocks
    VedleggRessurs ressurs;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");

        Vedlegg vedlegg = new Vedlegg();
        vedlegg.setStorrelse(MAKS_TOTAL_FILSTORRELSE + 1L);

        when(vedleggService.hentVedlegg(VEDLEGGSID, false)).thenReturn(new Vedlegg());
        when(vedleggService.hentVedleggUnderBehandling(eq(BEHANDLINGSID), anyString())).thenReturn(singletonList(vedlegg));
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test(expected = OpplastingException.class)
    public void opplastingSkalKasteExceptionHvisVedleggeneErForStore() {
        ressurs.lastOppFiler(VEDLEGGSID, BEHANDLINGSID, Collections.<FormDataBodyPart>emptyList());
    }
}
