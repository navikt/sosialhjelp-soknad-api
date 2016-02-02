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
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.FerdigSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.PredicateUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.InstanceofPredicate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;

@Component
public class FerdigSoknadService {

    public static final String SKJEMANUMMER_KVITTERING = "L7";

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private VedleggService vedleggService;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    private final Predicate<Vedlegg> IKKE_KVITTERING = new Predicate<Vedlegg>() {
        @Override
        public boolean evaluate(Vedlegg vedlegg) {
            return !SKJEMANUMMER_KVITTERING.equalsIgnoreCase(vedlegg.getSkjemaNummer());
        }
    };
    public static final Predicate<Vedlegg> LASTET_OPP = new Predicate<Vedlegg>() {
        @Override
        public boolean evaluate(Vedlegg v) {
            return Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg());
        }
    };
    public static final Predicate<Vedlegg> IKKE_LASTET_OPP = PredicateUtils.notPredicate(LASTET_OPP);


    public FerdigSoknad hentFerdigSoknad(String behandlingsId) {
        final XMLHenvendelse xmlHenvendelse = henvendelseService.hentInformasjonOmAvsluttetSoknad(behandlingsId);

        Optional<XMLMetadata> head = on(xmlHenvendelse.getMetadataListe().getMetadata()).filter(new InstanceofPredicate(XMLHovedskjema.class)).head();

        XMLHovedskjema hovedskjema = (XMLHovedskjema) head.getOrThrow(new ApplicationException(String.format("Soknaden %s har ikke noe hovedskjema", behandlingsId)));
        KravdialogInformasjon konfigurasjon = kravdialogInformasjonHolder.hentKonfigurasjon(hovedskjema.getSkjemanummer());

        List<Vedlegg> vedlegg = on(xmlHenvendelse.getMetadataListe().getMetadata()).map(new Transformer<XMLMetadata, Vedlegg>() {
            @Override
            public Vedlegg transform(XMLMetadata xmlMetadata) {
                XMLVedlegg xmlVedlegg = (XMLVedlegg) xmlMetadata;
                return new Vedlegg()
                        .medInnsendingsvalg(Transformers.toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                        .medSkjemaNummer(xmlVedlegg.getSkjemanummer())
                        .medSkjemanummerTillegg(xmlVedlegg.getSkjemanummerTillegg())
                        .medNavn(xmlVedlegg.getTilleggsinfo());
            }
        }).filter(IKKE_KVITTERING).collect();

        vedleggService.leggTilKodeverkFelter(vedlegg);

        List<Vedlegg> innsendteVedlegg = on(vedlegg).filter(LASTET_OPP).collect();
        List<Vedlegg> ikkeInnsendteVedlegg = on(vedlegg).filter(IKKE_LASTET_OPP).collect();

        return new FerdigSoknad()
                .medSoknadPrefix(konfigurasjon.getSoknadTypePrefix())
                .medBehandlingId(xmlHenvendelse.getBehandlingsId())
                .medTemakode(xmlHenvendelse.getTema())
                .medInnsendteVedlegg(innsendteVedlegg)
                .medIkkeInnsendteVedlegg(ikkeInnsendteVedlegg)
                .medDato(xmlHenvendelse.getAvsluttetDato());
    }
}