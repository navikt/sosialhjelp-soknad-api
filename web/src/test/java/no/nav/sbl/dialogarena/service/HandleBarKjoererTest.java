package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.helpers.*;
import no.nav.sbl.dialogarena.service.helpers.faktum.ForFaktaMedPropertySattTilTrueHelper;
import no.nav.sbl.dialogarena.service.helpers.faktum.ForFaktumHelper;
import no.nav.sbl.dialogarena.service.helpers.faktum.HvisFlereErTrueHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HandleBarKjoererTest {
    @InjectMocks
    private HandleBarKjoerer handleBarKjoerer;

    @InjectMocks
    private HentTekstHelper hentTekstHelper;

    @InjectMocks
    private HentLandHelper hentLandHelper;

    @Mock
    private CmsTekst cmsTekst;

    @Mock
    private Kodeverk kodeverk;

    @Before
    public void setup() {
        when(cmsTekst.getCmsTekst(any(String.class), any(Object[].class), anyString(), any(Locale.class))).thenReturn("mock");
        registerHelper(new HvisSantHelper());
        registerHelper(new HvisLikHelper());
        registerHelper(new ForFaktumHelper());
        registerHelper(new HvisMerHelper());
        registerHelper(new HvisMindreHelper());
        registerHelper(new ForFaktaMedPropertySattTilTrueHelper());
        registerHelper(new ForFaktaHelper());
        registerHelper(new FormaterLangDatoHelper());
        registerHelper(new ForFaktumHvisSantHelper());
        registerHelper(new VariabelHelper());
        registerHelper(new ForBarnefaktaHelper());
        registerHelper(new HvisFlereErTrueHelper());
        registerHelper(new HvisIkkeTomHelper());
        registerHelper(hentTekstHelper);
        registerHelper(hentLandHelper);
    }

    private <T> void registerHelper(RegistryAwareHelper<T> helper) {
        handleBarKjoerer.registrerHelper(helper.getNavn(), helper);
    }

    private ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

    @Test
    public void skalKompilereDagpenger() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("personalia").medProperty("fnr", "15038000000").medProperty("navn", "Test Nordmann").medProperty("alder", "40").medProperty("statsborgerskap", "NOR"))
                .medFaktum(new Faktum().medKey("arbeidsforhold").medProperty("type", "Arbeidsgiver er konkurs").medProperty("navn", "Test").medProperty("datofra", "2010-01-01").medProperty("datoTil", "2013-01-01"))
                .medFaktum(new Faktum().medKey("barn").medType(SYSTEMREGISTRERT).medProperty("fnr", "01010091736").medProperty("navn", "test barn").medProperty("barnetillegg", "true"))
                .medFaktum(new Faktum().medKey("barn").medType(BRUKERREGISTRERT).medProperty("fodselsdato", "2013-01-01").medProperty("navn", "test barn").medProperty("barnetillegg", "true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigflytte").medValue("true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigdeltid").medValue("false"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigdeltid.maksimalarbeidstid").medValue("20"))
                .medVedlegg(Collections.singletonList(new Vedlegg().medSkjemaNummer("L6").medInnsendingsvalg(Vedlegg.Status.LastetOpp)));

        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger.ordinaer");
        assertThat(html, containsString("15038000000"));
    }

    @Test
    public void skalKompilereTilleggstsstonader() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("personalia").medProperty("fnr", "15038000000").medProperty("navn", "Test Nordmann").medProperty("alder", "40").medProperty("statsborgerskap", "NOR"))
                .medFaktum(new Faktum().medKey("informasjonsside.stonad.reiseaktivitet").medValue("true"))
                .medFaktum(new Faktum().medKey("reise.aktivitet.periode").medProperty("fom", "2015-01-01").medProperty("tom", "2015-02-02"))
                .medFaktum(new Faktum().medKey("bostotte.periode").medProperty("fom", "2015-01-01").medProperty("tom", "2015-02-03"))
                .medFaktum(new Faktum().medKey("reise.aktivitet.medisinskeaarsaker").medValue("false"));
        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(soknad, "/skjema/soknadtilleggsstonader");
        System.out.println(html);
        assertThat(html, containsString("15038000000"));
    }

    @Test
    public void skalReprodusereFeil() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("personalia").medProperty("fnr", "15038000000").medProperty("navn", "Test Nordmann").medProperty("alder", "40").medProperty("statsborgerskap", "NOR"))
                .medFaktum(new Faktum().medKey("arbeidsforhold")
                        .medProperty("type", "Arbeidsgiver er konkurs")
                        .medProperty("navn", "Test")
                        .medProperty("datofra", "2010-01-01")
                        .medProperty("datoTil", "2013-01-01"))
                .medFaktum(new Faktum().medKey("utdanning").medValue("underUtdanning"))
                .medFaktum(new Faktum().medKey("utdanning.kveld").medValue("true"))
                .medFaktum(new Faktum().medKey("utdanning.kveld.progresjonUnder50").medValue("false"))
                .medFaktum(new Faktum().medKey("utdanning.kveld.navn").medValue("test"))
                .medFaktum(new Faktum().medKey("utdanning.kveld.PaabegyntUnder6mnd").medValue("true"))
                .medFaktum(new Faktum().medKey("utdanning.kveld.varighet").medValue(null).medProperty("fom", "2014-02-05"))
                .medFaktum(new Faktum().medKey("utdanning.kveld.folges").medValue("true"))
                .medFaktum(new Faktum().medKey("utdanning.kveld.sted").medValue("test"))
                .medFaktum(new Faktum().medKey("barn").medType(SYSTEMREGISTRERT).medProperty("fnr", "01010091736").medProperty("navn", "test barn").medProperty("barnetillegg", "true"))
                .medFaktum(new Faktum().medKey("barn").medType(BRUKERREGISTRERT).medProperty("fodselsdato", "2013-01-01").medProperty("navn", "test barn").medProperty("barnetillegg", "true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigflytte").medValue("true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigdeltid").medValue("false"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigdeltid.maksimalarbeidstid").medValue("20"))
                .medVedlegg(Collections.singletonList(new Vedlegg().medSkjemaNummer("L6").medInnsendingsvalg(Vedlegg.Status.LastetOpp)));
        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger.ordinaer");
        assertThat(html, containsString("15038000000"));
    }
}