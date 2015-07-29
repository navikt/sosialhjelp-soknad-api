package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadFaktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.EttersendingService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.StartDatoService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.sort;
import static java.util.UUID.randomUUID;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadFaktum.sammenlignEtterDependOn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadServiceUtil.hentFraHenvendelse;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadServiceUtil.hentSisteIkkeAvbrutteSoknadIBehandlingskjede;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadServiceUtil.hentSoknadFraDbEllerHenvendelse;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadServiceUtil.lagEttersendingFraWsSoknad;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadServiceUtil.validerSkjemanummer;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.SORTER_INNSENDT_DATO;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.STATUS;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.erIkkeSystemfaktumOgKunEtErTillatt;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.hentOrginalInnsendtDato;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadService implements SendSoknadService, EttersendingService {

    private static final Logger logger = getLogger(SoknadService.class);

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    @Named("vedleggRepository")
    private VedleggRepository vedleggRepository;

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private Kodeverk kodeverk;

    @Inject
    private StartDatoService startDatoService;

    @Inject
    private FaktaService faktaService;

    @Inject
    public ApplicationContext applicationContext;

    @Inject
    private WebSoknadConfig config;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    private Map<String, BolkService> bolker;

    @Inject
    private VedleggService vedleggService;

    @PostConstruct
    public void initBolker() {
        bolker = applicationContext.getBeansOfType(BolkService.class);
    }

    @Override
    public void settDelsteg(String behandlingsId, DelstegStatus delstegStatus) {
        repository.settDelstegstatus(behandlingsId, delstegStatus);
    }

    @Override
    public void settJournalforendeEnhet(String behandlingsId, String journalforendeEnhet) {
        repository.settJournalforendeEnhet(behandlingsId, journalforendeEnhet);
    }

    @Override
    public WebSoknad hentSoknad(long soknadId) {
        return repository.hentSoknad(soknadId);
    }

    @Override
    public WebSoknad hentSoknad(String behandlingsId) {
        WebSoknad soknad = hentSoknadFraDbEllerHenvendelse(behandlingsId, repository, henvendelseService, fillagerService, vedleggRepository);
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));

        oppdaterKjentInformasjon(getSubjectHandler().getUid(), soknad);

        return soknad;
    }

    @Override
    public WebSoknad hentSoknadForTilgangskontroll(String behandlingsId) {
        return hentSoknadFraDbEllerHenvendelse(behandlingsId, repository, henvendelseService, fillagerService, vedleggRepository);
    }

    @Override
    public WebSoknad hentSoknadMedFaktaOgVedlegg(String behandlingsId) {
        WebSoknad soknad = repository.hentSoknadMedData(behandlingsId);
        if (soknad == null) {
            soknad = hentFraHenvendelse(behandlingsId, true, henvendelseService, fillagerService, repository, vedleggRepository);
        }
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));
        return soknad;
    }

    @Override
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

    @Override
    public WebSoknad hentEttersendingForBehandlingskjedeId(String behandlingsId) {
        Optional<WebSoknad> soknad = repository.hentEttersendingMedBehandlingskjedeId(behandlingsId);
        return soknad.isSome() ? soknad.get() : null;
    }

    @Override
    public String startEttersending(String behandlingsIdSoknad, String fodselsnummer) {
        List<WSBehandlingskjedeElement> behandlingskjede = henvendelseService.hentBehandlingskjede(behandlingsIdSoknad);
        WSHentSoknadResponse wsSoknadsdata = hentSisteIkkeAvbrutteSoknadIBehandlingskjede(behandlingskjede, henvendelseService);

        if (wsSoknadsdata.getInnsendtDato() == null) {
            throw new ApplicationException("Kan ikke starte ettersending på en ikke fullfort soknad");
        }
        DateTime innsendtDato = hentOrginalInnsendtDato(behandlingskjede, behandlingsIdSoknad);
        WebSoknad ettersending = lagEttersendingFraWsSoknad(wsSoknadsdata, innsendtDato,
                henvendelseService, repository, faktaService, vedleggRepository, vedleggService);
        return ettersending.getBrukerBehandlingId();
    }

    @Override
    @Transactional
    public void avbrytSoknad(String behandlingsId) {
        WebSoknad soknad = repository.hentSoknad(behandlingsId);

        /**
         * Sletter alle vedlegg til søknader som blir avbrutt.
         * Dette burde egentlig gjøres i henvendelse, siden vi uansett skal slette alle vedlegg på avbrutte søknader.
         * I tillegg blir det liggende igjen mange vedlegg for søknader som er avbrutt før dette kallet ble lagt til.
         * */

        fillagerService.slettAlle(soknad.getBrukerBehandlingId());
        henvendelseService.avbrytSoknad(soknad.getBrukerBehandlingId());
        repository.slettSoknad(soknad.getSoknadId());
    }

    @Override
    public SoknadStruktur hentSoknadStruktur(Long soknadId) {
        return config.hentStruktur(soknadId);
    }

    @Override
    public SoknadStruktur hentSoknadStruktur(String skjemanummer) {
        return config.hentStruktur(skjemanummer);
    }

    @Override
    @Transactional
    public String startSoknad(String navSoknadId, String fodselsnummer) {
        validerSkjemanummer(navSoknadId, kravdialogInformasjonHolder);
        String mainUid = randomUUID().toString();
        String behandlingsId = henvendelseService.startSoknad(getSubjectHandler().getUid(), navSoknadId, mainUid);

        WebSoknad soknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId)
                .medskjemaNummer(navSoknadId)
                .medUuid(mainUid)
                .medAktorId(getSubjectHandler().getUid())
                .medOppretteDato(DateTime.now());

        Long soknadId = repository.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);
        Faktum bolkerFaktum = new Faktum().medSoknadId(soknadId).medKey("bolker").medType(BRUKERREGISTRERT);
        repository.lagreFaktum(soknadId, bolkerFaktum);

        Faktum personalia = new Faktum()
                .medSoknadId(soknadId)
                .medType(SYSTEMREGISTRERT)
                .medKey("personalia");
        faktaService.lagreSystemFaktum(soknadId, personalia);

        prepopulerSoknadsFakta(soknadId);
        Faktum lonnsOgTrekkoppgaveFaktum = new Faktum()
                .medSoknadId(soknadId)
                .medKey("lonnsOgTrekkOppgave")
                .medType(SYSTEMREGISTRERT)
                .medValue(startDatoService.erJanuarEllerFebruar().toString());
        faktaService.lagreSystemFaktum(soknadId, lonnsOgTrekkoppgaveFaktum);

        return behandlingsId;
    }

    @Override
    @Transactional
    public void sendSoknad(String behandlingsId, byte[] pdf) {
        SoknadServiceUtil.sendSoknad(hentSoknadMedFaktaOgVedlegg(behandlingsId), pdf,
                fillagerService, vedleggRepository, kravdialogInformasjonHolder, henvendelseService, repository, logger);
    }

    private void oppdaterKjentInformasjon(String fodselsnummer, final WebSoknad soknad) {
        WebSoknad soknadMedFakta = hentSoknadMedFaktaOgVedlegg(soknad.getBrukerBehandlingId());
        if (soknad.erEttersending()) {
            lagrePersonalia(fodselsnummer, soknadMedFakta);
        } else {
            lagreAllInformasjon(fodselsnummer, soknadMedFakta);
        }
    }

    private void lagrePersonalia(String fodselsnummer, WebSoknad soknad) {
        faktaService.lagreSystemFakta(soknad, bolker.get(PersonaliaService.class.getName()).genererSystemFakta(fodselsnummer, soknad.getSoknadId()));
    }

    private void lagreAllInformasjon(String fodselsnummer, WebSoknad soknad) {
        List<BolkService> soknadBolker = config.getSoknadBolker(soknad, bolker.values());
        List<Faktum> systemfaktum = new ArrayList<>();
        for (BolkService bolk : soknadBolker) {
            systemfaktum.addAll(bolk.genererSystemFakta(fodselsnummer, soknad.getSoknadId()));
        }
        faktaService.lagreSystemFakta(soknad, systemfaktum);

    }

    private void prepopulerSoknadsFakta(Long soknadId) {
        SoknadStruktur soknadStruktur = hentSoknadStruktur(soknadId);
        List<SoknadFaktum> fakta = soknadStruktur.getFakta();
        sort(fakta, sammenlignEtterDependOn());

        for (SoknadFaktum soknadFaktum : fakta) {
            if (erIkkeSystemfaktumOgKunEtErTillatt(soknadFaktum)) {
                Faktum f = new Faktum()
                        .medKey(soknadFaktum.getId())
                        .medValue("")
                        .medType(BRUKERREGISTRERT);

                if (soknadFaktum.getDependOn() != null) {
                    Faktum parentFaktum = repository.hentFaktumMedKey(soknadId, soknadFaktum.getDependOn().getId());
                    f.setParrentFaktum(parentFaktum.getFaktumId());
                }
                repository.lagreFaktum(soknadId, f);
            }
        }
    }
}