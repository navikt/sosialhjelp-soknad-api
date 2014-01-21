package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;

import java.util.ArrayList;
import java.util.List;

public class Transformers {

    public static XMLVedlegg[] convertToXmlVedleggListe(List<Vedlegg> vedleggForventnings) {
        List<XMLVedlegg> resultat = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggForventnings) {
            if (vedlegg.getInnsendingsvalg().er(Vedlegg.Status.LastetOpp)) {
                resultat.add(new XMLVedlegg()
                        .withFilnavn(vedlegg.getNavn())
                        .withSideantall(vedlegg.getAntallSider())
                        .withFilstorrelse(vedlegg.getStorrelse().toString())
                        .withSkjemanummer(vedlegg.getGosysId())
                        .withArkivreferanse("TODO")
                        .withInnsendingsvalg("INNSENDT"));
            } else {
                resultat.add(new XMLVedlegg().withInnsendingsvalg("SENDES_IKKE")
                        .withSkjemanummer(vedlegg.getGosysId()));
            }

        }
        return resultat.toArray(new XMLVedlegg[resultat.size()]);
    }
}
