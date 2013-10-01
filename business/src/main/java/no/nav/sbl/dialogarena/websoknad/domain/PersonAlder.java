package no.nav.sbl.dialogarena.websoknad.domain;

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

    public PersonAlder(String personnummer){
        this.fodselsdato = LocalDate.parse(hentFodselsdatoFraFnr(personnummer));
    }
	public int getAlder() {
        return Years.yearsBetween(fodselsdato, new LocalDate()).getYears();
	}

    private String hentFodselsdatoFraFnr(String personnummer){
        Fodselsnummer fnr =  FodselsnummerValidator.getFodselsnummer(personnummer);
        String dag = fnr.getDayInMonth();
        String maned = fnr.getMonth();
        String aar = fnr.getBirthYear();

        return aar+"-"+maned +"-"+dag;
    }
}

