package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InnsendtSoknadServiceTest {

    public static final String SKJEMANUMMER_KVITTERING = InnsendtSoknadService.SKJEMANUMMER_KVITTERING;
    public static final String SOKNAD_PREFIX = "kravdialog.prefix";
    public static final String SPRAK = "no_NB";

    @Mock
    private HenvendelseService henvendelseService;

    @Mock
    private VedleggService vedleggService;

    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Mock
    private KravdialogInformasjon kravdialogInformasjon;

    @InjectMocks
    private InnsendtSoknadService service;

    @Before
    public void setUp() throws Exception {
        when(kravdialogInformasjonHolder.hentKonfigurasjon("NAV 11-12.12")).thenReturn(kravdialogInformasjon);
        when(kravdialogInformasjon.getSoknadTypePrefix()).thenReturn(SOKNAD_PREFIX);

        SoknadMetadata soknadMetadata = new SoknadMetadata();
        soknadMetadata.status = SoknadInnsendingStatus.FERDIG;
        soknadMetadata.skjema = "NAV 11-12.12";
        soknadMetadata.behandlingsId = "123beh";
        soknadMetadata.navEnhet = "NAV Oslo";
        soknadMetadata.orgnr = "999123";
        soknadMetadata.innsendtDato = LocalDateTime.of(2018, 3, 14, 13, 37, 55);

        List<VedleggMetadata> vedlegg = soknadMetadata.vedlegg.vedleggListe;
        VedleggMetadata v1 = new VedleggMetadata();
        v1.status = Vedlegg.Status.LastetOpp;
        v1.skjema = "123";
        v1.filnavn = "abc.jpg";
        vedlegg.add(v1);

        VedleggMetadata v2 = new VedleggMetadata();
        v2.status = Vedlegg.Status.SendesIkke;
        vedlegg.add(v2);

        VedleggMetadata kvittering = new VedleggMetadata();
        kvittering.status = Vedlegg.Status.LastetOpp;
        kvittering.skjema = "L7";
        vedlegg.add(kvittering);

        when(henvendelseService.hentSoknad("123beh")).thenReturn(soknadMetadata);
    }

    @Test
    public void skalFjerneKvitteringerFraVedleggene() throws Exception {
        InnsendtSoknad soknad = service.hentInnsendtSoknad("123beh", SPRAK);
        assertThat(soknad.getIkkeInnsendteVedlegg()).areNot(liktSkjemanummer(SKJEMANUMMER_KVITTERING));
        assertThat(soknad.getInnsendteVedlegg()).areNot(liktSkjemanummer(SKJEMANUMMER_KVITTERING));
    }

    @Test
    public void skalPlassereOpplastetVedleggUnderInnsendteVedlegg() throws Exception {
        InnsendtSoknad soknad = service.hentInnsendtSoknad("123beh", SPRAK);
        assertThat(soknad.getInnsendteVedlegg()).hasSize(1);
        assertThat(soknad.getIkkeInnsendteVedlegg()).hasSize(1);
    }

    @Test
    public void skalMappeDetaljerFraHenvendelse() throws Exception {
        InnsendtSoknad soknad = service.hentInnsendtSoknad("123beh", SPRAK);
        assertThat(soknad.getDato()).isEqualTo("14. mars 2018");
        assertThat(soknad.getKlokkeslett()).isEqualTo("13.37");
        assertThat(soknad.getNavenhet()).isEqualToIgnoringCase("NAV Oslo");
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