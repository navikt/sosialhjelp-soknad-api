package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Barn;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterBarn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoKodeverkVerdier.BarnepassAarsak.ingen;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoKodeverkVerdier.BarnepassAarsak.langvarig;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoKodeverkVerdier.BarnepassAarsak.trengertilsyn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTestUtils.periodeMatcher;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
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
    public void skalLeggeTilUtbetalingsdato() {
        soknad.getFakta().add(new Faktum().medKey("barnepass.utbetalingsdato").medValue("1"));

        tilsynsutgifterBarnXml = tilsynBarnepassTilXml.transform(soknad);
        assertThat(tilsynsutgifterBarnXml.getOensketUtbetalingsdag()).isEqualTo(new BigInteger("1"));
    }

    @Test
    public void skalIkkeLeggeTilUtbetalingsdato() {
        tilsynsutgifterBarnXml = tilsynBarnepassTilXml.transform(soknad);
        assertThat(tilsynsutgifterBarnXml.getOensketUtbetalingsdag()).isNotEqualTo(new BigInteger("1"));
    }

    @Test
    public void skalLeggeTilBarnSomDetSokesBarnepassFor() {
        String oleNavn = "Ole Mockmann";
        String oleFnr = "01020312345";
        String oleAnnenForsorger = "09080745612";

        String doleNavn = "Dole Mockmann";
        String doleFnr = "01020312346";
        String doleAnnenForsorger = "09080745610";

        String barnehage = "true";
        String dagmamma = "true";
        String privat = "true";

        leggTilBarn(oleFnr, oleNavn, "true", oleAnnenForsorger, barnehage, null, null, true, true, false, false);
        leggTilBarn(doleFnr, doleNavn, "true", doleAnnenForsorger, null, dagmamma, null, false, false, true, false);
        leggTilBarn("12312312312", "Doffen Mockmann", "false", null, null, null, privat, false, false, false, false);

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
        assertThat(barn.get(0).getAarsakTilBarnepass().getValue()).isEqualTo("tilsyn");

        assertThat(barn.get(1).getNavn()).isEqualTo("Dole");
        assertThat(barn.get(1).getPersonidentifikator()).isEqualTo(doleFnr);
        assertThat(barn.get(1).getTilsynskategori().getValue()).isEqualTo(StofoKodeverkVerdier.TilsynForetasAvKodeverk.dagmamma.kodeverksverdi);
        assertThat(barn.get(1).isHarFullfoertFjerdeSkoleaar()).isEqualTo(false);
        assertThat(tilsynsutgifterBarnXml.getAnnenForsoergerperson()).isEqualTo("09080745610");
        assertThat(barn.get(1).getAarsakTilBarnepass().getValue()).isEqualTo("langvarig");
    }

    private void leggTilBarn(String fnr, String navn, String sokesOm, String annenForsorger, String barnehage, String dagpmamma, String privat, boolean fullortFjerdeSkolear, boolean tilsyn, boolean langvarig, boolean ingen) {
        long faktumId = barnId++;
        soknad.getFakta().add(new Faktum().medKey("barn")
                .medFaktumId(faktumId)
                .medProperty("fnr", fnr)
                .medProperty("sammensattnavn", navn)
                .medProperty("fornavn", navn.split(" ")[0])
                .medProperty("etternavn", navn.split(" ")[1]));
        soknad.getFakta().add(new Faktum()
                .medFaktumId(faktumId + 1000)
                .medKey("barnepass.sokerbarnepass")
                .medValue(sokesOm)
                .medProperty("tilknyttetbarn", "" + faktumId)
                .medProperty("sokerOmBarnepass", sokesOm)
                .medProperty("andreforelder", annenForsorger)
                .medProperty("barnepassBarnehage", barnehage)
                .medProperty("barnepassDagmamma", dagpmamma)
                .medProperty("barnepassPrivat", privat));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_TYPER_BARNEHAGE).medValue(barnehage));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_TYPER_DAGMAMMA).medValue(dagpmamma));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_TYPER_PRIVAT).medValue(privat));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_ANDREFORELDER).medValue(annenForsorger));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_FOLLFORT_FJERDE).medValue("" + fullortFjerdeSkolear));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAKER.get(0)).medValue("" + langvarig));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAKER.get(1)).medValue("" + tilsyn));
        soknad.getFakta().add(new Faktum().medParrentFaktumId(faktumId + 1000).medKey(TilsynBarnepassTilXml.BARNEPASS_AARSAKER.get(2)).medValue("" + ingen));
    }
}
