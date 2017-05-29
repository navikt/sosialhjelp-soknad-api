package no.nav.sbl.dialogarena.sendsoknad.domain.transformer;


import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

public interface AlternativRepresentasjonTransformer extends Transformer<WebSoknad, AlternativRepresentasjon> {

    AlternativRepresentasjonType getRepresentasjonsType();

}
