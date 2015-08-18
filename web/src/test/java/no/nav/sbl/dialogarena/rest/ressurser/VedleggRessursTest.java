package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.rest.ressurser.VedleggRessurs.MAKS_TOTAL_FILSTORRELSE;
import static no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator.generateXsrfToken;
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
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());

        Vedlegg vedlegg = new Vedlegg();
        vedlegg.setStorrelse(MAKS_TOTAL_FILSTORRELSE + 1L);

        when(vedleggService.hentVedlegg(VEDLEGGSID, false)).thenReturn(new Vedlegg());
        when(vedleggService.hentVedleggUnderBehandling(eq(BEHANDLINGSID), anyString())).thenReturn(singletonList(vedlegg));
    }

    @Test(expected = OpplastingException.class)
    public void opplastingSkalKasteExceptionHvisVedleggeneErForStore() {
        ressurs.lastOppFiler(VEDLEGGSID, BEHANDLINGSID, generateXsrfToken(BEHANDLINGSID), Collections.<FormDataBodyPart>emptyList());
    }
}
