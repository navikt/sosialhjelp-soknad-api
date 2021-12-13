//package no.nav.sosialhjelp.soknad.web.rest.mappers;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BARNEBIDRAG;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_ANNET;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BRUKSKONTO;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BSU;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_LIVSFORSIKRING;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_SPAREKONTO;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_VERDIPAPIRER;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_ANNET;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_FORSIKRING;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SALG;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_UTBYTTE;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_ANNET;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_BOLIG;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_CAMPINGVOGN;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_FRITIDSEIENDOM;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_KJORETOY;
//
//public final class VedleggTypeToSoknadTypeMapper {
//
//    private VedleggTypeToSoknadTypeMapper() {
//    }
//
//    public static final Map<String,String> vedleggTypeToSoknadType = new HashMap<>();
//
//    static {
//        vedleggTypeToSoknadType.put("kontooversikt|aksjer", FORMUE_VERDIPAPIRER);
//        vedleggTypeToSoknadType.put("faktura|annetbarnutgift", UTGIFTER_ANNET_BARN);
//        vedleggTypeToSoknadType.put("dokumentasjon|annetboutgift", UTGIFTER_ANNET_BO);
//        vedleggTypeToSoknadType.put("dokumentasjon|annetinntekter", UTBETALING_ANNET);
//        vedleggTypeToSoknadType.put("dokumentasjon|annetverdi", VERDI_ANNET); // Økonomisk verdi. Usikker på om denne brukes
//        vedleggTypeToSoknadType.put("dokumentasjon|campingvogn", VERDI_CAMPINGVOGN); // Økonomisk verdi. Usikker på om denne brukes
//        vedleggTypeToSoknadType.put("dokumentasjon|fritidseiendom", VERDI_FRITIDSEIENDOM); // Økonomisk verdi. Usikker på om denne brukes
//        vedleggTypeToSoknadType.put("kjopekontrakt|kjopekontrakt", VERDI_BOLIG); // Økonomisk verdi. Usikker på om denne brukes
//        vedleggTypeToSoknadType.put("dokumentasjon|kjoretoy", VERDI_KJORETOY); // Økonomisk verdi. Usikker på om denne brukes
//        vedleggTypeToSoknadType.put("faktura|barnehage", UTGIFTER_BARNEHAGE);
//        vedleggTypeToSoknadType.put("barnebidrag|betaler", BARNEBIDRAG);
//        vedleggTypeToSoknadType.put("kontooversikt|brukskonto", FORMUE_BRUKSKONTO);
//        vedleggTypeToSoknadType.put("kontooversikt|bsu", FORMUE_BSU);
//        vedleggTypeToSoknadType.put("salgsoppgjor|eiendom", UTBETALING_SALG);
//        vedleggTypeToSoknadType.put("dokumentasjon|forsikringsutbetaling", UTBETALING_FORSIKRING);
//        vedleggTypeToSoknadType.put("faktura|fritidsaktivitet", UTGIFTER_BARN_FRITIDSAKTIVITETER);
//        vedleggTypeToSoknadType.put("faktura|husleie", UTGIFTER_HUSLEIE);
//        vedleggTypeToSoknadType.put("faktura|kommunaleavgifter", UTGIFTER_KOMMUNAL_AVGIFT);
//        vedleggTypeToSoknadType.put("kontooversikt|livsforsikring", FORMUE_LIVSFORSIKRING);
//        vedleggTypeToSoknadType.put("barnebidrag|mottar", BARNEBIDRAG);
//        vedleggTypeToSoknadType.put("faktura|oppvarming", UTGIFTER_OPPVARMING);
//        vedleggTypeToSoknadType.put("faktura|sfo", UTGIFTER_SFO);
//        vedleggTypeToSoknadType.put("kontooversikt|sparekonto", FORMUE_SPAREKONTO);
//        vedleggTypeToSoknadType.put("faktura|strom", UTGIFTER_STROM);
//        vedleggTypeToSoknadType.put("faktura|tannbehandling", UTGIFTER_BARN_TANNREGULERING);
//        vedleggTypeToSoknadType.put("dokumentasjon|utbytte", UTBETALING_UTBYTTE);
//        vedleggTypeToSoknadType.put("husbanken|vedtak", UTBETALING_HUSBANKEN);
//        vedleggTypeToSoknadType.put("student|vedtak", STUDIELAN);
//        vedleggTypeToSoknadType.put("lonnslipp|arbeid", JOBB);
//        vedleggTypeToSoknadType.put("sluttoppgjor|arbeid", SLUTTOPPGJOER);
//        vedleggTypeToSoknadType.put("kontooversikt|annet", FORMUE_ANNET);
//        vedleggTypeToSoknadType.put("annet|annet", UTGIFTER_ANDRE_UTGIFTER);
//        vedleggTypeToSoknadType.put("dokumentasjon|annet", UTBETALING_ANNET);
//        vedleggTypeToSoknadType.put("nedbetalingsplan|avdraglaan", UTGIFTER_BOLIGLAN_AVDRAG); // vedleggstypen er også knyttet til soknadstypen "boliglanRenter"
//    }
//
//    public static String getSoknadPath(String vedleggType){
//        switch (vedleggType){
//            case "dokumentasjon|annetboutgift":
//            case "faktura|annetbarnutgift":
//            case "faktura|tannbehandling":
//            case "faktura|kommunaleavgifter":
//            case "faktura|fritidsaktivitet":
//            case "faktura|oppvarming":
//            case "faktura|strom":
//            case "annet|annet":
//                return "opplysningerUtgift";
//            case "barnebidrag|betaler":
//            case "faktura|sfo":
//            case "faktura|barnehage":
//            case "faktura|husleie":
//            case "nedbetalingsplan|avdraglaan":
//                return "oversiktUtgift";
//            case "dokumentasjon|kjoretoy":
//            case "dokumentasjon|campingvogn":
//            case "dokumentasjon|fritidseiendom":
//            case "dokumentasjon|annetverdi":
//            case "kjopekontrakt|kjopekontrakt":
//            case "kontooversikt|brukskonto":
//            case "kontooversikt|bsu":
//            case "kontooversikt|sparekonto":
//            case "kontooversikt|livsforsikring":
//            case "kontooversikt|aksjer":
//            case "kontooversikt|annet":
//                return "formue";
//            case "dokumentasjon|forsikringsutbetaling":
//            case "dokumentasjon|annetinntekter":
//            case "dokumentasjon|utbytte":
//            case "dokumentasjon|annet":
//            case "salgsoppgjor|eiendom":
//            case "sluttoppgjor|arbeid":
//            case "husbanken|vedtak":
//                return "utbetaling";
//            case "barnebidrag|mottar":
//            case "lonnslipp|arbeid":
//            case "student|vedtak":
//                return "inntekt";
//            default:
//                throw new IllegalStateException("Vedleggstypen eksisterer ikke eller mangler mapping");
//        }
//    }
//
//    public static boolean isInSoknadJson(String vedleggType) {
//        switch (vedleggType){
//            case "oppholdstillatel|oppholdstillatel":
//            case "samvarsavtale|barn":
//            case "husleiekontrakt|husleiekontrakt":
//            case "husleiekontrakt|kommunal":
//            case "skattemelding|skattemelding":
//                return false;
//            default:
//                return true;
//        }
//    }
//}
