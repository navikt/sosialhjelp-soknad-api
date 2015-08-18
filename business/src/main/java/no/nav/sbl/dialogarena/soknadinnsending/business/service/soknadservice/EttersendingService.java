package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.collections.predicate.InstanceOf;
import no.nav.modig.lang.collections.transform.Cast;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
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
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.AVBRUTT_AV_BRUKER;
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


    public String start(String behandlingsIdSoknad) {
        List<WSBehandlingskjedeElement> behandlingskjede = henvendelseService.hentBehandlingskjede(behandlingsIdSoknad);
        List<WSBehandlingskjedeElement> nyesteForstBehandlinger = on(behandlingskjede)
                .filter(where(STATUS, not(equalTo(AVBRUTT_AV_BRUKER))))
                .collect(NYESTE_FORST);

        WSHentSoknadResponse nyesteSoknad = henvendelseService.hentSoknad(nyesteForstBehandlinger.get(0).getBehandlingsId());
        optional(nyesteSoknad.getInnsendtDato()).getOrThrow(new ApplicationException("Kan ikke starte ettersending på en ikke fullfort soknad"));

        String ettersendingsBehandlingId = henvendelseService.startEttersending(nyesteSoknad);
        List<XMLMetadata> alleVedlegg = ((XMLMetadataListe) henvendelseService.hentSoknad(ettersendingsBehandlingId).getAny()).getMetadata();
        List<XMLMetadata> vedleggBortsettFraKvittering = on(alleVedlegg).filter(not(kvittering())).collect();

        XMLHovedskjema hovedskjema = finnHovedskjema(vedleggBortsettFraKvittering);

        String behandlingskjedeId = optional(nyesteSoknad.getBehandlingskjedeId()).getOrElse(nyesteSoknad.getBehandlingsId());
        WebSoknad ettersending = lagEttersendingWebSoknad(ettersendingsBehandlingId, hovedskjema, behandlingskjedeId);

        Long soknadId = lokalDb.opprettSoknad(ettersending);
        ettersending.setSoknadId(soknadId);

        faktaService.lagreSystemFaktum(soknadId, soknadInnsendingsDato(behandlingsIdSoknad, behandlingskjede, soknadId));
        ettersending.setFakta(lokalDb.hentAlleBrukerData(soknadId));
        ettersending.setVedlegg(vedleggService.hentVedleggOgPersister(new XMLMetadataListe(vedleggBortsettFraKvittering), soknadId));
        return ettersending.getBrukerBehandlingId();
    }

    private Faktum soknadInnsendingsDato(String behandlingsIdSoknad, List<WSBehandlingskjedeElement> behandlingskjede, Long soknadId) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(hentOrginalInnsendtDato(behandlingskjede, behandlingsIdSoknad).getMillis()))
                .medType(SYSTEMREGISTRERT);
    }

    private WebSoknad lagEttersendingWebSoknad(String ettersendingsBehandlingId, XMLHovedskjema hovedskjema, String behandlingskjedeId) {
        return WebSoknad.startEttersending(ettersendingsBehandlingId)
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
