package no.nav.sosialhjelp.soknad.business.service.digisosapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "enhetsnummer",
})
public class JsonTilleggsinformasjon {
    @JsonProperty("enhetsnummer")
    private String enhetsnummer;

    /**
     * Enhetssnummer for søknadsmottaker.
     */
    @JsonProperty("enhetsnummer")
    public String getEnhetsnummer() {
        return enhetsnummer;
    }

    /**
     * Enhetssnummer for søknadsmottaker.
     */
    @JsonProperty("enhetsnummer")
    public void setEnhetsnummer(String enhetsnummer) {
        this.enhetsnummer = enhetsnummer;
    }

    public JsonTilleggsinformasjon withEnhetsnummer(String enhetsnummer) {
        this.enhetsnummer = enhetsnummer;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("enhetsnummer", enhetsnummer).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(enhetsnummer).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof JsonTilleggsinformasjon)) {
            return false;
        }
        JsonTilleggsinformasjon rhs = ((JsonTilleggsinformasjon) other);
        return new EqualsBuilder().append(enhetsnummer, rhs.enhetsnummer).isEquals();
    }
}
