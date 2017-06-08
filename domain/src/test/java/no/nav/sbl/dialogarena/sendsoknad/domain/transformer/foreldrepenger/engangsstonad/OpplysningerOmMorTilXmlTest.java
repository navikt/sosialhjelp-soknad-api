package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;


import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.OpplysningerOmMor;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OpplysningerOmMorTilXmlTest {

    @Test
    public void morErKjentOgHarNorskFodselsnummer() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("infomor.opplysninger.fornavn").medValue("Testmor"));
        soknad.getFakta().add(new Faktum().medKey("infomor.opplysninger.etternavn").medValue("Testesen"));
        soknad.getFakta().add(new Faktum().medKey("infomor.opplysninger.personinfo").medProperty("personnummer", "1234567890"));

        OpplysningerOmMor transformResultat = new OpplysningerOmMorTilXml().apply(soknad);

        assertThat(transformResultat).isNotNull();
        assertThat(transformResultat.getKanIkkeOppgiMor()).isNull();
        assertThat(transformResultat.getFornavn()).isEqualTo("Testmor");
        assertThat(transformResultat.getEtternavn()).isEqualTo("Testesen");
        assertThat(transformResultat.getPersonidentifikator()).isEqualTo("1234567890");
    }

    @Test
    public void morErUtenlandsk() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("infomor.opplysninger.fornavn").medValue("Testmor"));
        soknad.getFakta().add(new Faktum().medKey("infomor.opplysninger.etternavn").medValue("Testesen"));
        soknad.getFakta().add(new Faktum().medKey("infomor.opplysninger.kanIkkeOppgi").medValue("true"));
        soknad.getFakta().add(new Faktum()
                .medKey("infomor.opplysninger.kanIkkeOppgi.true.arsak")
                .medValue("utenlandsk")
                .medProperty("land", "ARG"));
        soknad.getFakta().add(new Faktum()
                .medKey("infomor.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer").medValue("1234567890"));

        OpplysningerOmMor transformResultat = new OpplysningerOmMorTilXml().apply(soknad);

        assertThat(transformResultat).isNotNull();
        assertThat(transformResultat.getFornavn()).isEqualTo("Testmor");
        assertThat(transformResultat.getEtternavn()).isEqualTo("Testesen");
        assertThat(transformResultat.getPersonidentifikator()).isNull();
        assertThat(transformResultat.getKanIkkeOppgiMor().getAarsak()).isEqualTo("utenlandsk");
        assertThat(transformResultat.getKanIkkeOppgiMor().getUtenlandskfnr()).isEqualTo("1234567890");
        assertThat(transformResultat.getKanIkkeOppgiMor().getUtenlandskfnrLand().getKode()).isEqualTo("ARG");
    }

    @Test
    public void morErUkjent() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("infomor.opplysninger.kanIkkeOppgi").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("infomor.opplysninger.kanIkkeOppgi.true.arsak").medValue("ukjent"));
        soknad.getFakta().add(new Faktum().medKey("infomor.opplysninger.kanIkkeOppgi.true.arsak.ukjent.begrunnelse").medValue("begrunnelse"));

        OpplysningerOmMor transformResultat = new OpplysningerOmMorTilXml().apply(soknad);

        assertThat(transformResultat).isNotNull();
        assertThat(transformResultat.getPersonidentifikator()).isNull();
        assertThat(transformResultat.getFornavn()).isNull();
        assertThat(transformResultat.getEtternavn()).isNull();
        assertThat(transformResultat.getKanIkkeOppgiMor().getAarsak()).isEqualTo("ukjent");
        assertThat(transformResultat.getKanIkkeOppgiMor().getBegrunnelse()).isEqualTo("begrunnelse");
    }
}
