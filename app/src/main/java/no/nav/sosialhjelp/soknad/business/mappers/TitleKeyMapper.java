package no.nav.sosialhjelp.soknad.business.mappers;

import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.*;

public class TitleKeyMapper {
    public static final Map<String, String> soknadTypeToTitleKey = new HashMap<>();

    static {
        soknadTypeToTitleKey.put(UTGIFTER_HUSLEIE, "opplysninger.utgifter.boutgift.husleie");
        soknadTypeToTitleKey.put(UTGIFTER_BOLIGLAN_AVDRAG, "opplysninger.utgifter.boutgift.avdraglaan.boliglanAvdrag");
        soknadTypeToTitleKey.put(UTGIFTER_BOLIGLAN_RENTER, "opplysninger.utgifter.boutgift.avdraglaan.boliglanRenter");
        soknadTypeToTitleKey.put(UTGIFTER_BARNEHAGE, "opplysninger.utgifter.barn.barnehage");
        soknadTypeToTitleKey.put(UTGIFTER_SFO, "opplysninger.utgifter.barn.sfo");
        soknadTypeToTitleKey.put(FORMUE_BRUKSKONTO, "opplysninger.inntekt.bankinnskudd.brukskonto");
        soknadTypeToTitleKey.put(FORMUE_BSU, "opplysninger.inntekt.bankinnskudd.bsu");
        soknadTypeToTitleKey.put(FORMUE_SPAREKONTO, "opplysninger.inntekt.bankinnskudd.sparekonto");
        soknadTypeToTitleKey.put(FORMUE_LIVSFORSIKRING, "opplysninger.inntekt.bankinnskudd.livsforsikring");
        soknadTypeToTitleKey.put(FORMUE_VERDIPAPIRER, "opplysninger.inntekt.bankinnskudd.aksjer");
        soknadTypeToTitleKey.put(FORMUE_ANNET, "opplysninger.inntekt.bankinnskudd.annet");
        soknadTypeToTitleKey.put(VERDI_BOLIG, "inntekt.eierandeler.true.type.bolig");
        soknadTypeToTitleKey.put(VERDI_CAMPINGVOGN, "inntekt.eierandeler.true.type.campingvogn");
        soknadTypeToTitleKey.put(VERDI_KJORETOY, "inntekt.eierandeler.true.type.kjoretoy");
        soknadTypeToTitleKey.put(VERDI_FRITIDSEIENDOM, "inntekt.eierandeler.true.type.fritidseiendom");
        soknadTypeToTitleKey.put(VERDI_ANNET, "inntekt.eierandeler.true.type.annet");
        soknadTypeToTitleKey.put(BOSTOTTE, "opplysninger.inntekt.bostotte");
        soknadTypeToTitleKey.put(JOBB, "opplysninger.arbeid.jobb");
        soknadTypeToTitleKey.put(STUDIELAN, "opplysninger.arbeid.student");
        soknadTypeToTitleKey.put(UTBETALING_UTBYTTE, "opplysninger.inntekt.inntekter.utbytte");
        soknadTypeToTitleKey.put(UTBETALING_SALG, "opplysninger.inntekt.inntekter.salg");
        soknadTypeToTitleKey.put(UTBETALING_FORSIKRING, "opplysninger.inntekt.inntekter.forsikringsutbetalinger");
        soknadTypeToTitleKey.put(SLUTTOPPGJOER, "opplysninger.arbeid.avsluttet");
        soknadTypeToTitleKey.put(UTGIFTER_STROM, "opplysninger.utgifter.boutgift.strom");
        soknadTypeToTitleKey.put(UTGIFTER_KOMMUNAL_AVGIFT, "opplysninger.utgifter.boutgift.kommunaleavgifter");
        soknadTypeToTitleKey.put(UTGIFTER_OPPVARMING, "opplysninger.utgifter.boutgift.oppvarming");
        soknadTypeToTitleKey.put(UTGIFTER_ANNET_BO, "opplysninger.utgifter.boutgift.andreutgifter");
        soknadTypeToTitleKey.put(UTGIFTER_BARN_FRITIDSAKTIVITETER, "opplysninger.utgifter.barn.fritidsaktivitet");
        soknadTypeToTitleKey.put(UTGIFTER_BARN_TANNREGULERING, "opplysninger.utgifter.barn.tannbehandling");
        soknadTypeToTitleKey.put(UTGIFTER_ANNET_BARN, "opplysninger.utgifter.barn.annet");
    }
}
