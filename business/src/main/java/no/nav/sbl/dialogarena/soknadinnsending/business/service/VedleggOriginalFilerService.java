package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
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
import java.util.List;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggKreves;
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

    private static Map<String, String> MIME_TIL_EXT;

    @PostConstruct
    public void setUp() {
        MIME_TIL_EXT = new HashMap<>();
        MIME_TIL_EXT.put("application/pdf", ".pdf");
        MIME_TIL_EXT.put("image/png", ".png");
        MIME_TIL_EXT.put("image/jpeg", ".jpg");
    }

    /**
     * Har egne vedleggsfakta som peker på faktumet som sendes inn
     * Om det bare finnes 1 slik, kan vi gjenbruke det og vedleggsforventningen
     * gitt at det ikke allerede er lastet opp. Om ikke, eller det finnes flere,
     * lager vi ny
     */
    public Vedlegg lagEllerFinnVedleggsForventning(Long faktumId) {
        String behandlingsId = faktaService.hentBehandlingsId(faktumId);
        WebSoknad soknad = repository.hentSoknad(behandlingsId);

        Faktum faktum = soknad.getFaktumMedId(faktumId + "");
        String vedleggKey = faktum.getKey() + ".vedlegg";
        List<Faktum> vedleggsFakta = soknad.getFaktaMedKeyOgParentFaktum(vedleggKey, faktumId);

        if (vedleggsFakta.size() == 0) {
            throw new ApplicationException("Kan ikke finne vedleggsfaktum, disse må genereres først");
        }

        Faktum vedleggFaktum = vedleggsFakta.get(0);
        Vedlegg vedlegg = vedleggService.hentPaakrevdeVedlegg(vedleggFaktum.getFaktumId()).get(0);

        if (vedleggsFakta.size() == 1 && vedlegg.getInnsendingsvalg().er(VedleggKreves)) {
            return vedlegg;
        } else {
            Faktum nyttVedleggFaktum = new Faktum()
                    .medKey(vedleggKey)
                    .medParrentFaktumId(faktumId);
            faktaService.opprettBrukerFaktum(soknad.getBrukerBehandlingId(), nyttVedleggFaktum);

            return vedleggService.hentPaakrevdeVedlegg(nyttVedleggFaktum.getFaktumId()).get(0);
        }
    }


    public void leggTilOriginalVedlegg(Vedlegg vedlegg, byte[] data, String filnavn) {
        validerFil(data);

        WebSoknad soknad = repository.hentSoknad(vedlegg.getSoknadId());

        String contentType = Detect.CONTENT_TYPE.transform(data);
        vedlegg.leggTilInnhold(data, 1);
        vedlegg.setMimetype(contentType);
        vedlegg.setFilnavn(lagFilnavn(filnavn, contentType, vedlegg.getFillagerReferanse()));

        logger.info("Lagrer originalfil til henvendelse for behandling {}, UUID: {}", soknad.getBrukerBehandlingId(), vedlegg.getFillagerReferanse());
        fillagerService.lagreFil(soknad.getBrukerBehandlingId(), vedlegg.getFillagerReferanse(), soknad.getAktoerId(), new ByteArrayInputStream(data));
        vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
    }

    public void slettOriginalVedlegg(Long vedleggId) {
        Vedlegg vedlegg = vedleggRepository.hentVedlegg(vedleggId);
        Long soknadId = vedlegg.getSoknadId();
        WebSoknad soknad = repository.hentSoknad(soknadId);

        Long faktumId = vedlegg.getFaktumId();
        Faktum faktum = soknad.getFaktumMedId(faktumId + "");

        List<Faktum> fakta = soknad.getFaktaMedKey(faktum.getKey());

        if (fakta.size() == 1) {
            vedlegg.fjernInnhold();
            vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), vedleggId, vedlegg);
        } else {
            vedleggRepository.slettVedleggMedVedleggId(vedleggId);
            repository.slettBrukerFaktum(soknad.getSoknadId(), faktumId);
        }

        fillagerService.slettFil(vedlegg.getFillagerReferanse());
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
