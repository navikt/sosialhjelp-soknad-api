package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.PredicateUtils;
import org.apache.commons.collections15.functors.InstanceofPredicate;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static no.nav.modig.lang.collections.IterUtils.on;

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

    public static final Predicate<Vedlegg> LASTET_OPP = new Predicate<Vedlegg>() {
        @Override
        public boolean evaluate(Vedlegg v) {
            return Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg());
        }
    };

    public static final Predicate<Vedlegg> IKKE_LASTET_OPP = PredicateUtils.notPredicate(LASTET_OPP);

    public InnsendtSoknad hentInnsendtSoknad(String behandlingsId, String sprak) {
        final XMLHenvendelse xmlHenvendelse = henvendelseService.hentInformasjonOmAvsluttetSoknad(behandlingsId);

        Optional<XMLMetadata> head = on(xmlHenvendelse.getMetadataListe().getMetadata()).filter(new InstanceofPredicate(XMLHovedskjema.class)).head();

        final XMLHovedskjema hovedskjema = (XMLHovedskjema) head.getOrThrow(new ApplicationException(String.format("Soknaden %s har ikke noe hovedskjema", behandlingsId)));

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

        }).filter(IKKE_KVITTERING).collect(Collectors.toList());

        Optional<Vedlegg> hovedskjemaVedlegg = on(vedlegg)
                .filter(medSkjemanummer(hovedskjema.getSkjemanummer()))
                .head();

        List<Vedlegg> innsendteVedlegg = on(vedlegg).filter(LASTET_OPP).collect();
        List<Vedlegg> ikkeInnsendteVedlegg = on(vedlegg).filter(IKKE_LASTET_OPP).collect();


        return innsendtSoknad
                .medTittel(hovedskjemaVedlegg.getOrElse(new Vedlegg()).getTittel())
                .medBehandlingId(xmlHenvendelse.getBehandlingsId())
                .medTemakode(xmlHenvendelse.getTema())
                .medInnsendteVedlegg(innsendteVedlegg)
                .medIkkeInnsendteVedlegg(ikkeInnsendteVedlegg)
                .medDato(xmlHenvendelse.getAvsluttetDato());
    }

    private Predicate<Vedlegg> medSkjemanummer(final String skjemanummer) {
        return new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedlegg) {
                return vedlegg.getSkjemaNummer().equalsIgnoreCase(skjemanummer);
            }
        };
    }
}