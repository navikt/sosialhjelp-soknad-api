package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

public enum Skjemanummer {
    N6("I000047", false),
    H1("I000006", true),
    T7("I000016", true),
    L9("I000023", true),
    L4("I000033", true),
    N9("I000037", true),
    O5("I000039", true),
    P5("I000042", true),
    T8("I000043", true),
    Y4("I000044", true),
    K3("I000051", true),
    K4("I000052", true),
    K1("I000053", true),
    M6("I000054", true),
    O9("I000056", true),
    P3("I000058", true),
    R4("I000059", true),
    T1("I000060", true),
    Z6("I000061", true);

    private final String dokumentTypeId;
    private final boolean erPaakrevd;

    Skjemanummer(String dokumentTypeId, boolean erPaakrevd) {
        this.dokumentTypeId = dokumentTypeId;
        this.erPaakrevd = erPaakrevd;
    }

    public String dokumentTypeId() {
        return dokumentTypeId;
    }

    public boolean erPaakrevd() {
        return erPaakrevd;
    }
}
