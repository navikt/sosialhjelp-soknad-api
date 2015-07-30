package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg.Status;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Arrays;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg.Status.*;

public class VedleggsgenereringUtil {


    public static boolean likeVedlegg(Vedlegg gammeltVedlegg, Vedlegg nyttVedlegg) {
        boolean vedleggErLike = new EqualsBuilder()
                .append(nyttVedlegg.getSoknadId(), gammeltVedlegg.getSoknadId())
                .append(nyttVedlegg.getSkjemaNummer(), gammeltVedlegg.getSkjemaNummer())
                .append(nyttVedlegg.getFaktumId(), gammeltVedlegg.getFaktumId())
                .append(nyttVedlegg.getSkjemanummerTillegg(), gammeltVedlegg.getSkjemanummerTillegg())
                .isEquals();

        if(gammeltVedlegg.getInnsendingsvalg() == nyttVedlegg.getInnsendingsvalg()){
            return vedleggErLike;
        }
        return erInnsendingsvalgPaakrevd(gammeltVedlegg, nyttVedlegg) &&
                vedleggErLike;

    }

    private static boolean erInnsendingsvalgPaakrevd(Vedlegg gammeltVedlegg, Vedlegg nyttVedlegg) {
        List<Status> paakrevdeVedleggsstatuser = Arrays.asList(
                LastetOpp,
                SendesIkke,
                SendesSenere,
                VedleggAlleredeSendt,
                VedleggSendesAvAndre,
                VedleggKreves);

        return paakrevdeVedleggsstatuser.contains(gammeltVedlegg.getInnsendingsvalg()) &&
                paakrevdeVedleggsstatuser.contains(nyttVedlegg.getInnsendingsvalg());
    }
}
