package no.nav.sbl.dialogarena.soknad.kodeverk;

import org.apache.commons.lang3.StringUtils;

public enum Spraak {
    NB("52"), NN("54"), EN("64"), PL("805380620"), ES("1073745768"), DE("1073745769"), FR("1073745770"), SA("152");

    private final String enonicKode;

    Spraak(String enonicKode) {
        this.enonicKode = enonicKode;
    }
    public boolean erKode(String kode){
        return this.enonicKode.equals(kode);
    }

    public static Spraak fromEnonic(String kode) {
        if (StringUtils.isEmpty(kode)) {
            return NB;
        }
        for (Spraak spraak : values()) {
            if (spraak.erKode(kode)) {
                return spraak;
            }
        }
        return NB;
    }
}