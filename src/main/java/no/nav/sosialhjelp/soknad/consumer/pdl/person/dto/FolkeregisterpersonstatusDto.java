package no.nav.sosialhjelp.soknad.consumer.pdl.person.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class FolkeregisterpersonstatusDto {

    private final String status;

    @JsonCreator
    public FolkeregisterpersonstatusDto(
            @JsonProperty("status") String status
    ) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
