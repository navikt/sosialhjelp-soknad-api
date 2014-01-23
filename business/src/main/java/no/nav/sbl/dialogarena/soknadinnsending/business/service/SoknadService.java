package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.detect.pdf.PdfDetector;
import no.nav.sbl.dialogarena.pdf.Convert;
import no.nav.sbl.dialogarena.pdf.ConvertToPng;
import no.nav.sbl.dialogarena.pdf.PdfMerger;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknadId;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Splitter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.getJournalforendeEnhet;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.getSkjemanummer;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadService implements SendSoknadService, VedleggService {
    private static final Logger logger = getLogger(SoknadService.class);
    private static final String BRUKERREGISTRERT_FAKTUM = "BRUKERREGISTRERT";
    private static final String SYSTEMREGISTRERT_FAKTUM = "SYSTEMREGISTRERT";
    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;
    @Inject
    @Named("vedleggRepository")
    private VedleggRepository vedleggRepository;
    @Inject
    private HenvendelseConnector henvendelseConnector;
    @Inject
    private FillagerConnector fillagerConnector;
    @Inject
    private Kodeverk kodeverk;

    private static void sjekkOmPdfErGyldig(PDDocument document) {
        PdfDetector detector = new PdfDetector(document);
        if (detector.pdfIsSigned()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være signert.", null,
                    "vedlegg.opplasting.feil.pdf.signert");
        } else if (detector.pdfIsEncrypted()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være krypert.", null,
                    "vedlegg.opplasting.feil.pdf.krypert");
        } else if (detector.pdfIsSavedOrExportedWithApplePreview()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være lagret med Apple Preview.", null,
                    "vedlegg.opplasting.feil.pdf.applepreview");
        }
    }

    @Override
    public WebSoknad hentSoknad(long soknadId) {
        return repository.hentSoknadMedData(soknadId);
    }

    @Override
    public Faktum lagreSoknadsFelt(Long soknadId, Faktum faktum) {
        faktum.setType(BRUKERREGISTRERT_FAKTUM);
        Long faktumId = repository.lagreFaktum(soknadId, faktum);
        repository.settSistLagretTidspunkt(soknadId);

        return repository.hentFaktum(soknadId, faktumId);
    }

    @Override
    public void slettBrukerFaktum(Long soknadId, Long faktumId) {
        repository.slettBrukerFaktum(soknadId, faktumId);
    }

    @Override
    public Long lagreSystemFaktum(Long soknadId, Faktum f, String uniqueProperty) {
        Faktum eksisterendeFaktum = repository.hentSystemFaktum(soknadId, f.getKey(), SYSTEMREGISTRERT_FAKTUM);
        f.setFaktumId(eksisterendeFaktum.getFaktumId());
        f.setType(SYSTEMREGISTRERT_FAKTUM);
        List<Faktum> fakta = repository.hentSystemFaktumList(soknadId, f.getKey(), SYSTEMREGISTRERT_FAKTUM);

        if (!uniqueProperty.isEmpty()) {
            for (Faktum faktum : fakta) {
                if (faktum.getProperties() != null
                        && faktum.getProperties().get(uniqueProperty) != null
                        && faktum.getProperties().get(uniqueProperty)
                                .equals(f.getProperties().get(uniqueProperty))) {
                    f.setFaktumId(faktum.getFaktumId());
                    return repository.lagreFaktum(soknadId, f, true);

                }
            }
        }
        return repository.lagreFaktum(soknadId, f, true);
    }

    @Override
    public Faktum lagreSystemSoknadsFelt(Long soknadId, String key, String value) {
        // TODO: her blir barn overskrevet. Hent ut fnr osv.
        Faktum faktum = repository.hentSystemFaktum(soknadId, key,
                SYSTEMREGISTRERT_FAKTUM);
        Faktum nyttFaktum = new Faktum(soknadId, faktum.getFaktumId(), key, value, SYSTEMREGISTRERT_FAKTUM);
        Long faktumId = repository.lagreFaktum(soknadId, nyttFaktum, true);
        return repository.hentFaktum(soknadId, faktumId);
    }

    // TODO: Kan sikkert slettes etter ny faktum-lagrings-modell
    @Override
    public Faktum lagreBarnSystemSoknadsFelt(Long soknadId, String key,
            String fnr, String json) {

        Faktum faktum = new Faktum(soknadId, null, key, json, SYSTEMREGISTRERT_FAKTUM);
        Long faktumId = repository.lagreFaktum(soknadId, faktum, true);
        return repository.hentFaktum(soknadId, faktumId);
    }

    @Override
    public void sendSoknad(long soknadId) {
        WebSoknad soknad = repository.hentSoknadMedData(soknadId);
        List<Vedlegg> vedleggForventnings = hentPaakrevdeVedlegg(soknadId);
        String skjemanummer = getSkjemanummer(soknad);
        String journalforendeEnhet = getJournalforendeEnhet(soknad);
        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer)
                .withJournalforendeEnhet(journalforendeEnhet);
        henvendelseConnector.avsluttSoknad(soknad.getBrukerBehandlingId(),
                hovedskjema,
                Transformers.convertToXmlVedleggListe(vedleggForventnings));
        repository.avslutt(soknad);

    }

    @Override
    public List<Long> hentMineSoknader(String aktorId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long hentSoknadMedBehandlinsId(String behandlingsId) {
        WebSoknad soknad = repository.hentMedBehandlingsId(behandlingsId);
        return soknad.getSoknadId();
    }

    @Override
    public void avbrytSoknad(Long soknadId) {
        WebSoknad soknad = repository.hentSoknad(soknadId);
        repository.avbryt(soknadId);
        henvendelseConnector.avbrytSoknad(soknad.getBrukerBehandlingId());
    }

    @Override
    public void endreInnsendingsvalg(Long soknadId, Faktum faktum) {
        repository.endreInnsendingsValg(soknadId, faktum.getFaktumId(), null);
    }

    @Override
    public List<Faktum> hentFakta(Long soknadId) {
        return repository.hentAlleBrukerData(soknadId);
    }

    @Override
    public String startSoknad(String navSoknadId) {
        String behandlingsId = henvendelseConnector
                .startSoknad(getSubjectHandler().getUid());
        WebSoknad soknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId).medskjemaNummer(navSoknadId)
                .
                // medAktorId(aktorIdService.hentAktorIdForFno(getSubjectHandler().getUid())).
                medAktorId(getSubjectHandler().getUid())
                .opprettetDato(DateTime.now());

        Long soknadId = repository.opprettSoknad(soknad);
        WebSoknadId websoknadId = new WebSoknadId();
        websoknadId.setId(soknadId);

        return behandlingsId;
    }

    @Override
    @Transactional
    public List<Long> splitOgLagreVedlegg(Vedlegg vedlegg,
            InputStream inputStream) {
        List<Long> resultat = new ArrayList<>();

        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            if (Detect.isImage(bytes)) {
                bytes = Convert.scaleImageAndConvertToPdf(bytes, new Dimension(
                        1240, 1754));
                Vedlegg sideVedlegg = new Vedlegg(null, vedlegg.getSoknadId(),
                        vedlegg.getFaktumId(), vedlegg.getskjemaNummer(),
                        vedlegg.getNavn(), (long) bytes.length, 1, UUID
                                .randomUUID().toString(), null,
                        Vedlegg.Status.UnderBehandling);
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
                    Vedlegg sideVedlegg = new Vedlegg(null,
                            vedlegg.getSoknadId(), vedlegg.getFaktumId(),
                            vedlegg.getskjemaNummer(), vedlegg.getNavn(),
                            (long) baos.size(), 1,
                            UUID.randomUUID().toString(), null,
                            Vedlegg.Status.UnderBehandling);
                    resultat.add(vedleggRepository.opprettVedlegg(sideVedlegg,
                            baos.toByteArray()));
                }
                document.close();
            } else {
                throw new UgyldigOpplastingTypeException(
                        "Ugyldig filtype for opplasting", null,
                        "vedlegg.opplasting.feil.filtype");
            }
        } catch (IOException | COSVisitorException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e,
                    "vedlegg.opplasting.feil.generell");
        }
        return resultat;
    }

    @Override
    public List<Vedlegg> hentVedleggUnderBehandling(Long soknadId,
            Long faktumId, String skjemaNummer) {
        return vedleggRepository.hentVedleggUnderBehandling(soknadId, faktumId,
                skjemaNummer);
    }

    @Override
    public Vedlegg hentVedlegg(Long soknadId, Long vedleggId, boolean medInnhold) {
        if (medInnhold) {
            return medKodeverk(vedleggRepository.hentVedleggMedInnhold(soknadId, vedleggId));
        } else {
            return medKodeverk(vedleggRepository.hentVedlegg(soknadId, vedleggId));
        }
    }

    @Override
    public void slettVedlegg(Long soknadId, Long vedleggId) {
        vedleggRepository.slettVedlegg(soknadId, vedleggId);
    }

    @Override
    public byte[] lagForhandsvisning(Long soknadId, Long vedleggId, int side) {
        try {
            return new ConvertToPng(new Dimension(600, 800), side)
                    .transform(IOUtils.toByteArray(vedleggRepository
                            .hentVedleggStream(soknadId, vedleggId)));
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke generere thumbnail " + e, e);
        }
    }

    @Override
    public Long genererVedleggFaktum(Long soknadId, Long vedleggId) {
        Vedlegg forventning = vedleggRepository
                .hentVedlegg(soknadId, vedleggId);
        List<Vedlegg> vedleggUnderBehandling = vedleggRepository
                .hentVedleggUnderBehandling(soknadId,
                        forventning.getFaktumId(),
                        forventning.getskjemaNummer());
        List<byte[]> bytes = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggUnderBehandling) {
            InputStream inputStream = vedleggRepository.hentVedleggStream(
                    soknadId, vedlegg.getVedleggId());
            try {
                bytes.add(IOUtils.toByteArray(inputStream));
            } catch (IOException e) {
                throw new RuntimeException("Kunne ikke merge filer", e);
            }

        }
        // vannmerk her!
        byte[] doc = new PdfMerger().transform(bytes);
        forventning.leggTilInnhold(doc, vedleggUnderBehandling.size());
        WebSoknad soknad = repository.hentSoknad(soknadId);
        fillagerConnector.lagreFil(soknad.getBrukerBehandlingId(),
                forventning.getFillagerReferanse(), soknad.getAktoerId(),
                new ByteArrayInputStream(doc));
        vedleggRepository.slettVedleggUnderBehandling(soknadId,
                forventning.getFaktumId(), forventning.getskjemaNummer());
        vedleggRepository.lagreVedleggMedData(soknadId, vedleggId, forventning);
        return vedleggId;
    }

    @Override
    public List<Vedlegg> hentPaakrevdeVedlegg(Long soknadId) {
        List<Vedlegg> forventninger = new ArrayList<>();
        WebSoknad webSoknad = hentSoknad(soknadId);
        SoknadStruktur struktur = hentStruktur(webSoknad.getskjemaNummer());

        for (Faktum faktum : repository.hentAlleBrukerData(soknadId)) {
            List<SoknadVedlegg> aktuelleVedlegg = struktur.vedleggFor(faktum.getKey());
            for (SoknadVedlegg soknadVedlegg : aktuelleVedlegg) {
                if (soknadVedlegg.trengerVedlegg(faktum.getValue())) {
                    Vedlegg vedlegg = vedleggRepository.hentVedleggForskjemaNummer(soknadId, faktum.getFaktumId(), soknadVedlegg.getskjemaNummer());
                    if (vedlegg == null) {
                        vedlegg = new Vedlegg(soknadId, faktum.getFaktumId(), soknadVedlegg.getskjemaNummer(), Vedlegg.Status.VedleggKreves);
                        vedlegg.setVedleggId(vedleggRepository.opprettVedlegg(vedlegg, null));
                    }
                    if (soknadVedlegg.getProperty() != null && faktum.getProperties().containsKey(soknadVedlegg.getProperty())) {
                        vedlegg.setNavn(faktum.getProperties().get(soknadVedlegg.getProperty()));
                        vedleggRepository.lagreVedlegg(soknadId, vedlegg.getVedleggId(), vedlegg);
                    }
                    forventninger.add(medKodeverk(vedlegg));
                }
            }
        }

        return forventninger;
    }

    private Vedlegg medKodeverk(Vedlegg vedlegg) {
        try {
            Map<Kodeverk.Nokkel,String> koder = kodeverk.getKoder(vedlegg.getskjemaNummer());
            for (Entry<Nokkel, String> nokkelEntry : koder.entrySet()) {
                if(nokkelEntry.getKey().toString().contains("URL")){
                    vedlegg.leggTilURL(nokkelEntry.getKey().toString(), koder.get(nokkelEntry.getKey()));
                }
            }
            vedlegg.setTittel(koder.get(Kodeverk.Nokkel.TITTEL));
        } catch (Exception ignore) {

        }
        return vedlegg;
    }

    @Override
    public void lagreVedlegg(Long soknadId, Long vedleggId, Vedlegg vedlegg) {
        vedleggRepository.lagreVedlegg(soknadId, vedleggId, vedlegg);
    }

    private SoknadStruktur hentStruktur(String skjema) {
        String type = skjema + ".xml";
        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class)
                    .createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class
                    .getResourceAsStream(format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }

    }
}
