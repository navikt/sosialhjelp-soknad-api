package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.collections.predicate.InstanceOf;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.StartDatoService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.sort;
import static java.util.UUID.randomUUID;
import static javax.xml.bind.JAXB.unmarshal;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.convertToXmlVedleggListe;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadDataFletter {

    private static final Logger logger = getLogger(SoknadDataFletter.class);
    private static final boolean MED_DATA = true;
    private static final boolean MED_VEDLEGG = true;

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private VedleggService vedleggService;

    @Inject
    private FaktaService faktaService;

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository lokalDb;

    @Inject
    private WebSoknadConfig config;

    @Inject
    public ApplicationContext applicationContext;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Inject
    private StartDatoService startDatoService;

    @Inject
    private NavMessageSource messageSource;

    private Map<String, BolkService> bolker;

    @PostConstruct
    public void initBolker() {
        bolker = applicationContext.getBeansOfType(BolkService.class);
    }


    private WebSoknad hentFraHenvendelse(String behandlingsId, boolean hentFaktumOgVedlegg) {
        WSHentSoknadResponse wsSoknadsdata = henvendelseService.hentSoknad(behandlingsId);

        Optional<XMLMetadata> hovedskjemaOptional = on(((XMLMetadataListe) wsSoknadsdata.getAny()).getMetadata())
                .filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        XMLHovedskjema hovedskjema = (XMLHovedskjema) hovedskjemaOptional.getOrThrow(new ApplicationException("Kunne ikke hente opp søknad"));

        SoknadInnsendingStatus status = SoknadInnsendingStatus.valueOf(wsSoknadsdata.getStatus());
        if (status.equals(UNDER_ARBEID)) {
            WebSoknad soknadFraFillager = unmarshal(new ByteArrayInputStream(fillagerService.hentFil(hovedskjema.getUuid())), WebSoknad.class);
            lokalDb.populerFraStruktur(soknadFraFillager);
            vedleggService.populerVedleggMedDataFraHenvendelse(soknadFraFillager, fillagerService.hentFiler(soknadFraFillager.getBrukerBehandlingId()));
            if (hentFaktumOgVedlegg) {
                return lokalDb.hentSoknadMedVedlegg(behandlingsId);
            }
            return lokalDb.hentSoknad(behandlingsId);
        } else {
            // søkndadsdata er slettet i henvendelse, har kun metadata
            return new WebSoknad()
                    .medBehandlingId(behandlingsId)
                    .medStatus(status)
                    .medskjemaNummer(hovedskjema.getSkjemanummer());
        }
    }

    @Transactional
    public String startSoknad(String skjemanummer) {
        if (!kravdialogInformasjonHolder.hentAlleSkjemanumre().contains(skjemanummer)) {
            throw new ApplicationException("Ikke gyldig skjemanummer " + skjemanummer);
        }
        String mainUid = randomUUID().toString();
        String aktorId = getSubjectHandler().getUid();
        String behandlingsId = henvendelseService.startSoknad(aktorId, skjemanummer, mainUid);

        Long soknadId = lagreSoknadILokalDb(skjemanummer, mainUid, aktorId, behandlingsId).getSoknadId();

        faktaService.lagreFaktum(soknadId, bolkerFaktum(soknadId));
        faktaService.lagreSystemFaktum(soknadId, personalia(soknadId));
        faktaService.lagreSystemFaktum(soknadId, lonnsOgTrekkOppgave(soknadId));

        lagreTommeFaktaFraStrukturTilLokalDb(soknadId, skjemanummer);

        return behandlingsId;
    }

    private void lagreTommeFaktaFraStrukturTilLokalDb(Long soknadId, String skjemanummer) {
        List<FaktumStruktur> fakta = config.hentStruktur(skjemanummer).getFakta();
        sort(fakta, sammenlignEtterDependOn());

        for (FaktumStruktur faktumStruktur : fakta) {
            if (faktumStruktur.ikkeSystemFaktum() && faktumStruktur.ikkeFlereTillatt()) {
                Faktum faktum = new Faktum()
                        .medKey(faktumStruktur.getId())
                        .medValue("")
                        .medType(BRUKERREGISTRERT);

                if (faktumStruktur.getDependOn() != null) {
                    Faktum parentFaktum = faktaService.hentFaktumMedKey(soknadId, faktumStruktur.getDependOn().getId());
                    faktum.setParrentFaktum(parentFaktum.getFaktumId());
                }
                faktaService.lagreFaktum(soknadId, faktum);
            }
        }
    }

    private WebSoknad lagreSoknadILokalDb(String skjemanummer, String uuid, String aktorId, String behandlingsId) {
        WebSoknad nySoknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId)
                .medskjemaNummer(skjemanummer)
                .medUuid(uuid)
                .medAktorId(aktorId)
                .medOppretteDato(DateTime.now());

        Long soknadId = lokalDb.opprettSoknad(nySoknad);
        nySoknad.setSoknadId(soknadId);
        return nySoknad;
    }

    private Faktum lonnsOgTrekkOppgave(Long soknadId) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medKey("lonnsOgTrekkOppgave")
                .medType(SYSTEMREGISTRERT)
                .medValue(startDatoService.erJanuarEllerFebruar().toString());
    }

    private Faktum bolkerFaktum(Long soknadId) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medKey("bolker")
                .medType(BRUKERREGISTRERT);
    }

    private Faktum personalia(Long soknadId) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medType(SYSTEMREGISTRERT)
                .medKey("personalia");
    }

    public WebSoknad hentSoknad(String behandlingsId, boolean medData, boolean medVedlegg) {
        WebSoknad soknadFraLokalDb;

        if (medVedlegg) {
            soknadFraLokalDb = lokalDb.hentSoknadMedVedlegg(behandlingsId);
        } else {
            soknadFraLokalDb = lokalDb.hentSoknad(behandlingsId);
        }

        WebSoknad soknad;
        if (medData) {
            soknad = soknadFraLokalDb != null ? lokalDb.hentSoknadMedData(soknadFraLokalDb.getSoknadId()) : hentFraHenvendelse(behandlingsId, true);
        } else {
            soknad = soknadFraLokalDb != null ? soknadFraLokalDb : hentFraHenvendelse(behandlingsId, false);
        }

        if (medData) {
            soknad = lokalDb.hentSoknadMedData(soknad.getSoknadId());
            soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                    .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                    .medStegliste(config.getStegliste(soknad.getSoknadId()))
                    .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));
            if (soknad.erEttersending()) {
                faktaService.lagreSystemFakta(soknad, bolker.get(PersonaliaService.class.getName()).genererSystemFakta(getSubjectHandler().getUid(), soknad.getSoknadId()));
            } else {
                List<Faktum> systemfaktum = new ArrayList<>();
                for (BolkService bolk : config.getSoknadBolker(soknad, bolker.values())) {
                    systemfaktum.addAll(bolk.genererSystemFakta(getSubjectHandler().getUid(), soknad.getSoknadId()));
                }
                faktaService.lagreSystemFakta(soknad, systemfaktum);
            }
        }
        return soknad;
    }

    public void sendSoknad(String behandlingsId, byte[] pdf) {
        WebSoknad soknad = hentSoknad(behandlingsId, MED_DATA, MED_VEDLEGG);
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

        XMLHovedskjema hovedskjema = lagXmlHovedskjemaMedAlternativRepresentasjon(pdf, soknad);
        XMLVedlegg[] vedlegg = convertToXmlVedleggListe(vedleggService.hentVedleggOgKvittering(soknad));
        henvendelseService.avsluttSoknad(soknad.getBrukerBehandlingId(), hovedskjema, vedlegg);
        lokalDb.slettSoknad(soknad.getSoknadId());
    }

    private XMLHovedskjema lagXmlHovedskjemaMedAlternativRepresentasjon(byte[] pdf, WebSoknad soknad) {
        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer(soknad))
                .withFilnavn(skjemanummer(soknad))
                .withMimetype("application/pdf")
                .withFilstorrelse("" + pdf.length)
                .withUuid(soknad.getUuid())
                .withJournalforendeEnhet(journalforendeEnhet(soknad));

        if(!soknad.erEttersending()) {
            hovedskjema = hovedskjema.withAlternativRepresentasjonListe(
                    new XMLAlternativRepresentasjonListe()
                            .withAlternativRepresentasjon(lagListeMedXMLAlternativeRepresentasjoner(soknad)));
        }

        return hovedskjema;
    }

    private List<XMLAlternativRepresentasjon> lagListeMedXMLAlternativeRepresentasjoner(WebSoknad soknad) {
        List<XMLAlternativRepresentasjon> alternativRepresentasjonListe = new ArrayList<>();
        List<Transformer<WebSoknad, AlternativRepresentasjon>> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getTransformers(messageSource);
        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(config.hentStruktur(soknad.getskjemaNummer()));
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
        return alternativRepresentasjonListe;
    }

    public Long hentOpprinneligInnsendtDato(String behandlingsId) {
        return on(henvendelseService.hentBehandlingskjede(behandlingsId))
                .filter(where(STATUS, equalTo(FERDIG)))
                .collect(ELDSTE_FORST)
                .get(0)
                .getInnsendtDato()
                .getMillis();
    }

    public String hentSisteInnsendteBehandlingsId(String behandlingsId) {
        return on(henvendelseService.hentBehandlingskjede(behandlingsId))
                .filter(where(STATUS, equalTo(FERDIG)))
                .collect(NYESTE_FORST)
                .get(0)
                .getBehandlingsId();
    }
}
