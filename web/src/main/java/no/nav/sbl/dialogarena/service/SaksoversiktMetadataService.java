package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SaksoversiktMetadataService {

    private static final Logger logger = getLogger(SaksoversiktMetadataService.class);

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private NavMessageSource navMessageSource;

    public List<InnsendtSoknad> hentInnsendteSoknaderForFnr(String fnr) {
        Properties bundle = getBundle();

        List<SoknadMetadata> soknader = soknadMetadataRepository.hentSoknaderMedStatusForBruker(fnr, FERDIG);

        List<InnsendtSoknad> innsendte = soknader.stream().map(soknad ->
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
                                .withTittel(bundle.getProperty("saksoversikt.soknadsnavn")))
                        .withVedlegg(tilJsonVedlegg(soknad.vedlegg, bundle))
                        .withTema("KOM")
                        .withTemanavn(bundle.getProperty("saksoversikt.temanavn"))
                        .withLenke(null)).collect(toList());

        return innsendte;
    }

    public List<PabegyntSoknad> hentPabegynteSoknaderForBruker(String fnr) {
        Properties bundle = getBundle();

        List<SoknadMetadata> soknader = soknadMetadataRepository.hentSoknaderMedStatusForBruker(fnr, UNDER_ARBEID);

        return soknader.stream().map(soknad ->
                new PabegyntSoknad()
                        .withBehandlingsId(soknad.behandlingsId)
                        .withTittel(bundle.getProperty("saksoversikt.soknadsnavn"))
                        .withSisteEndring(tilDate(soknad.sistEndretDato))
                        .withLenke(null)
        ).collect(toList());
    }

    private List<Vedlegg> tilJsonVedlegg(VedleggMetadataListe vedlegg, Properties bundle) {
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

}
