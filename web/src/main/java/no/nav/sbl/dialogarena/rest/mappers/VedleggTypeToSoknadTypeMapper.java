package no.nav.sbl.dialogarena.rest.mappers;

import java.util.HashMap;
import java.util.Map;

public class VedleggTypeToSoknadTypeMapper {
    public static final Map<String,String> vedleggTypeToSoknadType = new HashMap<>();

    static {
        vedleggTypeToSoknadType.put("kontooversikt|aksjer", "verdipapirer");
        vedleggTypeToSoknadType.put("faktura|annetbarnutgift", "annenBarneutgift");
        vedleggTypeToSoknadType.put("dokumentasjon|annetboutgift", "annenBoutgift");
        vedleggTypeToSoknadType.put("dokumentasjon|annetinntekter", "annen");
        vedleggTypeToSoknadType.put("dokumentasjon|annetverdi", "annet"); // Økonomisk verdi. Usikker på om denne brukes
        vedleggTypeToSoknadType.put("dokumentasjon|campingvogn", "campingvogn"); // Økonomisk verdi. Usikker på om denne brukes
        vedleggTypeToSoknadType.put("dokumentasjon|fritidseiendom", "fritidseiendom"); // Økonomisk verdi. Usikker på om denne brukes
        vedleggTypeToSoknadType.put("kjopekontrakt|kjopekontrakt", "bolig"); // Økonomisk verdi. Usikker på om denne brukes
        vedleggTypeToSoknadType.put("dokumentasjon|kjoretoy", "kjoretoy"); // Økonomisk verdi. Usikker på om denne brukes
        vedleggTypeToSoknadType.put("faktura|barnehage", "barnehage");
        vedleggTypeToSoknadType.put("barnebidrag|betaler", "barnebidrag");
        vedleggTypeToSoknadType.put("kontooversikt|brukskonto", "brukskonto");
        vedleggTypeToSoknadType.put("kontooversikt|bsu", "bsu");
        vedleggTypeToSoknadType.put("salgsoppgjor|eiendom", "salg");
        vedleggTypeToSoknadType.put("dokumentasjon|forsikringsutbetaling", "forsikring");
        vedleggTypeToSoknadType.put("faktura|fritidsaktivitet", "barnFritidsaktiviteter");
        vedleggTypeToSoknadType.put("faktura|husleie", "husleie");
        vedleggTypeToSoknadType.put("faktura|kommunaleavgifter", "kommunalAvgift");
        vedleggTypeToSoknadType.put("kontooversikt|livsforsikring", "livsforsikringssparedel");
        vedleggTypeToSoknadType.put("barnebidrag|mottar", "barnebidrag");
        vedleggTypeToSoknadType.put("faktura|oppvarming", "oppvarming");
        vedleggTypeToSoknadType.put("faktura|sfo", "sfo");
        vedleggTypeToSoknadType.put("kontooversikt|sparekonto", "sparekonto");
        vedleggTypeToSoknadType.put("faktura|strom", "strom");
        vedleggTypeToSoknadType.put("faktura|tannbehandling", "barnTannregulering");
        vedleggTypeToSoknadType.put("dokumentasjon|utbytte", "utbytte");
        vedleggTypeToSoknadType.put("bostotte|vedtak", "bostotte");
        vedleggTypeToSoknadType.put("student|vedtak", "studielanOgStipend");
        vedleggTypeToSoknadType.put("lonnslipp|arbeid", "jobb");
        vedleggTypeToSoknadType.put("sluttoppgjor|arbeid", "sluttoppgjoer");
        vedleggTypeToSoknadType.put("kontooversikt|annet", "belop");
        vedleggTypeToSoknadType.put("annet|annet", "annen");
        vedleggTypeToSoknadType.put("dokumentasjon|annet", "annen");
        vedleggTypeToSoknadType.put("nedbetalingsplan|avdraglaan", "boliglanAvdrag"); // vedleggstypen er også knyttet til soknadstypen "boliglanRenter"
    }

    public static String getSoknadPath(String vedleggType){
        switch (vedleggType){
            case "dokumentasjon|annetboutgift":
            case "faktura|annetbarnutgift":
            case "faktura|tannbehandling":
            case "faktura|kommunaleavgifter":
            case "faktura|fritidsaktivitet":
            case "faktura|oppvarming":
            case "faktura|strom":
            case "annet|annet":
                return "opplysningerUtgift";
            case "barnebidrag|betaler":
            case "faktura|sfo":
            case "faktura|barnehage":
            case "faktura|husleie":
            case "nedbetalingsplan|avdraglaan":
                return "oversiktUtgift";
            case "dokumentasjon|kjoretoy":
            case "dokumentasjon|campingvogn":
            case "dokumentasjon|fritidseiendom":
            case "dokumentasjon|annetverdi":
            case "kjopekontrakt|kjopekontrakt":
            case "kontooversikt|brukskonto":
            case "kontooversikt|bsu":
            case "kontooversikt|sparekonto":
            case "kontooversikt|livsforsikring":
            case "kontooversikt|aksjer":
            case "kontooversikt|annet":
                return "formue";
            case "dokumentasjon|forsikringsutbetaling":
            case "dokumentasjon|annetinntekter":
            case "dokumentasjon|utbytte":
            case "dokumentasjon|annet":
            case "salgsoppgjor|eiendom":
            case "sluttoppgjor|arbeid":
                return "utbetaling";
            case "barnebidrag|mottar":
            case "bostotte|vedtak":
            case "lonnslipp|arbeid":
            case "student|vedtak":
                return "inntekt";
            default:
                throw new IllegalStateException("Vedleggstypen eksisterer ikke eller mangler mapping");
        }
    }

    public static boolean isInSoknadJson(String vedleggType) {
        switch (vedleggType){
            case "oppholdstillatel|oppholdstillatel":
            case "samvarsavtale|barn":
            case "husleiekontrakt|husleiekontrakt":
            case "husleiekontrakt|kommunal":
            case "skattemelding|skattemelding":
                return false;
            default:
                return true;
        }
    }
}
