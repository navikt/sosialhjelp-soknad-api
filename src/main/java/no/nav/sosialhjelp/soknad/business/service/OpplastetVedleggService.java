package no.nav.sosialhjelp.soknad.business.service;

import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.util.FileDetectionUtils;
import no.nav.sosialhjelp.soknad.business.util.TikaFileType;
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.VedleggType;
import no.nav.sosialhjelp.soknad.domain.model.exception.OpplastingException;
import no.nav.sosialhjelp.soknad.domain.model.exception.SamletVedleggStorrelseForStorException;
import no.nav.sosialhjelp.soknad.domain.model.exception.UgyldigOpplastingTypeException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static no.nav.sosialhjelp.soknad.domain.Vedleggstatus.LastetOpp;
import static no.nav.sosialhjelp.soknad.domain.Vedleggstatus.VedleggKreves;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class OpplastetVedleggService {

    public static final Integer MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB = 150;
    public static final Integer MAKS_SAMLET_VEDLEGG_STORRELSE = MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB * 1024 * 1024; // 150 MB
    private static final Logger logger = getLogger(OpplastetVedleggService.class);

    private final OpplastetVedleggRepository opplastetVedleggRepository;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final VirusScanner virusScanner;

    public OpplastetVedleggService(
            OpplastetVedleggRepository opplastetVedleggRepository,
            SoknadUnderArbeidRepository soknadUnderArbeidRepository,
            VirusScanner virusScanner
    ) {
        this.opplastetVedleggRepository = opplastetVedleggRepository;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.virusScanner = virusScanner;
    }

    public OpplastetVedlegg saveVedleggAndUpdateVedleggstatus(String behandlingsId, String vedleggstype, byte[] data, String filnavn) {
        String eier = SubjectHandler.getUserId();
        String sha512 = ServiceUtils.getSha512FromByteArray(data);

        TikaFileType fileType = validerFil(data, filnavn);
        virusScanner.scan(filnavn, data, behandlingsId, fileType.name());

        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        Long soknadId = soknadUnderArbeid.getSoknadId();

        OpplastetVedlegg opplastetVedlegg = new OpplastetVedlegg()
                .withEier(eier)
                .withVedleggType(new VedleggType(vedleggstype))
                .withData(data)
                .withSoknadId(soknadId)
                .withSha512(sha512);

        filnavn = lagFilnavn(filnavn, fileType, opplastetVedlegg.getUuid());

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

    public void oppdaterVedleggsforventninger(SoknadUnderArbeid soknadUnderArbeid, String eier) {
        var jsonVedleggs = getVedleggFromInternalSoknad(soknadUnderArbeid);
        var paakrevdeVedlegg = VedleggsforventningMaster.finnPaakrevdeVedlegg(soknadUnderArbeid.getJsonInternalSoknad());
        var opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.getSoknadId(), eier);

        fjernIkkePaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg, opplastedeVedlegg);

        jsonVedleggs.addAll(
                paakrevdeVedlegg.stream()
                        .filter(isNotInList(jsonVedleggs))
                        .map(jsonVedlegg -> jsonVedlegg.withStatus(VedleggKreves.toString()))
                        .collect(Collectors.toList()));

        soknadUnderArbeid.getJsonInternalSoknad().setVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs));
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);
    }

    public void sjekkOmSoknadUnderArbeidTotalVedleggStorrelseOverskriderMaksgrense(String behandlingsId, byte[] data) {
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        Long soknadId = soknadUnderArbeid.getSoknadId();

        Integer samletVedleggStorrelse = opplastetVedleggRepository.hentSamletVedleggStorrelse(soknadId, eier);
        int newStorrelse = samletVedleggStorrelse + data.length;
        if (newStorrelse > MAKS_SAMLET_VEDLEGG_STORRELSE) {
            String feilmeldingId = soknadUnderArbeid.erEttersendelse() ? "ettersending.vedlegg.feil.samletStorrelseForStor" : "vedlegg.opplasting.feil.samletStorrelseForStor";
            throw new SamletVedleggStorrelseForStorException("Kunne ikke lagre fil fordi samlet størrelse på alle vedlegg er for stor", null, feilmeldingId);
        }
    }

    public void deleteVedleggAndUpdateVedleggstatus(String behandlingsId, String vedleggId) {
        final String eier = SubjectHandler.getUserId();
        final OpplastetVedlegg opplastetVedlegg = opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null);

        if (opplastetVedlegg == null) {
            return;
        }

        final String vedleggstype = opplastetVedlegg.getVedleggType().getSammensattType();

        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);

        JsonVedlegg jsonVedlegg = finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid);
        jsonVedlegg.getFiler().removeIf(jsonFiler ->
                jsonFiler.getSha512().equals(opplastetVedlegg.getSha512()) &&
                        jsonFiler.getFilnavn().equals(opplastetVedlegg.getFilnavn()));

        if (jsonVedlegg.getFiler().isEmpty()) {
            jsonVedlegg.setStatus(VedleggKreves.toString());
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);

        opplastetVedleggRepository.slettVedlegg(vedleggId, eier);
    }

    private void fjernIkkePaakrevdeVedlegg(List<JsonVedlegg> jsonVedleggs, List<JsonVedlegg> paakrevdeVedlegg, List<OpplastetVedlegg> opplastedeVedlegg) {
        final var ikkeLengerPaakrevdeVedlegg = jsonVedleggs.stream().filter(isNotInList(paakrevdeVedlegg)).collect(Collectors.toList());

        excludeTypeAnnetAnnetFromList(ikkeLengerPaakrevdeVedlegg);
        jsonVedleggs.removeAll(ikkeLengerPaakrevdeVedlegg);

        for (var ikkePaakrevdVedlegg : ikkeLengerPaakrevdeVedlegg) {
            for (var oVedlegg : opplastedeVedlegg) {
                if (isSameType(ikkePaakrevdVedlegg, oVedlegg)) {
                    opplastetVedleggRepository.slettVedlegg(oVedlegg.getUuid(), oVedlegg.getEier());
                }
            }
        }
    }

    private Predicate<JsonVedlegg> isNotInList(List<JsonVedlegg> jsonVedleggs) {
        return v -> jsonVedleggs.stream()
                .noneMatch(vedlegg ->
                        vedlegg.getType().equals(v.getType()) &&
                                vedlegg.getTilleggsinfo().equals(v.getTilleggsinfo())
                );
    }

    private void excludeTypeAnnetAnnetFromList(List<JsonVedlegg> jsonVedleggs) {
        jsonVedleggs.removeAll(jsonVedleggs.stream()
                .filter(vedlegg -> vedlegg.getType().equals("annet") &&
                        vedlegg.getTilleggsinfo().equals("annet")).collect(Collectors.toList()));
    }

    private boolean isSameType(JsonVedlegg jsonVedlegg, OpplastetVedlegg opplastetVedlegg) {
        return opplastetVedlegg.getVedleggType().getSammensattType().equals(jsonVedlegg.getType() + "|" + jsonVedlegg.getTilleggsinfo());
    }

    private JsonVedlegg finnVedleggEllerKastException(String vedleggstype, SoknadUnderArbeid soknadUnderArbeid) {
        Optional<JsonVedlegg> jsonVedleggOptional = getVedleggFromInternalSoknad(soknadUnderArbeid).stream()
                .filter(vedlegg -> vedleggstype.equals(vedlegg.getType() + "|" + vedlegg.getTilleggsinfo()))
                .findFirst();

        if (jsonVedleggOptional.isEmpty()) {
            throw new NotFoundException("Dette vedlegget tilhører " + vedleggstype + " utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtidig?");
        }
        return jsonVedleggOptional.get();
    }

    String lagFilnavn(String opplastetNavn, TikaFileType fileType, String uuid) {
        String filnavn = opplastetNavn;
        var fileExtension = findFileExtension(opplastetNavn);

        if (fileExtension != null) {
            int separatorPosition = opplastetNavn.lastIndexOf(".");
            if (separatorPosition != -1) {
                filnavn = opplastetNavn.substring(0, separatorPosition);
            }
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
        if (fileExtension != null && fileExtension.length() > 0 && erTikaOgFileExtensionEnige(fileExtension, fileType)) {
            filnavn += fileExtension;
        } else {
            logger.info("Opplastet vedlegg mangler fil extension -> setter fil extension lik validert filtype = {}", fileType.getExtension());
            filnavn += fileType.getExtension();
        }

        return filnavn;
    }

    private boolean erTikaOgFileExtensionEnige(String fileExtension, TikaFileType fileType) {
        if (TikaFileType.JPEG.equals(fileType)) {
            return ".jpg".equalsIgnoreCase(fileExtension) || ".jpeg".equalsIgnoreCase(fileExtension);
        }
        if (TikaFileType.PNG.equals(fileType)) {
            return ".png".equalsIgnoreCase(fileExtension);
        }
        if (TikaFileType.PDF.equals(fileType)) {
            return ".pdf".equalsIgnoreCase(fileExtension);
        }
        return false;
    }

    private TikaFileType validerFil(byte[] data, String filnavn) {
        TikaFileType fileType = FileDetectionUtils.detectTikaType(data);

        if (fileType == TikaFileType.UNKNOWN) {
            throw new UgyldigOpplastingTypeException(
                    String.format("Ugyldig filtype for opplasting. Mimetype var %s, filtype var %s",
                            FileDetectionUtils.getMimeType(data), findFileExtension(filnavn)),
                    null,
                    "opplasting.feilmelding.feiltype");
        }
        if (fileType == TikaFileType.JPEG || fileType == TikaFileType.PNG) {
            validerFiltypeForBilde(filnavn);
        }
        if (fileType == TikaFileType.PDF) {
            sjekkOmPdfErGyldig(data);
        }
        return fileType;
    }

    private String findFileExtension(String filnavn) {
        var sisteIndexForPunktum = filnavn.lastIndexOf(".");
        if (sisteIndexForPunktum < 0) {
            return null;
        }
        var fileExtension = filnavn.substring(sisteIndexForPunktum);
        if (!isValidFileExtension(fileExtension)) {
            return null;
        }
        return fileExtension;
    }

    private boolean isValidFileExtension(String fileExtension) {
        var validFileExtensions = List.of(".pdf", ".jpeg", ".jpg", ".png");
        return validFileExtensions.contains(fileExtension.toLowerCase());
    }

    private void validerFiltypeForBilde(String filnavn) {
        var fileExtension = findFileExtension(filnavn);
        if (fileExtension == null) {
            logger.info("Opplastet bilde validerer OK, men mangler filtype for fil");
        }
        if (filnavn.toLowerCase().endsWith(".jfif") || filnavn.toLowerCase().endsWith(".pjpeg") || filnavn.toLowerCase().endsWith(".pjp")) {
            throw new UgyldigOpplastingTypeException(
                    String.format("Ugyldig filtype for opplasting. Filtype var %s", fileExtension),
                    null,
                    "opplasting.feilmelding.feiltype");
        }
    }

    private static void sjekkOmPdfErGyldig(byte[] data) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(data))) {
            String text = (new PDFTextStripper()).getText(document);

            if (text == null || text.isEmpty()) {
                logger.warn("PDF er tom"); // En PDF med ett helt blankt ark generert av word gir text = "\r\n"
            }

            if (document.isEncrypted()) {
                throw new UgyldigOpplastingTypeException(
                        "PDF kan ikke være kryptert.", null,
                        "opplasting.feilmelding.pdf.kryptert");
            }

        } catch (InvalidPasswordException e) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være krypert.", null,
                    "opplasting.feilmelding.pdf.kryptert");
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e,
                    "vedlegg.opplasting.feil.generell");
        }
    }
}
