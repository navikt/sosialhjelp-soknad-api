package no.nav.sbl.dialogarena.dokumentinnsending.transform;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Person;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskAdresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

import java.util.List;

/**
 * Map from TPS data format to internal domain model
 *
 */
public class PersonTransform {

	public Person mapToPerson(XMLHentKontaktinformasjonOgPreferanserResponse response) {
        XMLBruker soapPerson = (XMLBruker) response.getPerson();
        return Person.identifisert(finnEpost(soapPerson), finnGjeldendePostadresseType(soapPerson));
    }

    private String finnGjeldendePostadresseType(XMLBruker soapPerson) {
		XMLPostadressetyper postAdresseType = soapPerson.getGjeldendePostadresseType();
		return postAdresseType.getValue();
	}

	private String finnEpost(XMLBruker soapPerson) {
        String epost = null;
        List<XMLElektroniskKommunikasjonskanal> elkanaler = soapPerson
                .getElektroniskKommunikasjonskanal();
        for (XMLElektroniskKommunikasjonskanal kanal : elkanaler) {
            XMLElektroniskAdresse adr = kanal.getElektroniskAdresse();
            if (adr instanceof XMLEPost) {
                epost = ((XMLEPost) adr).getIdentifikator();
            }
        }
        return epost;
    }
}
