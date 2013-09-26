package no.nav.sbl.dialogarena.konto;

import org.apache.commons.collections15.Predicate;

import static no.nav.modig.lang.collections.PredicateUtils.both;
import static no.nav.modig.lang.collections.PredicateUtils.numeric;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.modig.lang.collections.PredicateUtils.withLength;
import static no.nav.sbl.dialogarena.common.TekstUtils.utenSpesialtegn;


public final class GyldigNorskKontonummer {

    public static final Predicate<String> ELLEVE_SIFFER = where(utenSpesialtegn(), both(withLength(11)).and(numeric()));

    public static final Predicate<String> OPPFYLLER_MOD11 = where(utenSpesialtegn(), new KontonummerErMod11());

    private GyldigNorskKontonummer() {}

}
