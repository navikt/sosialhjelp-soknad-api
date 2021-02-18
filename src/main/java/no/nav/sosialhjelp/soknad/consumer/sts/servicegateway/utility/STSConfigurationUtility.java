package no.nav.sosialhjelp.soknad.consumer.sts.servicegateway.utility;

/* Originally from common-java-modules (no.nav.sbl.dialogarena.common.cxf) */

import no.nav.sosialhjelp.soknad.consumer.sts.servicegateway.StsSecurityConstants;
import no.nav.sosialhjelp.soknad.consumer.sts.servicegateway.StsType;
import no.nav.sosialhjelp.soknad.consumer.sts.servicegateway.client.NAVOidcSTSClient;
import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.policy.EndpointPolicy;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver;
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.apache.neethi.Policy;

import javax.xml.namespace.QName;
import java.util.HashMap;

import static no.nav.sosialhjelp.soknad.consumer.sts.servicegateway.StsType.ON_BEHALF_OF_WITH_JWT;
import static no.nav.sosialhjelp.soknad.consumer.sts.servicegateway.StsType.SYSTEM_USER_IN_FSS;


/**
 * A collection of configuration methods to configure an CXF WS-client
 * to use STS to retrieve SAML tokens for end user and system user.
 */
public class STSConfigurationUtility {

    /**
     * Configures endpoint to get SAML token for the system user from STS.
     * The SAML token will be added as a SupportingToken to the WS-Security headers.
     * <p/>
     * 1. Binds a WS-SecurityPolicy to the endpoint/client.
     * The policy requires a SupportingToken of type IssuedToken.
     * <p/>
     * 2. Configures the location and credentials of the STS.
     *
     * @param client CXF client
     */

    public static void configureStsForSystemUserInFSS(Client client) {
        configureStsClient(client, SYSTEM_USER_IN_FSS);
        setEndpointPolicyReference(client, "classpath:stspolicy.xml");
    }

    /**
     * Configures endpoint to get SAML token for the end user from STS in exchange for JWT token (OIDC).
     * The SAML token will be added as a SupportingToken to the WS-Security headers.
     * <p/>
     * 1. Binds a WS-SecurityPolicy to the endpoint/client.
     * The policy requires a SupportingToken of type IssuedToken.
     * <p/>
     * 2. Configures the location and credentials of the STS.
     *
     * @param client CXF client
     */

    public static void configureStsForOnBehalfOfWithJWT(Client client) {
        configureStsClient(client, ON_BEHALF_OF_WITH_JWT);
        setEndpointPolicyReference(client, "classpath:JwtSTSPolicy.xml");
    }

    private static void configureStsClient(Client client, StsType stsType) {
        final String location = requireProperty(StsSecurityConstants.STS_URL_KEY);
        final String username = requireProperty(StsSecurityConstants.SYSTEMUSER_USERNAME);
        final String password = requireProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD);

        new WSAddressingFeature().initialize(client, client.getBus());

        STSClient stsClient = createBasicSTSClient(client.getBus(), location, username, password, stsType);
        client.getRequestContext().put(SecurityConstants.STS_CLIENT, stsClient);
        client.getRequestContext().put(SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT, stsType.allowCachingInEndpoint());
    }

    private static STSClient createBasicSTSClient(Bus bus, String location, String username, String password, StsType stsType) {
        STSClient stsClient = new NAVOidcSTSClient(bus, stsType);

        stsClient.setWsdlLocation("/wsdl/ws-trust-1.4-service.wsdl");
        stsClient.setServiceQName(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/wsdl", "SecurityTokenServiceProvider"));
        stsClient.setEndpointQName(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/wsdl", "SecurityTokenServiceSOAP"));
        stsClient.setEnableAppliesTo(false);
        stsClient.setAllowRenewing(false);

        try {
            // Endpoint must be set on clients request context
            // as the wrapping requestcontext is not available
            // when creating the client from WSDL (ref cxf-users mailinglist)
            stsClient.getClient().getRequestContext().put(Message.ENDPOINT_ADDRESS, location);
        } catch (BusException | EndpointException e) {
            throw new RuntimeException("Failed to set endpoint adress of STSClient", e);
        }

        stsClient.getOutInterceptors().add(new LoggingOutInterceptor());
        stsClient.getInInterceptors().add(new LoggingInInterceptor());


        HashMap<String, Object> properties = new HashMap<>();
        properties.put(SecurityConstants.USERNAME, username);
        properties.put(SecurityConstants.PASSWORD, password);
        stsClient.setProperties(properties);
        return stsClient;
    }

    private static void setEndpointPolicyReference(Client client, String uri) {
        Policy policy = resolvePolicyReference(client, uri);
        setClientEndpointPolicy(client, policy);
    }

    private static Policy resolvePolicyReference(Client client, String uri) {
        PolicyBuilder policyBuilder = client.getBus().getExtension(PolicyBuilder.class);
        ReferenceResolver resolver = new RemoteReferenceResolver("", policyBuilder);
        return resolver.resolveReference(uri);
    }

    private static void setClientEndpointPolicy(Client client, Policy policy) {
        Endpoint endpoint = client.getEndpoint();
        EndpointInfo endpointInfo = endpoint.getEndpointInfo();

        PolicyEngine policyEngine = client.getBus().getExtension(PolicyEngine.class);
        EndpointPolicy endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, client.getConduit(), null);
        policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, null));
    }

    private static String requireProperty(String key) {
        String property = System.getProperty(key);
        if (property == null) {
            throw new RuntimeException("Required property " + key + " not available.");
        }
        return property;
    }
}
