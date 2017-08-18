package no.nav.sbl.dialogarena.sendsoknad.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


public class FaktumEgenskap implements Serializable {
    private Long faktumId;
    private Long soknadId;
    private String key;
    private String value;
    private Integer systemEgenskap;
    private static Set<String> datoKeys;

    public FaktumEgenskap() {

    }


    public FaktumEgenskap(Long soknadId, Long faktumId, String key, String value, Boolean systemEgenskap) {
        this.initDatoKeys();
        this.faktumId = faktumId;
        this.soknadId = soknadId;
        this.key = key;
        this.value = value;
        this.systemEgenskap = systemEgenskap ? 1 : 0;
    }

    private static void initDatoKeys(){
        datoKeys = new HashSet<>();
        datoKeys.add("fom");
        datoKeys.add("tom");
        datoKeys.add("fradato");
        datoKeys.add("tildato");
        datoKeys.add("startdato");
        datoKeys.add("sekundarAdresseGyldigFra");
        datoKeys.add("sekundarAdresseGyldigTil");
        datoKeys.add("gjeldendeAdresseGyldigFra");
        datoKeys.add("gjeldendeAdresseGyldigTil");
    }

    public static Set<String> datoKeys(){
        if (datoKeys == null){
            initDatoKeys();
        }
        return datoKeys;
    }

    public Long getFaktumId() {
        return faktumId;
    }

    public void setFaktumId(Long faktumId) {
        this.faktumId = faktumId;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSystemEgenskap() {
        return systemEgenskap;
    }

    public void setSystemEgenskap(Integer systemEgenskap) {

        this.systemEgenskap = systemEgenskap;
    }

}