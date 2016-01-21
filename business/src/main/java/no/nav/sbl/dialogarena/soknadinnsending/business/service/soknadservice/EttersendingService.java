package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.modig.core.exception.*;
import no.nav.modig.lang.collections.predicate.*;
import no.nav.modig.lang.collections.transform.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.*;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.*;
import org.joda.time.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

import javax.inject.*;
import java.util.*;

import static java.util.UUID.*;
import static no.nav.modig.core.context.SubjectHandler.*;
import static no.nav.modig.lang.collections.IterUtils.*;
import static no.nav.modig.lang.collections.PredicateUtils.*;
import static no.nav.modig.lang.option.Optional.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.*;
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

    public String start(String behandlingsIdDetEttersendesPaa) {
        List<WSBehandlingskjedeElement> behandlingskjede = henvendelseService.hentBehandlingskjede(behandlingsIdDetEttersendesPaa);
        WSHentSoknadResponse nyesteSoknad = hentNyesteSoknadFraHenvendelse(behandlingskjede);

        optional(nyesteSoknad.getInnsendtDato()).getOrThrow(new ApplicationException("Kan ikke starte ettersending på en ikke fullfort soknad"));

        String nyBehandlingsId = henvendelseService.startEttersending(nyesteSoknad);
        String behandlingskjedeId = optional(nyesteSoknad.getBehandlingskjedeId()).getOrElse(nyesteSoknad.getBehandlingsId());
        WebSoknad ettersending = lagreEttersendingTilLokalDb(behandlingsIdDetEttersendesPaa, behandlingskjede, behandlingskjedeId, nyBehandlingsId);

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
