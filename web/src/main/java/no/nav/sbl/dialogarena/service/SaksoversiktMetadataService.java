package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.*;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService.ETTERSENDELSE_FRIST_DAGER;
import static no.nav.sbl.sosialhjelp.domain.Vedleggstatus.LastetOpp;
import static no.nav.sbl.sosialhjelp.domain.Vedleggstatus.VedleggKreves;

@Service
public class SaksoversiktMetadataService {

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private SendtSoknadRepository sendtSoknadRepository;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private EttersendingService ettersendingService;

    @Inject
    private NavMessageSource navMessageSource;

    @Inject
    Clock clock;

    public List<InnsendtSoknad> hentInnsendteSoknaderForFnr(String fnr) {
        Properties bundle = getBundle();

        List<SendtSoknad> soknader = sendtSoknadRepository.hentAlleSendteSoknader(fnr);

        return soknader.stream().map(soknad ->
                new InnsendtSoknad()
                        .withAvsender(new Part()
                                .withType(Part.Type.BRUKER)
                                .withVisningsNavn(bundle.getProperty("saksoversikt.mottaker.deg")))
                        .withMottaker(new Part()
                                .withType(Part.Type.NAV)
                                .withVisningsNavn(bundle.getProperty("saksoversikt.mottaker.nav")))
                        .withBehandlingsId(soknad.getBehandlingsId())
                        .withInnsendtDato(tilDate(soknad.getSendtDato()))
                        .withHoveddokument(new Hoveddokument()
                                .withTittel(!soknad.erEttersendelse() ? bundle.getProperty("saksoversikt.soknadsnavn") : bundle.getProperty("saksoversikt.soknadsnavn.ettersending")))
                        .withVedlegg(tilInnsendteVedlegg(soknadMetadataRepository.hent(soknad.getBehandlingsId()).vedlegg, bundle))
                        .withTema("KOM")
                        .withTemanavn(bundle.getProperty("saksoversikt.temanavn"))
                        .withLenke(lagEttersendelseLenke(soknad.getBehandlingsId())))
                .collect(toList());
    }

    public List<PabegyntSoknad> hentPabegynteSoknaderForBruker(String fnr) {
        Properties bundle = getBundle();

        List<SoknadUnderArbeid> soknader = soknadUnderArbeidRepository.hentPabegynteSoknaderForBruker(fnr);

        return soknader.stream().map(soknad ->
                new PabegyntSoknad()
                        .withBehandlingsId(soknad.getBehandlingsId())
                        .withTittel(bundle.getProperty("saksoversikt.soknadsnavn"))
                        .withSisteEndring(tilDate(soknad.getSistEndretDato()))
                        .withLenke(lagFortsettSoknadLenke(soknad.getBehandlingsId()))
        ).collect(toList());
    }

    public List<EttersendingsSoknad> hentSoknaderBrukerKanEttersendePa(String fnr) {
        Properties bundle = getBundle();
        LocalDateTime ettersendelseFrist = LocalDateTime.now(clock)
                .minusDays(ETTERSENDELSE_FRIST_DAGER);

        List<SendtSoknad> soknader = sendtSoknadRepository.hentSoknaderForEttersending(fnr, ettersendelseFrist);

        return soknader.stream().map(soknad ->
            new EttersendingsSoknad()
                .withBehandlingsId(soknad.getBehandlingsId())
                .withTittel(bundle.getProperty("saksoversikt.soknadsnavn"))
                .withLenke(lagEttersendelseLenke(soknad.getBehandlingsId()))
                .withVedlegg(finnManglendeVedlegg(soknad, bundle))
        ).collect(toList());
    }

    private List<Vedlegg> finnManglendeVedlegg(SendtSoknad soknad, Properties bundle) {
        List<SoknadMetadata.VedleggMetadata> vedleggMetadata = ettersendingService.hentVedleggForNyesteSoknadIKjede(soknad);

        return vedleggMetadata.stream()
                .filter(v -> v.status.er(VedleggKreves))
                .filter(v -> !"annet".equals(v.skjema) || !"annet".equals(v.tillegg))
                .map(v -> "vedlegg." + v.skjema + "." + v.tillegg + ".tittel")
                .distinct()
                .map(bundle::getProperty)
                .map(navn -> new Vedlegg().withTittel(navn))
                .collect(toList());
    }

    private List<Vedlegg> tilInnsendteVedlegg(VedleggMetadataListe vedlegg, Properties bundle) {
        return vedlegg.vedleggListe.stream()
                .filter(v -> v.status.er(LastetOpp))
                .map(v -> "vedlegg." + v.skjema + "." + v.tillegg + ".tittel")
                .distinct()
                .map(bundle::getProperty)
                .map(navn -> new Vedlegg().withTittel(navn))
                .collect(toList());
    }

    private Date tilDate(LocalDateTime innsendtDato) {
        return Date.from(innsendtDato.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Properties getBundle() {
        return navMessageSource.getBundleFor("soknadsosialhjelp", new Locale("nb", "NO"));
    }

    private String lagEttersendelseLenke(String behandlingsId) {
        return lagContextLenke() + "skjema/" + behandlingsId + "/ettersendelse";
    }

    private String lagFortsettSoknadLenke(String behandlingsId) {
        return lagContextLenke() + "skjema/" + behandlingsId + "/1";
    }

    private String lagContextLenke() {
        String miljo = System.getProperty("environment.name", "");

        String tjenesterPostfix = "";
        if (miljo.contains("t") || miljo.contains("q")) {
            tjenesterPostfix = "-" + miljo;
        }

        return "https://tjenester" + tjenesterPostfix + ".nav.no/soknadsosialhjelp/";
    }

}
