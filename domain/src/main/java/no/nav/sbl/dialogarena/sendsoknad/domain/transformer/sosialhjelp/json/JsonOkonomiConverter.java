package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonOkonomiConverter {
    public static JsonOkonomi tilOkonomi(WebSoknad webSoknad) {

        final JsonOkonomi okonomi = new JsonOkonomi();

        okonomi.setOpplysninger(tilJsonOpplysninger(webSoknad));
        okonomi.setOversikt(tilJsonOversikt(webSoknad));

        return okonomi;
    }

    private static JsonOkonomioversikt tilJsonOversikt(WebSoknad webSoknad) {

        return new JsonOkonomioversikt()

                .withInntekt(tilJsonOkonomioversiktInntekt(webSoknad))
                .withUtgift(tilJsonOkonomioversiktUtgift(webSoknad))
                .withFormue(tilJsonOkonomioversiktFormue(webSoknad))
                ;
    }

    private static JsonOkonomiopplysninger tilJsonOpplysninger(WebSoknad webSoknad) {

        return new JsonOkonomiopplysninger()
                .withUtbetaling(tilJsonOkonomiopplysningerUtbetaling(webSoknad))
                .withUtgift(tilJsonOkonomiopplysningerUtgift(webSoknad))
                .withBekreftelse(tilJsonOkonomiopplysningerBekreftelse(webSoknad))
                .withBeskrivelseAvAnnet(tilJsonOkonomiopplysningerBeskrivelseAvAnnet(webSoknad))
                ;
    }

    private static List<JsonOkonomioversiktUtgift> tilJsonOkonomioversiktUtgift(WebSoknad webSoknad) {

        final List<JsonOkonomioversiktUtgift> result = new ArrayList<>();

        result.addAll(oversiktUtgift("husleie",
                "Husleie",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.husleie"),
                "permnd"));

        result.addAll(oversiktUtgift("boliglanAvdrag",
                "Avdrag på boliglån",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.avdraglaan"),
                "avdrag"));

        result.addAll(oversiktUtgift("boliglanRenter",
                "Renter på boliglån",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.avdraglaan"),
                "renter"));

        result.addAll(oversiktUtgift("barnehage",
                "Barnehage",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.barn.barnehage"),
                "sistemnd"));

        result.addAll(oversiktUtgift("sfo",
                "SFO",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.barn.sfo"),
                "sistemnd"));

        return result.stream().filter(r -> r != null).collect(Collectors.toList());
    }

    private static JsonOkonomibeskrivelserAvAnnet tilJsonOkonomiopplysningerBeskrivelseAvAnnet(WebSoknad webSoknad) {

        final JsonOkonomibeskrivelserAvAnnet jsonOkonomibeskrivelserAvAnnet = new JsonOkonomibeskrivelserAvAnnet().withKilde(JsonKildeBruker.BRUKER);

        // 6 - Eier du noe med økonomisk verdi? Annet
        jsonOkonomibeskrivelserAvAnnet.setVerdi(webSoknad.getValueForFaktum("inntekt.eierandeler.true.type.annet.true.beskrivelse"));

        // 6 - Hva har du av innskudd og/eller sparing? Annet
        jsonOkonomibeskrivelserAvAnnet.setSparing(webSoknad.getValueForFaktum( "inntekt.bankinnskudd.true.type.annet.true.beskrivelse"));

        // 6 - Har du de siste tre månedene fått utbetalt penger som ikke er lønn og/eller penger fra NAV? Annet
        jsonOkonomibeskrivelserAvAnnet.setUtbetaling(webSoknad.getValueForFaktum( "inntekt.inntekter.true.type.annet.true.beskrivelse"));

        // 7 - Har du boutgifter? Andre utgifter
        jsonOkonomibeskrivelserAvAnnet.setBoutgifter(webSoknad.getValueForFaktum("utgifter.boutgift.true.type.andreutgifter.true.beskrivelse"));

        //7 Har du utgifter til barn? Annet
        jsonOkonomibeskrivelserAvAnnet.setBarneutgifter(webSoknad.getValueForFaktum("utgifter.barn.true.utgifter.annet.true.beskrivelse"));

        return jsonOkonomibeskrivelserAvAnnet;
    }

    private static List<JsonOkonomibekreftelse> tilJsonOkonomiopplysningerBekreftelse(WebSoknad webSoknad) {

        final List<JsonOkonomibekreftelse> result = new ArrayList<>();

        result.add(opplysningBekreftelse("bostotte", "Søkt eller mottatt bostøtte fra Husbanken.", webSoknad.getFaktumMedKey("inntekt.bostotte")));

        result.add(opplysningBekreftelse("verdi", "Eier noe av økonomisk verdi.", webSoknad.getFaktumMedKey("inntekt.eierandeler")));
        result.add(opplysningBekreftelse("sparing", "Bankinnskudd eller annen sparing.", webSoknad.getFaktumMedKey("inntekt.bankinnskudd")));

        result.add(opplysningBekreftelse("utbetaling", "Annen utbetaling", webSoknad.getFaktumMedKey("inntekt.inntekter")));

        result.add(opplysningBekreftelse("boutgifter", "Boutgifter", webSoknad.getFaktumMedKey("utgifter.boutgift")));

        result.add(opplysningBekreftelse("barneutgifter", "Utgifter til barn", webSoknad.getFaktumMedKey("utgifter.barn")));

        return result.stream().filter(r -> r != null).collect(Collectors.toList());

    }

    private static List<JsonOkonomiOpplysningUtbetaling> tilJsonOkonomiopplysningerUtbetaling(WebSoknad webSoknad) {

        final List<JsonOkonomiOpplysningUtbetaling> result = new ArrayList<>();
        result.addAll(opplysningUtbetaling("utbytte",
                "Utbytte fra aksjer, obligasjoner eller fond",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.inntekter.utbytte"),
                "sum"));

        result.addAll(opplysningUtbetaling("salg",
                "Solgt eiendom og/eller eiendel",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.inntekter.salg"),
                "sum"));

        result.addAll(opplysningUtbetaling("forsikring",
                "Forsikringsutbetaling",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.inntekter.forsikringsutbetalinger"),
                "sum"));

        result.addAll(opplysningUtbetaling("annen",
                "Annen utbetaling",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.inntekter.annet"),
                "sum"));

        return result.stream().filter(r -> r != null).collect(Collectors.toList());

    }

    private static List<JsonOkonomioversiktFormue> tilJsonOkonomioversiktFormue(WebSoknad webSoknad) {

        final List<JsonOkonomioversiktFormue> result = new ArrayList<>();
        result.addAll(oversiktFormue("brukskonto",
                "Brukskonto",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.bankinnskudd.brukskonto"),
                "saldo"));

        result.addAll(oversiktFormue("bsu",
                "BSU",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.bankinnskudd.bsu"),
                "saldo"));

        result.addAll(oversiktFormue("sparekonto",
                "Sparekonto",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.bankinnskudd.sparekonto"),
                "saldo"));

        result.addAll(oversiktFormue("livsforsikringssparedel",
                "Livsforsikringssparedel",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.bankinnskudd.livsforsikring"),
                "saldo"));

        result.addAll(oversiktFormue("verdipapirer",
                "Aksjer, obligasjoner eller fond",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.bankinnskudd.aksjer"),
                "saldo"));

        result.addAll(oversiktFormue("belop",
                "Annen form for sparing",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.bankinnskudd.annet"),
                "saldo"));

        return result.stream().filter(r -> r != null).collect(Collectors.toList());
    }

    private static List<JsonOkonomiOpplysningUtgift> tilJsonOkonomiopplysningerUtgift(WebSoknad webSoknad) {
        final List<JsonOkonomiOpplysningUtgift> result = new ArrayList<>();

        result.addAll(opplysningUtgift("strom",
                "Strøm (siste regning)",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.strom"),
                "sisteregning"));

        result.addAll(opplysningUtgift("kommunalAvgift",
                "Kommunal avgift (siste regning)",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.kommunaleavgifter"),
                "sisteregning"));

        result.addAll(opplysningUtgift("oppvarming",
                "Oppvarming (siste regning)",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.oppvarming"),
                "sisteregning"));

        result.addAll(opplysningUtgift("annenBoutgift",
                "Annen, bo (brukerangitt): ",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.andreutgifter"),
                "sisteregning", "type"));

        result.addAll(opplysningUtgift("barnFritidsaktiviteter",
                "Fritidsaktiviteter for barn (siste regning): ",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.barn.fritidsaktivitet"),
                "sisteregning", "type"));

        result.addAll(opplysningUtgift("barnTannregulering",
                "Tannregulering for barn (siste regning)",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.barn.tannbehandling"),
                "sisteregning"));

        result.addAll(opplysningUtgift("annenBarneutgift",
                "Annen, barn(brukerangitt): ",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.barn.annet"),
                "sisteregning", "type"));

        result.addAll(opplysningUtgift("annen",
                "Annen (brukerangitt): ",
                webSoknad.getFaktaMedKey("opplysninger.ekstrainfo.utgifter"),
                "utgift", "beskrivelse"));

        return result.stream().filter(r -> r != null).collect(Collectors.toList());
    }

    private static final <E> void addIfNotNull(List<? super E> liste, E... elementer) {
        for (E e : elementer) {
            if (e != null) {
                liste.add(e);
            }
        }
    }

    private static List<JsonOkonomioversiktInntekt> tilJsonOkonomioversiktInntekt(WebSoknad webSoknad) {
        final List<JsonOkonomioversiktInntekt> result = new ArrayList<>();

        result.addAll(oversiktInntekt("bostotte",
                "Bostøtte",
                webSoknad.getFaktaMedKey("opplysninger.inntekt.bostotte"),
                "utbetaling"));
        result.addAll(oversiktInntekt("jobb",
                "Lønnsinntekt",
                webSoknad.getFaktaMedKey("opplysninger.arbeid.jobb"),
                "bruttolonn",
                "nettolonn"
        ));
        result.addAll(oversiktInntekt("studielanOgStipend",
                "Studielån og -stipend",
                webSoknad.getFaktaMedKey("opplysninger.arbeid.student"),
                "utbetaling"
        ));
        result.addAll(oversiktInntekt("barnebidrag",
                "Barnebidrag",
                webSoknad.getFaktaMedKey("opplysninger.familiesituasjon.barnebidrag.betaler"),
                "betaler"

        ));
        return result.stream().filter(r -> r != null).collect(Collectors.toList());
    }

    private static List<JsonOkonomioversiktInntekt> oversiktInntekt(String type, String tittel, List<Faktum> fakta, String belopNavn) {
        return oversiktInntekt(type, tittel, fakta, belopNavn, belopNavn);
    }

    private static List<JsonOkonomioversiktInntekt> oversiktInntekt(String type, String tittel, List<Faktum> fakta, String bruttoNavn, String nettoNavn) {
        return fakta.stream().filter(f -> f != null).map(faktum -> {
            final Map<String, String> properties = faktum.getProperties();
            return new JsonOkonomioversiktInntekt()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withBrutto(JsonUtils.tilInteger(properties.get(bruttoNavn)))
                    .withNetto(JsonUtils.tilInteger(properties.get(nettoNavn)))
                    .withOverstyrtAvBruker(false);
        }).collect(Collectors.toList());
    }

    private static List<JsonOkonomioversiktFormue> oversiktFormue(String type, String tittel, List<Faktum> fakta, String belopNavn) {
        return fakta.stream().filter(f -> f != null).map(faktum -> {
            final Map<String, String> properties = faktum.getProperties();
            return new JsonOkonomioversiktFormue()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withBelop(JsonUtils.tilInteger(properties.get(belopNavn)))
                    .withOverstyrtAvBruker(false);
        }).collect(Collectors.toList());
    }

    private static List<JsonOkonomiOpplysningUtbetaling> opplysningUtbetaling(String type, String tittel, List<Faktum> fakta, String belopNavn) {
        return fakta.stream().filter(f -> f != null).map(faktum -> {
            final Map<String, String> properties = faktum.getProperties();
            return new JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withBelop(JsonUtils.tilInteger(properties.get(belopNavn)))
                    .withOverstyrtAvBruker(false);
        }).collect(Collectors.toList());
    }

    private static Collection<? extends JsonOkonomiOpplysningUtgift> opplysningUtgift(String type, String tittel, List<Faktum> fakta, String belopNavn) {
        return opplysningUtgift(type, tittel, fakta, belopNavn, null);
    }

    private static Collection<? extends JsonOkonomiOpplysningUtgift> opplysningUtgift(String type, String tittel, List<Faktum> fakta, String belopNavn, String tittelDelNavn) {

        return fakta.stream().filter(f -> f != null).map(faktum -> {
            final Map<String, String> properties = faktum.getProperties();
            final String tittelDel;
            if (tittelDelNavn != null) {
                final String t = properties.get(tittelDelNavn);
                tittelDel = (t != null) ? t : "";
            } else {
                tittelDel = "";
            }
            return new JsonOkonomiOpplysningUtgift()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel + tittelDel)
                    .withBelop(JsonUtils.tilInteger(properties.get(belopNavn)))
                    .withOverstyrtAvBruker(false);
        }).collect(Collectors.toList());
    }

    private static Collection<? extends JsonOkonomioversiktUtgift> oversiktUtgift(String type, String tittel, List<Faktum> fakta, String belopnavn) {
        return fakta.stream().filter(f -> f != null).map(faktum -> {
            final Map<String, String> properties = faktum.getProperties();
            return new JsonOkonomioversiktUtgift()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withBelop(JsonUtils.tilInteger(properties.get(belopnavn)))
                    .withOverstyrtAvBruker(false);
        }).collect(Collectors.toList());
    }


    private static JsonOkonomibekreftelse opplysningBekreftelse(String type, String tittel, Faktum faktum) {
        if (faktum == null || faktum.getValue() == null) {
            return null;
        }
        return new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withTittel(tittel)
                .withType(type)
                .withVerdi(Boolean.parseBoolean(faktum.getValue()));
    }
}

