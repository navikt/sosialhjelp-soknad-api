package no.nav.sbl.dialogarena.sendsoknad.domain;

import no.bekk.bekkopen.common.StringNumber;

/**
 * Tatt fra Bekk Open for å unngå validering av fødselsnummer, da vi tydeligvis kan få reelle
 * fødselsnummer som ikke validerer i følge fnr-reglene...
 */
public class NavFodselsnummer extends StringNumber {

    public NavFodselsnummer(String value) {
        super(value);
    }

    /**
     * Returns the two digits of the Fodselsnummer that contains the year birth
     * (00-99).
     *
     * @return A String containing the year of birth.
     */
    public String get2DigitBirthYear() {
        return getValue().substring(4, 6);
    }

    /**
     * Returns the birthyear of the Fodselsnummer
     *
     * @return A String containing the year of birth.
     */
    public String getBirthYear() {
        return getCentury() + get2DigitBirthYear();
    }

    String getCentury() {
        String result = null;
        int individnummerInt = Integer.parseInt(getIndividnummer());
        int birthYear = Integer.parseInt(get2DigitBirthYear());
        if (individnummerInt <= 499) {
            result = "19";
        } else if (individnummerInt >= 500 && birthYear < 40) {
            result = "20";
        } else if (individnummerInt >= 500 && individnummerInt <= 749 && birthYear > 54) {
            result = "18";
        } else if (individnummerInt >= 900 && birthYear > 39) {
            result = "19";
        }
        return result;
    }

    /**
     * Returns the first three digits of the Personnummer, also known as the
     * Individnummer.
     *
     * @return A String containing the Individnummer.
     */
    public String getIndividnummer() {
        return getValue().substring(6, 9);
    }

    /**
     * Returns the first 2 digits of the Fodselsnummer that contains the date
     * (01-31), stripped for eventual d-numbers.
     *
     * @return A String containing the date of birth
     */
    public String getDayInMonth() {
        return parseDNumber(getValue()).substring(0, 2);
    }

    /**
     * Returns the digits 3 and 4 of the Fodselsnummer that contains the month
     * (01-12), stripped for eventual d-numbers.
     *
     * @return A String containing the date of birth
     */
    public String getMonth() {
        return parseDNumber(getValue()).substring(2, 4);
    }

    static String parseDNumber(String fodselsnummer) {
        if (!isDNumber(fodselsnummer)) {
            return fodselsnummer;
        } else {
            return (getFirstDigit(fodselsnummer) - 4) + fodselsnummer.substring(1);
        }
    }

    static boolean isDNumber(String fodselsnummer) {
        try {
            int firstDigit = getFirstDigit(fodselsnummer);
            if (firstDigit > 3 && firstDigit < 8) {
                return true;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return false;
    }

    private static int getFirstDigit(String fodselsnummer) {
        return Integer.parseInt(fodselsnummer.substring(0, 1));
    }
}
