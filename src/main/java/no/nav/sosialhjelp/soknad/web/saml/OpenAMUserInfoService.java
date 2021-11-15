package no.nav.sosialhjelp.soknad.web.saml;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestConfig;
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.domain.model.exception.SamlUnauthorizedException;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class OpenAMUserInfoService {
    public static final String PARAMETER_UID = "uid";
    public static final String PARAMETER_SECURITY_LEVEL = "SecurityLevel";

    public static final String BASE_PATH = "/identity/json/attributes";

    private final URI endpointURL;
    private final RestConfig restConfig = new RestConfig.Builder().withDisableParameterLogging(true).build();
    private final Client client = RestUtils.createClient(restConfig);

    public OpenAMUserInfoService() {
        this.endpointURL = resolveEndpointURL();
    }

    private static List<String> subjectAttributes() {
        List<String> attributes = new ArrayList<>();
        attributes.add(PARAMETER_UID);
        attributes.add(PARAMETER_SECURITY_LEVEL);
        return attributes;
    }

    private static URI resolveEndpointURL() {
        String endpoint = System.getProperty("openam.restUrl");
        URI uri = URI.create(endpoint);
        String scheme = uri.getScheme();
        if (isEmpty(scheme)) {
            throw new IllegalStateException(endpoint);
        }
        return uri;
    }

    public Subject convertTokenToSubject(String token) throws SamlUnauthorizedException {
        Map<String, String> attributeMap = getUserAttributes(token);
        return createSubject(attributeMap, token);
    }

    public Map<String, String> getUserAttributes(String token) throws SamlUnauthorizedException{
        Response userAttributesResponse = requestUserAttributes(token, subjectAttributes());
        OpenAMAttributes openAMAttributes = checkResponse(userAttributesResponse, token);
        return openAmAttributesToMap(openAMAttributes);
    }

    public Response requestUserAttributes(String token, List<String> attributes) {
        return client.target(getUrl(token, attributes)).request().get();
    }

    private OpenAMAttributes checkResponse(Response response, String sessionId) throws SamlUnauthorizedException {
        int status = response.getStatus();
        if (status < 399) {
            return response.readEntity(OpenAMAttributes.class);
        } else {
            String payload = response.readEntity(String.class);
            String phrase = response.getStatusInfo().getReasonPhrase();
            throw new SamlUnauthorizedException(String.format("Klarte ikke hente userAttributes fra openAM. Status: %d, phrase: %s, payload: %s", status, phrase, sanitize(payload, sessionId)));
        }
    }

    private String sanitize(String payload, String sessionId) {
        return payload.replaceAll(sessionId, "<session id removed>");
    }

    public String getUrl(String token, List<String> attributes) {
        UriBuilder uriBuilder = UriBuilder.fromUri(endpointURL).path(BASE_PATH).queryParam("subjectid", token);
        attributes.forEach(a -> uriBuilder.queryParam("attributenames", a));
        return uriBuilder.toString();
    }

    static Map<String, String> openAmAttributesToMap(OpenAMAttributes openAMAttributes) throws SamlUnauthorizedException{
        if (openAMAttributes == null || openAMAttributes.attributes == null) {
            throw new SamlUnauthorizedException("Saml-bruker ikke funnet. OpenAMAttributes var null og inneholdt derfor ingen userId");
        }

        return openAMAttributes.attributes.stream()
                .filter(a -> a.values != null && !a.values.isEmpty())
                .collect(toMap(
                        attribute -> attribute.name,
                        attribute -> attribute.values.get(0)
                ));
    }

    private Subject createSubject(Map<String, String> attributeMap, String token) throws SamlUnauthorizedException {
        if (attributeMap.containsKey(PARAMETER_UID)) {
            String uid = attributeMap.get(PARAMETER_UID);
            return new Subject(
                    uid,
                    IdentType.EksternBruker,
                    SsoToken.eksternOpenAM(token, attributeMap));
        }
        throw new SamlUnauthorizedException(String.format("OpenAm response manglet attributten %s. Får ikke hentet ut userId", PARAMETER_UID));
    }

    @SuppressWarnings("unused")
    public static class OpenAMAttributes {
        private List<OpenAMAttribute> attributes = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public static class OpenAMAttribute {
        private String name;
        private List<String> values = new ArrayList<>();
    }

}