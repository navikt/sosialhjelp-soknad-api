package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.OpplysningerOmFar;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OpplysningerOmFarTilXmlTest {

    @Test
    public void harFodselsnummerForFar(){
        WebSoknad soknad = settOppSoknad("engangsstonadMor", "fodsel");
        soknad.getFakta().add(new Faktum().medKey("infofar.opplysninger.fornavn").medValue("Nemanjus"));
        soknad.getFakta().add(new Faktum().medKey("infofar.opplysninger.etternavn").medValue("Kultetternavn"));
        soknad.getFakta().add(new Faktum().medKey("infofar.opplysninger.personinfo").medProperty("personnummer","***REMOVED***"));

        OpplysningerOmFar resultat = new OpplysningerOmFarTilXml().apply(soknad);
        assertThat(resultat.getFornavn()).isEqualTo("Nemanjus");
        assertThat(resultat.getEtternavn()).isEqualTo("Kultetternavn");
        assertThat(resultat.getPersonidentifikator()).isEqualTo("***REMOVED***");
    }

    @Test
    public void hvisKanIkkeOppgi(){
        WebSoknad soknad = settOppSoknad("engangsstonadMor", "fodsel");
        soknad.getFakta().add(new Faktum().medKey("infofar.opplysninger.fornavn").medValue("Nemanjus"));
        soknad.getFakta().add(new Faktum().medKey("infofar.opplysninger.etternavn").medValue("Kultetternavn"));
        soknad.getFakta().add(new Faktum().medKey("infofar.opplysninger.personinfo").medProperty("personnummer","***REMOVED***"));
        soknad.getFakta().add(new Faktum().medKey("infofar.opplysninger.kanIkkeOppgi").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("infofar.opplysninger.kanIkkeOppgi.true.arsak").medValue("utenlandsk").medProperty("land","AFG"));


        OpplysningerOmFar resultat = new OpplysningerOmFarTilXml().apply(soknad);
        assertThat(resultat.getFornavn()).isEqualTo("Nemanjus");
        assertThat(resultat.getEtternavn()).isEqualTo("Kultetternavn");
        assertThat(resultat.getPersonidentifikator()).isEqualTo(null);
        assertThat(resultat.getKanIkkeOppgiFar()).isNotNull();
        assertThat(resultat.getKanIkkeOppgiFar().getUtenlandskfnrLand().getKode()).isEqualTo("AFG");

        soknad.getFaktaMedKey("infofar.opplysninger.kanIkkeOppgi.true.arsak").get(0).medValue("ukjent");
        resultat = new OpplysningerOmFarTilXml().apply(soknad);

        assertThat(resultat.getFornavn()).isEqualTo(null);
        assertThat(resultat.getEtternavn()).isEqualTo(null);
        assertThat(resultat.getKanIkkeOppgiFar().getUtenlandskfnrLand()).isEqualTo(null);
    }

    private WebSoknad settOppSoknad(String soknadsType, String fodselEllerAdopsjon) {
        WebSoknad soknad = new WebSoknad();

        soknad.getFakta().add(new Faktum().medKey("soknadsvalg.stonadstype").medValue(soknadsType));
        soknad.getFakta().add(new Faktum().medKey("soknadsvalg.fodselelleradopsjon").medValue(fodselEllerAdopsjon));

        return soknad;
    }
}
