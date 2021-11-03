package no.nav.sosialhjelp.soknad.business.service.digisosapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sosialhjelp.metrics.Event;
import no.nav.sosialhjelp.metrics.MetricsFactory;
import no.nav.sosialhjelp.soknad.business.InnsendingService;
import no.nav.sosialhjelp.soknad.business.SoknadUnderArbeidService;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadMetricsService;
import no.nav.sosialhjelp.soknad.business.util.FileDetectionUtils;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApi;
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilMetadata;
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.FilOpplasting;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidVedlegg;
import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static no.nav.sosialhjelp.soknad.business.util.MetricsUtils.navKontorTilInfluxNavn;
import static no.nav.sosialhjelp.soknad.business.util.SenderUtils.createPrefixedBehandlingsIdInNonProd;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DigisosApiService {

    private static final Logger log = getLogger(DigisosApiService.class);

    @Inject
    private DigisosApi digisosApi;

    @Inject
    private SosialhjelpPdfGenerator sosialhjelpPdfGenerator;

    @Inject
    private InnsendingService innsendingService;

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;

    @Inject
    private SoknadMetricsService soknadMetricsService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    private final ObjectMapper objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper();

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
            filOpplastinger.add(lagDokumentForBrukerkvitteringPdf());
            List<FilOpplasting> dokumenterForVedlegg = lagDokumentListeForVedlegg(soknadUnderArbeid);
            antallVedleggForsendelse = dokumenterForVedlegg.size();
            filOpplastinger.addAll(dokumenterForVedlegg);
        } else {
            filOpplastinger.add(lagDokumentForSaksbehandlerPdf(soknadUnderArbeid));
            filOpplastinger.add(lagDokumentForJuridiskPdf(internalSoknad));
            filOpplastinger.add(lagDokumentForBrukerkvitteringPdf());
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
                log.warn("Ulikt antall vedlegg i vedlegg.json og forsendelse til Fiks. vedlegg.json: {}, forsendelse til Fiks: {}. Er ettersendelse: {}", antallBrukerOpplastedeVedlegg, antallVedleggForsendelse, soknadUnderArbeid.erEttersendelse());
            }
        } catch (RuntimeException e) {
            log.debug("Ignored exception");
        }
        return filOpplastinger;

    }

    String sendOgKrypter(String soknadJson, String tilleggsinformasjonJson, String vedleggJson, List<FilOpplasting> filOpplastinger, String kommunenr, String navEnhetsnavn, String behandlingsId, String token) {
        Event event = lagForsoktSendtDigisosApiEvent(navEnhetsnavn);
        try {
            return digisosApi.krypterOgLastOppFiler(soknadJson, tilleggsinformasjonJson, vedleggJson, filOpplastinger, kommunenr, behandlingsId, token);
        } catch (Exception e) {
            event.setFailed();
            throw e;
        } finally {
            event.report();
        }
    }

    private Event lagForsoktSendtDigisosApiEvent(String navEnhetsnavn){
        Event event = MetricsFactory.createEvent("fiks.digisosapi.sendt");
        event.addTagToReport("mottaker", navKontorTilInfluxNavn(navEnhetsnavn));
        return event;
    }

    private FilOpplasting lagDokumentForSaksbehandlerPdf(SoknadUnderArbeid soknadUnderArbeid) {
        String filnavn = "Soknad.pdf";
        String mimetype = "application/pdf";

        byte[] soknadPdf = sosialhjelpPdfGenerator.generate(soknadUnderArbeid.getJsonInternalSoknad(), false);
        return opprettFilOpplastingFraByteArray(filnavn, mimetype, soknadPdf);
    }

    private List<FilOpplasting> lagDokumentListeForVedlegg(SoknadUnderArbeid soknadUnderArbeid) {
        List<OpplastetVedlegg> opplastedeVedlegg = innsendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid);
        return opplastedeVedlegg.stream()
                .map(this::opprettDokumentForVedlegg)
                .collect(Collectors.toList());
    }

    private FilOpplasting lagDokumentForEttersendelsePdf(JsonInternalSoknad internalSoknad, String eier) {
        String filnavn = "ettersendelse.pdf";
        String mimetype = "application/pdf";

        byte[] pdf = sosialhjelpPdfGenerator.generateEttersendelsePdf(internalSoknad, eier);
        return opprettFilOpplastingFraByteArray(filnavn, mimetype, pdf);
    }

    private FilOpplasting lagDokumentForBrukerkvitteringPdf() {
        String filnavn = "Brukerkvittering.pdf";
        String mimetype = "application/pdf";
        byte[] pdf = sosialhjelpPdfGenerator.generateBrukerkvitteringPdf();

        return opprettFilOpplastingFraByteArray(filnavn, mimetype, pdf);
    }

    private FilOpplasting lagDokumentForJuridiskPdf(JsonInternalSoknad internalSoknad) {
        String filnavn = "Soknad-juridisk.pdf";
        String mimetype = "application/pdf";

        byte[] pdf = sosialhjelpPdfGenerator.generate(internalSoknad, true);
        return opprettFilOpplastingFraByteArray(filnavn, mimetype, pdf);
    }

    private FilOpplasting opprettDokumentForVedlegg(OpplastetVedlegg opplastetVedlegg) {
        byte[] pdf = opplastetVedlegg.getData();

        return new FilOpplasting(new FilMetadata()
                .withFilnavn(opplastetVedlegg.getFilnavn())
                .withMimetype(FileDetectionUtils.getMimeTypeForSending(opplastetVedlegg.getData()))
                .withStorrelse((long) pdf.length),
                new ByteArrayInputStream(pdf));
    }

    private FilOpplasting opprettFilOpplastingFraByteArray(String filnavn, String mimetype, byte[] bytes) {
        return new FilOpplasting(new FilMetadata()
                .withFilnavn(filnavn)
                .withMimetype(mimetype)
                .withStorrelse((long) bytes.length), new ByteArrayInputStream(bytes)
        );
    }

    public String sendSoknad(SoknadUnderArbeid soknadUnderArbeid, String token, String kommunenummer) {
        String behandlingsId = soknadUnderArbeid.getBehandlingsId();
        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(soknadUnderArbeid);

        log.info("Starter innsending av søknad med behandlingsId {}, skal sendes til DigisosApi", behandlingsId);

        SoknadMetadata.VedleggMetadataListe vedlegg = convertToVedleggMetadataListe(soknadUnderArbeid);
        henvendelseService.oppdaterMetadataVedAvslutningAvSoknad(behandlingsId, vedlegg, soknadUnderArbeid, true);

        List<FilOpplasting> filOpplastinger = lagDokumentListe(soknadUnderArbeid);
        log.info("Laster opp {}", filOpplastinger.size());
        String soknadJson = getSoknadJson(soknadUnderArbeid);
        String tilleggsinformasjonJson = getTilleggsinformasjonJson(soknadUnderArbeid.getJsonInternalSoknad().getSoknad());
        String vedleggJson = getVedleggJson(soknadUnderArbeid);

        behandlingsId = createPrefixedBehandlingsIdInNonProd(behandlingsId);
        log.info("Starter kryptering av filer for {}, skal sende til kommune {} med enhetsnummer {} og navenhetsnavn {}", behandlingsId,  kommunenummer,
                soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().getEnhetsnummer(),
                soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().getNavEnhetsnavn());
        String digisosId = sendOgKrypter(soknadJson, tilleggsinformasjonJson, vedleggJson, filOpplastinger, kommunenummer, soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().getNavEnhetsnavn(), behandlingsId, token);

        slettSoknadUnderArbeidEtterSendingTilFiks(soknadUnderArbeid);

        soknadMetricsService.reportSendSoknadMetrics(SubjectHandler.getUserId(), soknadUnderArbeid, vedlegg.vedleggListe);
        return digisosId;
    }

    String getSoknadJson(SoknadUnderArbeid soknadUnderArbeid) {
        try {
            String sonadJson = objectMapper.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad().getSoknad());
            ensureValidSoknad(sonadJson);
            return sonadJson;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Klarer ikke serialisere sonadJson", e);
        }
    }

    String getTilleggsinformasjonJson(JsonSoknad soknad) {

        if (soknad == null || soknad.getMottaker() == null) {
            log.error("Soknad eller soknadsmottaker er null ved sending av søknad. Dette skal ikke skje.");
            throw new IllegalStateException("Soknad eller soknadsmottaker er null ved sending av søknad.");
        }

        String enhetsnummer = soknad.getMottaker().getEnhetsnummer();
        if (enhetsnummer == null) {
            log.error("Enhetsnummer er null ved sending av søknad. Den blir lagt til i tilleggsinformasjon-filen med <null> som verdi.");
        }
        JsonTilleggsinformasjon tilleggsinformasjonJson = new JsonTilleggsinformasjon().withEnhetsnummer(enhetsnummer);

        try {
            return objectMapper.writeValueAsString(tilleggsinformasjonJson);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Klarer ikke serialisere tilleggsinformasjonJson", e);
        }
    }

    String getVedleggJson(SoknadUnderArbeid soknadUnderArbeid) {
        try {
            String vedleggJson = objectMapper.writeValueAsString(soknadUnderArbeid.getJsonInternalSoknad().getVedlegg());
            ensureValidVedlegg(vedleggJson);
            return vedleggJson;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Klarer ikke serialisere vedleggJson", e);
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

    private void slettSoknadUnderArbeidEtterSendingTilFiks(SoknadUnderArbeid soknadUnderArbeid) {
        log.info("Sletter SoknadUnderArbeid, behandlingsid {}", soknadUnderArbeid.getBehandlingsId());
        soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, soknadUnderArbeid.getEier());
    }
}
