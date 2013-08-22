package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.validators;

import org.apache.wicket.validation.validator.PatternValidator;

/**
 * Klasse for å validere epostadresse som brukeren taster inn
 */
public class EmailValidator extends PatternValidator {

    private static final EmailValidator INSTANCE = new EmailValidator();

    public static EmailValidator getInstance() {
        return INSTANCE;
    }

    protected EmailValidator() {
        super(".+@.+");
    }
}