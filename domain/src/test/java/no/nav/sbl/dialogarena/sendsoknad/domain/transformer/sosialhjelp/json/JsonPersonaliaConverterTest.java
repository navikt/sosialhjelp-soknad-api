package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import org.junit.Test;

public class JsonPersonaliaConverterTest {

	@Test
	public void skalIkkeSetteOppholdsadresseValgHvisOppholdsadresseErNull() {
		final WebSoknad webSoknad = new WebSoknad()
				.medFaktum(new Faktum().medKey("personalia").medValue("value").medProperty("property","42"))
				.medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue(JsonAdresseValg.FOLKEREGISTRERT.toString()));

		JsonPersonalia jsonPersonalia = JsonPersonaliaConverter.tilPersonalia(webSoknad);

		assertNotNull(jsonPersonalia);
		assertNull(jsonPersonalia.getOppholdsadresse());
	}

	@Test
	public void skalSetteOppholdsadresseValgHvisOppholdsadresseErSatt() {
		final WebSoknad webSoknad = new WebSoknad()
				.medFaktum(new Faktum().medKey("personalia").medValue("value").medProperty("property","42"))
				.medFaktum(new Faktum().medKey("kontakt.system.oppholdsadresse.valg").medValue(JsonAdresseValg.FOLKEREGISTRERT.toString()))
				.medFaktum(new Faktum().medKey("kontakt.system.folkeregistrert.adresse").medValue("value").medSystemProperty("type", "gateadresse"));

		JsonPersonalia jsonPersonalia = JsonPersonaliaConverter.tilPersonalia(webSoknad);

		assertNotNull(jsonPersonalia);
		assertNotNull(jsonPersonalia.getOppholdsadresse());
		assertEquals(JsonAdresseValg.FOLKEREGISTRERT, jsonPersonalia.getOppholdsadresse().getAdresseValg());
	}
}