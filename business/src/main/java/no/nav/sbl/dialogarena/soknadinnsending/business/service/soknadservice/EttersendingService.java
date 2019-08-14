package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.EttersendelseSendtForSentException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;

@Component
public class EttersendingService {
    public static final int ETTERSENDELSE_FRIST_DAGER = 90;

    @Inject
    HenvendelseService henvendelseService;

    @Inject
    SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    SendtSoknadRepository sendtSoknadRepository;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    Clock clock;

    public String start(String behandlingsIdDetEttersendesPaa, String eier) {
        SendtSoknad originalSoknad = hentOgVerifiserSoknad(behandlingsIdDetEttersendesPaa, eier);

        String nyBehandlingsId = henvendelseService.startEttersending(originalSoknad);

        List<VedleggMetadata> vedlegg = hentVedleggForNyesteSoknadIKjede(originalSoknad);
        List<VedleggMetadata> manglendeVedlegg = lagListeOverVedleggKreves(vedlegg);
        List<JsonVedlegg> manglendeJsonVedlegg = convertVedleggMetadataToJsonVedlegg(manglendeVedlegg);

        lagreSoknadILokalDb(originalSoknad, nyBehandlingsId, manglendeJsonVedlegg);

        return nyBehandlingsId;
    }

    private void lagreSoknadILokalDb(SendtSoknad originalSoknad, String nyBehandlingsId, List<JsonVedlegg> manglendeJsonVedlegg) {
        SoknadUnderArbeid ettersendingSoknad = new SoknadUnderArbeid().withBehandlingsId(nyBehandlingsId)
                .withVersjon(1L)
                .withEier(originalSoknad.getEier())
                .withInnsendingStatus(SoknadInnsendingStatus.UNDER_ARBEID)
                .withTilknyttetBehandlingsId(originalSoknad.getTilknyttetBehandlingsId())
                .withJsonInternalSoknad(new JsonInternalSoknad()
                        .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(manglendeJsonVedlegg))
                        .withMottaker(new JsonSoknadsmottaker()
                                .withOrganisasjonsnummer(originalSoknad.getOrgnummer())
                                .withNavEnhetsnavn(originalSoknad.getNavEnhetsnavn())))
                .withOpprettetDato(LocalDateTime.now())
                .withSistEndretDato(LocalDateTime.now());

        soknadUnderArbeidRepository.opprettSoknad(ettersendingSoknad, originalSoknad.getEier());
    }

    private List<JsonVedlegg> convertVedleggMetadataToJsonVedlegg(List<VedleggMetadata> manglendeVedlegg) {
        return manglendeVedlegg.stream()
                .map(v -> new JsonVedlegg()
                    .withType(v.skjema)
                    .withTilleggsinfo(v.tillegg)
                    .withStatus("VedleggKreves"))
                .collect(Collectors.toList());
    }

    protected SendtSoknad hentOgVerifiserSoknad(String behandlingsId, String eier) {
        Optional<SendtSoknad> soknadOptional = sendtSoknadRepository.hentSendtSoknad(behandlingsId, eier);
        if (!soknadOptional.isPresent()) {
            throw new IllegalStateException(String.format("SendtSoknad til behandlingsid %s finnes ikke", behandlingsId));
        }
        SendtSoknad soknad = soknadOptional.get();
        if (soknad.erEttersendelse()) {
            soknad = sendtSoknadRepository.hentSendtSoknad(soknad.getTilknyttetBehandlingsId(), eier).get();
        }

        if (soknad.getSendtDato().isBefore(LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER))) {
            long dagerEtterFrist = DAYS.between(soknad.getSendtDato(), LocalDateTime.now(clock).minusDays(ETTERSENDELSE_FRIST_DAGER));
            throw new EttersendelseSendtForSentException(String.format("Kan ikke starte ettersendelse %d dager etter frist", dagerEtterFrist));
        }
        return soknad;
    }

    public List<VedleggMetadata> hentVedleggForNyesteSoknadIKjede(SendtSoknad originalSoknad) {
        SendtSoknad nyesteSoknadIKjede = sendtSoknadRepository.hentBehandlingskjede(originalSoknad.getBehandlingsId()).stream()
                .max(Comparator.comparing(SendtSoknad::getSendtDato)).orElse(originalSoknad);
        SoknadMetadata nyesteSoknadMetadata = soknadMetadataRepository.hent(nyesteSoknadIKjede.getBehandlingsId());
        return nyesteSoknadMetadata.vedlegg.vedleggListe;
    }

    protected List<VedleggMetadata> lagListeOverVedleggKreves(List<VedleggMetadata> vedleggMetadata) {
        List<VedleggMetadata> manglendeVedlegg = vedleggMetadata.stream()
                .filter(v -> v.status == Vedleggstatus.VedleggKreves)
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
