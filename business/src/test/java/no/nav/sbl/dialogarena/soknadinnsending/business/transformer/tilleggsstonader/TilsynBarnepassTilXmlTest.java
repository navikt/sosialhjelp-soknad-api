package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Barn;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterBarn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTestUtils.periodeMatcher;
import static org.assertj.core.api.Assertions.assertThat;

public class TilsynBarnepassTilXmlTest {
    private final TilsynBarnepassTilXml tilsynBarnepassTilXml = new TilsynBarnepassTilXml();
    private TilsynsutgifterBarn tilsynsutgifterBarnXml;
    private WebSoknad soknad;

    @Before
    public void beforeEach() {
        soknad = new WebSoknad();
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
        String oleFnr = "***REMOVED***";
        String oleAnnenForsorger = "***REMOVED***";

        String doleNavn = "Dole Mockmann";
        String doleFnr = "***REMOVED***";
        String doleAnnenForsorger = "***REMOVED***";

        String barnehage = "true";
        String dagmamma = "true";
        String privat = "true";

        leggTilBarn(oleFnr, oleNavn, "true", oleAnnenForsorger, barnehage, null, null);
        leggTilBarn(doleFnr, doleNavn, "true", doleAnnenForsorger, null, dagmamma, null);
        leggTilBarn("12312312312", "Doffen Mockmann", "false", null, null, null, privat);

        tilsynsutgifterBarnXml = tilsynBarnepassTilXml.transform(soknad);
        List<Barn> barn = tilsynsutgifterBarnXml.getBarn();
        assertThat(barn.size()).isEqualTo(2);

        assertThat(barn.get(0).getNavn()).isEqualTo("Ole");
        assertThat(barn.get(0).getPersonidentifikator()).isEqualTo(oleFnr);
        //assertThat(bar.get(0).getAnnenForsoergerperson()).isEqualTo(oleAnnenForsorger);
        assertThat(barn.get(0).getTilsynskategori().getValue()).isEqualTo(StofoKodeverkVerdier.TilsynForetasAvKodeverk.barnehage.kodeverksverdi);

        assertThat(barn.get(1).getNavn()).isEqualTo("Dole");
        assertThat(barn.get(1).getPersonidentifikator()).isEqualTo(doleFnr);
        //assertThat(barn.get(1).getAnnenForsoergerperson()).isEqualTo(doleAnnenForsorger);
        assertThat(barn.get(1).getTilsynskategori().getValue()).isEqualTo(StofoKodeverkVerdier.TilsynForetasAvKodeverk.dagmamma.kodeverksverdi);
        assertThat(tilsynsutgifterBarnXml.getAnnenForsoergerperson()).isEqualTo("***REMOVED***");
    }
    long barnId = 10;
    private void leggTilBarn(String fnr, String navn, String sokesOm, String annenForsorger, String barnehage, String dagpmamma, String privat) {
        long faktumId = barnId++ ;
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
    }
}
