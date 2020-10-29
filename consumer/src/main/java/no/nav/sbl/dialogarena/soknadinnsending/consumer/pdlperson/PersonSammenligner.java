package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
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
        if (!person.getFornavn().equalsIgnoreCase(pdlPerson.getFornavn())) {
            ulikeFelter.add("Fornavn");
        }
        if (!person.getMellomnavn().equalsIgnoreCase(pdlPerson.getMellomnavn())) {
            ulikeFelter.add("Mellomnavn");
        }
        if (!person.getEtternavn().equalsIgnoreCase(pdlPerson.getEtternavn())) {
            ulikeFelter.add("Ettenavn");
        }
        if (person.getFnr() != null && !person.getFnr().equalsIgnoreCase(pdlPerson.getFnr())) {
            ulikeFelter.add("Fnr");
        }
        if (person.getSivilstatus() != null && !person.getSivilstatus().equalsIgnoreCase(pdlPerson.getSivilstatus())) {
            ulikeFelter.add("Sivilstatus");
        }
        if (person.getDiskresjonskode() != null && pdlPerson.getDiskresjonskode() != null && !person.getDiskresjonskode().equalsIgnoreCase(pdlPerson.getDiskresjonskode())) {
            ulikeFelter.add("Diskresjonskode");
        }
        if (!person.getStatsborgerskap().equalsIgnoreCase(pdlPerson.getStatsborgerskap())) {
            ulikeFelter.add("Statsborgerskap");
        }
        if (ulikeFelter.size() > 0) {
            log.info("Ulike felter i Person fra Person_V1 vs PDL: {}", String.join(",", ulikeFelter));
        }

        sammenlignEktefelle(person.getEktefelle(), pdlPerson.getEktefelle());
    }

    private void sammenlignEktefelle(Ektefelle ektefelle, Ektefelle pdlEktefelle) {
        if (ektefelle != null && pdlEktefelle != null) {
            List<String> ulikeFelter = new ArrayList<>();
            if (ektefelle.harIkketilgangtilektefelle() != pdlEktefelle.harIkketilgangtilektefelle()) {
                log.info("Ulik Ektefelle.adressebeskyttelse i Person_v1 og PDL");
                return;
            }
            if (!ektefelle.getFornavn().equalsIgnoreCase(pdlEktefelle.getFornavn())) {
                ulikeFelter.add("Fornavn");
            }
            if (!ektefelle.getMellomnavn().equalsIgnoreCase(pdlEktefelle.getMellomnavn())) {
                ulikeFelter.add("Mellomnavn");
            }
            if (!ektefelle.getEtternavn().equalsIgnoreCase(pdlEktefelle.getEtternavn())) {
                ulikeFelter.add("Ettenavn");
            }
            if (ektefelle.getFodselsdato() != null && !ektefelle.getFodselsdato().isEqual(pdlEktefelle.getFodselsdato())) {
                ulikeFelter.add("Fodselsdato");
            }
            if (ektefelle.getFnr() != null && !ektefelle.getFnr().equalsIgnoreCase(pdlEktefelle.getFnr())) {
                ulikeFelter.add("Fnr");
            }
            // todo: folkeregistrertsammen
            if (ulikeFelter.size() > 0) {
                log.info("Ulike felter i Ektefelle fra Person_V1 vs PDL: {}", String.join(",", ulikeFelter));
            }
        }
    }

    public void sammenlignBarn(List<Barn> alleBarn, List<Barn> allePdlBarn) {
        if (alleBarn.size() != allePdlBarn.size()) {
            log.info("Ulikt antall Barn i Person_v1 og PDL");
        }
        for (Barn barn : alleBarn) {
            allePdlBarn.stream()
                    .filter(pdlBarn -> pdlBarn.getFnr().equals(barn.getFnr()))
                    .forEach(pdlBarn -> sammenlignBarn(barn, pdlBarn));
        }
    }

    private void sammenlignBarn(Barn barn, Barn pdlBarn) {
        if (barn != null && pdlBarn != null) {
            // barn har ikke adressebeskyttelse, er myndig og lever
            List<String> ulikeFelter = new ArrayList<>();
            if (!barn.getFornavn().equalsIgnoreCase(pdlBarn.getFornavn())) {
                ulikeFelter.add("Fornavn");
            }
            if (!barn.getMellomnavn().equalsIgnoreCase(pdlBarn.getMellomnavn())) {
                ulikeFelter.add("Mellomnavn");
            }
            if (!barn.getEtternavn().equalsIgnoreCase(pdlBarn.getEtternavn())) {
                ulikeFelter.add("Ettenavn");
            }
            if (barn.getFodselsdato() != null && !barn.getFodselsdato().isEqual(pdlBarn.getFodselsdato())) {
                ulikeFelter.add("Fodselsdato");
            }
            if (barn.getFnr() != null && !barn.getFnr().equalsIgnoreCase(pdlBarn.getFnr())) {
                ulikeFelter.add("Fnr");
            }
            // todo: folkeregistrertsammen
            if (ulikeFelter.size() > 0) {
                log.info("Ulike felter i Barn fra Person_V1 og PDL: {}", String.join(",", ulikeFelter));
            }
        }
    }
}
