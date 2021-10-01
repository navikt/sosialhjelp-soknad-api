package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

public class Svar {

    private final String value;
    private final SvarType type;

    public Svar(Builder builder) {
        value = builder.value;
        type = builder.type;
    }

    public String getValue() {
        return value;
    }

    public SvarType getType() {
        return type;
    }

    public static class Builder {
        private String value;
        private SvarType type;

        public Builder(){}

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withType(SvarType type) {
            this.type = type;
            return this;
        }

        public Svar build() {
            return new Svar(this);
        }
    }
}
