package no.nav.sbl.dialogarena.sts.client;

/* Originally from common-java-modules (no.nav.sbl.dialogarena.common.cxf) */

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sts.StsType;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.MemoryTokenStoreFactory;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.apache.cxf.ws.security.trust.STSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.nav.sbl.dialogarena.sts.StsType.SYSTEM_USER_IN_FSS;


public class NAVOidcSTSClient extends STSClient {
    private static final Logger logger = LoggerFactory.getLogger(NAVOidcSTSClient.class);
    private static TokenStore tokenStore;
    private final StsType stsType;

    public NAVOidcSTSClient(Bus bus, StsType stsType) {
        super(bus);
        this.stsType = stsType;
        if (stsType == StsType.ON_BEHALF_OF_WITH_JWT) {
            setOnBehalfOf(new OnBehalfOfWithOidcCallbackHandler());
        }
    }

    @Override
    protected boolean useSecondaryParameters() {
        return false;
    }

    @Override
    public SecurityToken requestSecurityToken(String appliesTo, String action, String requestType, String binaryExchange) throws Exception {
        ensureTokenStoreExists();

        final String userId = getUserId();
        final String key = getTokenStoreKey();

        SecurityToken token = tokenStore.getToken(key);
        if (token == null) {
            logger.debug("Missing token for user {}, fetching it from STS", userId);
            token = super.requestSecurityToken(appliesTo, action, requestType, binaryExchange);
            tokenStore.add(key, token);
        } else {
            logger.debug("Retrived token for user {} from tokenStore", userId);
        }
        return token;
    }

    private String getTokenStoreKey() {
        return stsType.name() + "-" + getUserKey();
    }

    private String getUserKey() {
        if (stsType == SYSTEM_USER_IN_FSS) {
            return "systemSAML";
        } else {
            String token = SubjectHandler.getToken();
            if (token == null) {
                throw new IllegalStateException("Finner ingen sso token som kan bli cache-nøkkel for brukerens SAML-token");
            }
            return token;
        }
    }

    private String getUserId() {
        return stsType == SYSTEM_USER_IN_FSS ? toString(getProperty(SecurityConstants.USERNAME)) : SubjectHandler.getUserIdFromToken();
    }

    public static String toString(Object o) {
        return o != null ? o.toString() : "";
    }

    private void ensureTokenStoreExists() {
        if (tokenStore == null) {
            createTokenStore();
        }
    }

    private synchronized void createTokenStore() {
        if (tokenStore == null) {
            logger.debug("Creating tokenStore");
            tokenStore = new MemoryTokenStoreFactory().newTokenStore(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE, message);
        }
    }
}