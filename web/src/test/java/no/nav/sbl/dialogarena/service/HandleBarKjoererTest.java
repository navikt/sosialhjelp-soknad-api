package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.service.helpers.*;
import no.nav.sbl.dialogarena.service.helpers.faktum.ForFaktaMedPropertySattTilTrueHelper;
import no.nav.sbl.dialogarena.service.helpers.faktum.ForFaktumHelper;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import javax.xml.bind.JAXB;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
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
        when(cmsTekst.getCmsTekst(any(String.class), any(Object[].class), anyString())).thenReturn("mock");
        registerHelper(new HvisSantHelper());
        registerHelper(new HvisLikHelper());
        registerHelper(new ForFaktumHelper());
        registerHelper(new HvisMerHelper());
        registerHelper(new HvisMindreHelper());
        registerHelper(new ForFaktaMedPropertySattTilTrueHelper());
        registerHelper(new ForFaktaHelper());
        registerHelper(new FormaterLangDatoHelper());
        registerHelper(new ForFaktumHvisSantHelper());
        registerHelper(hentTekstHelper);
        registerHelper(hentLandHelper);
    }

    private <T> void registerHelper(RegistryAwareHelper<T> helper) {
        handleBarKjoerer.registrerHelper(helper.getNavn(), helper);
    }

    private ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

    @Test
    public void printsoknad() throws IOException {
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasenames("content/dagpenger", "content/sendsoknad");
        SoknadStruktur soknadStruktur = JAXB.unmarshal(HandleBarKjoerer.class.getResourceAsStream("/soknader/dagpenger_ordinaer.xml"), SoknadStruktur.class);
        WebSoknad soknad = JAXB.unmarshal(HandleBarKjoerer.class.getResourceAsStream("/testsoknaddagpenger.xml"), WebSoknad.class);
        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(soknadStruktur);
        Map<String, List<FaktumStruktur>> bolker = new LinkedHashMap<>();
        Map<String, List<FaktumStruktur>> underFaktum = new LinkedHashMap<>();
        StringBuilder output = new StringBuilder();
        output.append("<html><head><meta charset='utf-8'></head><body><h1>Søknad om dagpenger</h1>");
        for (FaktumStruktur faktumStruktur : soknadStruktur.getFakta()) {
            if (faktumStruktur.getDependOn() == null) {
                if (!bolker.containsKey(faktumStruktur.getPanel())) {
                    bolker.put(faktumStruktur.getPanel(), new ArrayList<FaktumStruktur>());
                }
                bolker.get(faktumStruktur.getPanel()).add(faktumStruktur);
            } else {
                if(!underFaktum.containsKey(faktumStruktur.getDependOn().getId())){
                    underFaktum.put(faktumStruktur.getDependOn().getId(), new ArrayList<FaktumStruktur>());
                }
                underFaktum.get(faktumStruktur.getDependOn().getId()).add(faktumStruktur);
            }
        }
        for (String bolk : bolker.keySet()) {
            System.out.println("\n" + bolk);
            output.append("<h2> " + bolk + "</h2><ul>");
            for (FaktumStruktur faktumStruktur : bolker.get(bolk)) {
                output.append("<li>" + printForFaktum(underFaktum, soknad, faktumStruktur, null, 3) + "</li>");
            }
            output.append("</ul>");
        }
        output.append("</body></html>");

        FileOutputStream output1 = new FileOutputStream("/testfil.html");
        IOUtils.write(output, output1);
        System.out.println(output.toString());
    }

    private  String printForFaktum(Map<String, List<FaktumStruktur>> underFaktum, WebSoknad soknad, FaktumStruktur faktumStruktur, Long parentId, int level) {
        StringBuilder builder = new StringBuilder();
        Faktum faktum = soknad.getFaktumMedKey(faktumStruktur.getId());
        if(parentId != null){
            if(soknad.getFaktaMedKeyOgParentFaktum(faktumStruktur.getId(), parentId) != null) {
                faktum = soknad.getFaktumMedKeyOgParentFaktum(faktumStruktur.getId(), parentId);
            }
        }

        if(faktum != null) {

            builder.append("<h" + level + ">" + hentTekst(faktumStruktur.getId() + ".sporsmal", "default") + "</h" + level + ">");
            builder.append("<div>" + hentTekst(faktumStruktur.getId() + "." + faktum.getValue(), "default") + "</div>");
            if(hentTekst(faktumStruktur.getId() + ".hjelpetekst.tittel", null) != null){
                builder.append("<div>").append(hentTekst(faktumStruktur.getId() + ".hjelpetekst.tittel", "default"))
                        .append(hentTekst(faktumStruktur.getId() + ".hjelpetekst.tekst", "default")).append("</div>");
            }
            builder.append( "<div>" + faktumStruktur.getId() + faktum + "<div>");
            if (underFaktum.containsKey(faktumStruktur.getId())) {
                builder.append("<ul>");
                for (FaktumStruktur struktur : underFaktum.get(faktumStruktur.getId())) {
                    Faktum underfaktum = soknad.getFaktumMedKey(faktumStruktur.getId());
                    if(parentId != null){
                        if(soknad.getFaktaMedKeyOgParentFaktum(faktumStruktur.getId(), parentId) != null) {
                            faktum = soknad.getFaktumMedKeyOgParentFaktum(faktumStruktur.getId(), parentId);
                        }
                    }
                    if(struktur.erSynlig(soknad, underfaktum)) {
                        builder.append("<li>" + printForFaktum(underFaktum, soknad, struktur, faktum.getFaktumId(), level++) + "</li>");
                    }
                }
                builder.append("</ul>");
            }
        }
        return builder.toString();
    }

    private String hentTekst(String code, String defaultMessage) {
        return messageSource.getMessage(code, new String[]{}, defaultMessage, new Locale("nb", "no"));
    }

    @Test
    public void skalKompilereDagpenger() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("personalia").medProperty("fnr", "***REMOVED***").medProperty("navn", "Test Nordmann").medProperty("alder", "40").medProperty("statsborgerskap", "NOR"))
                .medFaktum(new Faktum().medKey("arbeidsforhold").medProperty("type", "Arbeidsgiver er konkurs").medProperty("navn", "Test").medProperty("datofra", "2010-01-01").medProperty("datoTil", "2013-01-01"))
                .medFaktum(new Faktum().medKey("barn").medType(SYSTEMREGISTRERT).medProperty("fnr", "***REMOVED***").medProperty("navn", "test barn").medProperty("barnetillegg", "true"))
                .medFaktum(new Faktum().medKey("barn").medType(BRUKERREGISTRERT).medProperty("fodselsdato", "2013-01-01").medProperty("navn", "test barn").medProperty("barnetillegg", "true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigflytte").medValue("true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigdeltid").medValue("false"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigdeltid.maksimalarbeidstid").medValue("20"))
                .medVedlegg(Collections.singletonList(new Vedlegg().medSkjemaNummer("L6").medInnsendingsvalg(Vedlegg.Status.LastetOpp)));

        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger.ordinaer");
        assertThat(html, containsString("***REMOVED***"));
    }

    @Test
    public void skalKompilereTilleggstsstonader() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("personalia").medProperty("fnr", "***REMOVED***").medProperty("navn", "Test Nordmann").medProperty("alder", "40").medProperty("statsborgerskap", "NOR"))
                .medFaktum(new Faktum().medKey("informasjonsside.stonad.reiseaktivitet").medValue("true"))
                .medFaktum(new Faktum().medKey("reise.aktivitet.periode").medProperty("fom", "2015-01-01").medProperty("tom", "2015-02-02"))
                .medFaktum(new Faktum().medKey("bostotte.periode").medProperty("fom", "2015-01-01").medProperty("tom", "2015-02-03"))
                .medFaktum(new Faktum().medKey("reise.aktivitet.medisinskeaarsaker").medValue("false"));
        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(soknad, "/skjema/soknadtilleggsstonader");
        System.out.println(html);
        assertThat(html, containsString("***REMOVED***"));
    }

    @Test
    public void skalReprodusereFeil() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("personalia").medProperty("fnr", "***REMOVED***").medProperty("navn", "Test Nordmann").medProperty("alder", "40").medProperty("statsborgerskap", "NOR"))
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
                .medFaktum(new Faktum().medKey("barn").medType(SYSTEMREGISTRERT).medProperty("fnr", "***REMOVED***").medProperty("navn", "test barn").medProperty("barnetillegg", "true"))
                .medFaktum(new Faktum().medKey("barn").medType(BRUKERREGISTRERT).medProperty("fodselsdato", "2013-01-01").medProperty("navn", "test barn").medProperty("barnetillegg", "true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigflytte").medValue("true"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigdeltid").medValue("false"))
                .medFaktum(new Faktum().medKey("reellarbeidssoker.villigdeltid.maksimalarbeidstid").medValue("20"))
                .medVedlegg(Collections.singletonList(new Vedlegg().medSkjemaNummer("L6").medInnsendingsvalg(Vedlegg.Status.LastetOpp)));
        String html = handleBarKjoerer.fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger.ordinaer");
        assertThat(html, containsString("***REMOVED***"));
    }
}