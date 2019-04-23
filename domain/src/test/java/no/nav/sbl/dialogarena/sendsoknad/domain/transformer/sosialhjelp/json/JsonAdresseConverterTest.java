package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JsonAdresseConverterTest {

    @Test
    public void tilOppholdsadresseUtenNoenFaktum() {
        WebSoknad webSoknad = new WebSoknad();

        JsonAdresse jsonAdresse = JsonAdresseConverter.tilOppholdsadresse(webSoknad);
        assertNull(jsonAdresse);
    }

    @Test
    public void tilOppholdsadresseUtenValgOgUtenFolkeregistrertAdresse() {
        WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("kontakt.system.adresse"));

        JsonAdresse jsonAdresse = JsonAdresseConverter.tilOppholdsadresse(webSoknad);
        assertNull(jsonAdresse);
    }

    @Test
    public void skalReturnereNullHvisAdresseTypeErUstrukturertOgSystemAdresseIkkeErSatt() {
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue("value"))
                .medFaktum(new Faktum().medKey("kontakt.system.adresse").medValue("value").medSystemProperty("type", "ustrukturert"));

        JsonAdresse jsonAdresse = JsonAdresseConverter.tilOppholdsadresse(webSoknad);
        assertNull(jsonAdresse);
    }

    @Test
    public void skalReturnereJsonAdresseHvisAdresseTypeErUstrukturertOgSystemAdresseErSatt() {
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue("value"))
                .medFaktum(new Faktum().medKey("kontakt.system.adresse").medValue("value").medSystemProperty("type", "ustrukturert").medSystemProperty("adresse", "gate,vei"));

        JsonAdresse jsonAdresse = JsonAdresseConverter.tilOppholdsadresse(webSoknad);
        assertNotNull(jsonAdresse);
    }
}