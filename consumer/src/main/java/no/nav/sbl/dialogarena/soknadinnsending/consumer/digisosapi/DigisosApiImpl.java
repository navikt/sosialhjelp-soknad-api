package no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

import static no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils.isTillatMockRessurs;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.KommuneStatus.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DigisosApiImpl implements DigisosApi {

    private static final Logger log = getLogger(DigisosApiImpl.class);
    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper();


    @Override
    public void ping() {
        KommuneInfo kommuneInfo = hentKommuneInfo("0301");
        if (kommuneInfo.getKanMottaSoknader() == null) {
            throw new IllegalStateException("Fikk ikke kontakt med digisosapi");
        }
    }

    @Inject
    private IdPortenService idPortenService;

    // Det holder å sjekke om kommunen har en konfigurasjon hos fiks, har de det vil vi alltid kunne sende
    @Override
    public KommuneStatus kommuneInfo(String kommunenummer) {
        KommuneInfo kommuneInfo = hentKommuneInfo(kommunenummer);

        if (kommuneInfo.getKanMottaSoknader() == null) {
            return IKKE_PA_FIKS_ELLER_INNSYN;
        }

        if (!kommuneInfo.getKanMottaSoknader() && !kommuneInfo.getKanOppdatereStatus()) {
            return IKKE_PA_FIKS_ELLER_INNSYN;
        }
        if (kommuneInfo.getKanMottaSoknader() && !kommuneInfo.getKanOppdatereStatus()) {
            return KUN_PA_FIKS;
        }
        if (kommuneInfo.getKanMottaSoknader() && kommuneInfo.getKanOppdatereStatus()) {
            return PA_FIKS_OG_INNSYN;
        }
        return IKKE_PA_FIKS_ELLER_INNSYN;
    }

    private KommuneInfo hentKommuneInfo(String kommunenummer) {
        if (isTillatMockRessurs()) {
            return new KommuneInfo();
        }
        IdPortenService.IdPortenAccessTokenResponse accessToken = idPortenService.getVirksertAccessToken();
        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpGet http = new HttpGet(System.getProperty("digisos_api_baseurl") + "digisos/api/v1/nav/kommune/" + kommunenummer);
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", System.getProperty("integrasjonsid_fiks"));
            String integrasjonpassord_fiks = System.getProperty("integrasjonpassord_fiks");
            Objects.requireNonNull(integrasjonpassord_fiks, "integrasjonpassord_fiks");
            http.setHeader("IntegrasjonPassord", integrasjonpassord_fiks);
            http.setHeader("Authorization", "Bearer " + accessToken.accessToken);

            CloseableHttpResponse response = client.execute(http);
            String content = EntityUtils.toString(response.getEntity());
            log.info(content);
            return objectMapper.readValue(content, KommuneInfo.class);
        } catch (Exception e) {
            log.error("Hent kommuneinfo feiler", e);
            return new KommuneInfo();
        }
    }

}
