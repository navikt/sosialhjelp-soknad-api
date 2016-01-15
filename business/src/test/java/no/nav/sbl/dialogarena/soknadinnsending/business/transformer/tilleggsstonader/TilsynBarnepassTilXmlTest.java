package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Barn;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterBarn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.StofoKodeverkVerdier;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.TilsynBarnepassTilXml;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.StofoKodeverkVerdier.BarnepassAarsak.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTestUtils.periodeMatcher;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilsynBarnepassTilXmlTest {
    @Mock
    MessageSource navMessageSource;
    long barnId = 10;
    private TilsynBarnepassTilXml tilsynBarnepassTilXml;
    private TilsynsutgifterBarn tilsynsutgifterBarnXml;
    private WebSoknad soknad;

    @Before
    public void beforeEach() {
        soknad = new WebSoknad();
        tilsynBarnepassTilXml = new TilsynBarnepassTilXml(navMessageSource);
        List<Faktum> fakta = new ArrayList<>();

        fakta.add(new Faktum().medKey("barnepass.periode").medProperty("fom", "2015-01-01").medProperty("tom", "2016-01-01"));

        soknad.setFakta(fakta);
    }

    @Test
    public void skalLeggeTilPeriode() {
        tilsynsutgifterBarnXml = tilsynBarnepassTilXml.transform(soknad);
        assertThat(tilsynsutgifterBarnXml.getPeriode().getFom()).is(periodeMatcher(2015, 1, 1));
        assertThat(tilsynsutgifterBarnXml.getPeriode().getTom()).is(periodeMatcher(2016, 1, 1));
    }

    @Test
    public void skalLeggeTilBarnSomDetSokesBarnepassFor() {
        String oleNavn = "Ole Mockmann";
        String oleFnr = "01020312345";


        String doleNavn = "Dole Mockmann";
        String doleFodselsdag = "1991-01-08";

        String annenForsorger = "09080745610";

        String barnehage = "barnehage";
        String dagmamma = "dagmamma";
        String privat = "privat";

        leggTilBarn("fnr", oleFnr, oleNavn, "true", annenForsorger, barnehage, true, true, false, false, SYSTEMREGISTRERT);
        leggTilBarn("fodselsdato", doleFodselsdag, doleNavn, "true", annenForsorger, dagmamma, false, false, true, false, BRUKERREGISTRERT);
        leggTilBarn("fnr", "12312312312", "Doffen Mockmann", "false", null, privat, false, false, false, false, SYSTEMREGISTRERT);

        when(navMessageSource.getMessage(eq(trengertilsyn.cmsKey), isNull(Object[].class), eq(trengertilsyn.cmsKey), any(Locale.class))).thenReturn("tilsyn");
        when(navMessageSource.getMessage(eq(langvarig.cmsKey), isNull(Object[].class), eq(langvarig.cmsKey), any(Locale.class))).thenReturn("langvarig");
        when(navMessageSource.getMessage(eq(ingen.cmsKey), isNull(Object[].class), eq(ingen.cmsKey), any(Locale.class))).thenReturn("ingen");

        tilsynsutgifterBarnXml = tilsynBarnepassTilXml.transform(soknad);
        List<Barn> barn = tilsynsutgifterBarnXml.getBarn();
        assertThat(barn.size()).isEqualTo(2);


        assertThat(barn.get(0).getNavn()).isEqualTo("Ole");
        assertThat(barn.get(0).getPersonidentifikator()).isEqualTo(oleFnr);
        assertThat(barn.get(0).getTilsynskategori().getValue()).isEqualTo(StofoKodeverkVerdier.TilsynForetasAvKodeverk.barnehage.kodeverksverdi);
        assertThat(barn.get(0).isHarFullfoertFjerdeSkoleaar()).isEqualTo(true);
        assertThat(barn.get(0).getAarsakTilBarnepass()).contains("tilsyn");

        assertThat(barn.get(1).getNavn()).isEqualTo("Dole");
        assertThat(barn.get(1).getPersonidentifikator()).isEqualTo(doleFodselsdag);
        assertThat(barn.get(1).getTilsynskategori().getValue()).isEqualTo(StofoKodeverkVerdier.TilsynForetasAvKodeverk.dagmamma.kodeverksverdi);
        assertThat(barn.get(1).isHarFullfoertFjerdeSkoleaar()).isEqualTo(false);
        assertThat(tilsynsutgifterBarnXml.getAnnenForsoergerperson()).isEqualTo(annenForsorger);
        assertThat(barn.get(1).getAarsakTilBarnepass()).contains("langvarig");
    }

    private void leggTilBarn(String identifikatorType, String identifikator, String navn, String sokesOm, String annenForsorger, String type, boolean fullortFjerdeSkolear, boolean tilsyn, boolean langvarig, boolean ingen, Faktum.FaktumType barnefaktumType) {
        long faktumId = barnId++;
        soknad.getFakta().add(new Faktum().medKey("barn")
                .medFaktumId(faktumId)
                .medProperty(identifikatorType, identifikator)
                .medProperty("sammensattnavn", navn)
                .medProperty("fornavn", navn.split(" ")[0])
                .medProperty("etternavn", navn.split(" ")[1])
                .medType(barnefaktumType));
        soknad.getFakta().add(new Faktum().medKey("andreforelder").medValue(annenForsorger));
        soknad.getFakta().add(new Faktum()
                .medFaktumId(faktumId + 1000)
                .medKey("barnepass.sokerbarnepass")
                .medValue(sokesOm)
                .medProperty("tilknyttetbarn", "" + faktumId)
                .medProperty("sokerOmBarnepass", sokesOm));

        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_TYPER).medValue(type));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_ANDREFORELDER).medValue(annenForsorger));
        soknad.getFakta().add(new Faktum().medFaktumId(faktumId + 10000).medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_FOLLFORT_FJERDE).medValue("" + fullortFjerdeSkolear));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 10000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAKER.get(0)).medValue("" + langvarig));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 10000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAKER.get(1)).medValue("" + tilsyn));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 10000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAKER.get(2)).medValue("" + ingen));
    }
}
