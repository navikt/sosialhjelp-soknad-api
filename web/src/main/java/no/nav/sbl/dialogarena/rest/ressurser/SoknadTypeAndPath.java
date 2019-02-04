package no.nav.sbl.dialogarena.rest.ressurser;

public class SoknadTypeAndPath {
    private String type;
    private String path;

    public SoknadTypeAndPath(String type, String path) {
        this.type = type;
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }
}
