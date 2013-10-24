package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import no.bekk.bekkopen.person.Fodselsnummer;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import org.joda.time.LocalDate;
import org.joda.time.Years;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonAlder implements Serializable{

    private LocalDate fodselsdato;

    public PersonAlder(String dNummerEllerPersonnummer){
        this.fodselsdato = LocalDate.parse(hentFodselsdatoFraFnr(dNummerEllerPersonnummer)).plusMonths(1);
    }
	public int getAlder() {
        return Years.yearsBetween(fodselsdato, new LocalDate()).getYears();
	}
	
	public boolean sjekkAlder() {
		return getAlder() < 67;
	}

    private String hentFodselsdatoFraFnr(String personnummer){
        Fodselsnummer fnr =  FodselsnummerValidator.getFodselsnummer(personnummer);
        String maned = fnr.getMonth();
        String aar = fnr.getBirthYear();

        return aar+"-"+maned+"-01";
    }
}

