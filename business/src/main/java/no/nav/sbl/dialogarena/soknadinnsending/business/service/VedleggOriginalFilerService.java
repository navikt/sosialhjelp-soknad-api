package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

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

    private static Map<String, String> MIME_TIL_EXT;

    @PostConstruct
    public void setUp() {
        MIME_TIL_EXT = new HashMap<>();
        MIME_TIL_EXT.put("application/pdf", ".pdf");
        MIME_TIL_EXT.put("image/png", ".png");
        MIME_TIL_EXT.put("image/jpeg", ".jpg");
    }


    public void leggTilOriginalVedlegg(String behandlingsId, Long vedleggId, byte[] data, String filnavn) {
        validerFil(data);

        WebSoknad soknad = repository.hentSoknad(behandlingsId);
        Vedlegg vedlegg = vedleggRepository.hentVedlegg(vedleggId);

        String contentType = Detect.CONTENT_TYPE.transform(data);
        vedlegg.leggTilInnhold(data, 1);
        vedlegg.setMimetype(contentType);
        vedlegg.setFilnavn(lagFilnavn(filnavn, contentType, vedlegg.getFillagerReferanse()));

        logger.info("Lagrer originalfil til henvendelse for behandling {}, UUID: {}", behandlingsId, vedlegg.getFillagerReferanse());
        fillagerService.lagreFil(behandlingsId, vedlegg.getFillagerReferanse(), soknad.getAktoerId(), new ByteArrayInputStream(data));
        vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), vedleggId, vedlegg);
    }

    private void validerFil(byte[] data) {
        if (!(Detect.isImage(data) || Detect.isPdf(data))) {
            throw new UgyldigOpplastingTypeException(
                    "Ugyldig filtype for opplasting", null,
                    "opplasting.feilmelding.feiltype");
        }

    }

    protected String lagFilnavn(String opplastetNavn, String mimetype, String uuid) {
        String filnavn = opplastetNavn;
        int separator = opplastetNavn.lastIndexOf(".");
        if (separator != -1) {
            filnavn = opplastetNavn.substring(0, separator);
        }
        if (filnavn.length() > 50) {
            filnavn = filnavn.substring(0, 50);
        }

        filnavn += "-" + uuid.split("-")[0];
        filnavn += MIME_TIL_EXT.get(mimetype);

        return filnavn;
    }

}
