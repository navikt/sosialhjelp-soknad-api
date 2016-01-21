package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import javax.inject.*;

@Component
public class SoknadService {

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository lokalDb;

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private EttersendingService ettersendingService;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private WebSoknadConfig config;

    @Inject
    private SoknadDataFletter soknadDataFletter;

    public void settDelsteg(String behandlingsId, DelstegStatus delstegStatus) {
        lokalDb.settDelstegstatus(behandlingsId, delstegStatus);
    }

    public void settJournalforendeEnhet(String behandlingsId, String journalforendeEnhet) {
        lokalDb.settJournalforendeEnhet(behandlingsId, journalforendeEnhet);
    }

    public WebSoknad hentSoknadFraLokalDb(long soknadId) {
        return lokalDb.hentSoknad(soknadId);
    }

    public SoknadStruktur hentSoknadStruktur(String skjemanummer) {
        return config.hentStruktur(skjemanummer);
    }

    public WebSoknad hentEttersendingForBehandlingskjedeId(String behandlingsId) {
        return lokalDb.hentEttersendingMedBehandlingskjedeId(behandlingsId).orNull();
    }

    @Transactional
    public String startSoknad(String skjemanummer) {
        return soknadDataFletter.startSoknad(skjemanummer);
    }

    @Transactional
    public void avbrytSoknad(String behandlingsId) {
        WebSoknad soknad = lokalDb.hentSoknad(behandlingsId);

        /**
         * Sletter alle vedlegg til søknader som blir avbrutt.
         * Dette burde egentlig gjøres i henvendelse, siden vi uansett skal slette alle vedlegg på avbrutte søknader.
         * I tillegg blir det liggende igjen mange vedlegg for søknader som er avbrutt før dette kallet ble lagt til.
         * */
        fillagerService.slettAlle(soknad.getBrukerBehandlingId());
        henvendelseService.avbrytSoknad(soknad.getBrukerBehandlingId());
        lokalDb.slettSoknad(soknad.getSoknadId());
    }

    public String startEttersending(String behandlingsIdSoknad) {
        return ettersendingService.start(behandlingsIdSoknad);
    }

    public WebSoknad hentSoknad(String behandlingsId, boolean medData, boolean medVedlegg) {
        return soknadDataFletter.hentSoknad(behandlingsId, medData, medVedlegg);
    }

    public Faktum hentSprak(long soknadId) {
        return lokalDb.hentFaktumMedKey(soknadId, "skjema.sprak");
    }

    public Long hentOpprinneligInnsendtDato(String behandlingsId) {
        return soknadDataFletter.hentOpprinneligInnsendtDato(behandlingsId);
    }

    public String hentSisteInnsendteBehandlingsId(String behandlingsId) {
        return soknadDataFletter.hentSisteInnsendteBehandlingsId(behandlingsId);
    }

    @Transactional
    public void sendSoknad(String behandlingsId, byte[] pdf) {
        soknadDataFletter.sendSoknad(behandlingsId, pdf);
    }

}