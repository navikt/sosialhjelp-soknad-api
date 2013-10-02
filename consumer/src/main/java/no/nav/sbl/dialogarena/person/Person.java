package no.nav.sbl.dialogarena.person;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;

@XmlRootElement
public class Person implements Serializable {
	private final String FODSELSNUMMERKEY = "fnr";
	private final String FORNAVNKEY = "fornavn";
	private final String MELLOMNAVNKEY = "mellomnavn";
	private final String ETTERNAVNKEY = "etternavn";
	private final String SAMMENSATTNAVNKEY = "sammensattnavn";
	private final String ADRESSERKEY = "adresser";

	private Map<String, Object> fakta;

	public Person() {
		fakta = new HashMap<>();
	}

    public Person(Long soknadId, String fnr, String fornavn, String mellomnavn, String etternavn, List<Adresse> adresser) {
    	fakta = new HashMap<>();

    	fakta.put(FODSELSNUMMERKEY, genererFaktum(soknadId,FODSELSNUMMERKEY,fnr));
    	fakta.put(FORNAVNKEY, genererFaktum(soknadId,FORNAVNKEY,fornavn));
    	fakta.put(MELLOMNAVNKEY, genererFaktum(soknadId,MELLOMNAVNKEY,mellomnavn));
    	fakta.put(ETTERNAVNKEY, genererFaktum(soknadId,ETTERNAVNKEY,etternavn));
    	fakta.put(SAMMENSATTNAVNKEY, genererFaktum(soknadId, SAMMENSATTNAVNKEY, getSammenSattNavn(fornavn,mellomnavn, etternavn)));
    	
    	fakta.put(ADRESSERKEY, adresser);

    }

	private String getSammenSattNavn(String fornavn, String mellomnavn,
			String etternavn) {
		if(mellomnavn.equals("")) {
			return fornavn + " " + etternavn;
		} else {
			return fornavn +" " + mellomnavn + " " + etternavn;
		}
	}

	private Faktum genererFaktum(Long soknadId, String key, String value) {
		Faktum faktum = new Faktum();
		faktum.setSoknadId(soknadId);
		faktum.setKey(key);
		faktum.setValue(value);
		return faktum;
	}
	
	public Map<String, Object> getFakta() {
		return fakta;
	}
}
