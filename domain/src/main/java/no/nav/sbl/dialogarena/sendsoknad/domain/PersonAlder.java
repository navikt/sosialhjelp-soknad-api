package no.nav.sbl.dialogarena.sendsoknad.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;

import static java.time.LocalDate.parse;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonAlder implements Serializable{

    private LocalDate fodselsdato;

    public PersonAlder(String dNummerEllerFodselsnummer){
        this.fodselsdato = parse(hentFodselsdatoFraFnr(dNummerEllerFodselsnummer));
    }

    public int getAlder() {
        return Period.between(fodselsdato, LocalDate.now()).getYears();
    }

    private String hentFodselsdatoFraFnr(String fodselsnummer){
        NavFodselsnummer fnr = new NavFodselsnummer(fodselsnummer);
        return fnr.getBirthYear() + "-" + fnr.getMonth() + "-" + fnr.getDayInMonth();
    }

}

