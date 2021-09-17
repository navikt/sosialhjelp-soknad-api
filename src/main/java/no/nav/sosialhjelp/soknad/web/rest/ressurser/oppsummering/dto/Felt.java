package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;

public class Felt {

    private final String label;
    private final String svar;
    private final Type type;
    private final List<Vedlegg> vedlegg;

    public Felt(String label, String svar, Type type, List<Vedlegg> vedlegg) {
        this.label = label;
        this.svar = svar;
        this.type = type;
        this.vedlegg = vedlegg;
    }

    public Felt(String label, String svar, Type type) {
        this.label = label;
        this.svar = svar;
        this.type = type;
        this.vedlegg = null;
    }

    public Felt(Builder builder) {
        this.label = builder.label;
        this.svar = builder.svar;
        this.type = builder.type;
        this.vedlegg = builder.vedlegg;
    }

    public String getLabel() {
        return label;
    }

    public String getSvar() {
        return svar;
    }

    public Type getType() {
        return type;
    }

    public List<Vedlegg> getVedlegg() {
        return vedlegg;
    }

    public static class Builder {
        private String label;
        private String svar;
        private Type type;
        private List<Vedlegg> vedlegg;

        public Builder() {}

        public Builder withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder withSvar(String svar) {
            this.svar = svar;
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
