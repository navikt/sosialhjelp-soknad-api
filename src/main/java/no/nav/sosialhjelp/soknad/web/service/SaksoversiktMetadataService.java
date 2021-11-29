package no.nav.sosialhjelp.soknad.web.service;

import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.EttersendingsSoknad;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.Hoveddokument;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendtSoknad;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.PabegyntSoknad;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.Part;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.Vedlegg;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.EttersendingService;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static java.util.stream.Collectors.toList;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.EttersendingService.ETTERSENDELSE_FRIST_DAGER;
import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.isVedleggskravAnnet;
import static no.nav.sosialhjelp.soknad.domain.Vedleggstatus.LastetOpp;
import static no.nav.sosialhjelp.soknad.domain.Vedleggstatus.VedleggKreves;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType.SEND_SOKNAD_KOMMUNAL;

@Service
public class SaksoversiktMetadataService {

    private final SoknadMetadataRepository soknadMetadataRepository;
    private final EttersendingService ettersendingService;
    private final NavMessageSource navMessageSource;
    private final Clock clock;

    public SaksoversiktMetadataService(
            SoknadMetadataRepository soknadMetadataRepository,
            EttersendingService ettersendingService,
            NavMessageSource navMessageSource,
            Clock clock
    ) {
        this.soknadMetadataRepository = soknadMetadataRepository;
        this.ettersendingService = ettersendingService;
        this.navMessageSource = navMessageSource;
        this.clock = clock;
    }

    public List<InnsendtSoknad> hentInnsendteSoknaderForFnr(String fnr) {
        Properties bundle = getBundle();

        List<SoknadMetadata> soknader = soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(fnr);

        return soknader.stream().map(soknad ->
                new InnsendtSoknad()
                        .withAvsender(new Part()
                                .withType(Part.Type.BRUKER)
                                .withVisningsNavn(bundle.getProperty("saksoversikt.mottaker.deg")))
                        .withMottaker(new Part()
                                .withType(Part.Type.NAV)
                                .withVisningsNavn(bundle.getProperty("saksoversikt.mottaker.nav")))
                        .withBehandlingsId(soknad.behandlingsId)
                        .withInnsendtDato(tilDate(soknad.innsendtDato))
                        .withHoveddokument(new Hoveddokument()
                                .withTittel(soknad.type.equals(SEND_SOKNAD_KOMMUNAL) ? bundle.getProperty("saksoversikt.soknadsnavn") : bundle.getProperty("saksoversikt.soknadsnavn.ettersending")))
                        .withVedlegg(tilInnsendteVedlegg(soknad.vedlegg, bundle))
                        .withTema("KOM")
                        .withTemanavn(bundle.getProperty("saksoversikt.temanavn"))
                        .withLenke(lagEttersendelseLenke(soknad.behandlingsId)))
                .collect(toList());
    }

    public List<PabegyntSoknad> hentPabegynteSoknaderForBruker(String fnr) {
        List<SoknadMetadata> soknader = soknadMetadataRepository.hentPabegynteSoknaderForBruker(fnr);

        return soknader.stream().map(soknad ->
                new PabegyntSoknad()
                        .withBehandlingsId(soknad.behandlingsId)
                        .withTittel("Søknad om økonomisk sosialhjelp")
                        .withSisteEndring(tilDate(soknad.sistEndretDato))
                        .withLenke(lagFortsettSoknadLenke(soknad.behandlingsId))
        ).collect(toList());
    }

    public List<EttersendingsSoknad> hentSoknaderBrukerKanEttersendePa(String fnr) {
        Properties bundle = getBundle();
        LocalDateTime ettersendelseFrist = LocalDateTime.now(clock)
                .minusDays(ETTERSENDELSE_FRIST_DAGER);
        DateTimeFormatter datoFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy");

        List<SoknadMetadata> soknader = soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(fnr, ettersendelseFrist);

        return soknader.stream().map(soknad ->
            new EttersendingsSoknad()
                .withBehandlingsId(soknad.behandlingsId)
                .withTittel(bundle.getProperty("saksoversikt.soknadsnavn") + " (" + soknad.innsendtDato.format(datoFormatter) +  ")")
                .withLenke(lagEttersendelseLenke(soknad.behandlingsId))
                .withVedlegg(finnManglendeVedlegg(soknad, bundle))
        ).collect(toList());
    }

    private List<Vedlegg> finnManglendeVedlegg(SoknadMetadata soknad, Properties bundle) {
        SoknadMetadata nyesteSoknad = ettersendingService.hentNyesteSoknadIKjede(soknad);

        return nyesteSoknad.vedlegg.vedleggListe.stream()
                .filter(v -> v.status.er(VedleggKreves))
                .filter(v -> !(isVedleggskravAnnet(v)))
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

    static String lagEttersendelseLenke(String behandlingsId) {
        return lagContextLenke() + "skjema/" + behandlingsId + "/ettersendelse";
    }

    private String lagFortsettSoknadLenke(String behandlingsId) {
        return lagContextLenke() + "skjema/" + behandlingsId + "/0";
    }

    private static String lagContextLenke() {
        String miljo = System.getProperty("environment.name", "");

        String tjenesterPostfix = "";
        if (miljo.contains("t") || miljo.contains("q")) {
            tjenesterPostfix = "-" + miljo;
        }

        return "https://www" + tjenesterPostfix + ".nav.no/sosialhjelp/soknad/";
    }

}
