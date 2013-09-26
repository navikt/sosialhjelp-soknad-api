package no.nav.sbl.dialogarena.konto;

import org.apache.commons.collections15.Predicate;

import static no.nav.modig.lang.collections.PredicateUtils.equalOrLessThan;
import static no.nav.modig.lang.collections.PredicateUtils.withLength;


public final class GyldigUtenlandskKontonummer {

    public static final Predicate<String> MINDRE_ELLER_AKKURAT_36_TEGN = withLength(equalOrLessThan(36));

    private GyldigUtenlandskKontonummer() { }

}
