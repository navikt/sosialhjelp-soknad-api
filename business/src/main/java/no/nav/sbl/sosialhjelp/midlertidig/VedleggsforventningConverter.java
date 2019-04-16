package no.nav.sbl.sosialhjelp.midlertidig;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class VedleggsforventningConverter {

    public static List<Vedleggstatus> mapVedleggsforventningerTilVedleggstatusListe(List<Vedlegg> vedleggsforventninger, String eier) {
        if (isEmpty(eier) || vedleggsforventninger == null) {
            return null;
        }
        List<Vedlegg> ikkeInnsendtePaakrevdeVedlegg = vedleggsforventninger.stream()
                .filter(Objects::nonNull)
                .filter(vedlegg -> vedlegg.getInnsendingsvalg().erIkke(Vedlegg.Status.LastetOpp))
                .filter(vedlegg -> vedlegg.getInnsendingsvalg().erIkke(Vedlegg.Status.IkkeVedlegg))
                .collect(Collectors.toList());

        if (ikkeInnsendtePaakrevdeVedlegg.isEmpty()) {
            return new ArrayList<>();
        }
        List<Vedleggstatus> vedleggstatuser = new ArrayList<>();
        for (Vedlegg vedlegg : ikkeInnsendtePaakrevdeVedlegg) {
            vedleggstatuser.add(mapVedleggTilVedleggstatus(vedlegg, eier));
        }
        return vedleggstatuser;
    }

    static Vedleggstatus mapVedleggTilVedleggstatus(Vedlegg vedlegg, String eier) {
        Vedleggstatus.Status status;
        if (vedlegg.getInnsendingsvalg().er(Vedlegg.Status.VedleggKreves)) {
            status = Vedleggstatus.Status.VedleggKreves;
        } else {
            status = Vedleggstatus.Status.VedleggAlleredeSendt;
        }
        return new Vedleggstatus()
                .withVedleggType(new VedleggType(vedlegg.getSkjemaNummer(), vedlegg.getSkjemanummerTillegg()))
                .withEier(eier)
                .withStatus(status);
    }
}
