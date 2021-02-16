package no.nav.sosialhjelp.soknad.domain;

public class VedleggType {
    private String sammensattType;

    public VedleggType(String sammensattType) {
        this.sammensattType = sammensattType;
    }

    public String getType() {
        return sammensattType.substring(0, sammensattType.indexOf('|'));
    }

    public String getTilleggsinfo() {
        return sammensattType.substring(sammensattType.indexOf('|') + 1);
    }

    public String getSammensattType() {
        return sammensattType;
    }

    @Override
    public boolean equals(Object obj) {
        return this.sammensattType.equals(((VedleggType) obj).getSammensattType());
    }

    @Override
    public int hashCode() {
        return sammensattType != null ? 31 * sammensattType.hashCode() : 0;
    }
}
