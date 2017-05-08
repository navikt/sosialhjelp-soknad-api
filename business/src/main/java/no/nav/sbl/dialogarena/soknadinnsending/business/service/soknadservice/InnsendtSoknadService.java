package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
        final XMLHenvendelse xmlHenvendelse = henvendelseService.hentInformasjonOmAvsluttetSoknad(behandlingsId);

        XMLHovedskjema hovedskjema = (XMLHovedskjema) xmlHenvendelse.getMetadataListe().getMetadata().stream()
                .filter(xmlMetadata -> xmlMetadata instanceof XMLHovedskjema)
                .findFirst()
                .orElseThrow(()->new ApplicationException(String.format("Soknaden %s har ikke noe hovedskjema", behandlingsId)));

        final Locale locale = LocaleUtils.toLocale(sprak);
        InnsendtSoknad innsendtSoknad = new InnsendtSoknad(locale);

        try {
            KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon(hovedskjema.getSkjemanummer());
            String prefix = konfigurasjon.getSoknadTypePrefix();
            innsendtSoknad.medTittelCmsKey(prefix.concat(".").concat("skjema.tittel"));
        } catch (ApplicationException e) {//NOSONAR
            /*Dersom vi får en ApplicationException betyr det at soknaden ikke har noen konfigurasjon i sendsoknad.
            * Det er mest sannsynlig fordi soknaden er sendt inn via dokumentinnsending. I dette tilfellet bruker vi tittelen
            * på hoveddokumentet som skjematittel. Denne finnes for alle soknader.
            * */
        }

        final List<Vedlegg> vedlegg = xmlHenvendelse.getMetadataListe().getMetadata().stream().map(xmlMetadata -> {
                XMLVedlegg xmlVedlegg = (XMLVedlegg) xmlMetadata;
                Vedlegg v = new Vedlegg()
                        .medInnsendingsvalg(Transformers.toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                        .medSkjemaNummer(xmlVedlegg.getSkjemanummer())
                        .medSkjemanummerTillegg(xmlVedlegg.getSkjemanummerTillegg())
                        .medNavn(xmlVedlegg.getTilleggsinfo());
                vedleggService.medKodeverk(v, locale);
                return v;

        }).filter(IKKE_KVITTERING).collect(toList());

        Optional<Vedlegg> hovedskjemaVedlegg = vedlegg.stream()
                .filter(medSkjemanummer(hovedskjema.getSkjemanummer())).findFirst();

        List<Vedlegg> innsendteVedlegg = vedlegg.stream().filter(LASTET_OPP).collect(toList());
        List<Vedlegg> ikkeInnsendteVedlegg = vedlegg.stream().filter(IKKE_LASTET_OPP).collect(toList());


        return innsendtSoknad
                .medTittel(hovedskjemaVedlegg.orElse(new Vedlegg()).getTittel())
                .medBehandlingId(xmlHenvendelse.getBehandlingsId())
                .medTemakode(xmlHenvendelse.getTema())
                .medInnsendteVedlegg(innsendteVedlegg)
                .medIkkeInnsendteVedlegg(ikkeInnsendteVedlegg)
                .medDato(xmlHenvendelse.getAvsluttetDato());
    }

    private Predicate<Vedlegg> medSkjemanummer(final String skjemanummer) {
        return vedlegg -> vedlegg.getSkjemaNummer().equalsIgnoreCase(skjemanummer);
    }
}