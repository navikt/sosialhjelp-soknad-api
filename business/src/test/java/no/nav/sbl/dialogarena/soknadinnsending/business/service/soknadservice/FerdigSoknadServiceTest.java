package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.FerdigSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FerdigSoknadServiceTest {

    public static final String SKJEMANUMMER_KVITTERING = FerdigSoknadService.SKJEMANUMMER_KVITTERING;

    @Mock
    private HenvendelseService henvendelseService;

    @Mock
    private VedleggService vedleggService;

    @InjectMocks
    private FerdigSoknadService service;
    private XMLMetadataListe xmlMetadataListe;

    @Before
    public void setUp() throws Exception {
        XMLHenvendelse xmlHenvendelse = new XMLHenvendelse();
        xmlMetadataListe = new XMLMetadataListe();
        when(henvendelseService.hentInformasjonOmAvsluttetSoknad(anyString())).thenReturn(
                xmlHenvendelse.withMetadataListe(xmlMetadataListe));
    }

    @Test
    public void skalFjerneKvitteringerFraVedleggene() throws Exception {
        xmlMetadataListe.withMetadata(new XMLVedlegg()
                .withInnsendingsvalg("LASTET_OPP")
                .withSkjemanummer(SKJEMANUMMER_KVITTERING));

        FerdigSoknad soknad = service.hentFerdigSoknad("ID01");
        assertThat(soknad.getIkkeInnsendteVedlegg()).areNot(liktSkjemanummer(SKJEMANUMMER_KVITTERING));
        assertThat(soknad.getInnsendteVedlegg()).areNot(liktSkjemanummer(SKJEMANUMMER_KVITTERING));
    }

    @Test
    public void skalPlassereOpplastetVedleggUnderInnsendteVedlegg() throws Exception {
        String skjemanummer = "NAV 10-07.40";
        xmlMetadataListe.withMetadata(
                new XMLVedlegg()
                        .withInnsendingsvalg("LASTET_OPP")
                        .withSkjemanummer(skjemanummer));

        FerdigSoknad soknad = service.hentFerdigSoknad("ID01");
        assertThat(soknad.getInnsendteVedlegg()).are(liktSkjemanummer(skjemanummer));
        assertThat(soknad.getIkkeInnsendteVedlegg()).hasSize(0);

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