package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import org.joda.time.LocalDate;

import java.util.List;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.PersonaliaUtils.*;

public class DagpengerUtils {
    public static final String DAGPENGER = "NAV 04-01.03";
    public static final String DAGPENGER_VED_PERMITTERING = "NAV 04-01.04";
    public static final String GJENOPPTAK = "NAV 04-16.03";
    public static final String GJENOPPTAK_VED_PERMITTERING = "NAV 04-16.04";
    public static final String EOS_DAGPENGER = "4465";
    public static final String RUTES_I_BRUT = "";
    public static final String PERMITTERT = "permittert";
    public static final String ANNEN_AARSAK = "Annen årsak";

    public static String getSkjemanummer(WebSoknad soknad) {
        boolean erPermittert = finnSluttaarsakSisteArbeidsforhold(soknad).equals(PERMITTERT);
        if (soknad.erGjenopptak()) {
            return erPermittert ? GJENOPPTAK_VED_PERMITTERING : GJENOPPTAK;
        }
        return erPermittert ? DAGPENGER_VED_PERMITTERING : DAGPENGER;
    }

    public static String getJournalforendeEnhet(WebSoknad webSoknad) {
        String sluttaarsak = finnSluttaarsakSisteArbeidsforhold(webSoknad);
        Personalia personalia = adresserOgStatsborgerskap(webSoknad);

        if (sluttaarsak.equals(PERMITTERT) || (sluttaarsak.equals(REDUSERT_ARBEIDSTID))) {
            if (harUtenlandskAdresseIEOS(personalia)) {
                return EOS_DAGPENGER;
            }
            boolean erUtenlandskStatsborger = !personalia.getStatsborgerskap().equals("NOR");
            if (erGrensearbeider(webSoknad) && erUtenlandskStatsborger) {
                return EOS_DAGPENGER;
            }
        }
        return RUTES_I_BRUT;
    }

    private static String finnSluttaarsakSisteArbeidsforhold(WebSoknad soknad) {
        List<Faktum> sorterteArbeidsforholdIkkePermittert = soknad.getFaktaMedKey("arbeidsforhold").stream()
                .filter(faktum -> !PERMITTERT.equals(TYPE.transform(faktum)))
                .sorted(comparing(DATO_TIL).reversed())
                .collect(toList());

        LocalDate sluttdatoSistePermitteringsperiode = getSluttdatoForSistePermitteringsperiode(soknad);
        LocalDate sluttdatoSisteArbeidsforholdIkkePermittert = getSluttdatoForSisteArbeidsforhold(sorterteArbeidsforholdIkkePermittert);

        if (erSisteSluttaarsakPermittering(sluttdatoSistePermitteringsperiode, sluttdatoSisteArbeidsforholdIkkePermittert)) {
            return PERMITTERT;
        } else if (soknad.erGjenopptak() && ingenNyeArbeidsforhold(soknad) && varPermittertForrigeGangDuSokteOmDagpenger(soknad)) {
            return PERMITTERT;
        } else if (erSisteSluttaarsakRedusertArbeidstid(sluttdatoSisteArbeidsforholdIkkePermittert, sorterteArbeidsforholdIkkePermittert)) {
            return REDUSERT_ARBEIDSTID;
        }
        return ANNEN_AARSAK;
    }

    private static boolean erSisteSluttaarsakPermittering(LocalDate datoSistePermittering, LocalDate datoSisteArberidsforhold) {
        return datoSistePermittering != null && (datoSisteArberidsforhold == null || !datoSistePermittering.isBefore(datoSisteArberidsforhold));
    }

    private static boolean erSisteSluttaarsakRedusertArbeidstid(LocalDate datoSisteArberidsforhold, List<Faktum> arbeidsforholdSortert) {
        return arbeidsforholdSortert.stream()
                .filter(faktum -> DATO_TIL.apply(faktum).equals(datoSisteArberidsforhold))
                .filter(faktum  -> REDUSERT_ARBEIDSTID.equals(TYPE.transform(faktum)))
                .findFirst()
                .isPresent();
    }

    private static LocalDate getSluttdatoForSistePermitteringsperiode(WebSoknad soknad) {
        return soknad.getFaktaMedKey("arbeidsforhold.permitteringsperiode").stream()
                .filter(faktum -> PERMITTERT.equals(parentFaktumType(soknad, faktum)))
                .sorted(comparing(DATO_TIL_PERMITTERING).reversed())
                .map(DATO_TIL_PERMITTERING)
                .findFirst()
                .orElse(null);
    }

    private static LocalDate getSluttdatoForSisteArbeidsforhold(List<Faktum> arbeidsforholdSortert) {
        return arbeidsforholdSortert.stream().map(DATO_TIL).findFirst().orElse(null);
    }

    private static boolean varPermittertForrigeGangDuSokteOmDagpenger(WebSoknad soknad) {
        Faktum permittertForrigeGang = soknad.getFaktumMedKey("tidligerearbeidsforhold.permittert");
        if (permittertForrigeGang == null) {
            return false;
        }

        String value = permittertForrigeGang.getValue();
        return "permittertFiske".equals(value) || "permittert".equals(value);
    }

    private static boolean ingenNyeArbeidsforhold(WebSoknad soknad) {
        Faktum nyeArbeidsforhold = soknad.getFaktumMedKey("nyearbeidsforhold.arbeidsidensist");
        return nyeArbeidsforhold != null && "true".equals(nyeArbeidsforhold.getValue());
    }

    private static boolean erGrensearbeider(WebSoknad webSoknad) {
        Faktum grensearbeiderFaktum = webSoknad.getFaktumMedKey("arbeidsforhold.grensearbeider");
        boolean erGrensearbeider = false;
        if (grensearbeiderFaktum != null && grensearbeiderFaktum.getValue() != null) {
            erGrensearbeider = grensearbeiderFaktum.getValue().equals("false");
        }
        return erGrensearbeider;
    }

    private static boolean harUtenlandskAdresseIEOS(Personalia personalia) {
        return personalia.harUtenlandskAdresseIEOS() && (!personalia.harNorskMidlertidigAdresse());
    }
}
