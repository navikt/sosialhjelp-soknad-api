package no.nav.sbl.dialogarena.service.oppsummering;

public interface OppsummeringsBase {
    String key();
    String value();
    String originalValue();
    boolean erSynlig();
    String property(String configKey);
}
