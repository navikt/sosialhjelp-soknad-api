package no.nav.sbl.dialogarena.soknadinnsending;

import javax.xml.bind.annotation.XmlElement;

/**
 * Klasse for å returnere et resultat av en opplasting
 */
public class VedleggOpplastingResultat {
    private String name;
    private Integer size;
    private String url;
    private String thumbnailUrl;
    private String deleteUrl;
    private String deleteType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement(name="thumbnail_url")
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @XmlElement(name="delete_url")
    public String getDeleteUrl() {
        return deleteUrl;
    }

    public void setDeleteUrl(String deleteUrl) {
        this.deleteUrl = deleteUrl;
    }

    public String getDeleteType() {
        return deleteType;
    }

    public void setDeleteType(String deleteType) {
        this.deleteType = deleteType;
    }
}
