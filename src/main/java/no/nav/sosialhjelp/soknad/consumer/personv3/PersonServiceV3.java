package no.nav.sosialhjelp.soknad.consumer.personv3;

import no.nav.sosialhjelp.soknad.consumer.exceptions.IkkeFunnetException;
import no.nav.sosialhjelp.soknad.consumer.exceptions.SikkerhetsBegrensningException;
import no.nav.sosialhjelp.soknad.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sosialhjelp.soknad.consumer.person.domain.PersonData;
import no.nav.sosialhjelp.soknad.consumer.person.mappers.PersonDataMapper;
import no.nav.sosialhjelp.soknad.domain.model.Kontonummer;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
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
public class PersonServiceV3 {
    private static final Logger logger = getLogger(PersonServiceV3.class);

    @Inject
    @Named("personV3Endpoint")
    private PersonV3 personV3;

    @Cacheable("kontonummerCache")
    public Kontonummer hentKontonummer(String fodselsnummer) {
        try {
            Person person = getPerson(fodselsnummer);
            if (person == null) {
                logger.warn("Person er null");
                return new Kontonummer();
            }
            PersonDataMapper personDataMapper = new PersonDataMapper();
            PersonData personData = personDataMapper.tilPersonData(person);
            return mapResponsTilKontonummer(personData);
        } catch (WebServiceException e) {
            logger.warn("Ingen kontakt med TPS (Person_V3).", e);
            throw new TjenesteUtilgjengeligException("TPS:webserviceException", e);
        } catch (HentPersonSikkerhetsbegrensning e) {
            throw new SikkerhetsBegrensningException(e.getMessage(), e);
        } catch (HentPersonPersonIkkeFunnet e) {
            throw new IkkeFunnetException(e.getMessage(), e);
        }
    }

    private Person getPerson(String fodselsnummer) throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {

        Personidenter personidenter = new Personidenter();
        personidenter.setValue("FNR");
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(fodselsnummer);
        norskIdent.setType(personidenter);

        HentPersonRequest request = new HentPersonRequest().withAktoer(new PersonIdent().withIdent(
                norskIdent)).withInformasjonsbehov(Informasjonsbehov.ADRESSE, Informasjonsbehov.BANKKONTO);
        HentPersonResponse hentPersonResponse = personV3.hentPerson(request);

        return hentPersonResponse.getPerson();
    }

    private Kontonummer mapResponsTilKontonummer(PersonData personData) {
        if (personData == null) {
            return null;
        }

        return new Kontonummer()
                .withKontonummer(personData.getKontonummer());
    }

}
