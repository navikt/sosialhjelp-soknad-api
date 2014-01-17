package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.VedleggForventning;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.Status.LastetOpp;

public class Transformers {

    public static XMLVedlegg[] convertToXmlVedleggListe(List<VedleggForventning> vedleggForventnings) {
        List<XMLVedlegg> resultat = new ArrayList<>();
        for (VedleggForventning vedlegg : vedleggForventnings) {
            if (vedlegg.getFaktum().getInnsendingsvalg(vedlegg.getGosysId()).er(LastetOpp)) {
                resultat.add(new XMLVedlegg()
                        .withFilnavn(vedlegg.getVedlegg().getNavn())
                        .withSideantall(vedlegg.getVedlegg().getAntallSider())
                        .withFilstorrelse(vedlegg.getVedlegg().getStorrelse().toString())
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
