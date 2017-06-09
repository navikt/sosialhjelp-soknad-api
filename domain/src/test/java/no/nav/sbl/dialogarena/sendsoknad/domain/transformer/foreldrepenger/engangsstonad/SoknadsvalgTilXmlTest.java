package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.FoedselEllerAdopsjon;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Soknadsvalg;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Stoenadstype;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SoknadsvalgTilXmlTest {

    @Test
    public void farOgAdopsjon() {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.getFakta().add(new Faktum().medKey("soknadsvalg.stonadstype").medValue("engangsstonadFar"));
        webSoknad.getFakta().add(new Faktum().medKey("soknadsvalg.fodselelleradopsjon").medValue("adopsjon"));

        Soknadsvalg transformResultat = new SoknadsvalgTilXml().apply(webSoknad);

        assertThat(transformResultat).isNotNull();
        assertThat(transformResultat.getStoenadstype()).isEqualTo(Stoenadstype.ENGANGSSTOENADFAR);
        assertThat(transformResultat.getFoedselEllerAdopsjon()).isEqualTo(FoedselEllerAdopsjon.ADOPSJON);
    }

    @Test
    public void morOgAdopsjon() {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.getFakta().add(new Faktum().medKey("soknadsvalg.stonadstype").medValue("engangsstonadMor"));
        webSoknad.getFakta().add(new Faktum().medKey("soknadsvalg.fodselelleradopsjon").medValue("adopsjon"));

        Soknadsvalg transformResultat = new SoknadsvalgTilXml().apply(webSoknad);

        assertThat(transformResultat).isNotNull();
        assertThat(transformResultat.getStoenadstype()).isEqualTo(Stoenadstype.ENGANGSSTOENADMOR);
        assertThat(transformResultat.getFoedselEllerAdopsjon()).isEqualTo(FoedselEllerAdopsjon.ADOPSJON);
    }

    @Test
    public void farOgFodsel() {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.getFakta().add(new Faktum().medKey("soknadsvalg.stonadstype").medValue("engangsstonadFar"));
        webSoknad.getFakta().add(new Faktum().medKey("soknadsvalg.fodselelleradopsjon").medValue("fodsel"));

        Soknadsvalg transformResultat = new SoknadsvalgTilXml().apply(webSoknad);

        assertThat(transformResultat).isNotNull();
        assertThat(transformResultat.getStoenadstype()).isEqualTo(Stoenadstype.ENGANGSSTOENADFAR);
        assertThat(transformResultat.getFoedselEllerAdopsjon()).isEqualTo(FoedselEllerAdopsjon.FOEDSEL);
    }

    @Test
    public void morOgFodsel() {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.getFakta().add(new Faktum().medKey("soknadsvalg.stonadstype").medValue("engangsstonadMor"));
        webSoknad.getFakta().add(new Faktum().medKey("soknadsvalg.fodselelleradopsjon").medValue("fodsel"));

        Soknadsvalg transformResultat = new SoknadsvalgTilXml().apply(webSoknad);

        assertThat(transformResultat).isNotNull();
        assertThat(transformResultat.getStoenadstype()).isEqualTo(Stoenadstype.ENGANGSSTOENADMOR);
        assertThat(transformResultat.getFoedselEllerAdopsjon()).isEqualTo(FoedselEllerAdopsjon.FOEDSEL);
    }
}
