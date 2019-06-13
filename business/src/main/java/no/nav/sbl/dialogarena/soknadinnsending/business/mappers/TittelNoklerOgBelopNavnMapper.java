package no.nav.sbl.dialogarena.soknadinnsending.business.mappers;

import java.util.HashMap;
import java.util.Map;

public class TittelNoklerOgBelopNavnMapper {
    public static final Map<String, String> soknadTypeToTittelKey = new HashMap<>();
    public static final Map<String, String> soknadTypeToBelopNavn = new HashMap<>();
    public static final Map<String, String> soknadTypeToTittelDelNavn = new HashMap<>();

    static {
        soknadTypeToTittelKey.put("husleie", "opplysninger.utgifter.boutgift.husleie");
        soknadTypeToTittelKey.put("boliglanAvdrag", "opplysninger.utgifter.boutgift.avdraglaan");
        soknadTypeToTittelKey.put("boliglanRenter", "opplysninger.utgifter.boutgift.avdraglaan");
        soknadTypeToTittelKey.put("barnehage", "opplysninger.utgifter.barn.barnehage");
        soknadTypeToTittelKey.put("sfo", "opplysninger.utgifter.barn.sfo");
        soknadTypeToTittelKey.put("brukskonto", "opplysninger.inntekt.bankinnskudd.brukskonto");
        soknadTypeToTittelKey.put("bsu", "opplysninger.inntekt.bankinnskudd.bsu");
        soknadTypeToTittelKey.put("sparekonto", "opplysninger.inntekt.bankinnskudd.sparekonto");
        soknadTypeToTittelKey.put("livsforsikringssparedel", "opplysninger.inntekt.bankinnskudd.livsforsikring");
        soknadTypeToTittelKey.put("verdipapirer", "opplysninger.inntekt.bankinnskudd.aksjer");
        soknadTypeToTittelKey.put("belop", "opplysninger.inntekt.bankinnskudd.annet");
        soknadTypeToTittelKey.put("bolig", "inntekt.eierandeler.true.type.bolig");
        soknadTypeToTittelKey.put("campingvogn", "inntekt.eierandeler.true.type.campingvogn");
        soknadTypeToTittelKey.put("kjoretoy", "inntekt.eierandeler.true.type.kjoretoy");
        soknadTypeToTittelKey.put("fritidseiendom", "inntekt.eierandeler.true.type.fritidseiendom");
        soknadTypeToTittelKey.put("annet", "inntekt.eierandeler.true.type.annet");
        soknadTypeToTittelKey.put("bostotte", "opplysninger.inntekt.bostotte");
        soknadTypeToTittelKey.put("jobb", "opplysninger.arbeid.jobb");
        soknadTypeToTittelKey.put("studielanOgStipend", "opplysninger.arbeid.student");
        soknadTypeToTittelKey.put("utbytte", "opplysninger.inntekt.inntekter.utbytte");
        soknadTypeToTittelKey.put("salg", "opplysninger.inntekt.inntekter.salg");
        soknadTypeToTittelKey.put("forsikring", "opplysninger.inntekt.inntekter.forsikringsutbetalinger");
        soknadTypeToTittelKey.put("sluttoppgjoer", "opplysninger.arbeid.avsluttet");
        soknadTypeToTittelKey.put("strom", "opplysninger.utgifter.boutgift.strom");
        soknadTypeToTittelKey.put("kommunalAvgift", "opplysninger.utgifter.boutgift.kommunaleavgifter");
        soknadTypeToTittelKey.put("oppvarming", "opplysninger.utgifter.boutgift.oppvarming");
        soknadTypeToTittelKey.put("annenBoutgift", "opplysninger.utgifter.boutgift.andreutgifter");
        soknadTypeToTittelKey.put("barnFritidsaktiviteter", "opplysninger.utgifter.barn.fritidsaktivitet");
        soknadTypeToTittelKey.put("barnTannregulering", "opplysninger.utgifter.barn.tannbehandling");
        soknadTypeToTittelKey.put("annenBarneutgift", "opplysninger.utgifter.barn.annet");

        soknadTypeToBelopNavn.put("husleie", "permnd");
        soknadTypeToBelopNavn.put("boliglanAvdrag", "avdrag");
        soknadTypeToBelopNavn.put("boliglanRenter", "renter");
        soknadTypeToBelopNavn.put("barnehage", "sistemnd");
        soknadTypeToBelopNavn.put("sfo", "sistemnd");
        soknadTypeToBelopNavn.put("brukskonto", "saldo");
        soknadTypeToBelopNavn.put("bsu", "saldo");
        soknadTypeToBelopNavn.put("sparekonto", "saldo");
        soknadTypeToBelopNavn.put("livsforsikringssparedel", "saldo");
        soknadTypeToBelopNavn.put("verdipapirer", "saldo");
        soknadTypeToBelopNavn.put("belop", "saldo");
        soknadTypeToBelopNavn.put("bostotte", "utbetaling");
        soknadTypeToBelopNavn.put("jobb", "bruttolonn");
        soknadTypeToBelopNavn.put("studielanOgStipend", "utbetaling");
        soknadTypeToBelopNavn.put("utbytte", "sum");
        soknadTypeToBelopNavn.put("salg", "sum");
        soknadTypeToBelopNavn.put("forsikring", "sum");
        soknadTypeToBelopNavn.put("sluttoppgjoer", "netto");
        soknadTypeToBelopNavn.put("strom", "sisteregning");
        soknadTypeToBelopNavn.put("kommunalAvgift", "sisteregning");
        soknadTypeToBelopNavn.put("oppvarming", "sisteregning");
        soknadTypeToBelopNavn.put("annenBoutgift", "sisteregning");
        soknadTypeToBelopNavn.put("barnFritidsaktiviteter", "sisteregning");
        soknadTypeToBelopNavn.put("barnTannregulering", "sisteregning");
        soknadTypeToBelopNavn.put("annenBarneutgift", "sisteregning");

        soknadTypeToTittelDelNavn.put("annenBoutgift", "type");
        soknadTypeToTittelDelNavn.put("barnFritidsaktiviteter", "type");
        soknadTypeToTittelDelNavn.put("annenBarneutgift", "type");
        soknadTypeToTittelDelNavn.put("annen", "beskrivelse");
    }
}
