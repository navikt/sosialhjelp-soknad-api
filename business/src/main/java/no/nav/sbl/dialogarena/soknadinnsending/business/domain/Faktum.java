package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Faktum implements Serializable {
    public enum FaktumType { SYSTEMREGISTRERT, BRUKERREGISTRERT; }
    private Long faktumId;
    private Long soknadId;
    private Long vedleggId;
    private Long parrentFaktum;
    private Status innsendingsvalg;
    private String key;
    private String value;
    private List<Faktum> valuelist;
    private Map<String, String> properties = new HashMap<>();
    private String type;

    public Faktum() {

    }


    public Faktum(Long soknadId, Long faktumId, String key, String value, String type, Long parrentFaktum) {
        this(soknadId, faktumId, key, value, type);
        this.parrentFaktum = parrentFaktum;
    }

    public Faktum(Long soknadId, Long faktumId, String key, String value, String type) {
        this(soknadId, faktumId, key, value);
        this.type = type;
    }

    public Faktum(Long soknadId, Long faktumId, String key, String value) {
        this.soknadId = soknadId;
        this.faktumId = faktumId;
        this.key = key;
        this.value = value;
    }

    public Faktum(Long soknadId, String key) {
        this.soknadId = soknadId;
        this.key = key;
    }

    public Long getFaktumId() {
        return faktumId;
    }

    public void setFaktumId(Long faktumId) {
        this.faktumId = faktumId;
    }

    public Long getVedleggId() {
        return vedleggId;
    }

    public void setVedleggId(Long vedleggId) {
        this.vedleggId = vedleggId;
    }

    public String getValue() {
        return value;
    }

    public final void setValue(String value) {
        this.value = value;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Status getInnsendingsvalg() {
        if (innsendingsvalg == null) {
            innsendingsvalg = Status.IkkeVedlegg;
        }
        return innsendingsvalg;
    }

    public void setInnsendingsvalg(Status innsendingsvalg) {
        this.innsendingsvalg = innsendingsvalg;
    }

    public List<Faktum> getValuelist() {
        return valuelist;
    }

    public void setValuelist(List<Faktum> valueList) {
        this.valuelist = valueList;
    }

    public Faktum cloneFaktum() {
        return new Faktum(soknadId, key);
    }

    public Long getParrentFaktum() {
        return parrentFaktum;
    }

    public void setParrentFaktum(Long parrentFaktum) {
        this.parrentFaktum = parrentFaktum;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Faktum [soknadId=" + soknadId + ", key=" + key + ", value="
                + value + ", type=" + type + "]";
    }

    public enum Status {
        IkkeVedlegg,
        VedleggKreves,
        LastetOpp,
        SendesSenere,
        SendesIkke;

        public boolean er(Status status) {
            return this.equals(status);
        }
    }
}