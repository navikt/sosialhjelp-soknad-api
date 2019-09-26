package no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.sendsoknad.domain.PersonAlder;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.model.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadMetricsService;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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

import static no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils.isTillatMockRessurs;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.model.KommuneStatus.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
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

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private OppgaveHandterer oppgaveHandterer;

    @Inject
    private SoknadMetricsService soknadMetricsService;

    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper();

    public List<KommuneInfo> hentKommuneInfo() {
        IdPortenService.IdPortenAccessTokenResponse accessToken = idPortenService.getVirksertAccessToken();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet http = new HttpGet(System.getProperty("digisos_api_baseurl") + "digisos/api/v1/nav/kommune");
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", System.getProperty("integrasjonsid_fiks"));
            http.setHeader("IntegrasjonPassord", System.getProperty("integrasjonpassord_fiks"));
            http.setHeader("Authorization", "Bearer " + accessToken.accessToken);
            System.out.println(http);
            for (Header allHeader : http.getAllHeaders()) {
                System.out.println(allHeader);
            }
            CloseableHttpResponse response = client.execute(http);
            String content = EntityUtils.toString(response.getEntity());
            System.out.println(content);
            return Arrays.asList(objectMapper.readValue(content, KommuneInfo[].class));
        } catch (IOException e) {
            log.error("Hent kommuneinfo feiler",e);
            return Collections.emptyList();
        }
    }

    KommuneInfo hentKommuneInfo(String kommunenummer) {

        if (isTillatMockRessurs()) {
            return new KommuneInfo();
        }
        IdPortenService.IdPortenAccessTokenResponse accessToken = idPortenService.getVirksertAccessToken();
        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpGet http = new HttpGet(System.getProperty("digisos_api_baseurl") + "digisos/api/v1/nav/kommune/" + kommunenummer);
            http.setHeader("Accept", MediaType.APPLICATION_JSON);
            http.setHeader("IntegrasjonId", System.getProperty("integrasjonsid_fiks"));
            http.setHeader("IntegrasjonPassord", System.getProperty("integrasjonpassord_fiks"));
            http.setHeader("Authorization", "Bearer " + accessToken.accessToken);

            CloseableHttpResponse response = client.execute(http);
            return objectMapper.readValue(EntityUtils.toString(response.getEntity()), KommuneInfo.class);
        } catch (IOException e) {
            log.error("Hent kommuneinfo feiler",e);
            return new KommuneInfo();
        }
    }

    // Det holder å sjekke om kommunen har en konfigurasjon hos fiks, har de det vil vi alltid kunne sende
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
        return null;
    }



    List<FilOpplasting> lagDokumentListe(SoknadUnderArbeid soknadUnderArbeid) {
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
            for (JsonVedlegg vedlegg : opplastedeVedleggstyper) {
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

    private void sendOgKrypter(List<FilOpplasting> filOpplastinger, String kommunenr, String navEkseternRefId, String token) {
        for (DokumentInfo dokumentInfo : krypteringService.krypterOgLastOppFiler(filOpplastinger, kommunenr, navEkseternRefId, token)) {
            log.info(String.format("Filnavn %s id %s stoerrelse %d laster opp", dokumentInfo.filnavn, dokumentInfo.dokumentlagerDokumentId.toString(), dokumentInfo.storrelse));
        }
    }

    private FilOpplasting lagDokumentForSaksbehandlerPdf(SoknadUnderArbeid soknadUnderArbeid) {
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
            return new FilOpplasting(new FilMetadata()
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
            return new FilOpplasting(new FilMetadata().withFilnavn("vedlegg.json").withMimetype("application/json").withStorrelse((long) bytes.length), new ByteArrayInputStream(bytes));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<FilOpplasting> lagDokumentListeForVedlegg(SoknadUnderArbeid soknadUnderArbeid) {
        List<OpplastetVedlegg> opplastedeVedlegg = innSendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid);
        return opplastedeVedlegg.stream()
                .map(this::opprettDokumentForVedlegg)
                .collect(Collectors.toList());
    }

    private FilOpplasting lagDokumentForEttersendelsePdf(JsonInternalSoknad internalSoknad, String eier) {
        byte[] pdf = pdfService.genererEttersendelsePdf(internalSoknad, "/", eier);

        return new FilOpplasting(new FilMetadata()
                .withFilnavn("ettersendelse.pdf")
                .withMimetype("application/pdf")
                .withStorrelse((long) pdf.length),
                new ByteArrayInputStream(pdf));
    }

    private FilOpplasting lagDokumentForBrukerkvitteringPdf(JsonInternalSoknad internalSoknad, boolean erEttersendelse, String eier) {
        byte[] pdf = pdfService.genererBrukerkvitteringPdf(internalSoknad, "/", erEttersendelse, eier);

        return new FilOpplasting(new FilMetadata()
                .withFilnavn("Brukerkvittering.pdf")
                .withMimetype("application/pdf")
                .withStorrelse((long) pdf.length),
                new ByteArrayInputStream(pdf));
    }

    private FilOpplasting lagDokumentForJuridiskPdf(JsonInternalSoknad internalSoknad) {
        byte[] pdf = pdfService.genererJuridiskPdf(internalSoknad, "/");

        return new FilOpplasting(new FilMetadata()
                .withFilnavn("Soknad-juridisk.pdf")
                .withMimetype("application/pdf")
                .withStorrelse((long) pdf.length),
                new ByteArrayInputStream(pdf));
    }

    private FilOpplasting opprettDokumentForVedlegg(OpplastetVedlegg opplastetVedlegg) {
        byte[] pdf = opplastetVedlegg.getData();

        return new FilOpplasting(new FilMetadata()
                .withFilnavn(opplastetVedlegg.getFilnavn())
                .withMimetype(Detect.CONTENT_TYPE.transform(opplastetVedlegg.getData()))
                .withStorrelse((long) pdf.length),
                new ByteArrayInputStream(pdf));
    }

    public void sendSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        if (MockUtils.isTillatMockRessurs()) {
            return;
        }

        String behandlingsId = soknadUnderArbeid.getBehandlingsId();
        if (soknadUnderArbeid.erEttersendelse() && getVedleggFromInternalSoknad(soknadUnderArbeid).isEmpty()) {
            log.error(String.format("Kan ikke sende inn ettersendingen med ID %s uten å ha lastet opp vedlegg", behandlingsId));
            throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }
        log.info(String.format("Starter innsending av søknad med behandlingsId %s, skal sendes til DigisosApi", behandlingsId));


        SoknadMetadata.VedleggMetadataListe vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid);
        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid);

        List<FilOpplasting> filOpplastinger = lagDokumentListe(soknadUnderArbeid);
        log.info(String.format("Laster opp %d", filOpplastinger.size()));
        sendOgKrypter(filOpplastinger, soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().getKommunenummer(), behandlingsId, "token");

        soknadMetricsService.sendtSoknad(soknadUnderArbeid.erEttersendelse());
        if (!soknadUnderArbeid.erEttersendelse() && !isTillatMockRessurs()) {
            logAlderTilKibana(OidcFeatureToggleUtils.getUserId());
        }
    }


    private SoknadMetadata.VedleggMetadataListe convertToVedleggMetadataListe(SoknadUnderArbeid soknadUnderArbeid) {
        SoknadMetadata.VedleggMetadataListe vedlegg = new SoknadMetadata.VedleggMetadataListe();

        vedlegg.vedleggListe = getVedleggFromInternalSoknad(soknadUnderArbeid).stream().map(jsonVedlegg -> {
            SoknadMetadata.VedleggMetadata m = new SoknadMetadata.VedleggMetadata();
            m.skjema = jsonVedlegg.getType();
            m.tillegg = jsonVedlegg.getTilleggsinfo();
            m.filnavn = jsonVedlegg.getType();
            m.status = Vedleggstatus.valueOf(jsonVedlegg.getStatus());
            return m;
        }).collect(Collectors.toList());

        return vedlegg;
    }

    private static SoknadMetadata.VedleggMetadata mapJsonVedleggToVedleggMetadata(JsonVedlegg jsonVedlegg) {
        SoknadMetadata.VedleggMetadata m = new SoknadMetadata.VedleggMetadata();
        m.skjema = jsonVedlegg.getType();
        m.tillegg = jsonVedlegg.getTilleggsinfo();
        m.filnavn = jsonVedlegg.getType();
        m.status = Vedleggstatus.valueOf(jsonVedlegg.getStatus());
        return m;
    }

    private static void logAlderTilKibana(String eier) {
        int age = new PersonAlder(eier).getAlder();
        if (age > 0 && age < 30) {
            log.info("DIGISOS-1164: UNDER30 - Soknad sent av bruker med alder: " + age);
        } else {
            log.info("DIGISOS-1164: OVER30 - Soknad sent av bruker med alder:" + age);
        }
    }
}
