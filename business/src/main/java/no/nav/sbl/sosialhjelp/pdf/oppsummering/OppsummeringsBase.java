package no.nav.sbl.sosialhjelp.pdf.oppsummering;

import no.nav.sbl.sosialhjelp.pdf.PropertyAware;

public interface OppsummeringsBase extends PropertyAware {
    String key();
    String value();
    String originalValue();
    boolean erSynlig();
}
