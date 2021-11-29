package no.nav.sosialhjelp.soknad.business.service.soknadservice;

import no.nav.sosialhjelp.soknad.business.domain.BehandlingsKjede;
import no.nav.sosialhjelp.soknad.business.domain.BehandlingsKjede.InnsendtSoknad;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService;
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static no.nav.sosialhjelp.soknad.business.util.EttersendelseUtils.soknadSendtForMindreEnn30DagerSiden;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING;

@Component
public class InnsendtSoknadService {

    public static final String SKJEMANUMMER_KVITTERING = "L7";

    private final HenvendelseService henvendelseService;

    private Predicate<VedleggMetadata> ikkeKvittering = v -> !SKJEMANUMMER_KVITTERING.equals(v.skjema);
    private Predicate<VedleggMetadata> lastetOpp = v -> v.status.er(Vedleggstatus.LastetOpp);
    private Predicate<VedleggMetadata> ikkeLastetOpp = lastetOpp.negate();

    private DateTimeFormatter datoFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy");
    private DateTimeFormatter tidFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public InnsendtSoknadService(HenvendelseService henvendelseService) {
        this.henvendelseService = henvendelseService;
    }

    static long soknadsalderIMinutter(LocalDateTime tidspunktSendt) {
        if (tidspunktSendt == null) return -1;
        return tidspunktSendt.until(LocalDateTime.now(), ChronoUnit.MINUTES);
    }

    public BehandlingsKjede hentBehandlingskjede(String behandlingsId) {
        SoknadMetadata originalSoknad = hentOriginalSoknad(behandlingsId);
        List<SoknadMetadata> ettersendelser = hentEttersendelser(originalSoknad.behandlingsId);

        return new BehandlingsKjede()
                .medOriginalSoknad(konverter(originalSoknad))
                .medEttersendelser(ettersendelser.stream()
                        .map(this::konverter)
                        .collect(toList())
                );
    }

    private SoknadMetadata hentOriginalSoknad(String behandlingsId) {
        SoknadMetadata soknad = henvendelseService.hentSoknad(behandlingsId);
        if (soknad.type == SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            soknad = henvendelseService.hentSoknad(soknad.tilknyttetBehandlingsId);
        }
        return soknad;
    }

    private List<SoknadMetadata> hentEttersendelser(String behandlingsId) {
        return henvendelseService.hentBehandlingskjede(behandlingsId).stream()
                .filter(soknad -> soknad.status.equals(SoknadMetadataInnsendingStatus.FERDIG))
                .sorted(Comparator.comparing(o -> o.innsendtDato))
                .collect(toList());
    }

    private InnsendtSoknad konverter(SoknadMetadata metadata) {
        return new InnsendtSoknad()
                .medBehandlingId(metadata.behandlingsId)
                .medInnsendtDato(metadata.innsendtDato.format(datoFormatter))
                .medInnsendtTidspunkt(metadata.innsendtDato.format(tidFormatter))
                .medSoknadsalderIMinutter(soknadsalderIMinutter(metadata.innsendtDato))
                .medNavenhet(metadata.navEnhet)
                .medOrgnummer(metadata.orgnr)
                .medInnsendteVedlegg(tilVedlegg(metadata.vedlegg.vedleggListe, lastetOpp))
                .medIkkeInnsendteVedlegg(soknadSendtForMindreEnn30DagerSiden(metadata.innsendtDato.toLocalDate()) ? tilVedlegg(metadata.vedlegg.vedleggListe, ikkeLastetOpp) : null);
    }

    public LocalDateTime getInnsendingstidspunkt(String behandlingsId) {
        var soknadMetadata = hentOriginalSoknad(behandlingsId);
        return soknadMetadata.innsendtDato;
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