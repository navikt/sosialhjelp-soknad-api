package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.detect.pdf.PdfDetector;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.virusscan.VirusScanner;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static no.nav.sbl.dialogarena.soknadinnsending.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static no.nav.sbl.sosialhjelp.domain.Vedleggstatus.LastetOpp;
import static no.nav.sbl.sosialhjelp.domain.Vedleggstatus.VedleggKreves;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class OpplastetVedleggService {

    private static final Logger logger = getLogger(OpplastetVedleggService.class);

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private VirusScanner virusScanner;

    private static Map<String, String> MIME_TIL_EXT;

    @PostConstruct
    public void setUp() {
        MIME_TIL_EXT = new HashMap<>();
        MIME_TIL_EXT.put("application/pdf", ".pdf");
        MIME_TIL_EXT.put("image/png", ".png");
        MIME_TIL_EXT.put("image/jpeg", ".jpg");
    }

    public OpplastetVedlegg saveVedleggAndUpdateVedleggstatus(String behandlingsId, String vedleggstype, byte[] data, String filnavn) {
        String eier = OidcFeatureToggleUtils.getUserId();
        String sha512 = ServiceUtils.getSha512FromByteArray(data);
        String contentType = Detect.CONTENT_TYPE.transform(data);

        validerFil(data);
        virusScanner.scan(filnavn, data);

        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        Long soknadId = soknadUnderArbeid.getSoknadId();

        OpplastetVedlegg opplastetVedlegg = new OpplastetVedlegg()
                .withEier(eier)
                .withVedleggType(new VedleggType(vedleggstype))
                .withData(data)
                .withSoknadId(soknadId)
                .withSha512(sha512);

        filnavn = lagFilnavn(filnavn, contentType, opplastetVedlegg.getUuid());

        opplastetVedlegg.withFilnavn(filnavn);

        String uuid = opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier);
        opplastetVedlegg.withUuid(uuid);

        JsonVedlegg jsonVedlegg = finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid);
        if (jsonVedlegg.getFiler() == null) {
            jsonVedlegg.setFiler(new ArrayList<>());
        }
        jsonVedlegg.withStatus(LastetOpp.toString()).getFiler().add(new JsonFiler().withFilnavn(filnavn).withSha512(sha512));

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);

        return opplastetVedlegg;
    }

    public void deleteVedleggAndUpdateVedleggstatus(String behandlingsId, String vedleggId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final OpplastetVedlegg opplastetVedlegg = opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null);

        if (opplastetVedlegg == null){
            return;
        }

        final String vedleggstype = opplastetVedlegg.getVedleggType().getSammensattType();

        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);

        JsonVedlegg jsonVedlegg = finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid);
        jsonVedlegg.getFiler().removeIf(jsonFiler ->
                jsonFiler.getSha512().equals(opplastetVedlegg.getSha512()) &&
                jsonFiler.getFilnavn().equals(opplastetVedlegg.getFilnavn()));

        if (jsonVedlegg.getFiler().isEmpty()){
            jsonVedlegg.setStatus(VedleggKreves.toString());
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);

        opplastetVedleggRepository.slettVedlegg(vedleggId, eier);
    }

    private JsonVedlegg finnVedleggEllerKastException(String vedleggstype, SoknadUnderArbeid soknadUnderArbeid) {
        Optional<JsonVedlegg> jsonVedleggOptional = getVedleggFromInternalSoknad(soknadUnderArbeid).stream()
                .filter(vedlegg -> vedleggstype.equals(vedlegg.getType() + "|" + vedlegg.getTilleggsinfo()))
                .findFirst();

        if (!jsonVedleggOptional.isPresent()) {
            throw new NotFoundException("Dette vedlegget tilhører " + vedleggstype + " utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtig?");
        }
        return jsonVedleggOptional.get();
    }

    String lagFilnavn(String opplastetNavn, String mimetype, String uuid) {
        String filnavn = opplastetNavn;
        int separator = opplastetNavn.lastIndexOf(".");
        if (separator != -1) {
            filnavn = opplastetNavn.substring(0, separator);
        }

        try {
            filnavn = URLDecoder.decode(filnavn, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            logger.warn("Klarte ikke å URIdecode fil med navn {}", filnavn, e);
        }

        filnavn = filnavn
                .replace("æ", "e")
                .replace("ø", "o")
                .replace("å", "a")
                .replace("Æ", "E")
                .replace("Ø", "O")
                .replace("Å", "A");

        filnavn = filnavn.replaceAll("[^a-zA-Z0-9_-]", "");

        if (filnavn.length() > 50) {
            filnavn = filnavn.substring(0, 50);
        }

        filnavn += "-" + uuid.split("-")[0];
        filnavn += MIME_TIL_EXT.get(mimetype);

        return filnavn;
    }

    private void validerFil(byte[] data) {
        if (!(Detect.isImage(data) || Detect.isPdf(data))) {
            throw new UgyldigOpplastingTypeException(
                    "Ugyldig filtype for opplasting", null,
                    "opplasting.feilmelding.feiltype");
        }
        if (Detect.isPdf(data)) {
            sjekkOmPdfErGyldig(data);
        }
    }

    private static void sjekkOmPdfErGyldig(byte[] data) {
        PDDocument document;
        try {
            document = PDDocument.load(new ByteArrayInputStream(
                    data));
        } catch (InvalidPasswordException e) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være krypert.", null,
                    "opplasting.feilmelding.pdf.kryptert");
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e,
                    "vedlegg.opplasting.feil.generell");
        }
        PdfDetector detector = new PdfDetector(document);
        if (detector.pdfIsSigned()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være signert.", null,
                    "opplasting.feilmelding.pdf.signert");
        } else if (detector.pdfIsEncrypted()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være signert.", null,
                    "opplasting.feilmelding.pdf.signert");
        }
    }
}
