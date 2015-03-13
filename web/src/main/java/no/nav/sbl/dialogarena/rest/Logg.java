package no.nav.sbl.dialogarena.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static java.lang.String.format;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Logg {

    private String level;
    private String message;
    private String jsFileUrl;
    private String lineNumber;
    private String columnNumber;
    private String url;
    private String userAgent;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJsFileUrl() {
        return jsFileUrl;
    }

    public void setJsFileUrl(String jsFileUrl) {
        this.jsFileUrl = jsFileUrl;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(String columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String melding() {
        return format("%s at %s", message, url);
    }
}
