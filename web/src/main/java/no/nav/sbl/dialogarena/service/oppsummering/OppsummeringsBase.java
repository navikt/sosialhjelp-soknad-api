package no.nav.sbl.dialogarena.service.oppsummering;

import no.nav.sbl.dialogarena.service.PropertyAware;

public interface OppsummeringsBase extends PropertyAware {
    String key();
    String value();
    String originalValue();
    boolean erSynlig();
}
