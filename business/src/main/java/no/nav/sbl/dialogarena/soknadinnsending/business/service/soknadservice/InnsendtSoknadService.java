package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import org.apache.commons.lang3.LocaleUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Component
public class InnsendtSoknadService {

    public static final String SKJEMANUMMER_KVITTERING = "L7";

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private VedleggService vedleggService;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    private static final java.util.function.Predicate<Vedlegg> IKKE_KVITTERING = vedlegg -> !SKJEMANUMMER_KVITTERING.equalsIgnoreCase(vedlegg.getSkjemaNummer());

    private static final Predicate<Vedlegg> LASTET_OPP = v -> Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg());

    private static final Predicate<Vedlegg> IKKE_LASTET_OPP = LASTET_OPP.negate();

    public InnsendtSoknad hentInnsendtSoknad(String behandlingsId, String sprak) {
        SoknadMetadata soknadMetadata = henvendelseService.hentSoknad(behandlingsId);
        if (!soknadMetadata.status.equals(SoknadInnsendingStatus.FERDIG)) {
            throw new RuntimeException("Søknad ikke innsendt");
        }

        String skjemaNummer = soknadMetadata.skjema;
        KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon(skjemaNummer);
        final Locale locale = LocaleUtils.toLocale(sprak);
        String prefix = konfigurasjon.getSoknadTypePrefix();

        final List<Vedlegg> vedlegg = soknadMetadata.vedlegg.vedleggListe.stream()
                .map(vedleggMetadata -> {
                   Vedlegg v = new Vedlegg()
                            .medInnsendingsvalg(vedleggMetadata.status)
                            .medSkjemaNummer(vedleggMetadata.skjema)
                            .medSkjemanummerTillegg(vedleggMetadata.tillegg)
                            .medNavn(vedleggMetadata.filnavn);
                    vedleggService.medKodeverk(v, locale);
                    return v;
                }).filter(IKKE_KVITTERING).collect(toList());

        Vedlegg hovedSkjema = new Vedlegg().medSkjemaNummer(skjemaNummer);
        vedleggService.medKodeverk(hovedSkjema, locale);

        List<Vedlegg> innsendteVedlegg = vedlegg.stream().filter(LASTET_OPP).collect(toList());
        List<Vedlegg> ikkeInnsendteVedlegg = vedlegg.stream().filter(IKKE_LASTET_OPP).collect(toList());

        return new InnsendtSoknad(locale)
                .medTittelCmsKey(prefix.concat(".").concat("skjema.tittel"))
                .medTittel(hovedSkjema.getTittel())
                .medBehandlingId(soknadMetadata.behandlingsId)
                .medInnsendteVedlegg(innsendteVedlegg)
                .medIkkeInnsendteVedlegg(ikkeInnsendteVedlegg)
                .medDato(DateTime.parse(soknadMetadata.innsendtDato.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .medNavenhet(soknadMetadata.navEnhet)
                .medOrgnummer(soknadMetadata.orgnr);
    }

}