package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnsettelsesperiodeDto {

    private PeriodeDto periode;

    public AnsettelsesperiodeDto(PeriodeDto periode) {
        this.periode = periode;
    }

    public PeriodeDto getPeriode() {
        return periode;
    }
}
