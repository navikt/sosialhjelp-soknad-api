package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.EttersendelseSendtForSentException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING;

@Component
public class EttersendingService {
    public static final int ETTERSENDELSE_FRIST_DAGER = 90;

    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository lokalDb;

    @Inject
    HenvendelseService henvendelseService;

    @Inject
    private VedleggRepository vedleggRepository;

    @Inject
    private FaktaService faktaService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    Clock clock;

    public String legacyStart(String behandlingsIdDetEttersendesPaa) {
        SoknadMetadata originalSoknad = hentOgVerifiserSoknad(behandlingsIdDetEttersendesPaa);
        SoknadMetadata nyesteSoknad = hentNyesteSoknadIKjede(originalSoknad);

        String uuid = UUID.randomUUID().toString();
        String nyBehandlingsId = henvendelseService.startEttersending(originalSoknad, uuid);

        Long soknadId = legacyLagreSoknadILokalDb(originalSoknad, uuid, nyBehandlingsId);

        List<VedleggMetadata> manglendeVedlegg = lagListeOverVedlegg(nyesteSoknad);
        lagreVedleggPaSoknad(soknadId, manglendeVedlegg);
        lagreFaktaPaNySoknad(soknadId, originalSoknad);

        return nyBehandlingsId;
    }

    private void lagreFaktaPaNySoknad(Long soknadId, SoknadMetadata originalSoknad) {
        faktaService.lagreSystemFaktum(soknadId, new Faktum()
                .medSoknadId(soknadId)
                .medKey("ettersendelse.sendestil")
                .medValue(originalSoknad.navEnhet)
                .medType(SYSTEMREGISTRERT));
    }

    protected Long legacyLagreSoknadILokalDb(SoknadMetadata originalSoknad, String uuid, String nyBehandlingsId) {
        WebSoknad ettersendingSoknad = WebSoknad.startEttersending(nyBehandlingsId)
                .medUuid(uuid)
                .medAktorId(originalSoknad.fnr)
                .medskjemaNummer(originalSoknad.skjema)
                .medBehandlingskjedeId(originalSoknad.behandlingsId);

        return lokalDb.opprettSoknad(ettersendingSoknad);
    }

    protected void lagreVedleggPaSoknad(Long soknadId, List<VedleggMetadata> manglendeVedlegg) {
        manglendeVedlegg.forEach(v -> {
            Vedlegg nyttVedlegg = new Vedlegg()
                    .medSoknadId(soknadId)
                    .medSkjemaNummer(v.skjema)
                    .medSkjemanummerTillegg(v.tillegg)
                    .medInnsendingsvalg(Vedlegg.Status.VedleggKreves);
            vedleggRepository.opprettEllerEndreVedlegg(nyttVedlegg, null);
        });
    }

    public String start(String behandlingsIdDetEttersendesPaa) {
        SoknadMetadata originalSoknad = hentOgVerifiserSoknad(behandlingsIdDetEttersendesPaa);
        SoknadMetadata nyesteSoknad = hentNyesteSoknadIKjede(originalSoknad);

        String uuid = UUID.randomUUID().toString();
        String nyBehandlingsId = henvendelseService.startEttersending(originalSoknad, uuid);

        List<VedleggMetadata> manglendeVedlegg = lagListeOverVedlegg(nyesteSoknad);
        List<JsonVedlegg> manglendeJsonVedlegg = convertVedleggMetadataToJsonVedlegg(manglendeVedlegg);

        lagreSoknadILokalDb(originalSoknad, nyBehandlingsId, manglendeJsonVedlegg);

        return nyBehandlingsId;
    }

    private void lagreSoknadILokalDb(SoknadMetadata originalSoknad, String nyBehandlingsId, List<JsonVedlegg> manglendeJsonVedlegg) {
        SoknadUnderArbeid ettersendingSoknad = new SoknadUnderArbeid().withBehandlingsId(nyBehandlingsId)
                .withVersjon(1L)
                .withEier(originalSoknad.fnr)
                .withInnsendingStatus(SoknadInnsendingStatus.UNDER_ARBEID)
                .withTilknyttetBehandlingsId(originalSoknad.behandlingsId)
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(manglendeJsonVedlegg))
                        .withMottaker(new JsonSoknadsmottaker()
                                .withOrganisasjonsnummer(originalSoknad.orgnr)
                                .withNavEnhetsnavn(originalSoknad.navEnhet)))
                .withOpprettetDato(LocalDateTime.now())
                .withSistEndretDato(LocalDateTime.now());

        soknadUnderArbeidRepository.opprettSoknad(ettersendingSoknad, originalSoknad.fnr);
    }

    private List<JsonVedlegg> convertVedleggMetadataToJsonVedlegg(List<VedleggMetadata> manglendeVedlegg) {
        return manglendeVedlegg.stream()
                .map(v -> new JsonVedlegg()
                    .withType(v.skjema)
                    .withTilleggsinfo(v.tillegg)
                    .withStatus("VedleggKreves"))
                .collect(Collectors.toList());
    }

    protected SoknadMetadata hentOgVerifiserSoknad(String behandlingsId) {
        SoknadMetadata soknad = henvendelseService.hentSoknad(behandlingsId);
        if (soknad.type == SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            soknad = henvendelseService.hentSoknad(soknad.tilknyttetBehandlingsId);
        }

        if (soknad.status != FERDIG) {
            throw new ApplicationException("Kan ikke starte ettersendelse på noe som ikke er innsendt");
        } else if (soknad.innsendtDato.isBefore(LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER))) {
            long dagerEtterFrist = DAYS.between(soknad.innsendtDato, LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER));
            throw new EttersendelseSendtForSentException(String.format("Kan ikke starte ettersendelse %d dager etter frist", dagerEtterFrist));
        }
        return soknad;
    }

    public SoknadMetadata hentNyesteSoknadIKjede(SoknadMetadata originalSoknad) {
        return henvendelseService.hentBehandlingskjede(originalSoknad.behandlingsId).stream()
                .filter(e -> e.status == FERDIG)
                .sorted(Comparator.comparing((SoknadMetadata o) -> o.innsendtDato).reversed())
                .findFirst()
                .orElse(originalSoknad);
    }

    protected List<VedleggMetadata> lagListeOverVedlegg(SoknadMetadata nyesteSoknad) {
        List<VedleggMetadata> manglendeVedlegg = nyesteSoknad.vedlegg.vedleggListe.stream()
                .filter(v -> v.status == Vedlegg.Status.VedleggKreves)
                .collect(toList());

        if (manglendeVedlegg.stream()
                .noneMatch(v -> "annet".equals(v.skjema) && "annet".equals(v.tillegg))) {
            VedleggMetadata annetVedlegg = new VedleggMetadata();
            annetVedlegg.skjema = "annet";
            annetVedlegg.tillegg = "annet";
            manglendeVedlegg.add(annetVedlegg);
        }

        return manglendeVedlegg;
    }

}
