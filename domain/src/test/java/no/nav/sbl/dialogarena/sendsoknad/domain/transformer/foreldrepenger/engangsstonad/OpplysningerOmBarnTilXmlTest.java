package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.OpplysningerOmBarn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


public class OpplysningerOmBarnTilXmlTest {

    @Test
    public void morFodselHarTermin() {
        WebSoknad soknad = settOppSoknad(Stonadstyper.ENGANGSSTONAD_MOR, "fodsel");
        soknad.getFakta().add(new Faktum().medKey("veiledning.mor.terminbekreftelse").medValue("merEnn26Uker"));
        soknad.getFakta().add(new Faktum().medKey("barnet.termindatering").medValue("2017-05-01"));
        soknad.getFakta().add(new Faktum().medKey("barnet.signertterminbekreftelse").medValue("Jordmor Mats"));

        OpplysningerOmBarn resultat = new OpplysningerOmBarnTilXml().apply(soknad);
        assertThat(resultat.getTermindato()).isEqualTo(LocalDate.of(2017, 6, 3));
        assertThat(resultat.getTerminbekreftelsedato()).isEqualTo(LocalDate.of(2017, 5, 1));
        assertThat(resultat.getNavnPaaTerminbekreftelse()).isEqualTo("Jordmor Mats");
        assertThat(resultat.getAntallBarn()).isEqualTo(2);
    }

    @Test
    public void morFodselHarFodt() {
        WebSoknad soknad = settOppSoknad(Stonadstyper.ENGANGSSTONAD_MOR, "fodsel");
        soknad.getFakta().add(new Faktum().medKey("veiledning.mor.terminbekreftelse").medValue("fodt"));

        OpplysningerOmBarn resultat = new OpplysningerOmBarnTilXml().apply(soknad);
        assertThat(resultat.getFoedselsdatoes().get(0)).isEqualTo(LocalDate.of(2017, 6, 3));
        assertThat(resultat.getAntallBarn()).isEqualTo(2);
    }

    @Test
    public void farFodsel() {
        WebSoknad soknad = settOppSoknad(Stonadstyper.ENGANGSSTONAD_FAR, "fodsel");

        OpplysningerOmBarn resultat = new OpplysningerOmBarnTilXml().apply(soknad);
        assertThat(resultat.getFoedselsdatoes().get(0)).isEqualTo(LocalDate.of(2017, 6, 3));
        assertThat(resultat.getAntallBarn()).isEqualTo(2);
    }

    @Test
    public void adopsjon() {
        WebSoknad soknad = settOppSoknad(Stonadstyper.ENGANGSSTONAD_FAR, "adopsjon");
        soknad.getFakta().add(new Faktum().medKey("barnet.alder").medValue("2013-01-02"));
        soknad.getFakta().add(new Faktum().medKey("barnet.alder").medValue("2015-02-03"));

        OpplysningerOmBarn resultat = new OpplysningerOmBarnTilXml().apply(soknad);
        assertThat(resultat.getOmsorgsovertakelsedato()).isEqualTo(LocalDate.of(2017, 6, 3));
        assertThat(resultat.getAntallBarn()).isEqualTo(2);
        assertThat(resultat.getFoedselsdatoes().get(0)).isEqualTo(LocalDate.of(2013, 1, 2));
        assertThat(resultat.getFoedselsdatoes().get(1)).isEqualTo(LocalDate.of(2015, 2, 3));
    }

    private WebSoknad settOppSoknad(String soknadsType, String fodselEllerAdopsjon) {
        WebSoknad soknad = new WebSoknad();

        soknad.getFakta().add(new Faktum().medKey("soknadsvalg.stonadstype").medValue(soknadsType));
        soknad.getFakta().add(new Faktum().medKey("soknadsvalg.fodselelleradopsjon").medValue(fodselEllerAdopsjon));
        soknad.getFakta().add(new Faktum().medKey("barnet.dato").medValue("2017-06-03"));
        soknad.getFakta().add(new Faktum().medKey("barnet.antall").medValue("2"));

        return soknad;
    }

}