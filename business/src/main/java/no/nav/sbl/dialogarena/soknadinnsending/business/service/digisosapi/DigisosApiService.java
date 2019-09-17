package no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.model.KommuneInfo;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.InnsendingService;
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
    private PDFService pdfService;

    @Inject
    private InnsendingService innSendingService;

    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper();

    public List<KommuneInfo> hentKommuneInfo() {
        IdPortenService.IdPortenAccessTokenResponse accessToken = idPortenService.getAccessToken();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet http = new HttpGet("https://api.fiks.test.ks.no/digisos/api/v1/nav/kommune");
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", System.getenv("INTEGRASJONSID_FIKS"));
            http.setHeader("IntegrasjonPassord", System.getenv("INTEGRASJONPASSORD_FIKS"));
            http.setHeader("Authorization", "Bearer " + accessToken.accessToken);

            CloseableHttpResponse response = client.execute(http);
            String content = EntityUtils.toString(response.getEntity());
            return Arrays.asList(objectMapper.readValue(content, KommuneInfo[].class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public KommuneInfo hentKommuneInfo(String kommunenr) {
        IdPortenService.IdPortenAccessTokenResponse accessToken = idPortenService.getAccessToken();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet http = new HttpGet("https://api.fiks.test.ks.no/digisos/api/v1/nav/kommune/" + kommunenr);
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", "fiksIntegrasjonId");
            http.setHeader("IntegrasjonPassord", "fiksIntegrasjon");
            http.setHeader("Authorization", "Bearer " + accessToken.accessToken);

            CloseableHttpResponse response = client.execute(http);
            return objectMapper.readValue(EntityUtils.toString(response.getEntity()), KommuneInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<FilOpplasting> lagDokumentListe(SoknadUnderArbeid soknadUnderArbeid) {
        JsonInternalSoknad internalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
        if (internalSoknad == null) {
            throw new RuntimeException("Kan ikke sende forsendelse til FIKS fordi søknad mangler");
        } else if (!soknadUnderArbeid.erEttersendelse() && internalSoknad.getSoknad() == null) {
            throw new RuntimeException("Kan ikke sende søknad fordi søknaden mangler");
        } else if (soknadUnderArbeid.erEttersendelse() && internalSoknad.getVedlegg() == null) {
            throw new RuntimeException("Kan ikke sende ettersendelse fordi vedlegg mangler");
        }
        int antallVedleggForsendelse;

        List<FilOpplasting> filOpplastinger = new ArrayList<>();

        if (soknadUnderArbeid.erEttersendelse()) {
            filOpplastinger.add(lagDokumentForEttersendelsePdf(internalSoknad, soknadUnderArbeid.getEier()));
            filOpplastinger.add(lagDokumentForVedleggJson(soknadUnderArbeid));
            filOpplastinger.add(lagDokumentForBrukerkvitteringPdf(internalSoknad, true, soknadUnderArbeid.getEier()));
            List<FilOpplasting> dokumenterForVedlegg = lagDokumentListeForVedlegg(soknadUnderArbeid);
            antallVedleggForsendelse = dokumenterForVedlegg.size();
            filOpplastinger.addAll(dokumenterForVedlegg);
        } else {
            filOpplastinger.add(lagDokumentForSoknad(soknadUnderArbeid));
            filOpplastinger.add(lagDokumentForSaksbehandlerPdf(soknadUnderArbeid));
            filOpplastinger.add(lagDokumentForVedleggJson(soknadUnderArbeid));
            filOpplastinger.add(lagDokumentForJuridiskPdf(internalSoknad));
            filOpplastinger.add(lagDokumentForBrukerkvitteringPdf(internalSoknad, false, soknadUnderArbeid.getEier()));
            List<FilOpplasting> dokumenterForVedlegg = lagDokumentListeForVedlegg(soknadUnderArbeid);
            antallVedleggForsendelse = dokumenterForVedlegg.size();
            filOpplastinger.addAll(dokumenterForVedlegg);
        }
        int antallFiksDokumenter = filOpplastinger.size();
        log.info("Antall vedlegg: {}. Antall vedlegg lastet opp av bruker: {}", antallFiksDokumenter, antallVedleggForsendelse);

        try {
            List<JsonVedlegg> opplastedeVedleggstyper = internalSoknad.getVedlegg().getVedlegg().stream().filter(jsonVedlegg -> jsonVedlegg.getStatus().equals("LastetOpp"))
                    .collect(Collectors.toList());
            int antallBrukerOpplastedeVedlegg = 0;
            for (JsonVedlegg vedlegg : opplastedeVedleggstyper){
                antallBrukerOpplastedeVedlegg += vedlegg.getFiler().size();
            }
            if (antallVedleggForsendelse != antallBrukerOpplastedeVedlegg) {
                log.error("Ulikt antall vedlegg i vedlegg.json og forsendelse til Fiks. vedlegg.json: {}, forsendelse til Fiks: {}. Er ettersendelse: {}", antallBrukerOpplastedeVedlegg, antallVedleggForsendelse, soknadUnderArbeid.erEttersendelse());
            }
        } catch (RuntimeException e) {
            log.debug("Ignored exception");
        }
        return filOpplastinger;

    }
    public void sendOgKrypter(List<FilOpplasting> filOpplastinger, String kommunenr, String navEkseternRefId){
        krypteringService.krypterOgLastOppFiler(filOpplastinger, kommunenr, navEkseternRefId);
    }

    FilOpplasting lagDokumentForSaksbehandlerPdf(SoknadUnderArbeid soknadUnderArbeid) {
        byte[] soknadPdf = pdfService.genererSaksbehandlerPdf(soknadUnderArbeid.getJsonInternalSoknad(), "/");

        return new FilOpplasting(new FilMetadata()
                .withFilnavn("soknad.pdf")
                .withMimetype("application/pdf")
                .withStorrelse((long) soknadPdf.length),
                new ByteArrayInputStream(soknadPdf));
    }

    private FilOpplasting lagDokumentForSoknad(SoknadUnderArbeid soknadUnderArbeid) {
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
    private FilOpplasting lagDokumentForVedleggJson(SoknadUnderArbeid soknadUnderArbeid) {
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
        List<OpplastetVedlegg> opplastedeVedlegg = innSendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid);
        return opplastedeVedlegg.stream()
                .map(this::opprettDokumentForVedlegg)
                .collect(Collectors.toList());
    }

    FilOpplasting lagDokumentForEttersendelsePdf(JsonInternalSoknad internalSoknad, String eier) {
        byte[] pdf = pdfService.genererEttersendelsePdf(internalSoknad, "/", eier);

        return new FilOpplasting(new FilMetadata()
                .withFilnavn("ettersendelse.pdf")
                .withMimetype("application/pdf")
                .withStorrelse((long) pdf.length),
                new ByteArrayInputStream(pdf));
    }

    FilOpplasting lagDokumentForBrukerkvitteringPdf(JsonInternalSoknad internalSoknad, boolean erEttersendelse, String eier) {
        byte[] pdf = pdfService.genererBrukerkvitteringPdf(internalSoknad, "/",erEttersendelse, eier);

        return new FilOpplasting(new FilMetadata()
                .withFilnavn("Brukerkvittering.pdf")
                .withMimetype("application/pdf")
                .withStorrelse((long) pdf.length),
                new ByteArrayInputStream(pdf));
    }

    FilOpplasting lagDokumentForJuridiskPdf(JsonInternalSoknad internalSoknad) {
        byte[] pdf = pdfService.genererJuridiskPdf(internalSoknad, "/");

        return new FilOpplasting(new FilMetadata()
                .withFilnavn("Soknad-juridisk.pdf")
                .withMimetype("application/pdf")
                .withStorrelse((long) pdf.length),
                new ByteArrayInputStream(pdf));
    }

    FilOpplasting opprettDokumentForVedlegg(OpplastetVedlegg opplastetVedlegg) {
        byte[] pdf = opplastetVedlegg.getData();

        return new FilOpplasting(new FilMetadata()
                .withFilnavn(opplastetVedlegg.getFilnavn())
                .withMimetype(Detect.CONTENT_TYPE.transform(opplastetVedlegg.getData()))
                .withStorrelse((long) pdf.length),
                new ByteArrayInputStream(pdf));
    }
}
