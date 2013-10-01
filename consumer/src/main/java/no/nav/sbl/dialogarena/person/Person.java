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
	private final String SAMMENSATTNAVNKEY = "sammensattnavn";
	private final String ADRESSERKEY = "adresser";

	private Map<String, Object> fakta;

	public Person() {
		fakta = new HashMap<>();
	}

    public Person(Long soknadId, String fnr, String sammensattNavn, List<PersonAdresse> adresser) {
    	fakta = new HashMap<>();

    	fakta.put(FODSELSNUMMERKEY, genererFaktum(soknadId,FODSELSNUMMERKEY,fnr));
    	fakta.put(SAMMENSATTNAVNKEY, genererFaktum(soknadId, SAMMENSATTNAVNKEY,sammensattNavn));
    	
    	fakta.put(ADRESSERKEY, adresser);

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
