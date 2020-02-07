package no.nav.sbl.dialogarena.soknadinnsending.consumer.sts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FssToken {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;

    @JsonCreator
    public FssToken(
            @JsonProperty(value = "access_token", required = true) String accessToken,
            @JsonProperty(value = "token_type", required = true) String tokenType,
            @JsonProperty(value = "expires_in", required = true) Long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }
}
