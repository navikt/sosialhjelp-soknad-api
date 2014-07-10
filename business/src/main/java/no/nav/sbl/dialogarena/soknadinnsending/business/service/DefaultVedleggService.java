package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.detect.pdf.PdfDetector;
import no.nav.sbl.dialogarena.pdf.Convert;
import no.nav.sbl.dialogarena.pdf.ConvertToPng;
import no.nav.sbl.dialogarena.pdf.PdfMerger;
import no.nav.sbl.dialogarena.pdf.PdfWatermarker;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Splitter;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DefaultVedleggService implements VedleggService {
    private static final Logger logger = getLogger(DefaultVedleggService.class);

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    @Named("vedleggRepository")
    private VedleggRepository vedleggRepository;

    @Inject
    private Kodeverk kodeverk;

    @Inject
    private SendSoknadService soknadService;

    @Inject
    private FillagerConnector fillagerConnector;

    private PdfMerger pdfMerger = new PdfMerger();
    private PdfWatermarker watermarker = new PdfWatermarker();

    @Override
    public List<Long> splitOgLagreVedlegg(Vedlegg vedlegg, InputStream inputStream) {
        List<Long> resultat = new ArrayList<>();
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            if (Detect.isImage(bytes)) {
                bytes = Convert.scaleImageAndConvertToPdf(bytes, new Dimension(1240, 1754));

                Vedlegg sideVedlegg = new Vedlegg()
                        .medVedleggId(null)
                        .medSoknadId(vedlegg.getSoknadId())
                        .medFaktumId(vedlegg.getFaktumId())
                        .medSkjemaNummer(vedlegg.getSkjemaNummer())
                        .medNavn(vedlegg.getNavn())
                        .medStorrelse((long) bytes.length)
                        .medAntallSider(1)
                        .medData(null)
                        .medOpprettetDato(vedlegg.getOpprettetDato())
                        .medFillagerReferanse(vedlegg.getFillagerReferanse())
                        .medInnsendingsvalg(Vedlegg.Status.UnderBehandling);

                resultat.add(vedleggRepository.opprettVedlegg(sideVedlegg,
                        bytes));

            } else if (Detect.isPdf(bytes)) {
                PDDocument document = PDDocument.load(new ByteArrayInputStream(
                        bytes));
                sjekkOmPdfErGyldig(document);
                List<PDDocument> pages = new Splitter().split(document);
                for (PDDocument page : pages) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    page.save(baos);
                    page.close();

                    Vedlegg sideVedlegg = new Vedlegg()
                            .medVedleggId(null)
                            .medSoknadId(vedlegg.getSoknadId())
                            .medFaktumId(vedlegg.getFaktumId())
                            .medSkjemaNummer(vedlegg.getSkjemaNummer())
                            .medNavn(vedlegg.getNavn())
                            .medStorrelse((long) baos.size())
                            .medAntallSider(1)
                            .medData(null)
                            .medOpprettetDato(vedlegg.getOpprettetDato())
                            .medFillagerReferanse(vedlegg.getFillagerReferanse())
                            .medInnsendingsvalg(Vedlegg.Status.UnderBehandling);

                    resultat.add(vedleggRepository.opprettVedlegg(sideVedlegg,
                            baos.toByteArray()));
                }
                document.close();
            } else {
                throw new UgyldigOpplastingTypeException(
                        "Ugyldig filtype for opplasting", null,
                        "opplasting.feilmelding.feiltype");
            }
        } catch (IOException | COSVisitorException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e,
                    "vedlegg.opplasting.feil.generell");
        }
        repository.settSistLagretTidspunkt(vedlegg.getSoknadId());
        return resultat;
    }

    @Override
    public List<Vedlegg> hentVedleggUnderBehandling(Long soknadId, String fillagerReferanse) {
        return vedleggRepository.hentVedleggUnderBehandling(soknadId, fillagerReferanse);
    }

    @Override
    public Vedlegg hentVedlegg(Long soknadId, Long vedleggId, boolean medInnhold) {
        if (medInnhold) {
            Vedlegg vedlegg = vedleggRepository.hentVedleggMedInnhold(soknadId, vedleggId);
            medKodeverk(vedlegg);
            return vedlegg;
        } else {
            Vedlegg vedlegg = vedleggRepository.hentVedlegg(soknadId, vedleggId);
            medKodeverk(vedlegg);
            return vedlegg;
        }
    }

    @Override
    public void slettVedlegg(Long soknadId, Long vedleggId) {
        WebSoknad soknad = soknadService.hentSoknad(soknadId);
        vedleggRepository.slettVedlegg(soknadId, vedleggId);
        repository.settSistLagretTidspunkt(soknadId);
        if (soknad != null && !soknad.erEttersending()) {
            repository.settDelstegstatus(soknadId, DelstegStatus.SKJEMA_VALIDERT);
        }
    }

    @Override
    public byte[] lagForhandsvisning(Long soknadId, Long vedleggId, int side) {
        return new ConvertToPng(new Dimension(600, 800), side).transform(vedleggRepository.hentVedleggData(soknadId, vedleggId));
    }

    @Override
    public Long genererVedleggFaktum(Long soknadId, Long vedleggId) {
        Vedlegg forventning = vedleggRepository
                .hentVedlegg(soknadId, vedleggId);
        List<Vedlegg> vedleggUnderBehandling = vedleggRepository
                .hentVedleggUnderBehandling(soknadId,
                        forventning.getFillagerReferanse());
        List<byte[]> bytes = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggUnderBehandling) {
            bytes.add(vedleggRepository.hentVedleggData(soknadId, vedlegg.getVedleggId()));
        }
        byte[] doc = pdfMerger.transform(bytes);
        doc = watermarker.forIdent(getSubjectHandler().getUid(), false).transform(doc);

        forventning.leggTilInnhold(doc, vedleggUnderBehandling.size());
        WebSoknad soknad = repository.hentSoknad(soknadId);

        fillagerConnector.lagreFil(soknad.getBrukerBehandlingId(),
                forventning.getFillagerReferanse(), soknad.getAktoerId(),
                new ByteArrayInputStream(doc));

        vedleggRepository.slettVedleggUnderBehandling(soknadId,
                forventning.getFaktumId(), forventning.getSkjemaNummer());
        vedleggRepository.lagreVedleggMedData(soknadId, vedleggId, forventning);
        return vedleggId;
    }

    @Override
    public List<Vedlegg> hentPaakrevdeVedlegg(Long soknadId) {
        List<Vedlegg> paakrevdeVedlegg = vedleggRepository.hentPaakrevdeVedlegg(soknadId);
        leggTilKodeverkFelter(paakrevdeVedlegg);
        return paakrevdeVedlegg;
    }

    @Override
    public void lagreVedlegg(Long soknadId, Long vedleggId, Vedlegg vedlegg) {
        if(nedgradertEllerForLavtInnsendingsValg(vedlegg)) {
            throw new ApplicationException("Ugyldig innsendingsstatus, opprinnelig innsendingstatus kan aldri nedgraderes");
        }
        vedleggRepository.lagreVedlegg(soknadId, vedleggId, vedlegg);
        repository.settSistLagretTidspunkt(soknadId);

        if (!soknadService.hentSoknad(soknadId).erEttersending()) {
            repository.settDelstegstatus(soknadId, DelstegStatus.SKJEMA_VALIDERT);
        }
    }

    @Override
    public void leggTilKodeverkFelter(List<Vedlegg> vedlegg) {
        for (Vedlegg v : vedlegg) {
            medKodeverk(v);
        }
    }

    private static void sjekkOmPdfErGyldig(PDDocument document) {
        PdfDetector detector = new PdfDetector(document);
        if (detector.pdfIsSigned()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være signert.", null,
                    "opplasting.feilmelding.pdf.signert");
        } else if (detector.pdfIsEncrypted()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være krypert.", null,
                    "opplasting.feilmelding.pdf.krypert");
        } else if (detector.pdfIsSavedOrExportedWithApplePreview()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være lagret med Apple Preview.", null,
                    "opplasting.feilmelding.pdf.applepreview");
        }
    }

    private boolean nedgradertEllerForLavtInnsendingsValg(Vedlegg vedlegg) {
        Vedlegg.Status nyttInnsendingsvalg = vedlegg.getInnsendingsvalg();
        Vedlegg.Status opprinneligInnsendingsvalg = vedlegg.getOpprinneligInnsendingsvalg();
        if(nyttInnsendingsvalg != null && opprinneligInnsendingsvalg != null){
            if(nyttInnsendingsvalg.getPrioritet() <= 1 || (nyttInnsendingsvalg.getPrioritet() < opprinneligInnsendingsvalg.getPrioritet())) {
                return true;
            }
        }
        return false;
    }

    private void medKodeverk(Vedlegg vedlegg) {
        try {
            Map<Kodeverk.Nokkel, String> koder = kodeverk.getKoder(vedlegg.getSkjemaNummerFiltrert());
            for (Map.Entry<Kodeverk.Nokkel, String> nokkelEntry : koder.entrySet()) {
                if (nokkelEntry.getKey().toString().contains("URL")) {
                    vedlegg.leggTilURL(nokkelEntry.getKey().toString(), koder.get(nokkelEntry.getKey()));
                }
            }
            vedlegg.setTittel(koder.get(Kodeverk.Nokkel.TITTEL));
        } catch (Exception ignore) {
            logger.debug("ignored exception");

        }
    }
}
