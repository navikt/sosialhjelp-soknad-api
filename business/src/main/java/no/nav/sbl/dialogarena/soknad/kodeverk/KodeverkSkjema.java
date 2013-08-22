package no.nav.sbl.dialogarena.soknad.kodeverk;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class KodeverkSkjema implements Serializable {

    private String tittel,
            url,
            urlengelsk,
            urlnynorsk,
            urlpolsk,
            urlfransk,
            urlspansk,
            urltysk,
            urlsamisk,
            vedleggsid,
            beskrivelse,
            skjemanummer,
            tema,
            gosysId;

    public String getTittel() {
        return tittel;
    }

    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlengelsk() {
        return urlengelsk;
    }

    public void setUrlengelsk(String urlengelsk) {
        this.urlengelsk = urlengelsk;
    }

    public String getUrlnynorsk() {
        return urlnynorsk;
    }

    public void setUrlnynorsk(String urlnynorsk) {
        this.urlnynorsk = urlnynorsk;
    }

    public String getUrlpolsk() {
        return urlpolsk;
    }

    public void setUrlpolsk(String urlpolsk) {
        this.urlpolsk = urlpolsk;
    }

    public String getUrlfransk() {
        return urlfransk;
    }

    public void setUrlfransk(String urlfransk) {
        this.urlfransk = urlfransk;
    }

    public String getUrltysk() {
        return urltysk;
    }

    public void setUrlsamisk(String urlsamisk) {
        this.urlsamisk = urlsamisk;
    }

    public String getUrlsamisk() {
        return urlsamisk;
    }

    public void setUrltysk(String urltysk) {
        this.urltysk = urltysk;
    }

    public String getUrlspansk() {
        return urlspansk;
    }

    public void setUrlspansk(String urlspansk) {
        this.urlspansk = urlspansk;
    }

    public String getVedleggsid() {
        return vedleggsid;
    }

    public void setVedleggsid(String vedleggsid) {
        this.vedleggsid = vedleggsid;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public String getSkjemanummer() {
        return skjemanummer;
    }

    public void setSkjemanummer(String skjemanummer) {
        this.skjemanummer = skjemanummer;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getGosysId() {
        return gosysId;
    }

    public void setGosysId(String gosysId) {
        this.gosysId = gosysId;
    }



    public String getUrl(Spraak languageCode) {
        switch (languageCode) {
            case NB:
                return defaultIfNull(getUrl());
            case NN:
                return defaultIfNull(getUrlnynorsk());
            case EN:
                return defaultIfNull(getUrlengelsk());
            case PL:
                return defaultIfNull(getUrlpolsk());
            case ES:
                return defaultIfNull(getUrlspansk());
            case DE:
                return defaultIfNull(getUrltysk());
            case FR:
                return defaultIfNull(getUrlfransk());
            case SA:
                return defaultIfNull(getUrlsamisk());
            default:
                return getUrl();
        }

    }

    private String defaultIfNull(String url) {
        if (StringUtils.isBlank(url)) {
            return getUrl();
        }
        return url;
    }


}