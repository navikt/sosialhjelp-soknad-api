package no.nav.sbl.dialogarena.websoknad.domain;

import no.bekk.bekkopen.person.Fodselsnummer;
import no.bekk.bekkopen.person.FodselsnummerValidator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Alder implements Serializable{

	private int alder;
    public Alder(String personnummer){
        this.alder = hentAlderFraFnr(personnummer);
    }
	public int getAlder() {
		return alder;
	}

    private int hentAlderFraFnr(String personnummer){
        Fodselsnummer fnr =  FodselsnummerValidator.getFodselsnummer(personnummer);
        return Integer.parseInt(fnr.get2DigitBirthYear());
    }
}

