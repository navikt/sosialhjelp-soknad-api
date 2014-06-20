package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import org.joda.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import static org.joda.time.LocalDate.parse;
import static org.joda.time.Years.yearsBetween;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonAlder implements Serializable{

    private LocalDate fodselsdato;
    private LocalDate utslagsFodselsdato;

    public PersonAlder(String dNummerEllerFodselsnummer){
        this.fodselsdato = parse(hentFodselsdatoFraFnr(dNummerEllerFodselsnummer));
        this.utslagsFodselsdato = parse(hentUtslagsFodselsdatoFraFnr(dNummerEllerFodselsnummer)).plusMonths(1);
    }

    public int getAlder() {
        return yearsBetween(fodselsdato, new LocalDate()).getYears();
    }

	public int getUtslagsAlder() {
        return yearsBetween(utslagsFodselsdato, new LocalDate()).getYears();
	}
	
	public Boolean sjekkAlder() {
		return getUtslagsAlder() < 67;
	}

    private String hentUtslagsFodselsdatoFraFnr(String fodselsnummer){
        NavFodselsnummer fnr = new NavFodselsnummer(fodselsnummer);
        return fnr.getBirthYear() + "-" + fnr.getMonth() + "-01";
    }

    private String hentFodselsdatoFraFnr(String fodselsnummer){
        NavFodselsnummer fnr = new NavFodselsnummer(fodselsnummer);
        return fnr.getBirthYear() + "-" + fnr.getMonth() + "-" + fnr.getDayInMonth();
    }

}

