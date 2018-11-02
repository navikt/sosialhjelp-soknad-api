package no.nav.sbl.sosialhjelp.domain;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class VedleggType {
    private String type;
    private String tilleggsinfo;

    public VedleggType(String type, String tilleggsinfo) {
        this.type = type;
        this.tilleggsinfo = tilleggsinfo;
    }

    public String getType() {
        return type;
    }

    public String getTilleggsinfo() {
        return tilleggsinfo;
    }

    public String getSammensattVedleggType() {
        return type + "|" + tilleggsinfo;
    }

    public static VedleggType mapSammensattVedleggTypeTilVedleggType(String sammensattVedleggType) {
        if (isEmpty(sammensattVedleggType) || !sammensattVedleggType.contains("|")) {
            return null;
        }
        String[] sammensattVedleggTypeSplittet = sammensattVedleggType.split(Pattern.quote("|"));
        if (sammensattVedleggTypeSplittet.length == 2) {
            return new VedleggType(sammensattVedleggTypeSplittet[0], sammensattVedleggTypeSplittet[1]);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VedleggType that = (VedleggType) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return tilleggsinfo != null ? tilleggsinfo.equals(that.tilleggsinfo) : that.tilleggsinfo == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (tilleggsinfo != null ? tilleggsinfo.hashCode() : 0);
        return result;
    }
}
