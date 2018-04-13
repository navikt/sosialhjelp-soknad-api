package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class EttersendelseVedleggService {

    private static final Logger logger = getLogger(EttersendelseVedleggService.class);

    @Inject
    private SoknadService soknadService;


    public static class EttersendelseVedlegg {
        public long vedleggId;
        public String skjemanummerTillegg;
        public Vedlegg.Status innsendingsvalg;
        public String skjemaNummer;
        public List<Filer> filer = new ArrayList<>();


        public static class Filer {
            public long filId;
            public String filnavn;
        }
    }

    // hent
    // bare finn alle vedlegg rått, og merge til riktig struktur
    public List<EttersendelseVedlegg> hentVedleggForSoknad(String behandlingsId) {

        WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, true);
        webSoknad.getVedlegg(); // TODO

        return null;
    }


    // lastOpp
    // finn vedlegg på id
    // om vedlegg er lastet opp, lag en kopi
    // hent soknad på vedlegg?
    public List<EttersendelseVedlegg> lastOppVedlegg(Long faktumId, byte[] data, String filnavn) {
        return null;
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
