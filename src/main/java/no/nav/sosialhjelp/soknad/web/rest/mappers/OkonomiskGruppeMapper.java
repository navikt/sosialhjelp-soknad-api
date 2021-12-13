//package no.nav.sosialhjelp.soknad.web.rest.mappers;
//
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.VedleggTypeToSoknadTypeMapper.getSoknadPath;
//
//public final class OkonomiskGruppeMapper {
//
//    private OkonomiskGruppeMapper() {
//    }
//
//    public static String getGruppe(String vedleggType) {
//        switch (vedleggType) {
//            case "barnebidrag|mottar":
//            case "barnebidrag|betaler":
//            case "samvarsavtale|barn":
//                return "familie";
//            case "husleiekontrakt|husleiekontrakt":
//            case "husleiekontrakt|kommunal":
//                return "bosituasjon";
//            case "sluttoppgjor|arbeid":
//            case "lonnslipp|arbeid":
//            case "student|vedtak":
//                return "arbeid";
//            case "annet|annet":
//                return "andre utgifter";
//            case "skattemelding|skattemelding":
//                return "generelle vedlegg";
//            case "oppholdstillatel|oppholdstillatel":
//                return "statsborgerskap";
//            default:
//                final String soknadPath = getSoknadPath(vedleggType);
//
//                if (soknadPath.equals("utbetaling") || soknadPath.equals("formue") || soknadPath.equals("inntekt")) {
//                    return "inntekt";
//                }
//                if (soknadPath.equals("opplysningerUtgift") || soknadPath.equals("oversiktUtgift")) {
//                    return "utgifter";
//                }
//                break;
//        }
//
//        throw new IllegalStateException("Vedleggstypen eksisterer ikke eller mangler mapping");
//    }
//}
