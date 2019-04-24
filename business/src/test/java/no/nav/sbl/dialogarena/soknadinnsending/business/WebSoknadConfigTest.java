package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.arbeid.SosialhjelpArbeidsforholdBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BarnBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebSoknadConfigTest {

    private static final Long SOKNAD_ID = 123L;
    public static final String AAP_SKJEMANUMMER = "NAV 11-13.05";

    @Mock
    SoknadRepository repository;

    @Spy
    KravdialogInformasjonHolder kravdialog = new KravdialogInformasjonHolder();

    @InjectMocks
    WebSoknadConfig config;

    @Before
    public void setUp() {
        when(repository.hentSoknadType(SOKNAD_ID)).thenReturn(SosialhjelpInformasjon.SKJEMANUMMER);
    }

    @Test
    public void testSoknadtypePrefix() {
        String prefix = config.getSoknadTypePrefix(SOKNAD_ID);
        assertThat(prefix).isEqualTo("soknadsosialhjelp");
    }

    @Test
    public void testAtRiktigeSoknadBolkerErInkludert() {
        BolkService personalia = new PersonaliaBolk();
        BolkService barn = new BarnBolk();
        BolkService arbeidsforhold = new SosialhjelpArbeidsforholdBolk(mock(FaktaService.class), mock(ArbeidsforholdService.class));

        List<BolkService> bolker = config.getSoknadBolker(new WebSoknad().medskjemaNummer(SosialhjelpInformasjon.SKJEMANUMMER), asList(personalia, barn, arbeidsforhold));
        assertThat(bolker.contains(personalia)).isTrue();
        assertThat(bolker.contains(barn)).isTrue();
        assertThat(bolker.contains(arbeidsforhold)).isTrue();
    }
}
