package no.nav.sosialhjelp.soknad.business.util;

public enum TikaFileType {
    JPEG(".jpg"),
    PNG(".png"),
    PDF(".pdf"),
    UNKNOWN("");

    private final String extension;

    TikaFileType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
