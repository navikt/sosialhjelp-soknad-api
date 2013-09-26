package no.nav.sbl.dialogarena.konto;

import no.nav.sbl.dialogarena.common.TekstUtils;


public final class Formatering {

    public static String formaterNorskKontonummer(String kontonummer) {
        if (kontonummer == null) {
            return null;
        }
        String vasketKontonummer = TekstUtils.fjernSpesialtegn(kontonummer);
        StringBuilder formatertKontonummer = new StringBuilder();
        for (int i = 0; i < vasketKontonummer.length(); i++) {
            if (formatertKontonummer.length() == 4 || formatertKontonummer.length() == 7) {
                formatertKontonummer.append(" ");
            }
            formatertKontonummer.append(vasketKontonummer.charAt(i));
        }
        return formatertKontonummer.toString();
    }

    private Formatering() { }
}
