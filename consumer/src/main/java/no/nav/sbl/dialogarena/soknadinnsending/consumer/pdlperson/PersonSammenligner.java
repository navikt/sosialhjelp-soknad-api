package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson;

import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PersonSammenligner {

    private static final Logger log = getLogger(PersonSammenligner.class);

    public void sammenlign(Person person, Person pdlPerson) {
        List<String> ulikeFelter = new ArrayList<>();
        if (!person.getFornavn().equalsIgnoreCase(pdlPerson.getFornavn())){
            ulikeFelter.add("Fornavn");
        }
        if (!person.getMellomnavn().equalsIgnoreCase(pdlPerson.getMellomnavn())){
            ulikeFelter.add("Mellomnavn");
        }
        if (!person.getEtternavn().equalsIgnoreCase(pdlPerson.getEtternavn())){
            ulikeFelter.add("Ettenavn");
        }
        if (person.getFnr() != null && !person.getFnr().equalsIgnoreCase(pdlPerson.getFnr())) {
            ulikeFelter.add("Fnr");
        }
        if (!person.getAlder().equalsIgnoreCase(pdlPerson.getAlder())){
            ulikeFelter.add("Alder");
        }
        if (person.getSivilstatus() != null && !person.getSivilstatus().equalsIgnoreCase(pdlPerson.getSivilstatus())){
            ulikeFelter.add("Sivilstatus");
        }
        if (person.getDiskresjonskode() != null && pdlPerson.getDiskresjonskode() != null && !person.getDiskresjonskode().equalsIgnoreCase(pdlPerson.getDiskresjonskode())) {
            ulikeFelter.add("Diskresjonskode");
        }
        if (!person.getStatsborgerskap().equalsIgnoreCase(pdlPerson.getStatsborgerskap())){
            ulikeFelter.add("Statsborgerskap");
        }
        // ektefelle:
//        if (person.getEktefelle() != null && pdlPerson.getEktefelle() != null && !person.getEktefelle().getFnr().equalsIgnoreCase(pdlPerson.getEktefelle().getFnr())){
//            ulikeFelter.add("Ektefelle.fnr");
//        }

        if (ulikeFelter.size() > 0) {
            log.info("Ulike felter i response fra Person_V1 vs PDL: {}", String.join(",", ulikeFelter));
        }
    }
}
