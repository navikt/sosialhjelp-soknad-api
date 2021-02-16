package no.nav.sbl.dialogarena.mock;

import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandlerService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

public class MockSubjectHandlerService implements SubjectHandlerService {

    public String getUserIdFromToken() {
        if (!MockUtils.isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);

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
