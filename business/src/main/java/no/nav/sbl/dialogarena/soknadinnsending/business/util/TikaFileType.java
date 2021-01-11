package no.nav.sbl.dialogarena.soknadinnsending.business.util;

public enum TikaFileType {
    JPEG(".jpg"),
    PNG(".png"),
    PDF(".pdf"),
    UNKNOWN("");

    private final String extention;

    TikaFileType(String extention) {
        this.extention = extention;
    }

    public String getExtention() {
        return extention;
    }
}
