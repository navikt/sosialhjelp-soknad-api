package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.EttersendelseVedleggService.EttersendelseVedlegg.Fil;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class EttersendelseVedleggService {

    private static final Logger logger = getLogger(EttersendelseVedleggService.class);

    @Inject
    private SoknadService soknadService;

    @Inject
    private VedleggService vedleggService;

    @Inject
    private VedleggRepository vedleggRepository;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private VedleggOriginalFilerService vedleggOriginalFilerService;


    public static class EttersendelseVedlegg {
        public long vedleggId;
        public String skjemaNummer;
        public String skjemanummerTillegg;
        public Status innsendingsvalg;
        public List<Fil> filer = new ArrayList<>();


        public static class Fil {
            public long filId;
            public String filnavn;

            public Fil() {
            }

            public Fil(long filId, String filnavn) {
                this.filId = filId;
                this.filnavn = filnavn;
            }
        }
    }

    public List<EttersendelseVedlegg> hentVedleggForSoknad(String behandlingsId) {

        WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, true);
        List<Vedlegg> originaleVedlegg = webSoknad.getVedlegg();

        Map<String, EttersendelseVedlegg> ettersendelseVedlegg = new HashMap<>();

        originaleVedlegg.stream().forEach(vedlegg -> {
            String sammensattNavn = vedlegg.getSkjemaNummer() + "|" + vedlegg.getSkjemanummerTillegg();

            if (!ettersendelseVedlegg.containsKey(sammensattNavn)) {
                EttersendelseVedlegg ettersendelse = new EttersendelseVedlegg();
                ettersendelse.vedleggId = vedlegg.getVedleggId();
                ettersendelse.skjemaNummer = vedlegg.getSkjemaNummer();
                ettersendelse.skjemanummerTillegg = vedlegg.getSkjemanummerTillegg();
                ettersendelse.innsendingsvalg = vedlegg.getInnsendingsvalg();

                ettersendelseVedlegg.put(sammensattNavn, ettersendelse);
            }

            if (vedlegg.getInnsendingsvalg().er(Status.LastetOpp)) {
                ettersendelseVedlegg.get(sammensattNavn).filer.add(new Fil(vedlegg.getVedleggId(), vedlegg.getFilnavn()));
            }
        });

        return new ArrayList<>(ettersendelseVedlegg.values());
    }

    public List<EttersendelseVedlegg> lastOppVedlegg(Long vedleggId, byte[] data, String filnavn) {
        String behandlingsId = vedleggService.hentBehandlingsId(vedleggId);
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, false);

        Vedlegg originalVedlegg = soknad.getVedlegg()
                .stream().filter(v -> v.getVedleggId().equals(vedleggId))
                .findFirst().get();

        if (originalVedlegg.getInnsendingsvalg().er(Status.LastetOpp)) {
            Vedlegg nyttVedlegg = new Vedlegg()
                    .medSoknadId(soknad.getSoknadId())
                    .medSkjemaNummer(originalVedlegg.getSkjemaNummer())
                    .medSkjemanummerTillegg(originalVedlegg.getSkjemanummerTillegg());

            vedleggRepository.opprettEllerEndreVedlegg(nyttVedlegg, null);
            vedleggOriginalFilerService.leggTilOriginalVedlegg(nyttVedlegg, data, filnavn, soknad);
        } else {
            vedleggOriginalFilerService.leggTilOriginalVedlegg(originalVedlegg, data, filnavn, soknad);
        }

        return hentVedleggForSoknad(behandlingsId);
    }

    // slett
    // filid kan være likt vedleggid...
    // finn alle vedlegg som er av samme type som den for filid
    // om det er flere > 1, bare slett vedlegget med filID
    // om det er 1, slett data og sett status
    public List<EttersendelseVedlegg> slettVedlegg() {
        return null;
    }

}
