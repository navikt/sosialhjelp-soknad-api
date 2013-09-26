package no.nav.sbl.dialogarena.konto;

import org.apache.commons.collections15.Predicate;

import java.io.Serializable;

/**
 * Kontroll av modulus-11 for kontonummer
 */
class KontonummerErMod11 implements Predicate<String>, Serializable {

    @Override
    public boolean evaluate(String kontonummer) {
        int tverrsum = 0;

        //Vekttall som skal vektes mot tallene fra høyre mot venstre (ikke siste siffer)
        int vektall = 2;
        int vekttallStart = 2;
        int vekttallSlutt = 7;

        char[] knr = kontonummer.toCharArray();

        for (int i = knr.length - 2; i >= 0; i--) {
            tverrsum += Character.getNumericValue(knr[i]) * vektall;
            vektall++;

            if (vektall > vekttallSlutt) {
                vektall = vekttallStart;
            }
        }
        int rest = 11 - tverrsum % 11;

        int kontrollsiffer = 0;
        if (rest != 11) {
            kontrollsiffer = rest;
        }

        int sisteTallIKontonummer = Character.getNumericValue(knr[knr.length - 1]);
        return kontrollsiffer == sisteTallIKontonummer;
    }

}
