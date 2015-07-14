package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadFaktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import org.apache.commons.collections15.Closure;
import org.slf4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Locale;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class FaktaService {

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    @Named("vedleggRepository")
    private VedleggRepository vedleggRepository;

    @Inject
    private NavMessageSource navMessageSource;

    @Inject
    private WebSoknadConfig config;

    private static final String EKSTRA_VEDLEGG_KEY = "ekstraVedlegg";
    private static final Logger logger = getLogger(FaktaService.class);

    public List<Faktum> hentFakta(String behandlingsId) {
        return repository.hentAlleBrukerData(behandlingsId);
    }

    public String hentBehandlingsId(Long faktumId) {
        return repository.hentBehandlingsIdTilFaktum(faktumId);
    }

    @Transactional
    public Faktum lagreSoknadsFelt(String behandlingsId, Faktum faktum) {
        WebSoknad soknad = repository.hentSoknad(behandlingsId);
        faktum.setSoknadId(soknad.getSoknadId());

        return lagreSoknadsFelt(faktum);
    }

    @Transactional
    public Faktum lagreSoknadsFelt(Faktum faktum) {
        Long soknadId = faktum.getSoknadId();
        faktum.setType(BRUKERREGISTRERT);

        Long faktumId = repository.lagreFaktum(soknadId, faktum);
        repository.settSistLagretTidspunkt(soknadId);

        settDelstegStatus(soknadId, faktum.getKey());

        Faktum resultat = repository.hentFaktum(faktumId);
        genererVedleggForFaktum(resultat);

        return resultat;
    }

    @Transactional
    public void lagreSystemFakta(final WebSoknad soknad, List<Faktum> fakta) {
        on(fakta).forEach(new Closure<Faktum>() {
            @Override
            public void execute(Faktum faktum) {
                Faktum existing;

                if(faktum.getUnikProperty() == null) {
                    existing = soknad.getFaktumMedKey(faktum.getKey());
                } else {
                    existing = soknad.getFaktaMedKeyOgProperty(faktum.getKey(), faktum.getUnikProperty(), faktum.getProperties().get(faktum.getUnikProperty()));
                }

                if (existing != null) {
                    faktum.setFaktumId(existing.getFaktumId());
                    faktum.kopierFaktumegenskaper(existing);
                }
                faktum.setType(SYSTEMREGISTRERT);
                repository.lagreFaktum(soknad.getSoknadId(), faktum, true);

                genererVedleggForFaktum(faktum);
            }
        });
    }

    @Transactional
    public Long lagreSystemFaktum(Long soknadId, Faktum f) {
        logger.debug("*** Lagrer systemfaktum ***: " + f.getKey());
        f.setType(SYSTEMREGISTRERT);
        List<Faktum> fakta = repository.hentSystemFaktumList(soknadId, f.getKey());


        for (Faktum faktum : fakta) {
            if(faktum.getKey().equals(f.getKey())) {
                f.setFaktumId(faktum.getFaktumId());
                break;
            }
        }

        Long lagretFaktumId = repository.lagreFaktum(soknadId, f, true);
        Faktum hentetFaktum = repository.hentFaktum(lagretFaktumId);
        genererVedleggForFaktum(hentetFaktum);

        repository.settSistLagretTidspunkt(soknadId);
        return lagretFaktumId;
    }


    @Transactional()
    public void slettBrukerFaktum(Long faktumId) {
        final Faktum faktum;
        try {
            faktum = repository.hentFaktum(faktumId);
        } catch (IncorrectResultSizeDataAccessException e) {
            logger.info("Skipped delete because faktum does not exist.");
            throw new IkkeFunnetException("Faktum ikke funnet", e, "faktum.exception.ikkefunnet");
        }
        Long soknadId = faktum.getSoknadId();

        String faktumKey = faktum.getKey();
        List<Vedlegg> vedleggliste = vedleggRepository.hentVedleggForFaktum(soknadId, faktumId);

        for (Vedlegg vedlegg : vedleggliste) {
            vedleggRepository.slettVedleggOgData(soknadId, vedlegg);
        }
        repository.slettBrukerFaktum(soknadId, faktumId);
        repository.settSistLagretTidspunkt(soknadId);
        settDelstegStatus(soknadId, faktumKey);
    }

    private void settDelstegStatus(Long soknadId, String faktumKey) {
        WebSoknad webSoknad = repository.hentSoknad(soknadId);
        //Sjekker og setter delstegstatus dersom et faktum blir lagret, med mindre det er epost eller ekstra vedlegg. Bør gjøres mer elegant, fremdeles litt quickfix
        if (!Personalia.EPOST_KEY.equals(faktumKey) && !EKSTRA_VEDLEGG_KEY.equals(faktumKey)) {
            webSoknad.validerDelstegEndring(DelstegStatus.UTFYLLING);
            repository.settDelstegstatus(soknadId, DelstegStatus.UTFYLLING);
        }
    }

    private void genererVedleggForFaktum(Faktum faktum) {
        SoknadStruktur struktur = hentSoknadStruktur(faktum.getSoknadId());
        List<SoknadVedlegg> aktuelleVedlegg = struktur.vedleggFor(faktum);
        for (SoknadVedlegg soknadVedlegg : aktuelleVedlegg) {
            oppdaterOgLagreVedlegg(struktur, soknadVedlegg, faktum);
        }
        genererVedleggForBarnefakta(faktum);
    }

    private SoknadStruktur hentSoknadStruktur(Long soknadId) {
        return config.hentStruktur(soknadId);
    }

    private void oppdaterOgLagreVedlegg(SoknadStruktur struktur, SoknadVedlegg soknadVedlegg, Faktum faktum) {
        Long faktumId = soknadVedlegg.getFlereTillatt() ? faktum.getFaktumId() : null;
        Vedlegg vedlegg = vedleggRepository.hentVedleggForskjemaNummerMedTillegg(
                faktum.getSoknadId(), faktumId, soknadVedlegg.getSkjemaNummer(), soknadVedlegg.getSkjemanummerTillegg()
        );
        Faktum parentFaktum = repository.hentFaktum(faktum.getParrentFaktum());

        if (soknadVedlegg.trengerVedlegg(faktum) && erParentAktiv(soknadVedlegg.getFaktum(), parentFaktum)) {
            lagrePaakrevdVedlegg(faktum, soknadVedlegg, vedlegg);
        } else if (vedlegg != null && !erVedleggKrevdAvAnnetFaktum(faktum, struktur, soknadVedlegg)) {
            vedlegg.setInnsendingsvalg(Vedlegg.Status.IkkeVedlegg);
            vedleggRepository.lagreVedlegg(faktum.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
        }
    }

    private void genererVedleggForBarnefakta(Faktum parentFaktum) {
        on(repository.hentBarneFakta(parentFaktum.getSoknadId(), parentFaktum.getFaktumId())).forEach(new Closure<Faktum>() {
            @Override
            public void execute(Faktum faktum) {
                genererVedleggForFaktum(faktum);
            }
        });
    }

    private boolean erParentAktiv(SoknadFaktum faktum, Faktum parent) {
        if (parent == null) {
            return true;
        }

        if (parentValueErLikEnAvVerdieneIDependOnValues(faktum, parent)) {
            Faktum parentParentFaktum = repository.hentFaktum(parent.getParrentFaktum());
            SoknadFaktum parentSoknadFaktum = faktum.getDependOn();
            return erParentAktiv(parentSoknadFaktum, parentParentFaktum);
        }
        return false;
    }

    private void lagrePaakrevdVedlegg(Faktum faktum, SoknadVedlegg soknadVedlegg, Vedlegg v) {
        Vedlegg vedlegg = v;
        if (vedlegg == null) {
            Long faktumId = soknadVedlegg.getFlereTillatt() ? faktum.getFaktumId() : null;
            vedlegg = new Vedlegg()
                    .medSoknadId(faktum.getSoknadId())
                    .medFaktumId(faktumId)
                    .medSkjemaNummer(soknadVedlegg.getSkjemaNummer())
                    .medSkjemanummerTillegg(soknadVedlegg.getSkjemanummerTillegg())
                    .medInnsendingsvalg(Vedlegg.Status.VedleggKreves);
            vedlegg.setVedleggId(vedleggRepository.opprettVedlegg(vedlegg, null));
        }

        if (faktum.getType().equals(Faktum.FaktumType.BRUKERREGISTRERT)) {
            vedlegg.oppdatertInnsendtStatus();
        }

        if (vedleggHarTittelFraProperty(soknadVedlegg, faktum)) {
            vedlegg.setNavn(faktum.getProperties().get(soknadVedlegg.getProperty()));
        } else if (soknadVedlegg.harOversetting()) {
            vedlegg.setNavn(navMessageSource.getMessage(soknadVedlegg.getOversetting().replace("${key}", faktum.getKey()), new Object[0], new Locale("nb", "NO")));
        }
        vedleggRepository.lagreVedlegg(faktum.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
    }

    private boolean erVedleggKrevdAvAnnetFaktum(Faktum faktum, SoknadStruktur struktur, SoknadVedlegg soknadVedlegg) {
        return !soknadVedlegg.getFlereTillatt() && annetFaktumHarForventning(faktum.getSoknadId(), soknadVedlegg.getSkjemaNummer(), soknadVedlegg.getSkjemanummerTillegg(), struktur);
    }

    private boolean parentValueErLikEnAvVerdieneIDependOnValues(SoknadFaktum faktum, Faktum parent) {
        if (faktum.getDependOn() == null) {
            return true;
        }

        String parentVerdi = hentVerdiFaktumErAvhengigAvPaaParent(faktum, parent);
        List<String> dependOnValues = faktum.getDependOnValues();
        for (String dependOnValue : dependOnValues) {
            if (dependOnValue.equalsIgnoreCase(parentVerdi)) {
                return true;
            }
        }
        return false;
    }

    private boolean vedleggHarTittelFraProperty(SoknadVedlegg vedlegg, Faktum faktum) {
        return vedlegg.getProperty() != null && faktum.getProperties().containsKey(vedlegg.getProperty());
    }

    /**
     * Looper alle mulige vedleggsforventinger for gitt skjemanummer,
     * dersom soknadbrukerdata har et innslag som har riktig onValue, returneres true (et annet faktum trigger vedlegget)
     * ellers returneres false
     */
    private boolean annetFaktumHarForventning(Long soknadId, String skjemaNummer, String skjemaNrTillegg, SoknadStruktur struktur) {
        List<SoknadVedlegg> vedleggMedGittSkjemanummer = struktur.vedleggForSkjemanrMedTillegg(skjemaNummer, skjemaNrTillegg);
        for (SoknadVedlegg sv : vedleggMedGittSkjemanummer) {
            if (repository.isVedleggPaakrevd(soknadId, sv)) {
                return true;
            }
        }
        return false;
    }

    private String hentVerdiFaktumErAvhengigAvPaaParent(SoknadFaktum faktum, Faktum parent) {
        String dependOnPropertyName = faktum.getDependOnProperty();
        String verdiManErAvhengigAv;
        if (dependOnPropertyName != null) {
            verdiManErAvhengigAv = parent.getProperties().get(dependOnPropertyName);
        } else {
            verdiManErAvhengigAv = parent.getValue();
        }
        return verdiManErAvhengigAv == null ? "false" : verdiManErAvhengigAv;
    }

    public Faktum hentFaktumMedKey(Long soknadId, String key) {
        return repository.hentFaktumMedKey(soknadId, key);
    }

    public Faktum hentFaktum(Long faktumId) {
        return repository.hentFaktum(faktumId);
    }
}
