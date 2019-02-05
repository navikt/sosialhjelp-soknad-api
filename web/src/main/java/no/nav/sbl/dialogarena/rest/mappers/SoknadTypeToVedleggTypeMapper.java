package no.nav.sbl.dialogarena.rest.mappers;

import java.util.*;

public class SoknadTypeToVedleggTypeMapper {
    private static final Map<String,String> tilleggsinfoToJsonType = new HashMap<>();
    private static final Set<String> opplysningerUtgift = new HashSet<>();
    private static final Set<String> oversiktUtgift = new HashSet<>();
    private static final Set<String> formue = new HashSet<>();
    private static final Set<String> utbetaling = new HashSet<>();

    static {
        tilleggsinfoToJsonType.put("aksjer", "verdipapirer");
        tilleggsinfoToJsonType.put("annetbarnutgift", "annenBarneutgift");
        tilleggsinfoToJsonType.put("annetboutgift", "annenBoutgift");
        tilleggsinfoToJsonType.put("annetinntekter", "annen");
        tilleggsinfoToJsonType.put("annetverdi", "annet");
        tilleggsinfoToJsonType.put("barnehage", "barnehage");
        tilleggsinfoToJsonType.put("betaler", "barnebidrag");
        tilleggsinfoToJsonType.put("brukskonto", "brukskonto");
        tilleggsinfoToJsonType.put("bsu", "bsu");
        tilleggsinfoToJsonType.put("campingvogn", "campingvogn");
        tilleggsinfoToJsonType.put("eiendom", "salg");
        tilleggsinfoToJsonType.put("forsikringsutbetaling", "forsikring");
        tilleggsinfoToJsonType.put("fritidsaktivitet", "barnFritidsaktiviteter");
        tilleggsinfoToJsonType.put("fritidseiendom", "fritidseiendom");
        tilleggsinfoToJsonType.put("husleie", "husleie");
        tilleggsinfoToJsonType.put("kjopekontrakt", "bolig");
        tilleggsinfoToJsonType.put("kjoretoy", "kjoretoy");
        tilleggsinfoToJsonType.put("kommunaleavgifter", "kommunalAvgift");
        tilleggsinfoToJsonType.put("livsforsikring", "livsforsikringssparedel");
        tilleggsinfoToJsonType.put("mottar", "barnebidrag");
        tilleggsinfoToJsonType.put("oppvarming", "oppvarming");
        tilleggsinfoToJsonType.put("sfo", "sfo");
        tilleggsinfoToJsonType.put("sparekonto", "sparekonto");
        tilleggsinfoToJsonType.put("strom", "strom");
        tilleggsinfoToJsonType.put("tannbehandling", "barnTannregulering");
        tilleggsinfoToJsonType.put("utbytte", "utbytte");

        opplysningerUtgift.addAll(Arrays.asList("annetbarnutgift", "annetboutgift", "tannbehandling", "kommunaleavgifter",
                "fritidsaktivitet", "oppvarming", "strom"));
        oversiktUtgift.addAll(Arrays.asList("sfo", "barnehage", "betaler", "husleie"));
        formue.addAll(Arrays.asList("kjoretoy", "campingvogn", "fritidseiendom",
                "brukskonto", "bsu", "sparekonto", "kjopekontrakt", "livsforsikring",
                "annetverdi", "aksjer"));
        utbetaling.addAll(Arrays.asList("eiendom", "forsikringsutbetaling", "annetinntekter", "utbytte"));
    }

    public static SoknadTypeAndPath mapVedleggTypeToSoknadTypeAndPath(String type, String tilleggsinfo){
        if (tilleggsinfo.equals("avdraglaan")){
            return new SoknadTypeAndPath("boliglanAvdrag", "oversiktUtgift");
            // Spesialtilfelle for avdraglaan siden vedleggstypen korrelerer med to typer i soknad.json: boliglanAvdrag og boliglanRenter
        }

        if (utbetaling.contains(tilleggsinfo)){
            return new SoknadTypeAndPath(tilleggsinfoToJsonType.get(tilleggsinfo), "utbetaling");
        }
        if (opplysningerUtgift.contains(tilleggsinfo)){
            return new SoknadTypeAndPath(tilleggsinfoToJsonType.get(tilleggsinfo), "opplysningerUtgift");
        }
        if (oversiktUtgift.contains(tilleggsinfo)){
            return new SoknadTypeAndPath(tilleggsinfoToJsonType.get(tilleggsinfo), "oversiktUtgift");
        }
        if (formue.contains(tilleggsinfo)){
            return new SoknadTypeAndPath(tilleggsinfoToJsonType.get(tilleggsinfo), "formue");
        }

        if (tilleggsinfo.equals("mottar")){
            return new SoknadTypeAndPath("barnebidrag", "inntekt");
        } else if (tilleggsinfo.equals("vedtak") && type.equals("bostotte")){
            return new SoknadTypeAndPath("bostotte", "inntekt");
        } else if (tilleggsinfo.equals("vedtak") && type.equals("student")){
            return new SoknadTypeAndPath("studielanOgStipend", "inntekt");
        } else if (tilleggsinfo.equals("arbeid") && type.equals("lonnslipp")){
            return new SoknadTypeAndPath("jobb", "inntekt");
        } else if (tilleggsinfo.equals("arbeid") && type.equals("sluttoppgjor")){
            return new SoknadTypeAndPath("sluttoppgjoer", "utbetaling");
        } else if (tilleggsinfo.equals("annet") && type.equals("kontooversikt")){
            return new SoknadTypeAndPath("belop", "formue");
        } else if (tilleggsinfo.equals("annet") && type.equals("annet")){
            return new SoknadTypeAndPath("annen", "opplysningerUtgift");
        } else if (tilleggsinfo.equals("annet") && type.equals("dokumentasjon")){
            return new SoknadTypeAndPath("annen", "utbetaling");
        }

        throw new IllegalStateException("Vedleggstypen eksisterer ikke eller mangler mapping");
    }
}
