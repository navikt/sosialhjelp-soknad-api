package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLFiksMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;

public class FiksMetadataTransformer implements EkstraMetadataTransformer {
    @Override
    public XMLMetadata apply(WebSoknad webSoknad) {
        // TODO sette riktige verdier
        return new XMLFiksMetadata()
                .withKontornavn("NAV Horten")
                .withOrgnr("123456789");
    }
}
