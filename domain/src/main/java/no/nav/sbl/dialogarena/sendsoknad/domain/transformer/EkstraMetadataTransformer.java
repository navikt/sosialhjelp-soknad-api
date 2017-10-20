package no.nav.sbl.dialogarena.sendsoknad.domain.transformer;


import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

public interface EkstraMetadataTransformer extends Function<WebSoknad, XMLMetadata> {
}
