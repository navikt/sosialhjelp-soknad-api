package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

public class StaticSubjectHandlerService implements SubjectHandlerService {
    private final static String DEFAULT_USER = "23079403598"; //Testbruker i idporten.
    private final static String DEFAULT_TOKEN = "token";
    private String user = DEFAULT_USER;
    private String fakeToken = DEFAULT_TOKEN;

    public String getUserIdFromToken() {
        return user;
    }

    public String getToken() {
        return fakeToken; //JwtTokenGenerator.createSignedJWT(user).serialize();
    }

    public String getConsumerId() {
        return "StaticConsumerId";
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setFakeToken(String fakeToken) {
        this.fakeToken = fakeToken;
    }

    public void reset() {
        this.user = DEFAULT_USER;
        this.fakeToken = DEFAULT_TOKEN;
    }
}