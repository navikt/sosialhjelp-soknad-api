package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.collections.predicate.InstanceOf;
import no.nav.modig.lang.collections.transform.Cast;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static java.util.UUID.randomUUID;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.*;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.AVBRUTT_AV_BRUKER;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.*;

@Component
public class EttersendingService {

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private VedleggService vedleggService;

    @Inject
    private FaktaService faktaService;

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository lokalDb;

    @Inject
    public ApplicationContext applicationContext;

    @Inject
    private SoknadMetricsService soknadMetricsService;

    public String start(String behandlingsIdDetEttersendesPaa) {
        List<WSBehandlingskjedeElement> behandlingskjede = henvendelseService.hentBehandlingskjede(behandlingsIdDetEttersendesPaa);
        WSHentSoknadResponse nyesteSoknad = hentNyesteSoknadFraHenvendelse(behandlingskjede);

        optional(nyesteSoknad.getInnsendtDato()).getOrThrow(new ApplicationException("Kan ikke starte ettersending på en ikke fullfort soknad"));

        String nyBehandlingsId = henvendelseService.startEttersending(nyesteSoknad);
        String behandlingskjedeId = optional(nyesteSoknad.getBehandlingskjedeId()).getOrElse(nyesteSoknad.getBehandlingsId());
        WebSoknad ettersending = lagreEttersendingTilLokalDb(behandlingsIdDetEttersendesPaa, behandlingskjede, behandlingskjedeId, nyBehandlingsId);

        soknadMetricsService.startetSoknad(ettersending.getskjemaNummer(), true);

        return ettersending.getBrukerBehandlingId();
    }

    private WSHentSoknadResponse hentNyesteSoknadFraHenvendelse(List<WSBehandlingskjedeElement> behandlingskjede) {
        List<WSBehandlingskjedeElement> nyesteForstBehandlinger = on(behandlingskjede)
                .filter(where(STATUS, not(equalTo(AVBRUTT_AV_BRUKER))))
                .collect(NYESTE_FORST);

        return henvendelseService.hentSoknad(nyesteForstBehandlinger.get(0).getBehandlingsId());
    }

    private WebSoknad lagreEttersendingTilLokalDb(String originalBehandlingsId, List<WSBehandlingskjedeElement> behandlingskjede, String behandlingskjedeId, String ettersendingsBehandlingId) {
        List<XMLMetadata> alleVedlegg = ((XMLMetadataListe) henvendelseService.hentSoknad(ettersendingsBehandlingId).getAny()).getMetadata();
        List<XMLMetadata> vedleggBortsettFraKvittering = on(alleVedlegg).filter(not(kvittering())).collect();

        WebSoknad ettersending = lagSoknad(ettersendingsBehandlingId, behandlingskjedeId, finnHovedskjema(vedleggBortsettFraKvittering));

        Long soknadId = lokalDb.opprettSoknad(ettersending);
        ettersending.setSoknadId(soknadId);

        DateTime originalInnsendtDato = hentOrginalInnsendtDato(behandlingskjede, originalBehandlingsId);
        faktaService.lagreSystemFaktum(soknadId, soknadInnsendingsDato(soknadId, originalInnsendtDato));

        vedleggService.hentVedleggOgPersister(new XMLMetadataListe(vedleggBortsettFraKvittering), soknadId);
        return ettersending;
    }

    private Faktum soknadInnsendingsDato(Long soknadId, DateTime innsendtDato) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(innsendtDato.getMillis()))
                .medType(SYSTEMREGISTRERT);
    }

    private WebSoknad lagSoknad(String behandlingsId, String behandlingskjedeId, XMLHovedskjema hovedskjema) {
        return WebSoknad.startEttersending(behandlingsId)
                .medUuid(randomUUID().toString())
                .medAktorId(getSubjectHandler().getUid())
                .medskjemaNummer(hovedskjema.getSkjemanummer())
                .medBehandlingskjedeId(behandlingskjedeId)
                .medJournalforendeEnhet(hovedskjema.getJournalforendeEnhet());
    }

    private XMLHovedskjema finnHovedskjema(List<XMLMetadata> vedleggBortsettFraKvittering) {
        return on(vedleggBortsettFraKvittering)
                .filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class))
                .map(new Cast<>(XMLHovedskjema.class))
                .head()
                .getOrThrow(new ApplicationException("Kunne ikke hente opp hovedskjema for søknad"));
    }
}
