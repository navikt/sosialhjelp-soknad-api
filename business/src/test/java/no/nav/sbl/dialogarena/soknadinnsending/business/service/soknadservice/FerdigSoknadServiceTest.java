package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.FerdigSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.assertj.core.api.Condition;
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

    public static final XMLVedlegg KVITTERING = new XMLVedlegg()
            .withInnsendingsvalg("LASTET_OPP")
            .withSkjemanummer(SKJEMANUMMER_KVITTERING);
    @Mock
    private HenvendelseService henvendelseService;

    @Mock
    private VedleggService vedleggService;

    @InjectMocks
    private FerdigSoknadService service;

    @Test
    public void skalFjerneKvitteringerFraVedleggene() throws Exception {
        when(henvendelseService.hentInformasjonOmAvsluttetSoknad(anyString())).thenReturn(
                new XMLHenvendelse()
                        .withMetadataListe(
                                new XMLMetadataListe()
                                        .withMetadata(
                                                KVITTERING)));

        FerdigSoknad soknad = service.hentFerdigSoknad("ID01");
        assertThat(soknad.getIkkeInnsendteVedlegg()).areNot(new Condition<Vedlegg>() {
            @Override
            public boolean matches(Vedlegg vedlegg) {
                return SKJEMANUMMER_KVITTERING.equals(vedlegg.getSkjemaNummer());
            }
        });
        assertThat(soknad.getInnsendteVedlegg()).areNot(new Condition<Vedlegg>() {
            @Override
            public boolean matches(Vedlegg vedlegg) {
                return SKJEMANUMMER_KVITTERING.equals(vedlegg.getSkjemaNummer());
            }
        });


    }

    @Test
    public void skalPlassereOpplastetVedleggUnderInnsendteVedlegg() throws Exception {
        when(henvendelseService.hentInformasjonOmAvsluttetSoknad(anyString())).thenReturn(
                new XMLHenvendelse()
                        .withMetadataListe(
                                new XMLMetadataListe()
                                        .withMetadata(
                                                new XMLVedlegg()
                                                        .withInnsendingsvalg("LASTET_OPP")
                                                        .withSkjemanummer("NAV 10-07.40"))));

        FerdigSoknad soknad = service.hentFerdigSoknad("ID01");
        assertThat(soknad.getInnsendteVedlegg()).are(new Condition<Vedlegg>() {
            @Override
            public boolean matches(Vedlegg vedlegg) {
                return "NAV 10-07.40".equals(vedlegg.getSkjemaNummer());
            }
        });
        assertThat(soknad.getIkkeInnsendteVedlegg()).hasSize(0);

    }
}