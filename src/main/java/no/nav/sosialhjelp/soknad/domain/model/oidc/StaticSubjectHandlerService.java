//package no.nav.sosialhjelp.soknad.domain.model.oidc;
//
//public class StaticSubjectHandlerService implements SubjectHandlerService {
//    private static final String DEFAULT_USER = "26104500284"; //Testbruker i idporten. Nathalie
//    private static final String DEFAULT_TOKEN = "token";
//    private String user = DEFAULT_USER;
//    private String fakeToken = DEFAULT_TOKEN;
//
//    public String getUserIdFromToken() {
//        return user;
//    }
//
//    public String getToken() {
//        return fakeToken; //JwtTokenGenerator.createSignedJWT(user).serialize();
//    }
//
//    public String getConsumerId() {
//        return "StaticConsumerId";
//    }
//
//    public void setUser(String user) {
//        this.user = user;
//    }
//
//    public void setFakeToken(String fakeToken) {
//        this.fakeToken = fakeToken;
//    }
//
//    public void reset() {
//        this.user = DEFAULT_USER;
//        this.fakeToken = DEFAULT_TOKEN;
//    }
//}