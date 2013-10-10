package no.nav.sbl.dialogarena.person;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class Person implements Serializable {
	private static final String FODSELSNUMMERKEY = "fnr";
	private static final String FORNAVNKEY = "fornavn";
	private static final String MELLOMNAVNKEY = "mellomnavn";
	private static final String ETTERNAVNKEY = "etternavn";
	private static final String SAMMENSATTNAVNKEY = "sammensattnavn";
	private static final String ADRESSERKEY = "adresser";
	private static final String GJELDENDEADRESSETYPE = "gjeldendeAdresseType";

	private Map<String, Object> fakta;

	public Person() {
		fakta = new HashMap<>();
	}

    public Person(Long soknadId, String fnr, String fornavn, String mellomnavn, String etternavn, String gjeldendeAdresseType, List<Adresse> adresser) {
    	fakta = new HashMap<>();

    	fakta.put(FODSELSNUMMERKEY, genererFaktum(soknadId,FODSELSNUMMERKEY,fnr));
    	fakta.put(FORNAVNKEY, genererFaktum(soknadId,FORNAVNKEY,fornavn));
    	fakta.put(MELLOMNAVNKEY, genererFaktum(soknadId,MELLOMNAVNKEY,mellomnavn));
    	fakta.put(ETTERNAVNKEY, genererFaktum(soknadId,ETTERNAVNKEY,etternavn));
    	fakta.put(GJELDENDEADRESSETYPE, genererFaktum(soknadId, GJELDENDEADRESSETYPE, gjeldendeAdresseType));
    	fakta.put(SAMMENSATTNAVNKEY, genererFaktum(soknadId, SAMMENSATTNAVNKEY, getSammenSattNavn(fornavn,mellomnavn, etternavn)));
    	
    	fakta.put(ADRESSERKEY, adresser);

    }

	private String getSammenSattNavn(String fornavn, String mellomnavn,
			String etternavn) {
		if("".equals(mellomnavn) || mellomnavn == null) {
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
