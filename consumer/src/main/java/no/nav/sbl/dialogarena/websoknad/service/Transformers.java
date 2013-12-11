package no.nav.sbl.dialogarena.websoknad.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLFakta;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLFaktumListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLSoknadfaktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Transformers {

    private static final Logger LOG = LoggerFactory
            .getLogger(Transformers.class);

    public static XMLFakta convertToFaktumListe(List<Faktum> fakta) {
        XMLFaktumListe faktumListe = new XMLFaktumListe();
        if (fakta != null) {
            for (Faktum faktum : fakta) {
                XMLSoknadfaktum soknadsFaktum = mapFaktum(faktum);
                faktumListe.getFaktum().add(soknadsFaktum);
            }
        }

        return new XMLFakta().withProduserteFakta(faktumListe);
    }
    
    private static XMLSoknadfaktum mapFaktum(Faktum faktum) {
        XMLSoknadfaktum soknadsFaktum = new XMLSoknadfaktum();
        soknadsFaktum.setNokkel(faktum.getKey());
        soknadsFaktum.setVerdi(faktum.getValue());
        soknadsFaktum.setType(faktum.getType());
        return soknadsFaktum;
    }

    public static WebSoknad convertToSoknad(WSSoknadsdata soknadData) {
        LOG.warn("søknadsdata: " + soknadData.getAny());
        return null;
    }
}
