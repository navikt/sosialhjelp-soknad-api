package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Arrays;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.SendesIkke;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.SendesSenere;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggAlleredeSendt;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggKreves;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggSendesAvAndre;


public class VedleggsgenereringUtil {
    public static boolean likeVedlegg(Vedlegg gammeltVedlegg, Vedlegg nyttVedlegg) {
        boolean vedleggErLike = new EqualsBuilder()
                .append(nyttVedlegg.getSoknadId(), gammeltVedlegg.getSoknadId())
                .append(nyttVedlegg.getSkjemaNummer(), gammeltVedlegg.getSkjemaNummer())
                .append(nyttVedlegg.getFaktumId(), gammeltVedlegg.getFaktumId())
                .append(nyttVedlegg.getSkjemanummerTillegg(), gammeltVedlegg.getSkjemanummerTillegg())
                .isEquals();

        if (gammeltVedlegg.getInnsendingsvalg() == nyttVedlegg.getInnsendingsvalg()) {
            return vedleggErLike;
        }

        return erInnsendingsvalgPaakrevd(gammeltVedlegg, nyttVedlegg) && vedleggErLike;
    }

    private static boolean erInnsendingsvalgPaakrevd(Vedlegg gammeltVedlegg, Vedlegg nyttVedlegg) {
        List<Vedlegg.Status> paakrevdeVedleggsstatuser = Arrays.asList(
                LastetOpp,
                SendesIkke,
                SendesSenere,
                VedleggAlleredeSendt,
                VedleggSendesAvAndre,
                VedleggKreves);

        return paakrevdeVedleggsstatuser.contains(gammeltVedlegg.getInnsendingsvalg()) &&
                paakrevdeVedleggsstatuser.contains(nyttVedlegg.getInnsendingsvalg());
    }

    public static boolean likeVedlegg(List<Vedlegg> gamleVedlegg, Vedlegg nyttVedlegg) {
        boolean erLikeVedlegg = false;

        int i = 0;
        while (!erLikeVedlegg && i < gamleVedlegg.size()) {
            erLikeVedlegg = likeVedlegg(gamleVedlegg.get(i), nyttVedlegg);
            i++;
        }

        return erLikeVedlegg;
    }

    public static boolean likeVedlegg(List<Vedlegg> gamleVedlegg, List<Vedlegg> nyeVedlegg) {
        if(gamleVedlegg.size() == nyeVedlegg.size()) {
            boolean erLikeVedlegg = true;

            int i = 0;
            while (erLikeVedlegg && i < gamleVedlegg.size()) {
                erLikeVedlegg = likeVedlegg(gamleVedlegg, nyeVedlegg.get(i));
                i++;
            }

            return erLikeVedlegg;
        }
        return false;
    }
}
