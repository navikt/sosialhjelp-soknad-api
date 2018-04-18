package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.dagpenger.ordinaer;

public class JsonFaktumEgenskap {
    private Long faktumId;



    private Long soknadId;
    private String key;
    private String value;
    private Integer systemEgenskap;

    JsonFaktumEgenskap medFaktumId(Long faktumId) {
        this.faktumId = faktumId;
        return this;
    }

    JsonFaktumEgenskap medSoknadId(Long soknadId) {
        this.soknadId = soknadId;
        return this;
    }

    JsonFaktumEgenskap medKey(String key) {
        this.key = key;
        return this;
    }

    JsonFaktumEgenskap medValue(String value) {
        this.value = value;
        return this;
    }

    JsonFaktumEgenskap medSystemEgenskap(Integer systemEgenskap) {
        this.systemEgenskap = systemEgenskap;
        return this;
    }

    public Long getFaktumId() {
        return faktumId;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public Integer getSystemEgenskap() {
        return systemEgenskap;
    }
}

