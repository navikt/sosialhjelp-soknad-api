package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.FerdigSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.PredicateUtils;
import org.apache.commons.collections15.Transformer;
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

        List<Vedlegg> vedlegg = on(xmlHenvendelse.getMetadataListe().getMetadata()).map(new Transformer<XMLMetadata, Vedlegg>() {
            @Override
            public Vedlegg transform(XMLMetadata xmlMetadata) {
                XMLVedlegg xmlVedlegg = (XMLVedlegg) xmlMetadata;
                return new Vedlegg()
                        .medInnsendingsvalg(Transformers.toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                        .medSkjemaNummer(xmlVedlegg.getSkjemanummer())
                        .medSkjemanummerTillegg(xmlVedlegg.getSkjemanummerTillegg());
            }
        }).filter(IKKE_KVITTERING).collect();

        vedleggService.leggTilKodeverkFelter(vedlegg);

        List<Vedlegg> innsendteVedlegg = on(vedlegg).filter(LASTET_OPP).collect();
        List<Vedlegg> ikkeInnsendteVedlegg = on(vedlegg).filter(IKKE_LASTET_OPP).collect();

        return new FerdigSoknad()
                .medBehandlingId(xmlHenvendelse.getBehandlingsId())
                .medInnsendteVedlegg(innsendteVedlegg)
                .medIkkeInnsendteVedlegg(ikkeInnsendteVedlegg)
                .medDato(xmlHenvendelse.getAvsluttetDato());
    }
}