package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

public enum Skjemanummer {
    N6("***REMOVED***", false),
    H1("***REMOVED***", true),
    T7("***REMOVED***", true),
    L9("***REMOVED***", true),
    L4("***REMOVED***", true),
    N9("***REMOVED***", true),
    O5("***REMOVED***", true),
    P5("***REMOVED***", true),
    T8("***REMOVED***", true),
    Y4("***REMOVED***", true),
    K3("***REMOVED***", true),
    K4("***REMOVED***", true),
    K1("***REMOVED***", true),
    M6("***REMOVED***", true),
    O9("***REMOVED***", true),
    P3("***REMOVED***", true),
    R4("***REMOVED***", true),
    T1("***REMOVED***", true),
    Z6("***REMOVED***", true);

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
