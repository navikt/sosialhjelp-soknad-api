package no.nav.sbl.dialogarena.rest.mappers;

public class PersonMapper {
    public static String getPersonnummerFromFnr(String fnr){
        return fnr != null ? fnr.substring(6) : null;
    }
}
