package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.detect.IsImage;
import no.nav.sbl.dialogarena.detect.IsPdf;
import no.nav.sbl.dialogarena.pdf.ConvertToPng;
import no.nav.sbl.dialogarena.pdf.ImageScaler;
import no.nav.sbl.dialogarena.pdf.ImageToPdf;
import no.nav.sbl.dialogarena.pdf.PdfMerger;
import no.nav.sbl.dialogarena.pdf.PdfWatermarker;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.VedleggForventning;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.Status.LastetOpp;

@Component
public class SoknadService implements SendSoknadService, VedleggService {

    public static final String PDF_PDFA = "-dNOPAUSE -dBATCH -dSAFER -dPDFA -dNOGA -sDEVICE=pdfwrite -sOutputFile=%stdout%  -q -c \"30000000 setvmthreshold\" -_ -c quit";
    private static final String BRUKERREGISTRERT_FAKTUM = "BRUKERREGISTRERT";
    private static final String SYSTEMREGISTRERT_FAKTUM = "SYSTEMREGISTRERT";
    private static final String IMAGE_RESIZE = "- -units PixelsPerInch -density 150 -quality 50 -resize 1240x1754 jpeg:-";
    private static final String IMAGE_PDFA = "-  pdfa:-";
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
        List<Faktum> fakta = repository.hentSystemFaktumList(soknadId, f.getKey(), FaktumType.SYSTEMREGISTRERT.toString());

