package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
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

    private static JsonOkonomibeskrivelserAvAnnet tilJsonOkonomiopplysningerBeskrivelseAvAnnet(WebSoknad webSoknad) {
       return null;

    }

    private static List<JsonOkonomibekreftelse> tilJsonOkonomiopplysningerBekreftelse(WebSoknad webSoknad) {

        final List<JsonOkonomibekreftelse> result = new ArrayList<>();

        return result.stream().filter(r -> r != null).collect(Collectors.toList());

    }

    private static List<JsonOkonomiOpplysningUtgift> tilJsonOkonomiopplysningerUtgift(WebSoknad webSoknad) {

        final List<JsonOkonomiOpplysningUtgift> result = new ArrayList<>();

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

    private static List<JsonOkonomioversiktUtgift> tilJsonOkonomioversiktUtgift(WebSoknad webSoknad) {
        final List<JsonOkonomioversiktUtgift> result = new ArrayList<>();
        result.addAll(oversiktUtgift("husleie",
                "Husleie",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.husleie"),
                "permnd"));

        result.addAll(oversiktUtgift("strom",
                "Strøm (siste regning)",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.strom"),
                "sisteregning"));

        result.addAll(oversiktUtgift("kommunalAvgift",
                "Kommunal avgift (siste regning)",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.kommunaleavgifter"),
                "sisteregning"));

        result.addAll(oversiktUtgift("oppvarming",
                "Oppvarming (siste regning)",
                webSoknad.getFaktaMedKey("opplysninger.utgifter.boutgift.oppvarming"),
                "sisteregning"));

        Jeg kom hit
                /Du har svart at du har oppvarming som boutgift, vi ber deg derfor oppgi



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

        return fakta.stream().filter(f -> f != null).map(faktum -> {
            final Map<String, String> properties = faktum.getProperties();
            return new JsonOkonomiOpplysningUtgift()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withBelop(JsonUtils.tilInteger(properties.get(belopNavn)))
                    .withOverstyrtAvBruker(false);
        }).collect(Collectors.toList());


    }

    private static Collection<? extends JsonOkonomioversiktUtgift> oversiktUtgift(String type, String tittel, List<Faktum> fakta, String belopNavn) {

        return fakta.stream().filter(f -> f != null).map(faktum -> {
            final Map<String, String> properties = faktum.getProperties();
            return new JsonOkonomioversiktUtgift()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withBelop(JsonUtils.tilInteger(properties.get(belopNavn)))
                    .withOverstyrtAvBruker(false);
        }).collect(Collectors.toList());


    }




}

