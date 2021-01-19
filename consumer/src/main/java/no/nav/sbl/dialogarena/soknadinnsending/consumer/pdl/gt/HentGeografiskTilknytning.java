package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.gt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.gt.dto.GeografiskTilknytningDto;

public class HentGeografiskTilknytning {

    private final GeografiskTilknytningDto geografiskTilknytning;

    @JsonCreator
    public HentGeografiskTilknytning(
            @JsonProperty("hentGeografiskTilknytning") GeografiskTilknytningDto geografiskTilknytning
    ) {
        this.geografiskTilknytning = geografiskTilknytning;
    }

    public GeografiskTilknytningDto getGeografiskTilknytning() {
        return geografiskTilknytning;
    }
}
