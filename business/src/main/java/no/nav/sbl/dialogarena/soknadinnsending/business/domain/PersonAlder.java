package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import no.bekk.bekkopen.person.Fodselsnummer;
import org.joda.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import static no.bekk.bekkopen.person.FodselsnummerValidator.getFodselsnummer;
import static org.joda.time.LocalDate.parse;
import static org.joda.time.Years.yearsBetween;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonAlder implements Serializable{

    private LocalDate fodselsdato;

    public PersonAlder(String dNummerEllerFodselsnummer){
        this.fodselsdato = parse(hentFodselsdatoFraFnr(dNummerEllerFodselsnummer)).plusMonths(1);
    }
	public int getAlder() {
        return yearsBetween(fodselsdato, new LocalDate()).getYears();
	}
	
	public Boolean sjekkAlder() {
		return getAlder() < 67;
	}

    private String hentFodselsdatoFraFnr(String fodselsnummer){
        Fodselsnummer fnr = getFodselsnummer(fodselsnummer);
        return fnr.getBirthYear() + "-" + fnr.getMonth() + "-01";
    }

}

