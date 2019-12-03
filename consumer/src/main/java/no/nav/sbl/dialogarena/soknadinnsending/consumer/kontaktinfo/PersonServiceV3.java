package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.AdresserOgKontonummer;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.SikkerhetsBegrensningException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Gateadresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Matrikkeladresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.PersonData;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.StrukturertAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.mappers.PersonDataMapper;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.finnBarnForPerson;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PersonServiceV3 {
    private static final Logger logger = getLogger(PersonServiceV3.class);

    @Inject
    @Named("personV3Endpoint")
    private PersonV3 personV3Endpoint;

    @Inject
    @Named("personV3SelftestEndpoint")
    private PersonV3 personSelftestEndpoint;

    @Inject
    private Kodeverk kodeverk;

    @Cacheable("adresserOgKontonummerCache")
    public AdresserOgKontonummer hentAddresserOgKontonummer(String fodselsnummer) {
        try {
            Person person = getPerson(fodselsnummer);
            if (person == null) {
                logger.warn("Person er null");
                return new AdresserOgKontonummer();
            }
            PersonDataMapper personDataMapper = new PersonDataMapper();
            PersonData personData = personDataMapper.tilPersonData(person);
            return mapResponsTilAdresserOgKontonummer(personData);
        } catch (WebServiceException e) {
            logger.warn("Ingen kontakt med TPS (Person_V3).", e);
            throw new TjenesteUtilgjengeligException("TPS:webserviceException", e);
        } catch (HentPersonSikkerhetsbegrensning e) {
            throw new SikkerhetsBegrensningException(e.getMessage(), e);
        } catch (HentPersonPersonIkkeFunnet e) {
            throw new IkkeFunnetException(e.getMessage(), e);
        }
    }


    public Person getPerson(String fodselsnummer) throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {

        Personidenter personidenter = new Personidenter();
        personidenter.setValue("FNR");
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(fodselsnummer);
        norskIdent.setType(personidenter);

        HentPersonRequest request = new HentPersonRequest().withAktoer(new PersonIdent().withIdent(
                norskIdent)).withInformasjonsbehov(Informasjonsbehov.ADRESSE, Informasjonsbehov.BANKKONTO, Informasjonsbehov.FAMILIERELASJONER);
        HentPersonResponse hentPersonResponse = personV3Endpoint.hentPerson(request);

        return hentPersonResponse.getPerson();
    }

    public PersonData getPersonData(String fodselsnummer) {
        try {
            Person person = getPerson(fodselsnummer);
            if (person == null) {
                logger.warn("Person er null");
                return null;
            }
            PersonDataMapper personDataMapper = new PersonDataMapper();
            PersonData personData = personDataMapper.tilPersonData(person);
            return personData;
        } catch (WebServiceException e) {
            logger.warn("Ingen kontakt med TPS (Person_V3).", e);
            throw new TjenesteUtilgjengeligException("TPS:webserviceException", e);
        } catch (HentPersonSikkerhetsbegrensning e) {
            throw new SikkerhetsBegrensningException(e.getMessage(), e);
        } catch (HentPersonPersonIkkeFunnet e) {
            throw new IkkeFunnetException(e.getMessage(), e);
        }
    }


    private AdresserOgKontonummer mapResponsTilAdresserOgKontonummer(PersonData personData) {
        if (personData == null) {
            return null;
        }

        String diskresjonskode = personData.getDiskresjonskode();

        boolean diskresjonsKodeSatt = diskresjonskode != null && (diskresjonskode.equals("6") || diskresjonskode.equals("7"));

        return new AdresserOgKontonummer()
                .withMidlertidigAdresse(diskresjonsKodeSatt ? null : getAdresse(personData.getMidlertidigAdresseNorge() == null ? null : personData.getMidlertidigAdresseNorge().getStrukturertAdresse()))
                .withFolkeregistrertAdresse(diskresjonsKodeSatt ? null : getAdresse(personData.getBostedsadresse() == null ? null : personData.getBostedsadresse().getStrukturertAdresse()))
                .withKontonummer(personData.getKontonummer());
    }

    private Adresse getAdresse(StrukturertAdresse strukturertAdresse) {
        Adresse adresse = new Adresse();
        if (strukturertAdresse instanceof Matrikkeladresse) {
            Matrikkeladresse matrikkeladresseStrukturert = (Matrikkeladresse) strukturertAdresse;
            Adresse.MatrikkelAdresse matrikkelAdresse = new Adresse.MatrikkelAdresse();
            matrikkelAdresse.eiendomsnavn = matrikkeladresseStrukturert.getEiendomsnavn();
            matrikkelAdresse.gaardsnummer = matrikkeladresseStrukturert.getGardsnummer();
            matrikkelAdresse.bruksnummer = matrikkeladresseStrukturert.getBruksnummer();
            matrikkelAdresse.festenummer = matrikkeladresseStrukturert.getFestenummer();
            matrikkelAdresse.seksjonsnummer = matrikkeladresseStrukturert.getSeksjonsnummer();
            matrikkelAdresse.undernummer = matrikkeladresseStrukturert.getUndernummer();
            matrikkelAdresse.type = "matrikkel";
            matrikkelAdresse.kommunenummer = matrikkeladresseStrukturert.getKommunenummer();
            matrikkelAdresse.postnummer = matrikkeladresseStrukturert.getPostnummer();
            matrikkelAdresse.poststed =  kodeverk.getPoststed( matrikkelAdresse.postnummer);
            adresse.setStrukturertAdresse(matrikkelAdresse);
            adresse.setAdressetype("matrikkel");
        }

        if (strukturertAdresse instanceof Gateadresse) {
            Gateadresse gateAdresseStrukturert = (Gateadresse) strukturertAdresse;
            Adresse.Gateadresse gateadresse = new Adresse.Gateadresse();
            gateadresse.type = "gateadresse";
            gateadresse.gatenavn = gateAdresseStrukturert.getGatenavn();
            gateadresse.husnummer = String.valueOf(gateAdresseStrukturert.getHusnummer());
            gateadresse.husbokstav = gateAdresseStrukturert.getHusbokstav();
            gateadresse.bolignummer = gateAdresseStrukturert.getBolignummer();
            gateadresse.kommunenummer = gateAdresseStrukturert.getKommunenummer();
            gateadresse.postnummer = gateAdresseStrukturert.getPostnummer();
            gateadresse.poststed = kodeverk.getPoststed( gateadresse.postnummer);
            adresse.setStrukturertAdresse(gateadresse);
            adresse.setAdressetype("gateadresse");
        }

        return adresse;
    }

    public List<Barn> hentBarn(String fodselsnummer) {
        no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person;
        try {
            person = getPerson(fodselsnummer);
        } catch (HentPersonPersonIkkeFunnet | HentPersonSikkerhetsbegrensning hentPersonPersonIkkeFunnet) {
            return new ArrayList<>();
        }
        return finnBarnForPerson(person);
    }

    public void ping() {
        personSelftestEndpoint.ping();
    }
}
