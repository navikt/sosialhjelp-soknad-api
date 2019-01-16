package no.nav.sbl.dialogarena.mock;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

public class MockSubjectHandler extends OidcSubjectHandler {

    @Override
    public String getUserIdFromToken() {
        if (!TjenesteMockRessurs.isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        try {
            final ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            final HttpSession session = attr.getRequest().getSession(true);

            return (String) session.getAttribute("mockRessursUid");
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public String getToken() {
        return getUserIdFromToken();
    }
}
