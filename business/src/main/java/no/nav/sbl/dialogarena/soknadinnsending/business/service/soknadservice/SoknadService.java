package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLAlternativRepresentasjon;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLAlternativRepresentasjonListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.convertToXmlVedleggListe;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.SORTER_INNSENDT_DATO;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.STATUS;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.journalforendeEnhet;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.skjemanummer;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadService {

    private static final Logger logger = getLogger(SoknadService.class);

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository lokalDb;

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private FaktaService faktaService;

    @Inject
    public ApplicationContext applicationContext;

    @Inject
    private WebSoknadConfig config;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Inject
    private SoknadServiceUtil soknadServiceUtil;

    private Map<String, BolkService> bolker;

    @Inject
    private VedleggService vedleggService;

    @PostConstruct
    public void initBolker() {
        bolker = applicationContext.getBeansOfType(BolkService.class);
    }

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
        Optional<WebSoknad> soknad = lokalDb.hentEttersendingMedBehandlingskjedeId(behandlingsId);
        return soknad.isSome() ? soknad.get() : null;
    }

    public WebSoknad hentSoknad(String behandlingsId, boolean medData, boolean medVedlegg) {
        WebSoknad soknadFraLokalDb;

        if (medVedlegg) {
            soknadFraLokalDb = lokalDb.hentSoknadMedVedlegg(behandlingsId);
        } else {
            soknadFraLokalDb = lokalDb.hentSoknad(behandlingsId);
        }

        WebSoknad soknad = soknadFraLokalDb != null ? soknadFraLokalDb : soknadServiceUtil.hentFraHenvendelse(behandlingsId, false);

        if (medData) {
            soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                    .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                    .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));

            String fodselsnummer = getSubjectHandler().getUid();
            if (soknad.erEttersending()) {
                faktaService.lagreSystemFakta(soknad, bolker.get(PersonaliaService.class.getName()).genererSystemFakta(fodselsnummer, soknad.getSoknadId()));
            } else {
                List<BolkService> soknadBolker = config.getSoknadBolker(soknad, bolker.values());
                List<Faktum> systemfaktum = new ArrayList<>();
                for (BolkService bolk : soknadBolker) {
                    systemfaktum.addAll(bolk.genererSystemFakta(fodselsnummer, soknad.getSoknadId()));
                }
                faktaService.lagreSystemFakta(soknad, systemfaktum);
            }
        }
        return soknad;
    }

    public Map<String, String> hentInnsendtDatoOgSisteInnsending(String behandlingsId) {
        Map<String, String> result = new HashMap<>();
        List<WSBehandlingskjedeElement> wsBehandlingskjedeElements = henvendelseService.hentBehandlingskjede(behandlingsId);
        List<WSBehandlingskjedeElement> sorterteBehandlinger =
                on(wsBehandlingskjedeElements).filter(where(STATUS, (equalTo(FERDIG)))).collect(SORTER_INNSENDT_DATO);

        WSBehandlingskjedeElement innsendtSoknad = sorterteBehandlinger.get(0);
        result.put("innsendtdatoSoknad", String.valueOf(innsendtSoknad.getInnsendtDato().getMillis()));
        result.put("sistInnsendteBehandlingsId", sorterteBehandlinger.get(sorterteBehandlinger.size() - 1).getBehandlingsId());
        return result;
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

    @Transactional
    public String startSoknad(String navSoknadId) {
        return soknadServiceUtil.startSoknad(navSoknadId);
    }

    @Transactional
    public void sendSoknad(String behandlingsId, byte[] pdf) {
        WebSoknad soknad = hentSoknad(behandlingsId, true, true);
        if (soknad.erEttersending() && soknad.getOpplastedeVedlegg().size() <= 0) {
            logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", soknad.getBrukerBehandlingId());
            throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }

        if (soknad.harAnnetVedleggSomIkkeErLastetOpp()) {
            logger.error("Kan ikke sende inn behandling (ID: {0}) med Annet vedlegg (skjemanummer N6) som ikke er lastet opp", soknad.getBrukerBehandlingId());
            throw new ApplicationException("Kan ikke sende inn behandling uten å ha lastet opp alle  vedlegg med skjemanummer N6");
        }

        logger.info("Lagrer søknad som fil til henvendelse for behandling {}", soknad.getBrukerBehandlingId());
        fillagerService.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(pdf));

        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer(soknad))
                .withFilnavn(skjemanummer(soknad))
                .withMimetype("application/pdf")
                .withFilstorrelse("" + pdf.length)
                .withUuid(soknad.getUuid())
                .withJournalforendeEnhet(journalforendeEnhet(soknad));

        List<Transformer<WebSoknad, AlternativRepresentasjon>> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getTransformers();
        XMLAlternativRepresentasjonListe xmlAlternativRepresentasjonListe = new XMLAlternativRepresentasjonListe();

        List<XMLAlternativRepresentasjon> alternativRepresentasjonListe = xmlAlternativRepresentasjonListe.getAlternativRepresentasjon();

        for (Transformer<WebSoknad, AlternativRepresentasjon> transformer : transformers) {
            AlternativRepresentasjon altrep = transformer.transform(soknad);
            fillagerService.lagreFil(soknad.getBrukerBehandlingId(),
                    altrep.getUuid(),
                    soknad.getAktoerId(),
                    new ByteArrayInputStream(altrep.getContent()));

            alternativRepresentasjonListe.add(new XMLAlternativRepresentasjon()
                    .withFilnavn(altrep.getFilnavn())
                    .withFilstorrelse(altrep.getContent().length + "")
                    .withMimetype(altrep.getMimetype())
                    .withUuid(altrep.getUuid()));
        }
        hovedskjema.withAlternativRepresentasjonListe(xmlAlternativRepresentasjonListe);

        henvendelseService.avsluttSoknad(soknad.getBrukerBehandlingId(), hovedskjema, convertToXmlVedleggListe(vedleggService.hentVedleggOgKvittering(soknad)));
        lokalDb.slettSoknad(soknad.getSoknadId());
    }

    public String startEttersending(String behandlingsIdSoknad) {
        return soknadServiceUtil.startEttersending(behandlingsIdSoknad);
    }

    }