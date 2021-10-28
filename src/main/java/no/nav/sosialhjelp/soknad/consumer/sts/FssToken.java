package no.nav.sosialhjelp.soknad.consumer.sts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FssToken {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private LocalDateTime expirationTime;

    @JsonCreator
    public FssToken(
            @JsonProperty(value = "access_token", required = true) String accessToken,
            @JsonProperty(value = "token_type", required = true) String tokenType,
            @JsonProperty(value = "expires_in", required = true) Long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.expirationTime = LocalDateTime.now().plusSeconds(expiresIn - 60L); // fornyer token 1 min før token går ut
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

    public boolean isExpired() {
        return expirationTime.isBefore(LocalDateTime.now());
    }
}
