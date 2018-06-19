package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.InputSource;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;

import java.util.*;
import java.util.stream.Collectors;

public class JsonOkonomiOpplysningerConverter {


    public static JsonOkonomiopplysninger tilJsonOpplysninger(InputSource inputSource) {

        WebSoknad webSoknad = inputSource.getWebSoknad();

        return new JsonOkonomiopplysninger()
                .withUtbetaling(tilJsonOkonomiopplysningerUtbetaling(inputSource))
                .withUtgift(tilJsonOkonomiopplysningerUtgift(inputSource))
                .withBekreftelse(tilJsonOkonomiopplysningerBekreftelse(inputSource))
                .withBeskrivelseAvAnnet(tilJsonOkonomiopplysningerBeskrivelseAvAnnet(webSoknad))
                ;
    }


    private static JsonOkonomibeskrivelserAvAnnet tilJsonOkonomiopplysningerBeskrivelseAvAnnet(WebSoknad webSoknad) {

        final JsonOkonomibeskrivelserAvAnnet jsonOkonomibeskrivelserAvAnnet = new JsonOkonomibeskrivelserAvAnnet().withKilde(JsonKildeBruker.BRUKER);

        // 6 - Eier du noe med økonomisk verdi? Annet
        jsonOkonomibeskrivelserAvAnnet.setVerdi(webSoknad.getValueForFaktum("inntekt.eierandeler.true.type.annet.true.beskrivelse"));

        // 6 - Hva har du av innskudd og/eller sparing? Annet
        jsonOkonomibeskrivelserAvAnnet.setSparing(webSoknad.getValueForFaktum("inntekt.bankinnskudd.true.type.annet.true.beskrivelse"));

        // 6 - Har du de siste tre månedene fått utbetalt penger som ikke er lønn og/eller penger fra NAV? Annet
        jsonOkonomibeskrivelserAvAnnet.setUtbetaling(webSoknad.getValueForFaktum("inntekt.inntekter.true.type.annet.true.beskrivelse"));

        // 7 - Har du boutgifter? Andre utgifter
        jsonOkonomibeskrivelserAvAnnet.setBoutgifter(webSoknad.getValueForFaktum("utgifter.boutgift.true.type.andreutgifter.true.beskrivelse"));

        //7 Har du utgifter til barn? Annet
        jsonOkonomibeskrivelserAvAnnet.setBarneutgifter(webSoknad.getValueForFaktum("utgifter.barn.true.utgifter.annet.true.beskrivelse"));

        return jsonOkonomibeskrivelserAvAnnet;
    }

    private static List<JsonOkonomibekreftelse> tilJsonOkonomiopplysningerBekreftelse(InputSource inputSource) {

        final List<JsonOkonomibekreftelse> result = new ArrayList<>();
        final WebSoknad webSoknad = inputSource.getWebSoknad();
        final NavMessageSource navMessageSource = inputSource.getMessageSource();

        String key = "inntekt.bostotte";
        result.add(opplysningBekreftelse("bostotte", getTittel(key, navMessageSource), webSoknad.getFaktumMedKey(key)));

        key = "inntekt.eierandeler";
        result.add(opplysningBekreftelse("verdi", getTittel(key, navMessageSource), webSoknad.getFaktumMedKey(key)));

        key = "inntekt.bankinnskudd";
        result.add(opplysningBekreftelse("sparing", getTittel(key, navMessageSource), webSoknad.getFaktumMedKey(key)));

        key = "inntekt.inntekter";
        result.add(opplysningBekreftelse("utbetaling", getTittel(key, navMessageSource), webSoknad.getFaktumMedKey(key)));

        key = "utgifter.boutgift";
        result.add(opplysningBekreftelse("boutgifter", getTittel(key, navMessageSource), webSoknad.getFaktumMedKey(key)));

        key = "utgifter.barn";
        result.add(opplysningBekreftelse("barneutgifter", getTittel(key, navMessageSource), webSoknad.getFaktumMedKey(key)));

        return result.stream().filter(r -> r != null).collect(Collectors.toList());

    }

    private static List<JsonOkonomiOpplysningUtbetaling> tilJsonOkonomiopplysningerUtbetaling(InputSource inputSource) {

        final List<JsonOkonomiOpplysningUtbetaling> result = new ArrayList<>();
        final WebSoknad webSoknad = inputSource.getWebSoknad();
        final NavMessageSource navMessageSource = inputSource.getMessageSource();

        String key = "opplysninger.inntekt.inntekter.utbytte";
        result.addAll(opplysningUtbetaling("utbytte",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sum"));

        key = "opplysninger.inntekt.inntekter.salg";
        result.addAll(opplysningUtbetaling("salg",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sum"));

        key = "opplysninger.inntekt.inntekter.forsikringsutbetalinger";
        result.addAll(opplysningUtbetaling("forsikring",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sum"));

        key = "opplysninger.inntekt.inntekter.annet";
        result.addAll(opplysningUtbetaling("annen",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sum"));


        key = "opplysninger.arbeid.avsluttet";
        result.addAll(opplysningUtbetaling("sluttoppgjoer",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "netto"));

        key = "utbetalinger.utbetaling";
        result.addAll(opplysningUtbetalingFraNav(webSoknad.getFaktaMedKey(key)));

        return result.stream().filter(r -> r != null).collect(Collectors.toList());

    }

    private static List<JsonOkonomiOpplysningUtgift> tilJsonOkonomiopplysningerUtgift(InputSource inputSource) {
        final List<JsonOkonomiOpplysningUtgift> result = new ArrayList<>();
        final WebSoknad webSoknad = inputSource.getWebSoknad();
        final NavMessageSource navMessageSource = inputSource.getMessageSource();


        String key = "opplysninger.utgifter.boutgift.strom";
        result.addAll(opplysningUtgift("strom", getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sisteregning"));

        key = "opplysninger.utgifter.boutgift.kommunaleavgifter";
        result.addAll(opplysningUtgift("kommunalAvgift", getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sisteregning"));

        key = "opplysninger.utgifter.boutgift.oppvarming";
        result.addAll(opplysningUtgift("oppvarming", getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sisteregning"));

        key = "opplysninger.utgifter.boutgift.andreutgifter";
        result.addAll(opplysningUtgift("annenBoutgift", getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sisteregning", "type"));

        key = "opplysninger.utgifter.barn.fritidsaktivitet";
        result.addAll(opplysningUtgift("barnFritidsaktiviteter", getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sisteregning", "type"));

        key = "opplysninger.utgifter.barn.tannbehandling";
        result.addAll(opplysningUtgift("barnTannregulering", getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sisteregning"));

        key = "opplysninger.utgifter.barn.annet";
        result.addAll(opplysningUtgift("annenBarneutgift", getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sisteregning", "type"));

        key = "opplysninger.ekstrainfo.utgifter";
        result.addAll(opplysningUtgift("annen", getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "utgift", "beskrivelse"));

        return result.stream().filter(r -> r != null).collect(Collectors.toList());
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

    private static List<JsonOkonomiOpplysningUtbetaling> opplysningUtbetalingFraNav(List<Faktum> fakta) {
        return fakta.stream().filter(f -> f != null).map(faktum -> {
            final Map<String, String> properties = faktum.getProperties();
            return new JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.SYSTEM)
                    .withType("navytelse")
                    .withTittel(properties.get("type"))
                    .withBelop(JsonUtils.tilIntegerMedAvrunding(properties.get("netto")))
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

    private static String getTittel(String key, NavMessageSource navMessageSource) {
        Properties properties = navMessageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));

        return properties.getProperty("json.okonomi." + key);

    }


}
