package no.nav.sbl.dialogarena.dokumentinnsending.builder;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.AdresseType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

public class PersonFactory {
	
	public static XMLHentKontaktinformasjonOgPreferanserResponse lagPersonMedMidlertidigUtlandsAdresse(String ident) {
		return lagPerson(AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND.name(), ident);
	}

	public static XMLHentKontaktinformasjonOgPreferanserResponse lagPersonMedNorskMidlertidigAdresse(String ident) {
		return lagPerson(AdresseType.MIDLERTIDIG_POSTADRESSE_NORGE.name(), ident);
	}
	
	public static XMLHentKontaktinformasjonOgPreferanserResponse lagPersonMedFastUtenlandskAdresse(String ident) {
		return lagPerson(AdresseType.POSTADRESSE_UTLAND.name(), ident);
	}
	
	public static XMLHentKontaktinformasjonOgPreferanserResponse lagPerson(String ident) {
		return lagPerson(AdresseType.UKJENT.name(), ident);
	}
	
	private static XMLHentKontaktinformasjonOgPreferanserResponse lagPerson(String gjeldendeAdresseType, String ident) {
		return new XMLHentKontaktinformasjonOgPreferanserResponse()
			.withPerson(new XMLBruker().withElektroniskKommunikasjonskanal(lagElektroniskKommunikasjonskanal())
				.withGjeldendePostadresseType(new XMLPostadressetyper().withValue(gjeldendeAdresseType))
				.withIdent(lagIdent(ident)));
	}
	
	public static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal() {
		return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse());
	}

	public static XMLElektroniskAdresse lagElektroniskAdresse() {
		return new XMLEPost().withIdentifikator("testmail@test.com");
	}

	
	private static XMLNorskIdent lagIdent(String ident) {
		return new XMLNorskIdent().withIdent(ident);
	}
	
}
