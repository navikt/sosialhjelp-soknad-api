package no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ks.svarut.servicesv9.Dokument;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DigisosApiService {

    private static final Logger log = getLogger(DigisosApiService.class);

    @Inject
    private IdPortenService idPortenService;

    @Inject
    private KrypteringService krypteringService;

    @Inject
    private PDFService pdfService;;

    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper();

    public List<KommuneInfo> hentKommuneInfo() {
        IdPortenService.AcceessToken accessToken = idPortenService.getAccessToken();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet http = new HttpGet("https://api.fiks.test.ks.no/digisos/api/v1/nav/kommune");
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", "fiksIntegrasjonId");
            http.setHeader("IntegrasjonPassord", "fiksIntegrasjon");
            http.setHeader("Authorization", "Bearer " + accessToken.token);

            CloseableHttpResponse response = client.execute(http);
            return Arrays.asList(objectMapper.readValue(EntityUtils.toString(response.getEntity()), KommuneInfo[].class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public KommuneInfo hentKommuneInfo(String kommunenr) {
        IdPortenService.AcceessToken accessToken = idPortenService.getAccessToken();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet http = new HttpGet("https://api.fiks.test.ks.no/digisos/api/v1/nav/kommune/" + kommunenr);
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", "fiksIntegrasjonId");
            http.setHeader("IntegrasjonPassord", "fiksIntegrasjon");
            http.setHeader("Authorization", "Bearer " + accessToken.token);

            CloseableHttpResponse response = client.execute(http);
            return objectMapper.readValue(EntityUtils.toString(response.getEntity()), KommuneInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendSoknad(SoknadUnderArbeid soknadUnderArbeid, String kommunenr, String navEkseternRefId) {
        ArrayList<FilOpplasting> filOpplastings = new ArrayList<>();


        filOpplastings.add(getFilopplastingForSoknad(soknadUnderArbeid));
        filOpplastings.add(getFilopplastingForVedlegg(soknadUnderArbeid));
        filOpplastings.add(getFilopplastingForSaksbehandlerPdf(soknadUnderArbeid));

        krypteringService.krypterOgLastOppFiler(Collections.emptyList(), kommunenr, navEkseternRefId);
    }

    FilOpplasting getFilopplastingForSaksbehandlerPdf(SoknadUnderArbeid soknadUnderArbeid) {
        byte[] soknadPdf = pdfService.genererSaksbehandlerPdf(soknadUnderArbeid.getJsonInternalSoknad(), "/");

        return new FilOpplasting(new FilMetadata()
                .withFilnavn("soknad.pdf")
                .withMimetype("application/pdf")
                .withStorrelse((long) soknadPdf.length),
                new ByteArrayInputStream(soknadPdf));
    }

    private FilOpplasting getFilopplastingForSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        try {
            String sonadJson = objectMapper.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad().getSoknad());
            ensureValidSoknad(sonadJson);
            byte[] bytes = sonadJson.getBytes(Charset.defaultCharset());
           return  new FilOpplasting(new FilMetadata()
                   .withFilnavn("soknad.json")
                   .withMimetype("application/json")
                   .withStorrelse((long) bytes.length),
                   new ByteArrayInputStream(bytes));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
    private FilOpplasting getFilopplastingForVedlegg(SoknadUnderArbeid soknadUnderArbeid) {
        try {
            String vedleggJson = objectMapper.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad().getVedlegg());
            ensureValidVedlegg(vedleggJson);
            byte[] bytes = vedleggJson.getBytes(Charset.defaultCharset());
           return  new FilOpplasting(new FilMetadata().withFilnavn("vedlegg.json").withMimetype("application/json").withStorrelse((long) bytes.length), new ByteArrayInputStream(bytes));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    List<FilOpplasting> lagDokumentListeForVedlegg(SoknadUnderArbeid soknadUnderArbeid) {
        final List<OpplastetVedlegg> opplastedeVedlegg = innsendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid);
        return opplastedeVedlegg.stream()
                .map(this::opprettDokumentForVedlegg)
                .collect(Collectors.toList());
    }

    public class KommuneInfo {
        public final String kommunenummer;
        public final Boolean kanMottaSoknader;
        public final Boolean kanOppdatereStatus;

        public KommuneInfo(String kommunenummer, Boolean kanMottaSoknader, Boolean kanOppdatereStatus) {
            this.kommunenummer = kommunenummer;
            this.kanMottaSoknader = kanMottaSoknader;
            this.kanOppdatereStatus = kanOppdatereStatus;
        }
    }
}
