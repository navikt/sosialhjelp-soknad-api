package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

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

    @Inject
    private SoknadMetricsService soknadMetricsService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    public void settDelsteg(String behandlingsId, DelstegStatus delstegStatus) {
        lokalDb.settDelstegstatus(behandlingsId, delstegStatus);
    }

    public void settSistLagret(long soknadId) {
        lokalDb.settSistLagretTidspunkt(soknadId);
    }

    public void settJournalforendeEnhet(String behandlingsId, String journalforendeEnhet) {
        lokalDb.settJournalforendeEnhet(behandlingsId, journalforendeEnhet);
    }

    public void logForskjeller(SoknadUnderArbeid soknadUnderArbeid1, SoknadUnderArbeid soknadUnderArbeid2, String melding){
        soknadDataFletter.logDersomForskjellMellomFaktumOgNyModell(soknadUnderArbeid1, soknadUnderArbeid2, melding);
    }

    public void sortOkonomi(SoknadUnderArbeid soknadUnderArbeid1, SoknadUnderArbeid soknadUnderArbeid2){
        JsonSoknad soknad = soknadUnderArbeid1.getJsonInternalSoknad().getSoknad();
        JsonSoknad soknadKonvertert = soknadUnderArbeid2.getJsonInternalSoknad().getSoknad();
        soknadDataFletter.sortOkonomi(soknad.getData().getOkonomi());
        soknadDataFletter.sortOkonomi(soknadKonvertert.getData().getOkonomi());
    }

    public WebSoknad hentSoknadFraLokalDb(long soknadId) {
        return lokalDb.hentSoknad(soknadId);
    }

    public SoknadStruktur hentSoknadStruktur(String skjemanummer) {
        return config.hentStruktur(skjemanummer);
    }

    public WebSoknad hentEttersendingForBehandlingskjedeId(String behandlingsId) {
        return lokalDb.hentEttersendingMedBehandlingskjedeId(behandlingsId).orElse(null);
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
        henvendelseService.avbrytSoknad(soknad.getBrukerBehandlingId(), false);
        lokalDb.slettSoknad(soknad, HendelseType.AVBRUTT_AV_BRUKER);

        final String eier = soknad.getAktoerId();
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        soknadUnderArbeidOptional.ifPresent(soknadUnderArbeid -> soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, eier));

        soknadMetricsService.avbruttSoknad(soknad.getskjemaNummer(), soknad.erEttersending());
    }

    public String legacyStartEttersending(String behandlingsIdSoknad) {
        return ettersendingService.legacyStart(behandlingsIdSoknad);
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

    @Transactional
    public void sendSoknad(String behandlingsId) {
        soknadDataFletter.sendSoknad(behandlingsId);
    }
}