package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.FerdigSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.assertj.core.api.Condition;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FerdigSoknadServiceTest {

    public static final String SKJEMANUMMER_KVITTERING = FerdigSoknadService.SKJEMANUMMER_KVITTERING;
    public static final String SOKNAD_PREFIX = "kravdialog.prefix";
    public static final XMLHovedskjema HOVEDSKJEMA = new XMLHovedskjema()
            .withSkjemanummer("NAV 11-12.12")
            .withInnsendingsvalg("LASTET_OPP");

    private XMLMetadataListe xmlMetadataListe;
    private XMLHenvendelse xmlHenvendelse;

    @Mock
    private HenvendelseService henvendelseService;

    @Mock
    private VedleggService vedleggService;

    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Mock
    private KravdialogInformasjon kravdialogInformasjon;

    @InjectMocks
    private FerdigSoknadService service;

    @Before
    public void setUp() throws Exception {

        when(kravdialogInformasjonHolder.hentKonfigurasjon(HOVEDSKJEMA.getSkjemanummer())).thenReturn(kravdialogInformasjon);
        when(kravdialogInformasjon.getSoknadTypePrefix()).thenReturn(SOKNAD_PREFIX);
        xmlHenvendelse = new XMLHenvendelse();
        xmlMetadataListe = new XMLMetadataListe();
        when(henvendelseService.hentInformasjonOmAvsluttetSoknad(anyString())).thenReturn(
                xmlHenvendelse.withMetadataListe(xmlMetadataListe));
    }

    @Test
    public void skalFjerneKvitteringerFraVedleggene() throws Exception {
        xmlMetadataListe.withMetadata(
                HOVEDSKJEMA,
                new XMLVedlegg()
                .withInnsendingsvalg("LASTET_OPP")
                .withSkjemanummer(SKJEMANUMMER_KVITTERING));

        FerdigSoknad soknad = service.hentFerdigSoknad("ID01");
        assertThat(soknad.getIkkeInnsendteVedlegg()).areNot(liktSkjemanummer(SKJEMANUMMER_KVITTERING));
        assertThat(soknad.getInnsendteVedlegg()).areNot(liktSkjemanummer(SKJEMANUMMER_KVITTERING));
    }

    @Test
    public void skalPlassereOpplastetVedleggUnderInnsendteVedlegg() throws Exception {
        xmlMetadataListe.withMetadata(HOVEDSKJEMA);
        FerdigSoknad soknad = service.hentFerdigSoknad("ID01");
        assertThat(soknad.getInnsendteVedlegg()).are(liktSkjemanummer(HOVEDSKJEMA.getSkjemanummer()));
        assertThat(soknad.getIkkeInnsendteVedlegg()).hasSize(0);
    }

    @Test
    public void skalMappeDetaljerFraHenvendelse() throws Exception {
        xmlMetadataListe.withMetadata(HOVEDSKJEMA);
        xmlHenvendelse
                .withAvsluttetDato(new DateTime(2016, 01, 01, 12, 00))
                .withTema("TSO");

        FerdigSoknad soknad = service.hentFerdigSoknad("ID01");
        assertThat(soknad.getDato()).isEqualToIgnoringCase("1. januar 2016 klokken 12.00");
        assertThat(soknad.getTemakode()).isEqualToIgnoringCase("TSO");
    }

    @Test
    public void skalHenteUtSoknadprefixFraHovedskjema() throws Exception {
        xmlMetadataListe.withMetadata(
                HOVEDSKJEMA,
                new XMLVedlegg().withInnsendingsvalg("SEND_SENERE").withSkjemanummer(SKJEMANUMMER_KVITTERING));

        FerdigSoknad soknad = service.hentFerdigSoknad("ID01");
        assertThat(soknad.getSoknadPrefix()).isEqualToIgnoringCase(SOKNAD_PREFIX);
    }

    @Test
    public void skalPlassereIkkeOpplastetVedleggUnderIkkeInnsendteVedlegg() throws Exception {
        Collection<XMLMetadata> ikkeInnsendteVedlegg = Arrays.asList(
                (XMLMetadata) new XMLVedlegg().withInnsendingsvalg("VEDLEGG_SENDES_AV_ANDRE"),
                new XMLVedlegg().withInnsendingsvalg("SEND_SENERE"),
                new XMLVedlegg().withInnsendingsvalg("VEDLEGG_ALLEREDE_SENDT"),
                new XMLVedlegg().withInnsendingsvalg("VEDLEGG_SENDES_IKKE"));
        xmlMetadataListe.withMetadata(HOVEDSKJEMA);
        xmlMetadataListe.withMetadata(ikkeInnsendteVedlegg);

        FerdigSoknad soknad = service.hentFerdigSoknad("ID01");
        assertThat(soknad.getInnsendteVedlegg()).hasSize(1);
        assertThat(soknad.getIkkeInnsendteVedlegg()).hasSameSizeAs(ikkeInnsendteVedlegg);

    }

    private Condition<Vedlegg> liktSkjemanummer(final String skjemanummer) {
        return new Condition<Vedlegg>() {
            @Override
            public boolean matches(Vedlegg vedlegg) {
                return skjemanummer.equals(vedlegg.getSkjemaNummer());
            }
        };
    }
}