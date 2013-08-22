function inlineValidation(elementId, validationOptions, removeOnFocus) {
    $('#' + elementId).focusout(function () {
        validate($(this), validationOptions, removeOnFocus);
    });
}

/**
 * Kjører validering på ett input-felt.
 *
 * @param element elementet som skal valideres
 * @param validationOptions JSON-objekt som inneholder feilmeldingen og kravene til valideringen
 * @param removeOnFocus boolean som sier om ø
 */
function validate(element, validationOptions, removeOnFocus) {
    var formElement = element.closest(".form-linje");
    element.closest(".form-linje").removeClass("feil");

    $.each(validationOptions, function(key, value) {
        runValidation(key, value, element);
    });

    if (formElement.hasClass("feil") && removeOnFocus) {
        element.one("focusin", function() {
            formElement.removeClass("feil");
        });
    }
}

/**
 * Kjører en enkelt valideringsmetode på input-feltet.
 *
 * @param key navnet til metoden som skal kjøres
 * @param options JSON-objekt som inneholder feilmeldingen og kravene til valideringen
 * @param element input-feltet som skal valideres
 * @returns {*} true dersom feltet valideres, ellers false
 */
function runValidation(key, options, element) {
    var formElement = element.closest(".form-linje");
    var errorMessageElement = formElement.children(".melding");

    valid = window[key](element, options.value);

    if (!valid) {
        setErrorMessage(errorMessageElement, options.error, element);
        formElement.addClass("feil");
    }

    return valid
}

/**
 * Setter feilmelding på et felt dersom det ikke validerer. Dersom feilmeldingen inneholder stringen ${input}
 * hentes verdien som er satt i input-feltet.
 *
 * @param errorMessageElement elementet som feilmeldingen skal settes i
 * @param errorMessage feilmeldingen som skal settes
 * @param inputElement inputfeltet som valideres
 */
function setErrorMessage(errorMessageElement, errorMessage, inputElement) {
    var inputIdx = errorMessage.indexOf("${input}");
    if (inputIdx > 0) {
        errorMessage = errorMessage.replace("${input}", inputElement.val());
    }

    errorMessageElement.html(errorMessage);
}

/**
 * Sjekker at teksten i det gitte elementet ikke overstiger en gitt lengde
 *
 * @param element elementet som sjekkes
 * @param length max lengde på elementet
 * @returns {boolean} true dersom elementet ikke har for lang tekst
 */
function maxLength(element, length) {
    return element.val().length <= length;
}

/**
 * Sjekker at teksten i det gitte elementet opprettholder reglene gitt i ett regular expression pattern
 *
 * @param element elementet som sjekkes
 * @param pattern Regular expression pattern
 * @returns {boolean} true dersom feltet er gyldig ihht. gitt pattern
 */
function pattern(element, pattern) {
    var regex = new RegExp(pattern);
    return regex.test(element.val()) || element.val().length === 0;
}

/**
 * Sjekker om elementet er påkrevd. Dersom det er det, settes en feilmelding dersom man etterlater feltet tomt
 *
 * @param element elementet som sjekkes
 * @param required boolean som sier om feltet er påkrevd
 * @returns {boolean} true dersom feltet er gyldig
 */
function required(element, required) {
    if (required) {
        return element.val().length > 0;
    }
    return true;
}

function deactiveButton(elementId) {
    var element = $('#' + elementId);
    element.addClass("knapp-deaktivert");
    element.siblings("input[type='email']").one("focusin", function() {
        element.removeClass("knapp-deaktivert");
    });
}
