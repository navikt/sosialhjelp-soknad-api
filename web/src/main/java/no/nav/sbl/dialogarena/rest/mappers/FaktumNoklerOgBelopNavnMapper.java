package no.nav.sbl.dialogarena.rest.mappers;

import java.util.HashMap;
import java.util.Map;

public class FaktumNoklerOgBelopNavnMapper {
    public static final Map<String, String> jsonTypeToFaktumKey = new HashMap<>();
    public static final Map<String, String> jsonTypeToBelopNavn = new HashMap<>();
    public static final Map<String, String> jsonTypeToTittelDelNavn = new HashMap<>();

    static {
        jsonTypeToFaktumKey.put("husleie", "opplysninger.utgifter.boutgift.husleie");
        jsonTypeToFaktumKey.put("boliglanAvdrag", "opplysninger.utgifter.boutgift.avdraglaan");
        jsonTypeToFaktumKey.put("boliglanRenter", "opplysninger.utgifter.boutgift.avdraglaan");
        jsonTypeToFaktumKey.put("barnehage", "opplysninger.utgifter.barn.barnehage");
        jsonTypeToFaktumKey.put("sfo", "opplysninger.utgifter.barn.sfo");
        jsonTypeToFaktumKey.put("barnebidrag", "opplysninger.familiesituasjon.barnebidrag.betaler");
        jsonTypeToFaktumKey.put("brukskonto", "opplysninger.inntekt.bankinnskudd.brukskonto");
        jsonTypeToFaktumKey.put("bsu", "opplysninger.inntekt.bankinnskudd.bsu");
        jsonTypeToFaktumKey.put("sparekonto", "opplysninger.inntekt.bankinnskudd.sparekonto");
        jsonTypeToFaktumKey.put("livsforsikringssparedel", "opplysninger.inntekt.bankinnskudd.livsforsikring");
        jsonTypeToFaktumKey.put("verdipapirer", "opplysninger.inntekt.bankinnskudd.aksjer");
        jsonTypeToFaktumKey.put("belop", "opplysninger.inntekt.bankinnskudd.annet");
        jsonTypeToFaktumKey.put("bolig", "inntekt.eierandeler.true.type.bolig");
        jsonTypeToFaktumKey.put("campingvogn", "inntekt.eierandeler.true.type.campingvogn");
        jsonTypeToFaktumKey.put("kjoretoy", "inntekt.eierandeler.true.type.kjoretoy");
        jsonTypeToFaktumKey.put("fritidseiendom", "inntekt.eierandeler.true.type.fritidseiendom");
        jsonTypeToFaktumKey.put("annet", "inntekt.eierandeler.true.type.annet");
        jsonTypeToFaktumKey.put("bostotte", "opplysninger.inntekt.bostotte");
        jsonTypeToFaktumKey.put("jobb", "opplysninger.arbeid.jobb");
        jsonTypeToFaktumKey.put("studielanOgStipend", "opplysninger.arbeid.student");
        jsonTypeToFaktumKey.put("utbytte", "opplysninger.inntekt.inntekter.utbytte");
        jsonTypeToFaktumKey.put("salg", "opplysninger.inntekt.inntekter.salg");
        jsonTypeToFaktumKey.put("forsikring", "opplysninger.inntekt.inntekter.forsikringsutbetalinger");
        jsonTypeToFaktumKey.put("sluttoppgjoer", "opplysninger.arbeid.avsluttet");
        jsonTypeToFaktumKey.put("strom", "opplysninger.utgifter.boutgift.strom");
        jsonTypeToFaktumKey.put("kommunalAvgift", "opplysninger.utgifter.boutgift.kommunaleavgifter");
        jsonTypeToFaktumKey.put("oppvarming", "opplysninger.utgifter.boutgift.oppvarming");
        jsonTypeToFaktumKey.put("annenBoutgift", "opplysninger.utgifter.boutgift.andreutgifter");
        jsonTypeToFaktumKey.put("barnFritidsaktiviteter", "opplysninger.utgifter.barn.fritidsaktivitet");
        jsonTypeToFaktumKey.put("barnTannregulering", "opplysninger.utgifter.barn.tannbehandling");
        jsonTypeToFaktumKey.put("annenBarneutgift", "opplysninger.utgifter.barn.annet");

        jsonTypeToBelopNavn.put("husleie", "permnd");
        jsonTypeToBelopNavn.put("boliglanAvdrag", "avdrag");
        jsonTypeToBelopNavn.put("boliglanRenter", "renter");
        jsonTypeToBelopNavn.put("barnehage", "sistemnd");
        jsonTypeToBelopNavn.put("sfo", "sistemnd");
        jsonTypeToBelopNavn.put("barnebidrag", "betaler");
        jsonTypeToBelopNavn.put("brukskonto", "saldo");
        jsonTypeToBelopNavn.put("bsu", "saldo");
        jsonTypeToBelopNavn.put("sparekonto", "saldo");
        jsonTypeToBelopNavn.put("livsforsikringssparedel", "saldo");
        jsonTypeToBelopNavn.put("verdipapirer", "saldo");
        jsonTypeToBelopNavn.put("belop", "saldo");
        jsonTypeToBelopNavn.put("bostotte", "utbetaling");
        jsonTypeToBelopNavn.put("jobb", "bruttolonn");
        jsonTypeToBelopNavn.put("studielanOgStipend", "utbetaling");
        jsonTypeToBelopNavn.put("utbytte", "sum");
        jsonTypeToBelopNavn.put("salg", "sum");
        jsonTypeToBelopNavn.put("forsikring", "sum");
        jsonTypeToBelopNavn.put("sluttoppgjoer", "netto");
        jsonTypeToBelopNavn.put("strom", "sisteregning");
        jsonTypeToBelopNavn.put("kommunalAvgift", "sisteregning");
        jsonTypeToBelopNavn.put("oppvarming", "sisteregning");
        jsonTypeToBelopNavn.put("annenBoutgift", "sisteregning");
        jsonTypeToBelopNavn.put("barnFritidsaktiviteter", "sisteregning");
        jsonTypeToBelopNavn.put("barnTannregulering", "sisteregning");
        jsonTypeToBelopNavn.put("annenBarneutgift", "sisteregning");

        jsonTypeToTittelDelNavn.put("annenBoutgift", "type");
        jsonTypeToTittelDelNavn.put("barnFritidsaktiviteter", "type");
        jsonTypeToTittelDelNavn.put("annenBarneutgift", "type");
        jsonTypeToTittelDelNavn.put("annen", "beskrivelse");
    }
}
