package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.AdresserOgKontonummer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Gateadresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Matrikkeladresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.PersonData;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.StrukturertAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.mappers.PersonDataMapper;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PersonService2 {
    private static final Logger logger = getLogger(PersonService2.class);

    @Inject
    @Named("personV3Endpoint")
    private PersonV3 personV3;

    @Inject
    private Kodeverk kodeverk;

    @Cacheable("adresserOgKontonummerCache")
    public AdresserOgKontonummer hentAddresserOgKontonummer(String fodselsnummer) {
        try {
            Person person = getPerson(fodselsnummer);
            PersonDataMapper personDataMapper = new PersonDataMapper();
            PersonData personData = personDataMapper.tilPersonData(person);
            return mapResponsTilAdresserOgKontonummer(personData);
        } catch (WebServiceException e) {
            logger.warn("Ingen kontakt med TPS (Brukerprofil_v1).", e);
            throw new TjenesteUtilgjengeligException("TPS:webserviceException", e);
        } catch (HentPersonSikkerhetsbegrensning | HentPersonPersonIkkeFunnet hentPersonSikkerhetsbegrensning) {
            hentPersonSikkerhetsbegrensning.printStackTrace();
        }
        return null;
    }

    private Person getPerson(String fodselsnummer) throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        HentPersonRequest request = new HentPersonRequest().withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(fodselsnummer)));
        HentPersonResponse hentPersonResponse = personV3.hentPerson(request);
        return hentPersonResponse.getPerson();
    }

    private AdresserOgKontonummer mapResponsTilAdresserOgKontonummer(PersonData personData) {
        if (personData == null) {
            return null;
        }

        String diskresjonskode = personData.getDiskresjonskode();
        boolean diskresjonsKodeSatt = diskresjonskode != null && (diskresjonskode.equals("6") || diskresjonskode.equals("7"));

        return new AdresserOgKontonummer()
                .withMidlertidigAdresse(diskresjonsKodeSatt ? null : getAdresse(personData.getMidlertidigAdresseNorge() == null ? null : personData.getMidlertidigAdresseNorge().getStrukturertAdresse()))
                .withFolkeregistrertAdresse(diskresjonsKodeSatt ? null : getAdresse(personData.getBostedsadresse() == null? null:personData.getBostedsadresse().getStrukturertAdresse()))
                .withKontonummer(personData.getKontonummer());
    }

    private Adresse getAdresse(StrukturertAdresse strukturertAdresse) {
        Adresse midlertidigAdresse = new Adresse();
        if (strukturertAdresse instanceof Matrikkeladresse) {
            Matrikkeladresse midlertidigMatrikkeladresse = (Matrikkeladresse) strukturertAdresse;
            Adresse.MatrikkelAdresse matrikkelAdresse = new Adresse.MatrikkelAdresse();
            matrikkelAdresse.eiendomsnavn = midlertidigMatrikkeladresse.getEiendomsnavn();
            matrikkelAdresse.gaardsnummer = midlertidigMatrikkeladresse.getGardsnummer();
            matrikkelAdresse.bruksnummer = midlertidigMatrikkeladresse.getBruksnummer();
            matrikkelAdresse.festenummer = midlertidigMatrikkeladresse.getFestenummer();
            matrikkelAdresse.seksjonsnummer = midlertidigMatrikkeladresse.getSeksjonsnummer();
            matrikkelAdresse.undernummer = midlertidigMatrikkeladresse.getUndernummer();
            matrikkelAdresse.type = "Matrikkel";
            matrikkelAdresse.kommunenummer = midlertidigMatrikkeladresse.getKommunenummer();
            matrikkelAdresse.postnummer = midlertidigMatrikkeladresse.getPostnummer();
            matrikkelAdresse.poststed = midlertidigMatrikkeladresse.getPoststed();
            midlertidigAdresse.setStrukturertAdresse(matrikkelAdresse);
        }
        if (strukturertAdresse instanceof Gateadresse) {
            Gateadresse midlertidigGateadresse = (Gateadresse) strukturertAdresse;
            Adresse.Gateadresse gateadresse = new Adresse.Gateadresse();
            gateadresse.type = "Gateadresse";
            gateadresse.gatenavn = midlertidigGateadresse.getGatenavn();
            gateadresse.husnummer = String.valueOf(midlertidigGateadresse.getHusnummer());
            gateadresse.husbokstav = midlertidigGateadresse.getHusbokstav();
            gateadresse.bolignummer = midlertidigGateadresse.getBolignummer();
            gateadresse.kommunenummer = midlertidigGateadresse.getKommunenummer();
            gateadresse.postnummer = midlertidigGateadresse.getPostnummer();
            gateadresse.poststed = midlertidigGateadresse.getPoststed();
            midlertidigAdresse.setStrukturertAdresse(gateadresse);
        }
        return midlertidigAdresse;
    }

}
