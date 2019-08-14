package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.BehandlingsKjede;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.BehandlingsKjede.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Component
public class InnsendtSoknadService {

    public static final String SKJEMANUMMER_KVITTERING = "L7";

    @Inject
    private SendtSoknadRepository sendtSoknadRepository;
    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    private Predicate<VedleggMetadata> ikkeKvittering = v -> !SKJEMANUMMER_KVITTERING.equals(v.skjema);
    private Predicate<VedleggMetadata> lastetOpp = v -> v.status.er(Vedleggstatus.LastetOpp);
    private Predicate<VedleggMetadata> ikkeLastetOpp = lastetOpp.negate();

    private DateTimeFormatter datoFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private DateTimeFormatter tidFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public BehandlingsKjede hentBehandlingskjede(String behandlingsId, String eier) {
        SendtSoknad originalSoknad = hentOriginalSoknad(behandlingsId, eier);
        List<SendtSoknad> ettersendelser = hentEttersendelser(originalSoknad.getBehandlingsId());

        return new BehandlingsKjede()
                .medOriginalSoknad(konverter(originalSoknad))
                .medEttersendelser(ettersendelser.stream()
                        .map(this::konverter)
                        .collect(toList())
                );
    }

    private SendtSoknad hentOriginalSoknad(String behandlingsId, String eier) {
        SendtSoknad soknad = sendtSoknadRepository.hentSendtSoknad(behandlingsId, eier).orElseThrow(IllegalStateException::new);
        if (soknad.erEttersendelse()) {
            soknad = sendtSoknadRepository.hentSendtSoknad(soknad.getTilknyttetBehandlingsId(), eier).orElseThrow(IllegalStateException::new);
        }
        return soknad;
    }

    private List<SendtSoknad> hentEttersendelser(String behandlingsId) {
        return sendtSoknadRepository.hentBehandlingskjede(behandlingsId).stream()
                .sorted(Comparator.comparing(SendtSoknad::getSendtDato))
                .collect(toList());
    }

    private InnsendtSoknad konverter(SendtSoknad sendtSoknad) {
        List<VedleggMetadata> vedleggListe = soknadMetadataRepository.hent(sendtSoknad.getBehandlingsId()).vedlegg.vedleggListe;
        return new InnsendtSoknad()
                .medBehandlingId(sendtSoknad.getBehandlingsId())
                .medInnsendtDato(sendtSoknad.getSendtDato().format(datoFormatter))
                .medInnsendtTidspunkt(sendtSoknad.getSendtDato().format(tidFormatter))
                .medNavenhet(sendtSoknad.getNavEnhetsnavn())
                .medOrgnummer(sendtSoknad.getOrgnummer())
                .medInnsendteVedlegg(tilVedlegg(vedleggListe, lastetOpp))
                .medIkkeInnsendteVedlegg(tilVedlegg(vedleggListe, ikkeLastetOpp));
    }

    private List<InnsendtSoknad.Vedlegg> tilVedlegg(List<VedleggMetadata> vedlegg, Predicate<VedleggMetadata> status) {
        List<VedleggMetadata> vedleggMedRiktigStatus = vedlegg.stream()
                .filter(ikkeKvittering)
                .filter(status)
                .collect(toList());

        Map<String, InnsendtSoknad.Vedlegg> unikeVedlegg = new HashMap<>();

        vedleggMedRiktigStatus.forEach(v -> {
            String sammensattnavn = v.skjema + "|" + v.tillegg;
            if (!unikeVedlegg.containsKey(sammensattnavn)) {
                unikeVedlegg.put(sammensattnavn, new InnsendtSoknad.Vedlegg(
                        v.skjema,
                        v.tillegg,
                        v.status
                ));
            }
        });

        return new ArrayList<>(unikeVedlegg.values());
    }

}