        if (!uniqueProperty.isEmpty()) {
            for (Faktum faktum : fakta) {
                if (faktum.getProperties().get(uniqueProperty).equals(f.getProperties().get(uniqueProperty))) {
                    f.setFaktumId(faktum.getFaktumId());
                    return repository.lagreFaktum(soknadId, f);

                }
            }
        }
        return repository.lagreFaktum(soknadId, f);
    }

    @Override
    public Faktum lagreSystemSoknadsFelt(Long soknadId, String key, String value) {
        //TODO: her blir barn overskrevet. Hent ut fnr osv.
        Faktum faktum = repository.hentSystemFaktum(soknadId, key, SYSTEMREGISTRERT_FAKTUM);

        Long faktumId = repository.lagreFaktum(soknadId, new Faktum(soknadId, faktum.getFaktumId(), key, value, SYSTEMREGISTRERT_FAKTUM));
        return repository.hentFaktum(soknadId, faktumId);
    }

    //TODO: Kan sikkert slettes etter ny faktum-lagrings-modell
    @Override
    public Faktum lagreBarnSystemSoknadsFelt(Long soknadId, String key, String fnr, String json) {

        Long faktumId = repository.lagreFaktum(soknadId, new Faktum(soknadId, null, key, json, SYSTEMREGISTRERT_FAKTUM));
        return repository.hentFaktum(soknadId, faktumId);
    }

    @Override
    public void sendSoknad(long soknadId) {
        WebSoknad soknad = repository.hentSoknadMedData(soknadId);
        List<VedleggForventning> vedleggForventnings = hentPaakrevdeVedlegg(soknadId);
        henvendelseConnector.avsluttSoknad(soknad.getBrukerBehandlingId(), new XMLHovedskjema(), Transformers.convertToXmlVedleggListe(vedleggForventnings));
        repository.avslutt(soknad);

    }

    @Override
    public List<Long> hentMineSoknader(String aktorId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void avbrytSoknad(Long soknadId) {
        WebSoknad soknad = repository.hentSoknad(soknadId);
        repository.avbryt(soknadId);
        henvendelseConnector.avbrytSoknad(soknad.getBrukerBehandlingId());
    }

    @Override
    public void endreInnsendingsvalg(Long soknadId, Faktum faktum) {
        repository.endreInnsendingsValg(soknadId, faktum.getFaktumId(), faktum.getInnsendingsvalg());
    }

    @Override
    public List<Faktum> hentFakta(Long soknadId) {
        return repository.hentAlleBrukerData(soknadId);
    }

    @Override
    public Long startSoknad(String navSoknadId) {
        String behandlingsId = henvendelseConnector.startSoknad(getSubjectHandler().getUid(), navSoknadId);

        WebSoknad soknad = WebSoknad.startSoknad().
                medBehandlingId(behandlingsId).
                medGosysId(navSoknadId).
                medAktorId(getSubjectHandler().getUid()).
                opprettetDato(DateTime.now());
        return repository.opprettSoknad(soknad);
    }

    @Override
    public Long lagreVedlegg(Vedlegg vedlegg, InputStream inputStream) {
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            if (new IsImage().evaluate(bytes)) {
                if (ScriptRunner.IM_EXISTS) {
                    bytes = new ScriptRunner(ScriptRunner.Type.IM, IMAGE_RESIZE, null, new ByteArrayInputStream(bytes)).call();
                    bytes = new ScriptRunner(ScriptRunner.Type.IM, IMAGE_PDFA, null, new ByteArrayInputStream(bytes)).call();
                } else {
                    bytes = new ImageToPdf().transform(bytes);
                }
            } else if (new IsPdf().evaluate(bytes)) {
                if (ScriptRunner.GS_EXISTS) {
                    bytes = new ScriptRunner(ScriptRunner.Type.GS, PDF_PDFA, null, new ByteArrayInputStream(bytes)).call();
                }
            }
            bytes = new PdfWatermarker().applyOn(bytes, SubjectHandler.getSubjectHandler().getUid());
            return vedleggRepository.lagreVedlegg(vedlegg, bytes);
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke lagre vedlegg: " + e, e);
        }
    }

    @Override
    public List<Vedlegg> hentVedleggForFaktum(Long soknadId, Long faktumId) {
        return vedleggRepository.hentVedleggForFaktum(soknadId, faktumId);
    }

    @Override
    public Vedlegg hentVedlegg(Long soknadId, Long vedleggId, boolean medInnhold) {
        if (medInnhold) {
            return vedleggRepository.hentVedleggMedInnhold(soknadId, vedleggId);
        } else {
            return vedleggRepository.hentVedlegg(soknadId, vedleggId);
        }
    }

    @Override
    public void slettVedlegg(Long soknadId, Long vedleggId) {
        vedleggRepository.slettVedlegg(soknadId, vedleggId);
    }

    @Override
    public byte[] lagForhandsvisning(Long soknadId, Long vedleggId, int side) {
        try {
            return new ConvertToPng(new Dimension(600, 800), ImageScaler.ScaleMode.SCALE_TO_FIT_INSIDE_BOX, side)
                    .transform(IOUtils.toByteArray(vedleggRepository.hentVedleggStream(soknadId, vedleggId)));
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke generere thumbnail " + e, e);
        }
    }

    @Override
    public Long genererVedleggFaktum(Long soknadId, Long faktumId) {
        List<Vedlegg> vedleggs = vedleggRepository.hentVedleggForFaktum(soknadId, faktumId);
        List<byte[]> bytes = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggs) {
            InputStream inputStream = vedleggRepository.hentVedleggStream(soknadId, vedlegg.getId());
            try {
                bytes.add(IOUtils.toByteArray(inputStream));
            } catch (IOException e) {
                throw new RuntimeException("Kunne ikke merge filer", e);
            }

        }
        byte[] doc = new PdfMerger().transform(bytes);
        Vedlegg vedlegg = new Vedlegg(null, soknadId, faktumId, "faktum.pdf", (long) doc.length, vedleggs.size(), UUID.randomUUID().toString(), doc);
        fillagerConnector.lagreFil(vedlegg.getFillagerReferanse(), new ByteArrayInputStream(doc));
        vedleggRepository.slettVedleggForFaktum(soknadId, faktumId);
        Long opplastetDokument = vedleggRepository.lagreVedlegg(vedlegg, doc);
        vedleggRepository.knyttVedleggTilFaktum(soknadId, faktumId, opplastetDokument);
        return opplastetDokument;
    }

    @Override
    public List<VedleggForventning> hentPaakrevdeVedlegg(Long soknadId) {
        List<VedleggForventning> forventninger = new ArrayList<>();
        WebSoknad webSoknad = hentSoknad(soknadId);
        SoknadStruktur struktur = hentStruktur(webSoknad.getGosysId());

        for (Faktum faktum : webSoknad.getFakta().values()) {
            SoknadVedlegg soknadVedlegg = struktur.vedleggFor(faktum.getKey());
            if (soknadVedlegg != null && soknadVedlegg.trengerVedlegg(faktum.getValue())) {
                Vedlegg vedlegg = faktum.getInnsendingsvalg().er(LastetOpp) ? vedleggRepository.hentVedlegg(soknadId, faktum.getVedleggId()) : null;
                forventninger.add(new VedleggForventning(faktum, vedlegg, soknadVedlegg.getGosysId()));
            }
        }

        return forventninger;
    }

    private SoknadStruktur hentStruktur(String skjema) {
        String type = skjema + ".xml";
        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class).createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class
                    .getResourceAsStream(format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }

    }
}
