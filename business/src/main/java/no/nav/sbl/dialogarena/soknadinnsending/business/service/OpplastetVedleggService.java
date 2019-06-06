package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.detect.pdf.PdfDetector;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.soknadinnsending.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static no.nav.sbl.sosialhjelp.domain.Vedleggstatus.Status.LastetOpp;
import static no.nav.sbl.sosialhjelp.domain.Vedleggstatus.Status.VedleggKreves;

@Component
public class OpplastetVedleggService {

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    private static Map<String, String> MIME_TIL_EXT;

    @PostConstruct
    public void setUp() {
        MIME_TIL_EXT = new HashMap<>();
        MIME_TIL_EXT.put("application/pdf", ".pdf");
        MIME_TIL_EXT.put("image/png", ".png");
        MIME_TIL_EXT.put("image/jpeg", ".jpg");
    }

    public OpplastetVedlegg saveVedleggAndUpdateVedleggstatus(String behandlingsId, String vedleggstype, byte[] data, String filnavn, boolean convertedFromFaktum) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final Long soknadId = soknadUnderArbeid.getSoknadId();
        final String sha512 = ServiceUtils.getSha512FromByteArray(data);
        final String contentType = Detect.CONTENT_TYPE.transform(data);

        validerFil(data);

        final OpplastetVedlegg opplastetVedlegg = new OpplastetVedlegg()
                .withEier(eier)
                .withVedleggType(new VedleggType(vedleggstype))
                .withData(data)
                .withSoknadId(soknadId)
                .withSha512(sha512);

        if (!convertedFromFaktum){
            filnavn = lagFilnavn(filnavn, contentType, opplastetVedlegg.getUuid());
        }
        opplastetVedlegg.withFilnavn(filnavn);

        final String uuid = opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier);
        opplastetVedlegg.withUuid(uuid);

        if (!convertedFromFaktum){
            final JsonVedlegg jsonVedlegg = getVedleggFromInternalSoknad(soknadUnderArbeid).stream()
                    .filter(vedlegg -> vedleggstype.equals(vedlegg.getType() + "|" + vedlegg.getTilleggsinfo()))
                    .findFirst().get();

            if (jsonVedlegg.getFiler() == null){
                jsonVedlegg.setFiler(new ArrayList<>());
            }
            jsonVedlegg.withStatus(LastetOpp.toString()).getFiler().add(new JsonFiler().withFilnavn(filnavn).withSha512(sha512));

            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);
        }

        return opplastetVedlegg;
    }

    public void deleteVedleggAndUpdateVedleggstatus(String behandlingsId, String vedleggId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final OpplastetVedlegg opplastetVedlegg = opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null);

        if (opplastetVedlegg == null){
            return;
        }

        final String vedleggstype = opplastetVedlegg.getVedleggType().getSammensattType();

        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();

        final JsonVedlegg jsonVedlegg = getVedleggFromInternalSoknad(soknadUnderArbeid).stream()
                .filter(vedlegg -> vedleggstype.equals(vedlegg.getType() + "|" + vedlegg.getTilleggsinfo()))
                .findFirst().get();

        jsonVedlegg.getFiler().removeIf(jsonFiler ->
                jsonFiler.getSha512().equals(opplastetVedlegg.getSha512()) &&
                jsonFiler.getFilnavn().equals(opplastetVedlegg.getFilnavn()));

        if (jsonVedlegg.getFiler().isEmpty()){
            jsonVedlegg.setStatus(VedleggKreves.toString());
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);

        opplastetVedleggRepository.slettVedlegg(vedleggId, eier);
    }

    String lagFilnavn(String opplastetNavn, String mimetype, String uuid) {
        String filnavn = opplastetNavn;
        int separator = opplastetNavn.lastIndexOf(".");
        if (separator != -1) {
            filnavn = opplastetNavn.substring(0, separator);
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
                    "PDF kan ikke være krypert.", null,
                    "opplasting.feilmelding.pdf.krypert");
        }
    }
}
