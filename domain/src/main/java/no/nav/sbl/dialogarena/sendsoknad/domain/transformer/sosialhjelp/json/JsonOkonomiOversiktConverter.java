package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.InputSource;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;

import java.util.*;
import java.util.stream.Collectors;

public class JsonOkonomiOversiktConverter {

    public static JsonOkonomioversikt tilJsonOversikt(InputSource inputSource) {

        WebSoknad webSoknad = inputSource.getWebSoknad();

        return new JsonOkonomioversikt()

                .withInntekt(tilJsonOkonomioversiktInntekt(inputSource))
                .withUtgift(tilJsonOkonomioversiktUtgift(inputSource))
                .withFormue(tilJsonOkonomioversiktFormue(inputSource))
                ;
    }


    private static List<JsonOkonomioversiktUtgift> tilJsonOkonomioversiktUtgift(InputSource inputSource) {

        final WebSoknad webSoknad = inputSource.getWebSoknad();
        final NavMessageSource navMessageSource = inputSource.getMessageSource();

        final List<JsonOkonomioversiktUtgift> result = new ArrayList<>();

        String key = "opplysninger.utgifter.boutgift.husleie";
        result.addAll(oversiktUtgift("husleie",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "permnd"));

        key = "opplysninger.utgifter.boutgift.avdraglaan";
        result.addAll(oversiktUtgift("boliglanAvdrag",
                getTittel(key + ".boliglanAvdrag", navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "avdrag"));

        key = "opplysninger.utgifter.boutgift.avdraglaan";
        result.addAll(oversiktUtgift("boliglanRenter",
                getTittel(key + ".boliglanRenter", navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "renter"));

        key = "opplysninger.utgifter.barn.barnehage";
        result.addAll(oversiktUtgift("barnehage",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sistemnd"));

        key = "opplysninger.utgifter.barn.sfo";
        result.addAll(oversiktUtgift("sfo",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "sistemnd"));

        key = "opplysninger.familiesituasjon.barnebidrag.betaler";
        result.addAll(oversiktUtgift("barnebidrag",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "betaler"

        ));

        return result.stream().filter(r -> r != null).collect(Collectors.toList());
    }


    private static List<JsonOkonomioversiktFormue> tilJsonOkonomioversiktFormue(InputSource inputSource) {

        final List<JsonOkonomioversiktFormue> result = new ArrayList<>();
        final WebSoknad webSoknad = inputSource.getWebSoknad();
        final NavMessageSource navMessageSource = inputSource.getMessageSource();


        String key = "opplysninger.inntekt.bankinnskudd.brukskonto";
        result.addAll(oversiktFormue("brukskonto",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "saldo"));


        key = "opplysninger.inntekt.bankinnskudd.bsu";
        result.addAll(oversiktFormue("bsu",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "saldo"));

        key = "opplysninger.inntekt.bankinnskudd.sparekonto";
        result.addAll(oversiktFormue("sparekonto",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "saldo"));

        key = "opplysninger.inntekt.bankinnskudd.livsforsikring";
        result.addAll(oversiktFormue("livsforsikringssparedel",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "saldo"));

        key = "opplysninger.inntekt.bankinnskudd.aksjer";
        result.addAll(oversiktFormue("verdipapirer",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "saldo"));

        key = "opplysninger.inntekt.bankinnskudd.annet";
        result.addAll(oversiktFormue("belop",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "saldo"));

        key = "inntekt.eierandeler.true.type.bolig";
        appendIfTrue(result, "bolig", getTittel(key, navMessageSource), webSoknad.getValueForFaktum(key));

        key = "inntekt.eierandeler.true.type.campingvogn";
        appendIfTrue(result, "campingvogn", getTittel(key, navMessageSource), webSoknad.getValueForFaktum(key));

        key = "inntekt.eierandeler.true.type.kjoretoy";
        appendIfTrue(result, "kjoretoy", getTittel(key, navMessageSource), webSoknad.getValueForFaktum(key));

        key = "inntekt.eierandeler.true.type.fritidseiendom";
        appendIfTrue(result, "fritidseiendom", getTittel(key, navMessageSource), webSoknad.getValueForFaktum(key));

        key = "inntekt.eierandeler.true.type.annet";
        appendIfTrue(result, "annet", getTittel(key, navMessageSource), webSoknad.getValueForFaktum(key));

        return result.stream().filter(r -> r != null).collect(Collectors.toList());
    }

    private static List<JsonOkonomioversiktInntekt> tilJsonOkonomioversiktInntekt(InputSource inputSource) {
        final List<JsonOkonomioversiktInntekt> result = new ArrayList<>();
        final WebSoknad webSoknad = inputSource.getWebSoknad();
        final NavMessageSource navMessageSource = inputSource.getMessageSource();

        String key = "opplysninger.inntekt.bostotte";
        result.addAll(oversiktInntekt("bostotte",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "utbetaling"));

        key = "opplysninger.arbeid.jobb";
        result.addAll(oversiktInntekt("jobb",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "bruttolonn",
                "nettolonn"
        ));

        key = "opplysninger.arbeid.student";
        result.addAll(oversiktInntekt("studielanOgStipend",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "utbetaling"
        ));

        key = "opplysninger.familiesituasjon.barnebidrag.mottar";
        result.addAll(oversiktInntekt("barnebidrag",
                getTittel(key, navMessageSource),
                webSoknad.getFaktaMedKey(key),
                "mottar"

        ));
        return result.stream().filter(r -> r != null).collect(Collectors.toList());
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

    private static String getTittel(String key, NavMessageSource navMessageSource) {
        Properties properties = navMessageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));

        return properties.getProperty("json.okonomi." + key);

    }

    private static void appendIfTrue(List<JsonOkonomioversiktFormue> result, String type, String tittel, String value) {
        if (value != null && !value.trim().equals("") && Boolean.parseBoolean(value)) {
            result.add(new JsonOkonomioversiktFormue()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false)
            );
        }
    }
}
