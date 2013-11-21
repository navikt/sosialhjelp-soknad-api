package no.nav.sbl.dialogarena.soknadinnsending.business.service;

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
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Component
public class SoknadService implements SendSoknadService {

    private static final Logger logger = LoggerFactory.getLogger(SoknadService.class);

    private static final String BRUKERREGISTRERT_FAKTUM = "BRUKERREGISTRERT";
    private static final String SYSTEMREGISTRERT_FAKTUM = "SYSTEMREGISTRERT";


    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;
    @Inject
    @Named("vedleggRepository")
    private VedleggRepository vedleggRepository;

    @Override
    public WebSoknad hentSoknad(long soknadId) {
        return repository.hentSoknadMedData(soknadId);
    }

    @Override
    public void lagreSoknadsFelt(long soknadId, String key, String value) {
        repository.lagreFaktum(soknadId, new Faktum(soknadId, key, value, BRUKERREGISTRERT_FAKTUM));

    }

    @Override
    public void lagreSystemSoknadsFelt(long soknadId, String key, String value) {
        repository.lagreFaktum(soknadId, new Faktum(soknadId, key, value, SYSTEMREGISTRERT_FAKTUM));
    }

    @Override
    public void sendSoknad(long soknadId) {
        repository.avslutt(new WebSoknad().medId(soknadId));

    }

    @Override
    public List<Long> hentMineSoknader(String aktorId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void avbrytSoknad(Long soknadId) {
        //TODO: Refaktorerer. Trenger bare å sende id
        repository.avbryt(soknadId);
    }

    public Long startSoknad(String navSoknadId) {
        logger.debug("Starter ny søknad");
        //TODO: Sende et signal til Henvendelse om at søknaden er startet
        String behandlingsId = UUID.randomUUID().toString();
        logger.debug("Start søknad");

        //TODO-KJ: Denne operasjonen er ikke klar enda.Vil kommenteres inn når den er
//        try {
//            behandlingsId = sendSoknadService.startBehandling(navSoknadId);
//        } catch (SOAPFaultException e) {
//            logger.error("Feil ved oppretting av søknad med ID", navSoknadId, e);
//            throw new ApplicationException("Kunne ikke opprette ny søknad", e);
//        }
//
        WebSoknad soknad = WebSoknad.startSoknad().
                medBehandlingId(behandlingsId).
                medGosysId(navSoknadId).
                medAktorId(SubjectHandler.getSubjectHandler().getUid()).
                opprettetDato(DateTime.now());
        return repository.opprettSoknad(soknad);
    }

    public static final String PDF_PDFA = "-dNOPAUSE -dBATCH -dSAFER -dPDFA -dNOGA -sDEVICE=pdfwrite -sOutputFile=%stdout%  -q -c \"30000000 setvmthreshold\" -_ -c quit";
    private static final String IMAGE_RESIZE = "- -units PixelsPerInch -density 150 -quality 50 -resize 1240x1754 jpeg:-";

    private static final String IMAGE_PDFA = "-  pdfa:-";


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
            bytes = new PdfWatermarker().applyOn(bytes, "TEST-IDENT MOCK");
            return vedleggRepository.lagreVedlegg(vedlegg, bytes);
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke lagre vedlegg: " + e, e);
        }
    }

    public void slettVedlegg(Long soknadId, Long vedleggId) {
        vedleggRepository.slettVedlegg(soknadId, vedleggId);
    }

    public byte[] lagForhandsvisning(Long soknadId, Long vedleggId) {
        try {
            return new ConvertToPng(new Dimension(600, 800), ImageScaler.ScaleMode.SCALE_TO_FIT_INSIDE_BOX)
                    .transform(IOUtils.toByteArray(vedleggRepository.hentVedlegg(soknadId, vedleggId)));
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke generere thumbnail " + e, e);
        }
    }

    public Long genererVedleggFaktum(Long soknadId, Long faktumId) {
        List<Vedlegg> vedleggs = vedleggRepository.hentVedleggForFaktum(soknadId, faktumId);
        List<byte[]> bytes = new ArrayList();
        for (Vedlegg vedlegg : vedleggs) {
            InputStream inputStream = vedleggRepository.hentVedlegg(soknadId, vedlegg.getId());
            try {
                bytes.add(IOUtils.toByteArray(inputStream));
            } catch (IOException e) {
                throw new RuntimeException("Kunne ikke merge filer", e);
            }

        }
        byte[] doc = new PdfMerger().transform(bytes);
        Vedlegg vedlegg = new Vedlegg();
        vedlegg.setNavn("faktum.pdf");
        vedlegg.setSoknadId(soknadId);
        vedlegg.setFaktum(faktumId);
        vedlegg.setStorrelse(doc.length);
        vedleggRepository.slettVedleggForFaktum(soknadId, faktumId);
        Long opplastetDokument = vedleggRepository.lagreVedlegg(vedlegg, doc);
        vedleggRepository.knyttVedleggTilFaktum(soknadId, faktumId, opplastetDokument);
        return opplastetDokument;
    }
}
