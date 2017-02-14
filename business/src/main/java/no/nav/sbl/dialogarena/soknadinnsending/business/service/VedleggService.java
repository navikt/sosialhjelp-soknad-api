package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.collections.iter.PreparedIterable;
import no.nav.modig.lang.collections.predicate.InstanceOf;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.detect.pdf.PdfDetector;
import no.nav.sbl.dialogarena.pdf.Convert;
import no.nav.sbl.dialogarena.pdf.ConvertToPng;
import no.nav.sbl.dialogarena.pdf.PdfMerger;
import no.nav.sbl.dialogarena.pdf.PdfWatermarker;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggForFaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggsGrunnlag;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.Splitter;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import static java.util.Collections.sort;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.KVITTERING;
import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel;
import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel.TITTEL;
import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel.TITTEL_EN;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.SKJEMA_VALIDERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.PAAKREVDE_VEDLEGG;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.UnderBehandling;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.toInnsendingsvalg;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class VedleggService {
    private static final Logger logger = getLogger(VedleggService.class);

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    @Named("vedleggRepository")
    private VedleggRepository vedleggRepository;

    @Inject
    private Kodeverk kodeverk;

    @Inject
    private SoknadService soknadService;

    @Inject
    private SoknadDataFletter soknadDataFletter;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private NavMessageSource navMessageSource;

    private PdfMerger pdfMerger = new PdfMerger();
    private PdfWatermarker watermarker = new PdfWatermarker();

    private static Vedlegg opprettVedlegg(Vedlegg vedlegg, long size) {
        return new Vedlegg()
                .medVedleggId(null)
                .medSoknadId(vedlegg.getSoknadId())
                .medFaktumId(vedlegg.getFaktumId())
                .medSkjemaNummer(vedlegg.getSkjemaNummer())
                .medSkjemanummerTillegg(vedlegg.getSkjemanummerTillegg())
                .medNavn(vedlegg.getNavn())
                .medStorrelse(size)
                .medAntallSider(1)
                .medData(null)
                .medOpprettetDato(vedlegg.getOpprettetDato())
                .medFillagerReferanse(vedlegg.getFillagerReferanse())
                .medInnsendingsvalg(UnderBehandling);
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
        }
    }

    public List<Vedlegg> hentVedleggOgKvittering(WebSoknad soknad) {
        ArrayList<Vedlegg> vedleggForventninger = Lists.newArrayList(soknad.hentPaakrevdeVedlegg());
        Vedlegg kvittering = vedleggRepository.hentVedleggForskjemaNummer(soknad.getSoknadId(), null, KVITTERING);
        if (kvittering != null) {
            vedleggForventninger.add(kvittering);
        }
        return vedleggForventninger;
    }

    @Transactional
    public List<Long> splitOgLagreVedlegg(Vedlegg vedlegg, InputStream inputStream) {
        List<Long> resultat = new ArrayList<>();
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            if (Detect.isImage(bytes)) {
                bytes = Convert.scaleImageAndConvertToPdf(bytes, new Dimension(1240, 1754));

                Vedlegg sideVedlegg = opprettVedlegg(vedlegg, (long) bytes.length);

                resultat.add(vedleggRepository.opprettEllerEndreVedlegg(sideVedlegg,
                        bytes));

            } else if (Detect.isPdf(bytes)) {
                PDDocument document = PDDocument.load(new ByteArrayInputStream(
                        bytes));
                sjekkOmPdfErGyldig(document);
                List<PDDocument> pages = new Splitter().split(document);
                for (PDDocument page : pages) {
                    PDPage pdPage = (PDPage) page.getDocumentCatalog().getAllPages().get(0);
                    pdPage.getContents().addCompression();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    page.save(baos);
                    page.close();

                    ByteArrayOutputStream finalBaos = komprimerPdfMedIText(baos.toByteArray());

                    Vedlegg sideVedlegg = opprettVedlegg(vedlegg, (long) finalBaos.size());

                    resultat.add(vedleggRepository.opprettEllerEndreVedlegg(sideVedlegg,
                            finalBaos.toByteArray()));
                }
                document.close();
            } else {
                throw new UgyldigOpplastingTypeException(
                        "Ugyldig filtype for opplasting", null,
                        "opplasting.feilmelding.feiltype");
            }
        } catch (DocumentException | IOException | COSVisitorException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e,
                    "vedlegg.opplasting.feil.generell");
        }
        repository.settSistLagretTidspunkt(vedlegg.getSoknadId());
        return resultat;
    }

    public List<Vedlegg> hentVedleggUnderBehandling(String behandlingsId, String fillagerReferanse) {
        return vedleggRepository.hentVedleggUnderBehandling(behandlingsId, fillagerReferanse);
    }

    public Vedlegg hentVedlegg(Long vedleggId) {
        return hentVedlegg(vedleggId, false);
    }

    public Vedlegg hentVedlegg(Long vedleggId, boolean medInnhold) {
        Vedlegg vedlegg;

        if (medInnhold) {
            vedlegg = vedleggRepository.hentVedleggMedInnhold(vedleggId);
        } else {
            vedlegg = vedleggRepository.hentVedlegg(vedleggId);
        }

        medKodeverk(vedlegg);
        return vedlegg;
    }

    public String hentBehandlingsId(Long vedleggId) {
        return vedleggRepository.hentBehandlingsIdTilVedlegg(vedleggId);
    }

    @Transactional
    public void slettVedlegg(Long vedleggId) {
        Vedlegg vedlegg = hentVedlegg(vedleggId, false);
        WebSoknad soknad = soknadService.hentSoknadFraLokalDb(vedlegg.getSoknadId());
        Long soknadId = soknad.getSoknadId();

        vedleggRepository.slettVedlegg(soknadId, vedleggId);
        repository.settSistLagretTidspunkt(soknadId);
        if (!soknad.erEttersending()) {
            repository.settDelstegstatus(soknadId, SKJEMA_VALIDERT);
        }
    }

    public byte[] lagForhandsvisning(Long vedleggId, int side) {
        return new ConvertToPng(new Dimension(600, 800), side).transform(vedleggRepository.hentVedleggData(vedleggId));
    }

    @Transactional
    public Long genererVedleggFaktum(String behandlingsId, Long vedleggId) {
        Vedlegg forventning = vedleggRepository.hentVedlegg(vedleggId);
        WebSoknad soknad = repository.hentSoknad(behandlingsId);
        List<Vedlegg> vedleggUnderBehandling = vedleggRepository.hentVedleggUnderBehandling(behandlingsId, forventning.getFillagerReferanse());
        Long soknadId = soknad.getSoknadId();

        sort(vedleggUnderBehandling, new Comparator<Vedlegg>() {
            @Override
            public int compare(Vedlegg v1, Vedlegg v2) {
                return v1.getVedleggId().compareTo(v2.getVedleggId());
            }
        });

        List<byte[]> bytes = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggUnderBehandling) {
            bytes.add(vedleggRepository.hentVedleggData(vedlegg.getVedleggId()));
        }
        byte[] doc = pdfMerger.transform(bytes);
        doc = watermarker.forIdent(getSubjectHandler().getUid(), false).transform(doc);

        try {
            doc = komprimerPdfMedIText(doc).toByteArray();
        } catch (IOException | DocumentException e) {
            logger.error("Kunne ikke komprimere vedlegg under merge", e);
        }

        forventning.leggTilInnhold(doc, vedleggUnderBehandling.size());

        logger.info("Lagrer fil til henvendelse for behandling {}, UUID: {}", soknad.getBrukerBehandlingId(), forventning.getFillagerReferanse());
        fillagerService.lagreFil(soknad.getBrukerBehandlingId(), forventning.getFillagerReferanse(), soknad.getAktoerId(), new ByteArrayInputStream(doc));

        vedleggRepository.slettVedleggUnderBehandling(soknadId, forventning.getFaktumId(), forventning.getSkjemaNummer(), forventning.getSkjemanummerTillegg());
        vedleggRepository.lagreVedleggMedData(soknadId, vedleggId, forventning);
        return vedleggId;
    }

    public List<Vedlegg> hentPaakrevdeVedlegg(final Long faktumId) {
        List<Vedlegg> paakrevdeVedlegg = genererPaakrevdeVedlegg(faktaService.hentBehandlingsId(faktumId));
        leggTilKodeverkFelter(paakrevdeVedlegg);
        return on(paakrevdeVedlegg).filter(new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedlegg) {
                return faktumId.equals(vedlegg.getFaktumId());
            }
        }).collect();
    }

    public List<Vedlegg> hentPaakrevdeVedlegg(String behandlingsId) {
        List<Vedlegg> paakrevdeVedleggVedNyUthenting = genererPaakrevdeVedlegg(behandlingsId);
        leggTilKodeverkFelter(paakrevdeVedleggVedNyUthenting);

        return paakrevdeVedleggVedNyUthenting;
    }

    private static final VedleggForFaktumStruktur N6_FORVENTNING = new VedleggForFaktumStruktur()
            .medFaktum(new FaktumStruktur().medId("ekstraVedlegg"))
            .medSkjemanummer("N6")
            .medOnValues(Arrays.asList("true"))
            .medFlereTillatt();

    public List<Vedlegg> genererPaakrevdeVedlegg(String behandlingsId) {
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true);
        if (soknad.erEttersending()) {
            oppdaterVedleggForForventninger(hentForventingerForEkstraVedlegg(soknad));
            return on(vedleggRepository.hentVedlegg(behandlingsId)).filter(PAAKREVDE_VEDLEGG).collect();
        } else {
            SoknadStruktur struktur = soknadService.hentSoknadStruktur(soknad.getskjemaNummer());
            List<VedleggsGrunnlag> alleMuligeVedlegg = struktur.hentAlleMuligeVedlegg(soknad, navMessageSource);
            oppdaterVedleggForForventninger(alleMuligeVedlegg);
            return hentPaakrevdeVedleggForForventninger(alleMuligeVedlegg);
        }
    }

    private List<VedleggsGrunnlag> hentForventingerForEkstraVedlegg(final WebSoknad soknad) {
        return on(soknad.getFaktaMedKey("ekstraVedlegg"))
                .map(new Transformer<Faktum, VedleggsGrunnlag>() {
                    @Override
                    public VedleggsGrunnlag transform(Faktum faktum) {
                        Vedlegg vedlegg = soknad.finnVedleggSomMatcherForventning(N6_FORVENTNING, faktum.getFaktumId());
                        return new VedleggsGrunnlag(soknad, vedlegg, navMessageSource).medGrunnlag(N6_FORVENTNING, faktum);
                    }
                }).collect();
    }

    private void oppdaterVedleggForForventninger(List<VedleggsGrunnlag> forventninger) {
        on(forventninger).forEach(new Closure<VedleggsGrunnlag>() {
            @Override
            public void execute(VedleggsGrunnlag vedleggsgrunnlag) {
                oppdaterVedlegg(vedleggsgrunnlag);
            }
        });
    }

    private void oppdaterVedlegg(VedleggsGrunnlag vedleggsgrunnlag) {
        boolean vedleggErPaakrevd = vedleggsgrunnlag.erVedleggPaakrevd();

        if (vedleggsgrunnlag.vedleggFinnes() || vedleggErPaakrevd) {

            if (vedleggsgrunnlag.vedleggIkkeFinnes()) {
                vedleggsgrunnlag.opprettVedleggFraFaktum();
            }

            Vedlegg.Status orginalStatus = vedleggsgrunnlag.vedlegg.getInnsendingsvalg();
            Vedlegg.Status status = vedleggsgrunnlag.oppdaterInnsendingsvalg(vedleggErPaakrevd);
            VedleggForFaktumStruktur vedleggForFaktumStruktur = vedleggsgrunnlag.grunnlag.get(0).getLeft();
            List<Faktum> fakta = vedleggsgrunnlag.grunnlag.get(0).getRight();
            Faktum faktum =  fakta.size() > 1 ? getFaktum(fakta, vedleggsgrunnlag.grunnlag.get(0).getLeft()) : fakta.get(0);
            if (vedleggsgrunnlag.vedleggHarTittelFraProperty(vedleggForFaktumStruktur, faktum)) {
                vedleggsgrunnlag.vedlegg.setNavn(faktum.getProperties().get(vedleggForFaktumStruktur.getProperty()));
            } else if (vedleggForFaktumStruktur.harOversetting()) {
                String cmsnokkel = vedleggForFaktumStruktur.getOversetting().replace("${key}", faktum.getKey());
                vedleggsgrunnlag.vedlegg.setNavn(vedleggsgrunnlag.navMessageSource.getMessage(cmsnokkel, new Object[0], vedleggsgrunnlag.soknad.getSprak()));
            }

            if (!status.equals(orginalStatus) || vedleggsgrunnlag.vedlegg.erNyttVedlegg()) {
                vedleggRepository.opprettEllerLagreVedleggVedNyGenereringUtenEndringAvData(vedleggsgrunnlag.vedlegg);
            }
        }
    }

    private Faktum getFaktum(List<Faktum> fakta, final VedleggForFaktumStruktur vedleggFaktumStruktur) {
        List<Faktum> filtrertFakta = on(fakta).filter(new Predicate<Faktum>() {
            @Override
            public boolean evaluate(Faktum faktum) {
                return  vedleggFaktumStruktur.getOnProperty().equals(faktum.getProperties().get(vedleggFaktumStruktur.getProperty()));
            }
        }).collect();

        return filtrertFakta.size() > 0 ? filtrertFakta.get(0) : fakta.get(0);
    }

    private List<Vedlegg> hentPaakrevdeVedleggForForventninger(List<VedleggsGrunnlag> alleMuligeVedlegg) {
        return on(alleMuligeVedlegg).map(new Transformer<VedleggsGrunnlag, Vedlegg>() {
            @Override
            public Vedlegg transform(VedleggsGrunnlag vedleggsgrunnlag) {
                return vedleggsgrunnlag.getVedlegg();
            }
        }).filter(PAAKREVDE_VEDLEGG).collect();
    }

    @Transactional
    public void lagreVedlegg(Long vedleggId, Vedlegg vedlegg) {
        if (nedgradertEllerForLavtInnsendingsValg(vedlegg)) {
            throw new ApplicationException("Ugyldig innsendingsstatus, opprinnelig innsendingstatus kan aldri nedgraderes");
        }
        vedleggRepository.lagreVedlegg(vedlegg.getSoknadId(), vedleggId, vedlegg);
        repository.settSistLagretTidspunkt(vedlegg.getSoknadId());

        if (!soknadService.hentSoknadFraLokalDb(vedlegg.getSoknadId()).erEttersending()) {
            repository.settDelstegstatus(vedlegg.getSoknadId(), SKJEMA_VALIDERT);
        }
    }

    public void leggTilKodeverkFelter(List<Vedlegg> vedleggListe) {
        for (Vedlegg vedlegg : vedleggListe) {
            medKodeverk(vedlegg);
        }
    }

    @Transactional
    public void lagreKvitteringSomVedlegg(String behandlingsId, byte[] kvittering) {
        WebSoknad soknad = repository.hentSoknad(behandlingsId);
        Vedlegg kvitteringVedlegg = vedleggRepository.hentVedleggForskjemaNummer(soknad.getSoknadId(), null, KVITTERING);
        if (kvitteringVedlegg == null) {
            kvitteringVedlegg = new Vedlegg(soknad.getSoknadId(), null, KVITTERING, LastetOpp);
            oppdaterInnholdIKvittering(kvitteringVedlegg, kvittering);
            vedleggRepository.opprettEllerEndreVedlegg(kvitteringVedlegg, kvittering);
        } else {
            oppdaterInnholdIKvittering(kvitteringVedlegg, kvittering);
            vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), kvitteringVedlegg.getVedleggId(), kvitteringVedlegg);
        }
        fillagerService.lagreFil(soknad.getBrukerBehandlingId(), kvitteringVedlegg.getFillagerReferanse(), soknad.getAktoerId(), new ByteArrayInputStream(kvitteringVedlegg.getData()));
    }

    private void oppdaterInnholdIKvittering(Vedlegg vedlegg, byte[] data) {
        vedlegg.medData(data);
        vedlegg.medStorrelse((long) data.length);
        try {
            vedlegg.medAntallSider(new PdfReader(data).getNumberOfPages());
        } catch (IOException e) {
            logger.info("Klarte ikke å finne antall sider i kvittering, vedleggid [{}]. Fortsetter uten sideantall.", vedlegg.getVedleggId(), e);
        }
    }

    private boolean nedgradertEllerForLavtInnsendingsValg(Vedlegg vedlegg) {
        Vedlegg.Status nyttInnsendingsvalg = vedlegg.getInnsendingsvalg();
        Vedlegg.Status opprinneligInnsendingsvalg = vedlegg.getOpprinneligInnsendingsvalg();
        if (nyttInnsendingsvalg != null && opprinneligInnsendingsvalg != null) {
            if (nyttInnsendingsvalg.getPrioritet() <= 1 || (nyttInnsendingsvalg.getPrioritet() < opprinneligInnsendingsvalg.getPrioritet())) {
                return true;
            }
        }
        return false;
    }

    public void medKodeverk(Vedlegg vedlegg, Locale locale) {
        boolean soknadErEngelsk = locale.equals(Locale.ENGLISH);
        medKodeverk(vedlegg, soknadErEngelsk);
    }

    private void medKodeverk(Vedlegg vedlegg) {
        try {
            Faktum sprakFaktum = soknadService.hentSprak(vedlegg.getSoknadId());
            boolean soknadHarSpraak = sprakFaktum != null;
            boolean soknadErEngelsk = soknadHarSpraak && "en".equals(sprakFaktum.getValue());

            medKodeverk(vedlegg, soknadErEngelsk);
        } catch (Exception ignore) {
            logger.debug("ignored exception");

        }
    }

    private void medKodeverk(Vedlegg vedlegg, boolean soknadErEngelsk) {
        Map<Nokkel, String> koder = kodeverk.getKoder(vedlegg.getSkjemaNummer());
        for (Map.Entry<Nokkel, String> nokkelEntry : koder.entrySet()) {
            if (nokkelEntry.getKey().toString().contains("URL")) {
                vedlegg.leggTilURL(nokkelEntry.getKey().toString(), koder.get(nokkelEntry.getKey()));
            }
        }
        boolean engelskTittelFinnes = !"".equals(koder.get(TITTEL_EN));
        Nokkel tittelKey = soknadErEngelsk && engelskTittelFinnes ? TITTEL_EN : TITTEL;

        vedlegg.setTittel(koder.get(tittelKey));
    }

    private ByteArrayOutputStream komprimerPdfMedIText(byte[] pdfBytes) throws DocumentException, IOException {
        ByteArrayOutputStream returnBaos = new ByteArrayOutputStream();
        Document pdf = new Document(PageSize.A4);
        PdfReader pdfReader = new PdfReader(pdfBytes);
        PdfStamper stamper = new PdfStamper(pdfReader, returnBaos, PdfWriter.VERSION_1_7);
        stamper.getWriter().setCompressionLevel(9);

        for (int i = 1; i < pdfReader.getNumberOfPages(); i++) {
            pdfReader.setPageContent(i, pdfReader.getPageContent(i));
        }
        stamper.setFullCompression();
        pdf.close();
        stamper.close();
        pdfReader.close();
        return returnBaos;
    }

    public List<Vedlegg> hentVedleggOgPersister(XMLMetadataListe xmlVedleggListe, Long soknadId) {
        PreparedIterable<XMLMetadata> vedlegg = on(xmlVedleggListe.getMetadata()).filter(new InstanceOf<XMLMetadata>(XMLVedlegg.class));
        List<Vedlegg> soknadVedlegg = new ArrayList<>();
        for (XMLMetadata xmlMetadata : vedlegg) {
            if (xmlMetadata instanceof XMLHovedskjema) {
                continue;
            }
            XMLVedlegg xmlVedlegg = (XMLVedlegg) xmlMetadata;

            Integer antallSider = xmlVedlegg.getSideantall() != null ? xmlVedlegg.getSideantall() : 0;

            Vedlegg v = new Vedlegg()
                    .medSkjemaNummer(xmlVedlegg.getSkjemanummer())
                    .medAntallSider(antallSider)
                    .medInnsendingsvalg(toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                    .medOpprinneligInnsendingsvalg(toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                    .medSoknadId(soknadId)
                    .medNavn(xmlVedlegg.getTilleggsinfo());

            String skjemanummerTillegg = xmlVedlegg.getSkjemanummerTillegg();
            if (isNotBlank(skjemanummerTillegg)) {
                v.setSkjemaNummer(v.getSkjemaNummer() + "|" + skjemanummerTillegg);
            }

            vedleggRepository.opprettEllerEndreVedlegg(v, null);
            soknadVedlegg.add(v);
        }
        leggTilKodeverkFelter(soknadVedlegg);
        return soknadVedlegg;
    }

    public void populerVedleggMedDataFraHenvendelse(WebSoknad soknad, List<WSInnhold> innhold) {
        for (WSInnhold wsInnhold : innhold) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                wsInnhold.getInnhold().writeTo(baos);
            } catch (IOException e) {
                throw new ApplicationException("Kunne ikke hente opp soknaddata", e);
            }
            Vedlegg vedlegg = soknad.hentVedleggMedUID(wsInnhold.getUuid());
            if (vedlegg != null) {
                vedlegg.setData(baos.toByteArray());
                vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
            }
        }
    }

}
