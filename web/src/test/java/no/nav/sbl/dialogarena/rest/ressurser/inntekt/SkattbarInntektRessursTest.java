package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonOkonomiOpplysningerConverter;
import no.nav.sbl.dialogarena.soknadinnsending.business.utbetaling.UtbetalingBolk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.SkattbarInntektService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class SkattbarInntektRessursTest {

    @Mock
    private LegacyHelper legacyHelper;
    @Mock
    private UtbetalingService utbetalingService;

    @Mock
    private ArbeidsforholdTransformer arbeidsforholdTransformer;

    @Spy
    SkattbarInntektService skattbarInntektService;

    @InjectMocks
    UtbetalingBolk utbetalingBolk;

    @InjectMocks
    SkattbarInntektRessurs ressurs;

    @Before
    public void setUp() throws Exception {
        System.setProperty("tillatmock", "true");
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("tillatmock", "false");
    }

    @Test
    public void feilIKalletMotSkattOgReturnereIngenUtbetalinger() {
        skattbarInntektService.mockFil = "tull";
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());

        List<Faktum> mockUtbetalinger = utbetalingBolk.genererSystemFakta("01234567890", 1234L);
        ressurs.mockUtbetalinger =  JsonOkonomiOpplysningerConverter.getOkonomiopplysningFraFaktum(mockUtbetalinger, Collections.emptyList());
        List<SkattbarInntektRessurs.SkattbarInntektOgForskuddstrekk> skattbarInntektOgForskuddstrekkListe = ressurs.hentSkattbareInntekter("");
        assertThat(skattbarInntektOgForskuddstrekkListe).isEmpty();
        assertThat(mockUtbetalinger).hasSize(2);
        assertThat(mockUtbetalinger.get(0).getKey()).isEqualTo("utbetalinger.ingen");
        assertThat(mockUtbetalinger.get(1).getKey()).isEqualTo("utbetalinger.fra.skatteetaten.feilet");
    }

    @Test
    public void hentMockOgProduserObjektmodellTilGui() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", StaticSubjectHandler.class.getName());

        List<Faktum> mockUtbetalinger = utbetalingBolk.genererSystemFakta("01234567890", 1234L);
        ressurs.mockUtbetalinger =  JsonOkonomiOpplysningerConverter.getOkonomiopplysningFraFaktum(mockUtbetalinger, Collections.emptyList());
        List<SkattbarInntektRessurs.SkattbarInntektOgForskuddstrekk> skattbarInntektOgForskuddstrekkListe = ressurs.hentSkattbareInntekter("");
        SkattbarInntektRessurs.SkattbarInntektOgForskuddstrekk skattbarInntektOgForskuddstrekk = skattbarInntektOgForskuddstrekkListe.get(0);
        assertThat(skattbarInntektOgForskuddstrekk.samletTrekk).isNegative();
        assertThat(skattbarInntektOgForskuddstrekk.samletInntekt).isPositive();
        assertThat(skattbarInntektOgForskuddstrekk.organisasjoner).hasSize(3);
        assertThat(skattbarInntektOgForskuddstrekk.organisasjoner.get(0).tom).isNotNull();
    }

}