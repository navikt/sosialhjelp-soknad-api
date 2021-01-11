package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SamletVedleggStorrelseForStorException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.FileDetectionUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.TikaFileType;
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

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

import static no.nav.sbl.dialogarena.soknadinnsending.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static no.nav.sbl.sosialhjelp.domain.Vedleggstatus.LastetOpp;
import static no.nav.sbl.sosialhjelp.domain.Vedleggstatus.VedleggKreves;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class OpplastetVedleggService {

    private static final Logger logger = getLogger(OpplastetVedleggService.class);
    public static final Integer MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB = 150;
    public static final Integer MAKS_SAMLET_VEDLEGG_STORRELSE = MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB * 1024 * 1024; // 150 MB

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private VirusScanner virusScanner;

    public OpplastetVedlegg saveVedleggAndUpdateVedleggstatus(String behandlingsId, String vedleggstype, byte[] data, String filnavn) {
        String eier = SubjectHandler.getUserId();
        String sha512 = ServiceUtils.getSha512FromByteArray(data);

        TikaFileType fileType = validerFil(data, filnavn);
        virusScanner.scan(filnavn, data, behandlingsId);

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

        if (jsonVedleggOptional.isEmpty()) {
            throw new NotFoundException("Dette vedlegget tilhører " + vedleggstype + " utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtidig?");
        }
        return jsonVedleggOptional.get();
    }

    String lagFilnavn(String opplastetNavn, TikaFileType fileType, String uuid) {
        String filnavn = opplastetNavn;
        var filExtention = findFileExtention(opplastetNavn);

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
        if(filExtention != null && filExtention.length() > 0) {
            filnavn += filExtention;
        } else {
            filnavn += fileType.getExtention();
        }

        return filnavn;
    }

    private TikaFileType validerFil(byte[] data, String filnavn) {
        TikaFileType fileType = FileDetectionUtils.detectTikaType(data);

        if (fileType == TikaFileType.UNKNOWN) {
            throw new UgyldigOpplastingTypeException(
                    String.format("Ugyldig filtype for opplasting. Mimetype var %s, filtype var %s", FileDetectionUtils.getMimeType(data), findFileExtention(filnavn)),
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

    private String findFileExtention(String filnavn) {
        var sisteIndexForPunktum = filnavn.lastIndexOf(".");
        if (sisteIndexForPunktum < 0) {
            return null;
        }
        return filnavn.substring(sisteIndexForPunktum);
    }

    private void validerFiltypeForBilde(String filnavn) {
        var filtype = findFileExtention(filnavn);
        if (filtype == null) {
            throw new UgyldigOpplastingTypeException(
                    "Ugyldig filtype for opplasting. Kunne ikke finne filtype for fil.",
                    null,
                    "opplasting.feilmelding.feiltype");
        }
        if (filnavn.endsWith(".jfif") || filnavn.endsWith(".pjpeg") || filnavn.endsWith(".pjp")) {
            throw new UgyldigOpplastingTypeException(
                    String.format("Ugyldig filtype for opplasting. Filtype var %s", filtype),
                    null,
                    "opplasting.feilmelding.feiltype");
        }
    }

    private static void sjekkOmPdfErGyldig(byte[] data) {
        PDDocument document;
        try {
            document = PDDocument.load(new ByteArrayInputStream(data));

        } catch (InvalidPasswordException e) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være krypert.", null,
                    "opplasting.feilmelding.pdf.kryptert");
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e,
                    "vedlegg.opplasting.feil.generell");
        }
        if (document.isEncrypted()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være kryptert.", null,
                    "opplasting.feilmelding.pdf.kryptert");
        }
    }
}
