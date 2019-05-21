package no.nav.sbl.dialogarena.mock;

import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

public class MockSubjectHandlerService implements SubjectHandlerService {

    public String getUserIdFromToken() {
        if (!MockUtils.isTillatMockRessurs()) {
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

    public String getToken() {
        return getUserIdFromToken();
    }

    public String getConsumerId() {
        return "MockedConsumerId";
    }
}
