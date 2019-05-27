package no.nav.sbl.dialogarena.sendsoknad.domain.saml;

public interface SamlSubjectHandlerService {
    String getUserIdFromToken();
    String getToken();
    String getConsumerId();
}
