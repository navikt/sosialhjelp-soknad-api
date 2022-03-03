package no.nav.sosialhjelp.soknad.domain;

public class OpplastetVedleggType {
    private String sammensattType;

    public OpplastetVedleggType(String sammensattType) {
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
        return this.sammensattType.equals(((OpplastetVedleggType) obj).getSammensattType());
    }

    @Override
    public int hashCode() {
        return sammensattType != null ? 31 * sammensattType.hashCode() : 0;
    }
}
