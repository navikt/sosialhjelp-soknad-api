package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.FerdigSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.PredicateUtils;
import org.apache.commons.collections15.Transformer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;

@Component
public class SoknadService {

    public static final String SKJEMANUMMER_KVITTERING = "L7";
    public static final Predicate<Vedlegg> LASTET_OPP = new Predicate<Vedlegg>() {
        @Override
        public boolean evaluate(Vedlegg v) {
            return Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg());
        }
    };
    public static final Predicate<Vedlegg> IKKE_LASTET_OPP = PredicateUtils.notPredicate(LASTET_OPP);

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
    private VedleggService vedleggService;

    private final Predicate<Vedlegg> IKKE_KVITTERING = new Predicate<Vedlegg>() {
        @Override
        public boolean evaluate(Vedlegg vedlegg) {
            return !SKJEMANUMMER_KVITTERING.equalsIgnoreCase(vedlegg.getSkjemaNummer());
        }
    };;

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

    public FerdigSoknad hentFerdigSoknad(String behandlingsId) {
        final XMLHenvendelse xmlHenvendelse = henvendelseService.hentInformasjonOmAvsluttetSoknad(behandlingsId);

        List<Vedlegg> vedlegg = on(xmlHenvendelse.getMetadataListe().getMetadata()).map(new Transformer<XMLMetadata, Vedlegg>() {
            @Override
            public Vedlegg transform(XMLMetadata xmlMetadata) {
                XMLVedlegg xmlVedlegg = (XMLVedlegg) xmlMetadata;
                return  new Vedlegg()
                        .medInnsendingsvalg(Transformers.toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                        .medSkjemaNummer(xmlVedlegg.getSkjemanummer())
                        .medSkjemanummerTillegg(xmlVedlegg.getSkjemanummerTillegg());
            }
        }).filter(IKKE_KVITTERING).collect();

        vedleggService.leggTilKodeverkFelter(vedlegg);

        List<Vedlegg> innsendteVedlegg = on(vedlegg).filter(LASTET_OPP).collect();
        List<Vedlegg> ikkeInnsendteVedlegg = on(vedlegg).filter(IKKE_LASTET_OPP).collect();

        return new FerdigSoknad()
                .medBehandlingId(xmlHenvendelse.getBehandlingsId())
                .medInnsendteVedlegg(innsendteVedlegg)
                .medIkkeInnsendteVedlegg(ikkeInnsendteVedlegg)
                .medDato(xmlHenvendelse.getAvsluttetDato());
    }
}