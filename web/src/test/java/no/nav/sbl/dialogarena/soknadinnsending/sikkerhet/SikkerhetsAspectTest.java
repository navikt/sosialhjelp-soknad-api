package no.nav.sbl.dialogarena.soknadinnsending.sikkerhet;


import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.exception.AuthorizationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SikkerhetsAspectTest {
    public static final SjekkTilgangTilSoknad TILGANG = new SjekkTilgangTilSoknad() {
        @Override
        public boolean sjekkXsrf() {
            return true;
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return null;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return SjekkTilgangTilSoknad.class;
        }
    };
    @Mock
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private SendSoknadService soknadService;

    @InjectMocks
    private SikkerhetsAspect sikkerhetsAspect;

    private String brukerBehandlingsId = "1";

    @Before
    public void init() {
        when(soknadService.hentSoknad(anyLong())).thenReturn(new WebSoknad().medBehandlingId(brukerBehandlingsId));
    }

    @Test
    public void skalTesteSikkerhet() {
        setup(XsrfGenerator.generateXsrfToken(brukerBehandlingsId));
        sikkerhetsAspect.sjekkSoknadIdModBruker(1L, TILGANG);
    }

    @Test(expected = AuthorizationException.class)
    public void skalKasteExceptionNaarTokenIkkeStemmer() {
        setup("tull");
        sikkerhetsAspect.sjekkSoknadIdModBruker(1L, TILGANG);
    }

    private void setup(String token) {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        MockHttpServletRequest request = new MockHttpServletRequest(null, null, null);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.setMethod("POST");
        request.setHeader("X-XSRF-TOKEN", token);
    }
}
