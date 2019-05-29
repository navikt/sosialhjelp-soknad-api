package no.nav.sbl.dialogarena.sendsoknad.domain.saml.domain;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import no.nav.modig.security.loginmodule.http.HttpClientConfig;
import no.nav.modig.security.loginmodule.userinfo.AbstractUserInfoService;
import no.nav.modig.security.loginmodule.userinfo.UserInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.UnsupportedSchemeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenAMUserInfoService extends AbstractUserInfoService {
    private static final Logger log = LoggerFactory.getLogger(OpenAMUserInfoService.class);
    private static final String OPENAM_GENERAL_ERROR = "Could not get user attributes from OpenAM. ";
    private static final String BASE_PATH = "/identity/json/attributes";
    private HttpClientConfig httpClientConfig;
    protected HttpClient client;

    public OpenAMUserInfoService(URI endpointURL) throws UnsupportedSchemeException {
        this(endpointURL, (File)null, (String)null);
    }

    public OpenAMUserInfoService(URI endpointURL, File trustStoreFile, String trustStorePassword) throws UnsupportedSchemeException {
        this(new HttpClientConfig(endpointURL, trustStoreFile, trustStorePassword));
    }

    public OpenAMUserInfoService(HttpClientConfig httpClientConfig) throws UnsupportedSchemeException {
        this.httpClientConfig = httpClientConfig;

        try {
            this.client = httpClientConfig.createHttpClient();
        } catch (GeneralSecurityException var3) {
            throw new RuntimeException(var3);
        }
    }

    public HttpClientConfig getHttpClientConfig() {
        return this.httpClientConfig;
    }

    public UserInfo getUserInfo(String subjectId) {
        String response = this.invokeRestClient(subjectId);
        return this.createUserInfo(response);
    }

    protected String invokeRestClient(String subjectId) {
        log.debug("Invoking OpenAM REST interface.");
        String url = this.httpClientConfig.getEndpoint() + "/identity/json/attributes" + String.format("?subjectid=%s&attributenames=%s&attributenames=%s", subjectId, "uid", "SecurityLevel");
        HttpGet request = new HttpGet(url);

        String message;
        try {
            HttpResponse response = this.client.execute(request);
            int status = response.getStatusLine().getStatusCode();
            String phrase = response.getStatusLine().getReasonPhrase();
            String payload = this.getPayloadAsString(response);
            if (status >= 399) {
                message = "Could not get user attributes from OpenAM. HTTP status: " + status + " " + phrase + ".";
                if (status == 401) {
                    message = message + " Response:" + payload;
                }

                log.debug(message);
                throw new OpenAMException(message);
            }

            log.debug("Received response: " + payload);
            message = payload;
        } catch (IOException var12) {
            throw new RuntimeException(var12);
        } finally {
            request.releaseConnection();
        }

        return message;
    }

    private String getPayloadAsString(HttpResponse response) {
        try {
            InputStream inputStream = response.getEntity().getContent();
            return IOUtils.toString(inputStream);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    protected UserInfo createUserInfo(String response) {
        Map<String, String> attributeMap = this.parseUserAttributes(response);
        if (attributeMap.containsKey("uid") && attributeMap.containsKey("SecurityLevel")) {
            return new UserInfo((String)attributeMap.get("uid"), Integer.valueOf((String)attributeMap.get("SecurityLevel")));
        } else {
            throw new OpenAMException("Could not get user attributes from OpenAM. Response did not contain attributes uid and/or SecurityLevel");
        }
    }

    protected Map<String, String> parseUserAttributes(String response) {
        try {
            JSONObject json = new JSONObject(response);
            Object o = json.get("attributes");
            Map<String, String> attributeMap = new HashMap();
            JSONArray array = (JSONArray)o;

            for(int i = 0; i < array.length(); ++i) {
                JSONObject obj = (JSONObject)array.get(i);
                String name = obj.getString("name");
                JSONArray values = (JSONArray)obj.get("values");
                String value = values.getString(0);
                attributeMap.put(name, value);
            }

            return attributeMap;
        } catch (JSONException var11) {
            throw new OpenAMException("Error parsing JSON response. ", var11);
        }
    }

    public String toString() {
        return this.getClass() + "[" + this.httpClientConfig + "]";
    }
}
