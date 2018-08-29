package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.*;
import no.nav.tjeneste.virksomhet.person.v1.*;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.*;
import static org.slf4j.LoggerFactory.getLogger;


@Component
public class PersonService {

    private static final Logger logger = getLogger(PersonService.class);
    static final String RELASJON_EKTEFELLE = "EKTE";
    static final String RELASJON_REGISTRERT_PARTNER = "REPA";
    static final String RELASJON_BARN = "BARN";

    @Inject
    @Named("personEndpoint")
    private PersonPortType personEndpoint;

    @Inject
    @Named("personSelftestEndpoint")
    private PersonPortType personSelftestEndpoint;

    public no.nav.sbl.dialogarena.sendsoknad.domain.Person hentPerson(String fodselsnummer) {
        HentKjerneinformasjonResponse response = hentKjerneinformasjon(fodselsnummer);
        return response != null ? mapXmlPersonTilPerson(response.getPerson()) : null;
    }

    @Cacheable(value = "personCache", key = "#fodselsnummer")
    public HentKjerneinformasjonResponse hentKjerneinformasjon(String fodselsnummer) {
        HentKjerneinformasjonRequest request = lagXMLRequestKjerneinformasjon(fodselsnummer);
        try {
            return personEndpoint.hentKjerneinformasjon(request);
        } catch (HentKjerneinformasjonPersonIkkeFunnet e) {
            logger.error("Fant ikke bruker i TPS (Person-servicen).", e);
            throw new IkkeFunnetException("fant ikke bruker: " + request.getIdent(), e);
        } catch (HentKjerneinformasjonSikkerhetsbegrensning e) {
            logger.warn("Kunne ikke hente bruker fra TPS (Person-servicen).", e);
            throw new SikkerhetsBegrensningException("Kunne ikke hente bruker på grunn av manglende tilgang", e);
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS (Person-servicen).", e);
            throw new TjenesteUtilgjengeligException("Person", e);
        }
    }

    public List<Barn> hentBarn(String fodselsnummer) {
        try {
            return hentBarnForPerson(hentKjerneinformasjon(fodselsnummer));
        } catch (IkkeFunnetException e) {
            logger.warn("Ikke funnet person i TPS");
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS.", e);
        }
        return new ArrayList<>();
    }

    public Ektefelle hentEktefelle(String fodselsnummer) {
        try {
            return finnEktefelleForPerson(hentKjerneinformasjon(fodselsnummer));
        } catch (IkkeFunnetException e) {
            logger.warn("Ikke funnet person i TPS");
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS.", e);
        }
        return null;
    }
    
    public void ping() {
        personSelftestEndpoint.ping();
    }

    List<Barn> hentBarnForPerson(HentKjerneinformasjonResponse response) {
        if (response == null) {
            return new ArrayList<>();
        }
        return finnBarn(response.getPerson());
    }

    List<Barn> finnBarn(Person xmlperson) {
        List<Barn> result = new ArrayList<>();
        List<Familierelasjon> familierelasjoner = xmlperson.getHarFraRolleI();
        if (familierelasjoner == null || familierelasjoner.isEmpty()) {
            return result;
        }
        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
            if (RELASJON_BARN.equals(familierelasjonType.getValue())) {
                Person xmlBarn = familierelasjon.getTilPerson();
                if (xmlPersonHarDiskresjonskode(xmlBarn)) {
                    result.add(new Barn().withIkkeTilgang(true));
                    break;
                }

                if (xmlBarn.getIdent() != null && xmlBarn.getIdent().getIdent() != null) {
                    HentKjerneinformasjonResponse barnResponse = hentKjerneinformasjon(xmlBarn.getIdent().getIdent());
                    if (barnResponse != null && barnResponse.getPerson() != null) {
                        Person xmlBarnMedMerInfo = barnResponse.getPerson();
                        if (!erMyndig(finnFodselsdato(xmlBarnMedMerInfo)) && !erDoed(xmlBarnMedMerInfo)) {
                            result.add(new Barn()
                                    .withFornavn(finnFornavn(xmlBarnMedMerInfo))
                                    .withMellomnavn(finnMellomnavn(xmlBarnMedMerInfo))
                                    .withEtternavn(finnEtternavn(xmlBarnMedMerInfo))
                                    .withFnr(finnFnr(xmlBarnMedMerInfo))
                                    .withFodselsdato(finnFodselsdato(xmlBarnMedMerInfo))
                                    .withFolkeregistrertsammen(familierelasjon.isHarSammeBosted() != null ? familierelasjon.isHarSammeBosted() : false)
                                    .withUtvandret(personErUtvandret(xmlBarnMedMerInfo))
                                    .withIkkeTilgang(false));
                        }
                    } else {
                        result.add(new Barn());
                    }
                }
            }
        }
        return result;
    }

    Ektefelle finnEktefelleForPerson(HentKjerneinformasjonResponse response) {
        if (response == null) {
            return null;
        }
        Person xmlPerson = response.getPerson();
        List<Familierelasjon> familierelasjoner = xmlPerson.getHarFraRolleI();
        if (familierelasjoner == null || familierelasjoner.isEmpty()) {
            return null;
        }
        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
            if (RELASJON_EKTEFELLE.equals(familierelasjonType.getValue()) || RELASJON_REGISTRERT_PARTNER.equals(familierelasjonType.getValue())) {
                Person xmlEktefelle = familierelasjon.getTilPerson();
                if (xmlPersonHarDiskresjonskode(xmlEktefelle)) {
                    return new Ektefelle()
                            .withIkketilgangtilektefelle(true);
                }
                if (xmlEktefelle.getIdent() != null && xmlEktefelle.getIdent().getIdent() != null) {
                    HentKjerneinformasjonResponse ektefelleResponse = hentKjerneinformasjon(xmlEktefelle.getIdent().getIdent());

                    if (ektefelleResponse != null && ektefelleResponse.getPerson() != null) {
                        Person xmlEktefelleMedMerInfo = ektefelleResponse.getPerson();

                        boolean ektefelleErUtvandret = personErUtvandret(xmlEktefelleMedMerInfo);
                        return new Ektefelle()
                                .withFornavn(finnFornavn(xmlEktefelleMedMerInfo))
                                .withMellomnavn(finnMellomnavn(xmlEktefelleMedMerInfo))
                                .withEtternavn(finnEtternavn(xmlEktefelleMedMerInfo))
                                .withFodselsdato(finnFodselsdato(xmlEktefelleMedMerInfo))
                                .withFnr(finnFnr(xmlEktefelleMedMerInfo))
                                .withFolkeregistrertsammen(ektefelleErUtvandret ? false : familierelasjon.isHarSammeBosted())
                                .withUtvandret(ektefelleErUtvandret)
                                .withIkketilgangtilektefelle(false);
                    }
                }
                return new Ektefelle();
            }
        }
        return null;
    }

    private HentKjerneinformasjonRequest lagXMLRequestKjerneinformasjon(String fodselsnummer) {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(fodselsnummer);
        return request;
    }

}
