package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import org.joda.time.DateTime;

import com.google.gson.GsonBuilder;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Barn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;

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
    private static final String EPOSTKEY = "epost";
    private static final String BARNKEY = "barn";
    private static final String STATSBORGERSKAP = "statsborgerskap";
    private static final String KJONN = "kjonn";

    private Map<String, Object> fakta;

    public Person() {
        fakta = new HashMap<>();
    }

    public Person(Long soknadId, String fnr, String fornavn, String mellomnavn, String etternavn, String gjeldendeAdresseType, List<Adresse> adresser) {
        fakta = new HashMap<>();

        fakta.put(KJONN, genererFaktum(soknadId, KJONN, finnKjonn(fnr)));
        fakta.put(FODSELSNUMMERKEY, genererFaktum(soknadId, FODSELSNUMMERKEY, fnr));
        fakta.put(FORNAVNKEY, genererFaktum(soknadId, FORNAVNKEY, fornavn));
        fakta.put(MELLOMNAVNKEY, genererFaktum(soknadId, MELLOMNAVNKEY, mellomnavn));
        fakta.put(ETTERNAVNKEY, genererFaktum(soknadId, ETTERNAVNKEY, etternavn));
        fakta.put(GJELDENDEADRESSETYPE, genererFaktum(soknadId, GJELDENDEADRESSETYPE, gjeldendeAdresseType));
        fakta.put(SAMMENSATTNAVNKEY, genererFaktum(soknadId, SAMMENSATTNAVNKEY, getSammenSattNavn(fornavn, mellomnavn, etternavn)));
        fakta.put(ADRESSERKEY, adresser);
    }

    public Person(Long soknadId, String fnr, String fornavn,
                  String mellomnavn, String etternavn, List<Barn> barn, String statsborgerskap) {
        fakta = new HashMap<>();

        fakta.put(FODSELSNUMMERKEY, genererFaktum(soknadId, FODSELSNUMMERKEY, fnr));
        fakta.put(FORNAVNKEY, genererFaktum(soknadId, FORNAVNKEY, fornavn));
        fakta.put(MELLOMNAVNKEY, genererFaktum(soknadId, MELLOMNAVNKEY, mellomnavn));
        fakta.put(ETTERNAVNKEY, genererFaktum(soknadId, ETTERNAVNKEY, etternavn));
        fakta.put(SAMMENSATTNAVNKEY, genererFaktum(soknadId, SAMMENSATTNAVNKEY, getSammenSattNavn(fornavn, mellomnavn, etternavn)));
        fakta.put(BARNKEY, barn);
        fakta.put(STATSBORGERSKAP, statsborgerskap);
    }

    public void setEpost(Long soknadId, String epost) {
        fakta.put(EPOSTKEY, genererFaktum(soknadId, EPOSTKEY, epost));
    }

    private String getSammenSattNavn(String fornavn, String mellomnavn, String etternavn) {
        if ("".equals(fornavn) || fornavn == null) {
            return etternavn;
        } else if ("".equals(mellomnavn) || mellomnavn == null) {
            return fornavn + " " + etternavn;
        } else {
            return fornavn + " " + mellomnavn + " " + etternavn;
        }
    }

    private Faktum genererFaktum(Long soknadId, String key, String value) {
        Faktum faktum = new Faktum();
        faktum.setSoknadId(soknadId);
        faktum.setKey(key);
        faktum.setValue(value);
        faktum.setType("System");
        return faktum;
    }

    private String finnKjonn(String fnr) {
        return Character.getNumericValue(fnr.charAt(8)) % 2 == 0 ? "jente" : "gutt";
    }

    public Map<String, Object> getFakta() {
        return fakta;
    }
    
    
    public String hentGjeldendeAdresse() {
        List<Adresse> adresser = getAdresser();
        
        Object object = getFakta().get(GJELDENDEADRESSETYPE);
        Faktum faktum = (Faktum) object;
        
        for (Adresse adresse : adresser) {
            if(adresse.getType().toString().equals(faktum.getValue())) {
                GsonBuilder gson = new GsonBuilder();
                gson.registerTypeAdapter(DateTime.class, new DateTimeSerializer());

                return gson.create().toJson(adresse);
            }
        }
        
        return "";
    }
    
    private List<Adresse> getAdresser() {
        Object adresserobject = getFakta().get(ADRESSERKEY);
        List<Adresse> adresser = (List<Adresse>) adresserobject;
        return adresser;
    }

    public boolean harUtenlandskAdresse() {
        Object object = getFakta().get(GJELDENDEADRESSETYPE);
        Faktum faktum = (Faktum) object;
        if (ingenFaktumReturnert(faktum)) {
            return false;
        }
        String adressetype = faktum.getValue();
        if (adressetype.equals(Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND.toString()) || adressetype.equals(Adressetype.POSTADRESSE_UTLAND.toString())) {
            return true;
        } else if (adressetype.equals(Adressetype.POSTADRESSE.toString())) {
            return erUtenlandskFolkeregistrertAdresse();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean erUtenlandskFolkeregistrertAdresse() {
        Object adresserobject = getFakta().get(ADRESSERKEY);
        List<Adresse> adresser = (List<Adresse>) adresserobject;
        if (adresser.isEmpty()) {
            return false;
        }
        for (Adresse adresse : adresser) {
            if (adresse.getType().equals(Adressetype.UTENLANDSK_ADRESSE)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Dersom faktum er null tyder det på at baksystemet er nede, dermed skal man anta man har norsk adresse.
     */
    private boolean ingenFaktumReturnert(Faktum faktum) {
        return faktum == null;
    }

}
