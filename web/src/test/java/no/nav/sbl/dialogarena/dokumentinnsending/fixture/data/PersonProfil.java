package no.nav.sbl.dialogarena.dokumentinnsending.fixture.data;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.AdresseType;

public class PersonProfil {

    public String ident;
    public String navn;
    public String adresse;
    public String landKode;
    public String kommentar;
    public AdresseType adresseType;
    public String epost;

    public static PersonProfil lagPersonBosattINorgeUtenEpost(String foedselsnr) {
        PersonProfil person = new PersonProfil();
        person.ident = foedselsnr;
        person.navn = "Ola Nordmann";
        person.adresse = "Testveien 1";
        person.landKode = "NO";
        person.adresseType = AdresseType.BOSTEDSADRESSE;
        return person;
    }

    public static PersonProfil lagPersonBosattINorgeMedEpost(String foedselsnr) {
        PersonProfil person = new PersonProfil();
        person.ident = foedselsnr;
        person.navn = "Ola Nordmann";
        person.adresse = "Testveien 1";
        person.landKode = "NO";
        person.adresseType = AdresseType.BOSTEDSADRESSE;
        person.epost = "ola.nordmann@nav.no";
        return person;
    }

	public static PersonProfil lagPersonMedMidlertidigAdresseUtland(String ident) {
		PersonProfil person = lagPerson(ident);
        person.adresseType = AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND;
        return person;
	}

	public static PersonProfil lagPersonMedPostadresseUtland(String ident) {
		PersonProfil person = lagPerson(ident);
        person.adresseType = AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND;
        return person;
	}

	private static PersonProfil lagPerson(String ident) {
		PersonProfil person = new PersonProfil();
        person.ident = ident;
        person.epost = "uwe@testmail.com";
		return person;
	}

	@Override
	public String toString() {
		return "PersonProfil [ident=" + ident + ", navn=" + navn + ", adresse="
				+ adresse + ", landKode=" + landKode + ", kommentar="
				+ kommentar + ", adresseType=" + adresseType + ", epost="
				+ epost + "]";
	}
}