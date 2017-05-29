package no.nav.sbl.dialogarena.sendsoknad.domain.transformer;


import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

public interface AlternativRepresentasjonTransformer extends Function<WebSoknad, AlternativRepresentasjon> {

    AlternativRepresentasjonType getRepresentasjonsType();

}
