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
        if (!person.getFornavn().equals(pdlPerson.getFornavn())) {
            ulikeFelter.add("Fornavn");
        }
        if (!person.getMellomnavn().equals(pdlPerson.getMellomnavn())) {
            ulikeFelter.add("Mellomnavn");
        }
        if (!person.getEtternavn().equals(pdlPerson.getEtternavn())) {
            ulikeFelter.add("Etternavn");
        }
        if (person.getFnr() != null && !person.getFnr().equals(pdlPerson.getFnr())) {
            ulikeFelter.add("Fnr");
        }
        if (person.getSivilstatus() != null && !person.getSivilstatus().equalsIgnoreCase(pdlPerson.getSivilstatus())) {
//            if (!person.getSivilstatus().equals("samboer")) {
                // sivilstand samboer er ikke videreført i PDL
                ulikeFelter.add(String.format("Sivilstatus [%s] vs [%s]", person.getSivilstatus(), pdlPerson.getSivilstatus()));
//            }
        }
        if (person.getDiskresjonskode() != null && pdlPerson.getDiskresjonskode() != null && !person.getDiskresjonskode().equals(pdlPerson.getDiskresjonskode())) {
            ulikeFelter.add("Diskresjonskode");
        }
        if (!person.getStatsborgerskap().equals(pdlPerson.getStatsborgerskap())) {
            ulikeFelter.add(String.format("Statsborgerskap [%s] vs [%s]", person.getStatsborgerskap(), pdlPerson.getStatsborgerskap()));
        }
        if (ulikeFelter.size() > 0) {
            log.info("Ulike felter i Person fra Person_V1 vs PDL: {}", String.join(",", ulikeFelter));
        } else {
            log.info("Person fra Person_v1 og PDL er like");
        }

        sammenlignEktefelle(person.getEktefelle(), pdlPerson.getEktefelle());
    }

    private void sammenlignEktefelle(Ektefelle ektefelle, Ektefelle pdlEktefelle) {
        if (ektefelle == null && pdlEktefelle != null) {
            log.info("Ektefelle er null i TPS, men ikke i PDL");
        } else if (ektefelle != null && pdlEktefelle == null) {
            log.info("Ektefelle er null i PDL, men ikke i TPS");
        } else if (ektefelle != null && pdlEktefelle != null) {
            if (ektefelle.harIkketilgangtilektefelle() && pdlEktefelle.harIkketilgangtilektefelle()) {
                return;
            }
            List<String> ulikeFelter = new ArrayList<>();
            if (ektefelle.harIkketilgangtilektefelle() != pdlEktefelle.harIkketilgangtilektefelle()) {
                log.info("Ulik Ektefelle.adressebeskyttelse i Person_v1 og PDL");
                return;
            }
            if (!ektefelle.getFornavn().equals(pdlEktefelle.getFornavn())) {
                ulikeFelter.add("Fornavn");
            }
            if (!ektefelle.getMellomnavn().equals(pdlEktefelle.getMellomnavn())) {
                ulikeFelter.add("Mellomnavn");
            }
            if (!ektefelle.getEtternavn().equals(pdlEktefelle.getEtternavn())) {
                ulikeFelter.add("Etternavn");
            }
            if (ektefelle.getFodselsdato() != null && !ektefelle.getFodselsdato().isEqual(pdlEktefelle.getFodselsdato())) {
                ulikeFelter.add("Fodselsdato");
            }
            if (ektefelle.getFnr() != null && !ektefelle.getFnr().equals(pdlEktefelle.getFnr())) {
                ulikeFelter.add("Fnr");
            }
            if (ektefelle.erFolkeregistrertsammen() != pdlEktefelle.erFolkeregistrertsammen()) {
                ulikeFelter.add("ErFolkeregistrertSammen");
            }
            if (ulikeFelter.size() > 0) {
                log.info("Ulike felter i Ektefelle fra Person_V1 vs PDL: {}", String.join(",", ulikeFelter));
            } else {
                log.info("Ektefelle fra Person_v1 og PDL er like");
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
            if (!barn.getFornavn().equals(pdlBarn.getFornavn())) {
                ulikeFelter.add("Fornavn");
            }
            if (!barn.getMellomnavn().equals(pdlBarn.getMellomnavn())) {
                ulikeFelter.add("Mellomnavn");
            }
            if (!barn.getEtternavn().equals(pdlBarn.getEtternavn())) {
                ulikeFelter.add("Etternavn");
            }
            if (barn.getFodselsdato() != null && !barn.getFodselsdato().isEqual(pdlBarn.getFodselsdato())) {
                ulikeFelter.add("Fodselsdato");
            }
            if (barn.getFnr() != null && !barn.getFnr().equals(pdlBarn.getFnr())) {
                ulikeFelter.add("Fnr");
            }
            if (barn.erFolkeregistrertsammen() != pdlBarn.erFolkeregistrertsammen()) {
                ulikeFelter.add("ErFolkeregistrertSammen");
            }
            if (ulikeFelter.size() > 0) {
                log.info("Ulike felter i Barn fra Person_V1 og PDL: {}", String.join(",", ulikeFelter));
            } else {
                log.info("Barn fra Person_v1 og Barn fra PDL er like");
            }
        }
    }
}
