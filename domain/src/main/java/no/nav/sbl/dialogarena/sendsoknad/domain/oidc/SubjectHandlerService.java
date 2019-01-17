package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

public interface SubjectHandlerService {
    String getUserIdFromToken();
    String getToken();
    String getConsumerId();
}
