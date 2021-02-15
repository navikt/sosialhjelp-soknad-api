package no.nav.sosialhjelp.soknad.consumer.virusscan;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class ScanResult {
    @JsonAlias("Filename")
    private final String filename;
    @JsonAlias("Result")
    private final Result result;

    @JsonCreator
    ScanResult(@JsonProperty("filename") String filename, @JsonProperty("result") Result result) {
        this.filename = filename;
        this.result = result;
    }

    public String getFilename() {
        return filename;
    }

    public Result getResult() {
        return result;
    }
}
