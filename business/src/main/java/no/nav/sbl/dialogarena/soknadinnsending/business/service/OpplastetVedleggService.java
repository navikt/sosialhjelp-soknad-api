package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.detect.pdf.PdfDetector;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
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

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class OpplastetVedleggService {

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    public String saveVedleggAndUpdateVedleggstatus(String behandlingsId, String vedleggstype, byte[] data, String filnavn) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final String type = vedleggstype.substring(0, vedleggstype.indexOf('|'));
        final String tilleggsinfo = vedleggstype.substring(vedleggstype.indexOf('|') + 1);
        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final Long soknadId = soknadUnderArbeid.getSoknadId();
        final String sha512 = ServiceUtils.getSha512FromByteArray(data);

        validerFil(data);

        final OpplastetVedlegg opplastetVedlegg = new OpplastetVedlegg()
                .withEier(eier)
                .withVedleggType(new VedleggType(type, tilleggsinfo))
                .withData(data)
                .withSoknadId(soknadId)
                .withFilnavn(filnavn)
                .withSha512(sha512);

        final String uuid = opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier);

        final JsonVedlegg jsonVedlegg = soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg().stream()
                .filter(vedlegg -> vedlegg.getType().equals(type) && vedlegg.getTilleggsinfo().equals(tilleggsinfo))
                .findFirst().get();

        if (jsonVedlegg.getFiler() == null){
            jsonVedlegg.setFiler(new ArrayList<>());
        }
        jsonVedlegg.withStatus("LastetOpp").getFiler().add(new JsonFiler().withFilnavn(filnavn).withSha512(sha512));

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);

        return uuid;
    }

    public void deleteVedleggAndUpdateVedleggstatus(String behandlingsId, String vedleggId) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final OpplastetVedlegg opplastetVedlegg = opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null);

        if (opplastetVedlegg == null){
            return;
        }

        opplastetVedleggRepository.slettVedlegg(vedleggId, eier);

        final String type = opplastetVedlegg.getVedleggType().getType();
        final String tilleggsinfo = opplastetVedlegg.getVedleggType().getTilleggsinfo();

        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();

        final JsonVedlegg jsonVedlegg = soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg().stream()
                .filter(vedlegg -> vedlegg.getType().equals(type) && vedlegg.getTilleggsinfo().equals(tilleggsinfo))
                .findFirst().get();

        jsonVedlegg.getFiler().removeIf(jsonFiler -> jsonFiler.getFilnavn().equals(opplastetVedlegg.getFilnavn()));

        if (jsonVedlegg.getFiler().isEmpty()){
            jsonVedlegg.setStatus("VedleggKreves");
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);
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
