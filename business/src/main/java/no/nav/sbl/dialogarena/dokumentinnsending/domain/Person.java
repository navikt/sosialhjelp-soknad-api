package no.nav.sbl.dialogarena.dokumentinnsending.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Personen som er logget inn i DokumentInnsending.
 * Foreløpig inneholder klassen kontaktinformasjon for personen. Data hentes fra TPS.
 * <p/>
 * En utenlands statsborger håndteres litt annerledes i applikasjonen enn personer bosatt i Norge.
 * Person er som er bosatt i Norge må svare på noen spørsmål som avklarer den videre behandling av henvendelsen.
 * Se MOD-3216 i Confluence for nærmere beskrivelse av regler.
 *
 * @author j139113
 */
public class Person {

	private static final Logger logger = LoggerFactory.getLogger(Person.class);
	
	private String epost;
    private AdresseType gjeldendePostadresseType;
    
    private Person(String epost) {
    	this(epost, AdresseType.UKJENT);
    }
    
    private Person() {
    	this(null);
    }

    public Person(String epost, AdresseType type) {
		this.epost = epost;
		this.gjeldendePostadresseType = type;
	}

	public static Person identifisert(String epost, String gjeldendePostadresseType) {
    	Person person = null;
    	try{
    		person = new Person(epost,AdresseType.valueOf(gjeldendePostadresseType));
    	} catch (IllegalArgumentException iae) {
    		logger.error("Mottok en verdi for gjeldendeAdresseType som er ukjent. Verdien fra TPS var:  " + gjeldendePostadresseType);
    		person = new Person(epost, AdresseType.UKJENT_VERDI);
    	}
    	return person;
	}

	public String getEpost() {
        return epost;
    }

    public boolean harUtenlandsAdresse() {
    	return gjeldendePostadresseType.erUtlandsadresse();
    }

    public static Person ikkeIdentifisert() {
        return new Person();
    }

}
