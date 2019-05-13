package no.nav.sbl.dialogarena.soknadinnsending.business.mappers;

import java.util.HashMap;
import java.util.Map;

public class FaktumNoklerOgBelopNavnMapper {
    public static final Map<String, String> soknadTypeToFaktumKey = new HashMap<>();
    public static final Map<String, String> soknadTypeToBelopNavn = new HashMap<>();
    public static final Map<String, String> soknadTypeToTittelDelNavn = new HashMap<>();

    static {
        soknadTypeToFaktumKey.put("husleie", "opplysninger.utgifter.boutgift.husleie");
        soknadTypeToFaktumKey.put("boliglanAvdrag", "opplysninger.utgifter.boutgift.avdraglaan");
        soknadTypeToFaktumKey.put("boliglanRenter", "opplysninger.utgifter.boutgift.avdraglaan");
        soknadTypeToFaktumKey.put("barnehage", "opplysninger.utgifter.barn.barnehage");
        soknadTypeToFaktumKey.put("sfo", "opplysninger.utgifter.barn.sfo");
        soknadTypeToFaktumKey.put("brukskonto", "opplysninger.inntekt.bankinnskudd.brukskonto");
        soknadTypeToFaktumKey.put("bsu", "opplysninger.inntekt.bankinnskudd.bsu");
        soknadTypeToFaktumKey.put("sparekonto", "opplysninger.inntekt.bankinnskudd.sparekonto");
        soknadTypeToFaktumKey.put("livsforsikringssparedel", "opplysninger.inntekt.bankinnskudd.livsforsikring");
        soknadTypeToFaktumKey.put("verdipapirer", "opplysninger.inntekt.bankinnskudd.aksjer");
        soknadTypeToFaktumKey.put("belop", "opplysninger.inntekt.bankinnskudd.annet");
        soknadTypeToFaktumKey.put("bolig", "inntekt.eierandeler.true.type.bolig");
        soknadTypeToFaktumKey.put("campingvogn", "inntekt.eierandeler.true.type.campingvogn");
        soknadTypeToFaktumKey.put("kjoretoy", "inntekt.eierandeler.true.type.kjoretoy");
        soknadTypeToFaktumKey.put("fritidseiendom", "inntekt.eierandeler.true.type.fritidseiendom");
        soknadTypeToFaktumKey.put("annet", "inntekt.eierandeler.true.type.annet");
        soknadTypeToFaktumKey.put("bostotte", "opplysninger.inntekt.bostotte");
        soknadTypeToFaktumKey.put("jobb", "opplysninger.arbeid.jobb");
        soknadTypeToFaktumKey.put("studielanOgStipend", "opplysninger.arbeid.student");
        soknadTypeToFaktumKey.put("utbytte", "opplysninger.inntekt.inntekter.utbytte");
        soknadTypeToFaktumKey.put("salg", "opplysninger.inntekt.inntekter.salg");
        soknadTypeToFaktumKey.put("forsikring", "opplysninger.inntekt.inntekter.forsikringsutbetalinger");
        soknadTypeToFaktumKey.put("sluttoppgjoer", "opplysninger.arbeid.avsluttet");
        soknadTypeToFaktumKey.put("strom", "opplysninger.utgifter.boutgift.strom");
        soknadTypeToFaktumKey.put("kommunalAvgift", "opplysninger.utgifter.boutgift.kommunaleavgifter");
        soknadTypeToFaktumKey.put("oppvarming", "opplysninger.utgifter.boutgift.oppvarming");
        soknadTypeToFaktumKey.put("annenBoutgift", "opplysninger.utgifter.boutgift.andreutgifter");
        soknadTypeToFaktumKey.put("barnFritidsaktiviteter", "opplysninger.utgifter.barn.fritidsaktivitet");
        soknadTypeToFaktumKey.put("barnTannregulering", "opplysninger.utgifter.barn.tannbehandling");
        soknadTypeToFaktumKey.put("annenBarneutgift", "opplysninger.utgifter.barn.annet");

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
