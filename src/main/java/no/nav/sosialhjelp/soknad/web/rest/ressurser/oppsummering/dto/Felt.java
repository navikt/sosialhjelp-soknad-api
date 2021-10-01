package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;
import java.util.Map;

public class Felt {

    private final String label;
    private final Svar svar;
    private final Map<String, Svar> labelSvarMap;
    private final Type type;
    private final List<Vedlegg> vedlegg;

    public Felt(Builder builder) {
        this.label = builder.label;
        this.svar = builder.svar;
        this.labelSvarMap = builder.labelSvarMap;
        this.type = builder.type;
        this.vedlegg = builder.vedlegg;
    }

    public String getLabel() {
        return label;
    }

    public Svar getSvar() {
        return svar;
    }

    public Map<String, Svar> getLabelSvarMap() {
        return labelSvarMap;
    }

    public Type getType() {
        return type;
    }

    public List<Vedlegg> getVedlegg() {
        return vedlegg;
    }

    public static class Builder {
        private String label;
        private Svar svar;
        private Map<String, Svar> labelSvarMap;
        private Type type;
        private List<Vedlegg> vedlegg;

        public Builder() {}

        public Builder withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder withSvar(Svar svar) {
            this.svar = svar;
            return this;
        }

        public Builder withLabelSvarMap(Map<String, Svar> labelSvarMap) {
            this.labelSvarMap = labelSvarMap;
            return this;
        }

        public Builder withType(Type type) {
            this.type = type;
            return this;
        }

        public Builder withVedlegg(List<Vedlegg> vedlegg) {
            this.vedlegg = vedlegg;
            return this;
        }

        public Felt build() {
            return new Felt(this);
        }
    }
}
