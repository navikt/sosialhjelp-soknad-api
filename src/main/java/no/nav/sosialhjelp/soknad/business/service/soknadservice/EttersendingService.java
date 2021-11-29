package no.nav.sosialhjelp.soknad.business.service.soknadservice;

import no.finn.unleash.Unleash;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService;
import no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import no.nav.sosialhjelp.soknad.domain.model.exception.EttersendelseSendtForSentException;
import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;
import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.FEATURE_UTVIDE_VEDLEGGJSON;
import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.FERDIG;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING;

@Component
public class EttersendingService {
    public static final int ETTERSENDELSE_FRIST_DAGER = 300;

    private final HenvendelseService henvendelseService;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final Unleash unleash;
    private final Clock clock;

    public EttersendingService(
            HenvendelseService henvendelseService,
            SoknadUnderArbeidRepository soknadUnderArbeidRepository,
            Unleash unleash,
            Clock clock
    ) {
        this.henvendelseService = henvendelseService;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.unleash = unleash;
        this.clock = clock;
    }

    public String start(String behandlingsIdDetEttersendesPaa) {
        SoknadMetadata originalSoknad = hentOgVerifiserSoknad(behandlingsIdDetEttersendesPaa);
        SoknadMetadata nyesteSoknad = hentNyesteSoknadIKjede(originalSoknad);

        String nyBehandlingsId = henvendelseService.startEttersending(originalSoknad);

        List<VedleggMetadata> manglendeVedlegg = lagListeOverVedlegg(nyesteSoknad);
        List<JsonVedlegg> manglendeJsonVedlegg = convertVedleggMetadataToJsonVedlegg(manglendeVedlegg);

        lagreSoknadILokalDb(originalSoknad, nyBehandlingsId, manglendeJsonVedlegg);

        return nyBehandlingsId;
    }

    private void lagreSoknadILokalDb(SoknadMetadata originalSoknad, String nyBehandlingsId, List<JsonVedlegg> manglendeJsonVedlegg) {
        SoknadUnderArbeid ettersendingSoknad = new SoknadUnderArbeid().withBehandlingsId(nyBehandlingsId)
                .withVersjon(1L)
                .withEier(originalSoknad.fnr)
                .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID)
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
                        .withStatus("VedleggKreves")
                        .withHendelseType(v.hendelseType)
                        .withHendelseReferanse(v.hendelseReferanse)
                )
                .collect(Collectors.toList());
    }

    protected SoknadMetadata hentOgVerifiserSoknad(String behandlingsId) {
        SoknadMetadata soknad = henvendelseService.hentSoknad(behandlingsId);
        if (soknad == null) {
            throw new IllegalStateException(String.format("SoknadMetadata til behandlingsid %s finnes ikke", behandlingsId));
        }
        if (soknad.type == SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            soknad = henvendelseService.hentSoknad(soknad.tilknyttetBehandlingsId);
        }

        if (soknad.status != FERDIG) {
            throw new SosialhjelpSoknadApiException("Kan ikke starte ettersendelse på noe som ikke er innsendt");
        } else if (soknad.innsendtDato.isBefore(LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER))) {
            throwDetailedExceptionForEttersendelserEtterFrist(soknad);
        }
        return soknad;
    }

    private void throwDetailedExceptionForEttersendelserEtterFrist(SoknadMetadata soknad) {
        long dagerEtterFrist = DAYS.between(soknad.innsendtDato, LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy HH:mm:ss");
        long antallEttersendelser = hentAntallEttersendelserSendtPaSoknad(soknad.behandlingsId);
        long antallNyereSoknader = henvendelseService.hentAntallInnsendteSoknaderEtterTidspunkt(soknad.fnr, soknad.innsendtDato);
        throw new EttersendelseSendtForSentException(
                String.format("Kan ikke starte ettersendelse %d dager etter frist, dagens dato: %s, soknadens dato: %s, frist(%d dager): %s. Antall ettersendelser som er sendt på denne søknaden tidligere er: %d. Antall nyere søknader denne brukeren har: %d",
                        dagerEtterFrist,
                        LocalDateTime.now().format(dateTimeFormatter),
                        soknad.innsendtDato.format(dateTimeFormatter),
                        ETTERSENDELSE_FRIST_DAGER,
                        LocalDateTime.now().minusDays(ETTERSENDELSE_FRIST_DAGER).format(dateTimeFormatter),
                        antallEttersendelser,
                        antallNyereSoknader));
    }

    public SoknadMetadata hentNyesteSoknadIKjede(SoknadMetadata originalSoknad) {
        return henvendelseService.hentBehandlingskjede(originalSoknad.behandlingsId).stream()
                .filter(e -> e.status == FERDIG)
                .max(Comparator.comparing((SoknadMetadata o) -> o.innsendtDato))
                .orElse(originalSoknad);
    }

    public long hentAntallEttersendelserSendtPaSoknad(String behandlingsId) {
        return henvendelseService.hentBehandlingskjede(behandlingsId).stream()
                .filter(e -> e.status == FERDIG)
                .count();
    }

    protected List<VedleggMetadata> lagListeOverVedlegg(SoknadMetadata nyesteSoknad) {
        List<VedleggMetadata> manglendeVedlegg = nyesteSoknad.vedlegg.vedleggListe.stream()
                .filter(v -> v.status == Vedleggstatus.VedleggKreves)
                .collect(toList());

        if (manglendeVedlegg.stream().noneMatch(JsonVedleggUtils::isVedleggskravAnnet)) {
            VedleggMetadata annetVedlegg = new VedleggMetadata();
            annetVedlegg.skjema = "annet";
            annetVedlegg.tillegg = "annet";
            if (unleash.isEnabled(FEATURE_UTVIDE_VEDLEGGJSON, false)) {
                annetVedlegg.hendelseType = JsonVedlegg.HendelseType.BRUKER;
            }
            manglendeVedlegg.add(annetVedlegg);
        }

        return manglendeVedlegg;
    }

}
