package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.ApplicationException;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.detect.pdf.PdfDetector;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.sort;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggKreves;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class VedleggOriginalFilerService {
    private static final Logger logger = getLogger(VedleggOriginalFilerService.class);

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    @Named("vedleggRepository")
    private VedleggRepository vedleggRepository;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private VedleggService vedleggService;

    @Inject
    private SoknadService soknadService;

    private static Map<String, String> MIME_TIL_EXT;

    @PostConstruct
    public void setUp() {
        MIME_TIL_EXT = new HashMap<>();
        MIME_TIL_EXT.put("application/pdf", ".pdf");
        MIME_TIL_EXT.put("image/png", ".png");
        MIME_TIL_EXT.put("image/jpeg", ".jpg");
    }

    public WebSoknad oppdaterVedleggOgBelopFaktum(String behandlingsId) {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, true);

        List<FaktumStruktur> faktaStruktur = soknadService.hentSoknadStruktur(soknad.getskjemaNummer()).getFakta();
        sort(faktaStruktur, sammenlignEtterDependOn());

        List<FaktumStruktur> opplysninger = faktaStruktur.stream()
                .filter(struktur -> struktur.getId().startsWith("opplysninger."))
                .filter(struktur -> soknad.getFaktaMedKey(struktur.getId()).isEmpty())
                .collect(Collectors.toList());

        Iterator<Long> faktumIder = repository.hentLedigeFaktumIder(opplysninger.size()).iterator();

        List<Faktum> nyeFakta = new ArrayList<>();

        for (FaktumStruktur struktur : opplysninger) {
            Long parentFaktumId = null;

            if (struktur.getDependOn() != null) {
                if (soknad.getFaktumMedKey(struktur.getDependOn().getId()) != null) {
                    parentFaktumId = soknad.getFaktumMedKey(struktur.getDependOn().getId()).getFaktumId();
                } else {
                    parentFaktumId = nyeFakta.stream()
                            .filter(f -> f.getKey().equals(struktur.getDependOn().getId()))
                            .findFirst()
                            .map(Faktum::getFaktumId)
                            .orElse(null);
                }
            }

            nyeFakta.add(new Faktum()
                    .medFaktumId(faktumIder.next())
                    .medParrentFaktumId(parentFaktumId)
                    .medKey(struktur.getId())
                    .medType(BRUKERREGISTRERT)
                    .medSoknadId(soknad.getSoknadId()));
        }

        repository.batchOpprettTommeFakta(nyeFakta);

        settSamvarsStatus(soknad);

        WebSoknad soknadOppdatert = soknadService.hentSoknad(behandlingsId, true, true);
        return soknadOppdatert;
    }

    private void settSamvarsStatus(WebSoknad soknad) {
        boolean skalBeOmSamvaersavtale = false;

        List<Faktum> barneFakta = soknad.getFaktaMedKey("system.familie.barn.true.barn");

        for (Faktum barn : barneFakta) {
            Map<String, String> props = barn.getProperties();
            boolean borIkkeSammen = "false".equals(props.get("folkeregistrertsammen"));
            int grad = 100;
            try {
                grad = Integer.parseInt(props.get("grad"));
            } catch (NumberFormatException e) {
                // ignore
            }
            if (borIkkeSammen && grad <= 50) {
                skalBeOmSamvaersavtale = true;
                break;
            }
        }

        Faktum avtaleFaktum = soknad.getFaktumMedKey("opplysninger.familiesituasjon.barn.samvarsavtale");
        avtaleFaktum.medProperty("skalvises", skalBeOmSamvaersavtale ? "true" : "false");
        faktaService.lagreBrukerFaktum(avtaleFaktum);
    }

    /**
     * Har egne vedleggsfakta som peker på faktumet som sendes inn
     * Om det bare finnes 1 slik, kan vi gjenbruke det og vedleggsforventningen
     * gitt at det ikke allerede er lastet opp. Om ikke, eller det finnes flere,
     * lager vi ny
     */
    public Forventning lagEllerFinnVedleggsForventning(Long faktumId) {
        String behandlingsId = faktaService.hentBehandlingsId(faktumId);
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, false);

        Faktum faktum = soknad.getFaktumMedId(faktumId + "");
        String vedleggKey = faktum.getKey() + ".vedlegg";
        List<Faktum> vedleggsFakta = soknad.getFaktaMedKeyOgParentFaktum(vedleggKey, faktumId);

        if (vedleggsFakta.size() == 0) {
            throw new ApplicationException("Kan ikke finne vedleggsfaktum, disse må genereres først");
        }

        Faktum vedleggFaktum = vedleggsFakta.get(0);
        Vedlegg vedlegg = vedleggService.hentPaakrevdeVedlegg(vedleggFaktum.getFaktumId()).get(0);

        if (vedleggsFakta.size() == 1 && vedlegg.getInnsendingsvalg().er(VedleggKreves)) {
            return new Forventning(vedleggFaktum, vedlegg, false);
        } else {
            Faktum nyttVedleggFaktum = new Faktum()
                    .medKey(vedleggKey)
                    .medParrentFaktumId(faktumId);
            nyttVedleggFaktum = faktaService.opprettBrukerFaktum(soknad.getBrukerBehandlingId(), nyttVedleggFaktum);
            Vedlegg nyttVedlegg = vedleggService.hentPaakrevdeVedlegg(nyttVedleggFaktum.getFaktumId()).get(0);

            return new Forventning(nyttVedleggFaktum, nyttVedlegg, true);
        }
    }


    public void leggTilOriginalVedlegg(Vedlegg vedlegg, byte[] data, String filnavn) {
        WebSoknad soknad = repository.hentSoknad(vedlegg.getSoknadId());

        leggTilOriginalVedlegg(vedlegg, data, filnavn, soknad);
    }

    public void leggTilOriginalVedlegg(Vedlegg vedlegg, byte[] data, String filnavn, WebSoknad soknad) {
        String contentType = Detect.CONTENT_TYPE.transform(data);
        vedlegg.leggTilInnhold(data, 1);
        vedlegg.setMimetype(contentType);
        vedlegg.setFilnavn(lagFilnavn(filnavn, contentType, vedlegg.getFillagerReferanse()));

        logger.info("Lagrer originalfil for behandlingsid {}, UUID: {}", soknad.getBrukerBehandlingId(), vedlegg.getFillagerReferanse());
        fillagerService.lagreFil(soknad.getBrukerBehandlingId(), vedlegg.getFillagerReferanse(), soknad.getAktoerId(), new ByteArrayInputStream(data));
        vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
    }

    public Vedlegg slettOriginalVedlegg(Long vedleggId) {
        Vedlegg vedlegg = vedleggRepository.hentVedlegg(vedleggId);
        Long soknadId = vedlegg.getSoknadId();
        WebSoknad soknad = repository.hentSoknadMedData(soknadId);

        Long faktumId = vedlegg.getFaktumId();
        Faktum faktum = soknad.getFaktumMedId(faktumId + "");

        List<Faktum> fakta = soknad.getFaktaMedKey(faktum.getKey());

        if (fakta.size() == 1) {
            vedlegg.fjernInnhold();
            vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), vedleggId, vedlegg);

            fillagerService.slettFil(vedlegg.getFillagerReferanse());
            return vedleggRepository.hentVedlegg(vedleggId);
        } else {
            vedleggRepository.slettVedleggMedVedleggId(vedleggId);
            repository.slettBrukerFaktum(soknad.getSoknadId(), faktumId);

            fillagerService.slettFil(vedlegg.getFillagerReferanse());
            return null;
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

    public void validerFil(byte[] data) {
        if (!(Detect.isImage(data) || Detect.isPdf(data))) {
            throw new UgyldigOpplastingTypeException(
                    "Ugyldig filtype for opplasting", null,
                    "opplasting.feilmelding.feiltype");
        }
        if (Detect.isPdf(data)) {
            sjekkOmPdfErGyldig(data);
        }
    }


    protected String lagFilnavn(String opplastetNavn, String mimetype, String uuid) {
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

    public static class Forventning {
        public final Faktum faktum;
        public final Vedlegg vedlegg;
        public final boolean nyForventning;

        public Forventning(Faktum faktum, Vedlegg vedlegg, boolean nyForventning) {
            this.faktum = faktum;
            this.vedlegg = vedlegg;
            this.nyForventning = nyForventning;
        }
    }

}
