package no.nav.sbl.dialogarena.dokumentinnsending.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PersonTest {

    private static final String EPOST_ADRESSE = "test@test.no";

	@Test
    public void personMedMidlertidigAdresseIUtlandetHarAdresseIUtlandet() {
        Person student = Person.identifisert(EPOST_ADRESSE, "MIDLERTIDIG_POSTADRESSE_UTLAND");
        assertTrue(student.harUtenlandsAdresse());
    }

	@Test
    public void personMedPostadresseUtlandHarUtlandsAdresse() {
    	assertTrue(Person.identifisert(EPOST_ADRESSE, "POSTADRESSE_UTLAND").harUtenlandsAdresse());
    }
	
    @Test
    public void utflytterTilSverigeMedMidlertidigNorskAdresseHarIkkeAdresseIUtlandet() {
        Person utflytterTilSverige = Person.identifisert(EPOST_ADRESSE, "MIDLERTIDIG_POSTADRESSE_NORGE");
        assertFalse(utflytterTilSverige.harUtenlandsAdresse());
    }

    @Test
    public void personMedUkjentAdresseSkalTolkesSomNorskAdresse() {
    	assertFalse(Person.identifisert(EPOST_ADRESSE, "UKJENT").harUtenlandsAdresse());
    }
    
    @Test
    public void personMedBostedsAdresseHarIkkeUtlandsAdresse() {
    	assertFalse(Person.identifisert(EPOST_ADRESSE, "BOSTEDSADRESSE").harUtenlandsAdresse());
    }
    
    @Test
    public void personMedPoststedAdresseHarIkkeUtlandsAdresse() {
    	assertFalse(Person.identifisert(EPOST_ADRESSE, "POSTADRESSE").harUtenlandsAdresse());
    }
    
    @Test
    public void nyVerdiIGjeldendeAdressetypeSkalIkkeFeileStygt() {
    	assertFalse(Person.identifisert(EPOST_ADRESSE, "NYVERDI").harUtenlandsAdresse());
    }
    
    @Test
    public void personHarEpost()  {
    	assertEquals(EPOST_ADRESSE, Person.identifisert(EPOST_ADRESSE, "UKJENT").getEpost());    
    }
    
    @Test
    public void personSomIkkeBlirIdentifisertSkalIkkeHaEpostEllerAdresseIUtlandet() {
    	Person ikkeIdentifisertPerson = Person.ikkeIdentifisert();
    	assertNull(ikkeIdentifisertPerson.getEpost());
    	assertFalse(ikkeIdentifisertPerson.harUtenlandsAdresse());
    }
}
