if (!Array.prototype.indexOf) {
    Array.prototype.indexOf = function (elt) {
        var len = this.length >>> 0;

        var from = Number(arguments[1]) || 0;

        from = (from < 0) ? Math.ceil(from) : Math.floor(from);

        if (from < 0) {
            from += len;
        }

        for (; from < len; from++) {
            if (from in this && this[from] === elt) {
                return from;
            }
        }
        return -1;
    }
}

if (!Array.prototype.last) {
    Array.prototype.last = function () {
        return this[this.length - 1];
    }
}

    if (!Array.prototype.contains) {
    Array.prototype.contains = function (val) {
        return $.inArray(val, this) > -1;
    }
}

// Returnerer index til ett objekt som inneholder value (ikke nødvendigvis første)
if (!Array.prototype.indexByValue) {
    Array.prototype.indexByValue = function (val) {
        return this.indexOf($.grep(this, function (obj) {
            for (key in obj) {
                if (obj[key] == val) {
                    return this;
                }
            }
            return false;
        })[0]);
    }
}

String.prototype.splice = function(idx, rem, str) {
    return (this.slice(0, idx) + str + this.slice(idx + Math.abs(rem)));
}

function sjekkOmGittEgenskapTilObjektErFalse(objekt) {
    if (objekt) {
        return checkFalse(objekt.value);
    }

    return false;
}

function sjekkOmGittEgenskapTilObjektErTrue(objekt) {
    if (objekt) {
        return checkTrue(objekt.value);
    }

    return false;
}

function checkTrue(element) {
    if (element == undefined) {
        return false;
    }
    return element.toString() == 'true';
}
function checkFalse(element) {
    if (element == undefined) {
        return false;
    }
    return element.toString() == 'false';
}

function scrollToElement(element, offset) {
    var animationSpeed = 200;
    var scrollPos = Math.max(element.offset().top - offset, 0);
    $('body, html').scrollToPos(scrollPos, animationSpeed);
}

function fiksNavn(element, navn, tmpNavn) {
    var formCtrl = element.parent().controller('form');
    var inputElement = element.find('input, textarea');
    if (inputElement) {
        inputElement.attr('name', navn);
    }
    var currentElementCtrl = formCtrl[tmpNavn];
    formCtrl.$removeControl(currentElementCtrl);
    currentElementCtrl.$name = navn;
    formCtrl.$addControl(currentElementCtrl);
}

function verdiErLagretISoknadData(scope, nokkel) {
    if (scope.soknadData && scope.soknadData.fakta[nokkel]) {
        return true;
    }
    return false;
}

function verdiErIkkeTom(verdi) {
    return verdi !== undefined && verdi !== null && verdi.length > 0;
}

function deepClone(obj) {
    return $.extend(true, {}, obj);
}

function harAttributt(objekt, attributt) {
    var capitalizedAttr = capitalizeFirstLetter(attributt);
    if (objekt.hasOwnProperty(attributt)) {
        return objekt[attributt];
    } else if (objekt.hasOwnProperty('ng' + capitalizedAttr)) {
        return objekt['ng' + capitalizedAttr];
    } else {
        return false;
    }
}

function capitalizeFirstLetter(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

function opprettEgendefinertFeilmelding(navn, errorMessage, referanseTilFeilmeldingslinken, valid, skalVisesAlene) {
    var feilmelding = new Object();
    feilmelding.$name = navn;
    feilmelding.$errorMessages = errorMessage;
    feilmelding.$linkId = referanseTilFeilmeldingslinken;
    feilmelding.$valid = valid;
    feilmelding.$invalid = !valid;
    feilmelding.$skalVisesAlene = skalVisesAlene


    return feilmelding;
}
function leggTilFeilmeldingHvisDenIkkeFinnes(form, feilmeldingskategori, feilmeldingsnavn, feilmelding, referanseTilFeilmeldingslinken, valid, skalVisesAlene) {
    var index = form.$error[feilmeldingskategori].indexByValue(feilmeldingsnavn);

    if (index == -1) {
        form.$error[feilmeldingskategori].push(opprettEgendefinertFeilmelding(feilmeldingsnavn, feilmelding, referanseTilFeilmeldingslinken, valid, skalVisesAlene));
    }
}
/**
 * endrer validiteten på en feilmelding. Sjekker hvis feilmeldingen ikke skal vises lenger, så i stedet for å oppdatere feilmeldingen fjernes den heller fra listen.
 Derfor trenger vi kun å legge til feilmeldinger som ikke finnes fra før og som er false.
 */
function settEgendefinertFeilmeldingsverdi(form, feilmeldingskategori, feilmeldingsnavn, feilmelding, referanseTilFeilmeldingslinken, valid, skalVisesAlene) {
    if (form.$error[feilmeldingskategori] === undefined) {
        form.$error[feilmeldingskategori] = [];
    }
    if (typeof form.$error[feilmeldingskategori] === 'boolean') {
        form.$setValidity(feilmeldingsnavn, valid);
    } else {
        var index = form.$error[feilmeldingskategori].indexByValue(feilmeldingsnavn);
        if (index > -1 && valid) {
            form.$error[feilmeldingskategori].splice(index, 1);
            form.$setValidity(feilmeldingsnavn, valid);

        } else if (index == -1 && !valid) {
            form.$setValidity(feilmeldingsnavn, valid);
            leggTilFeilmeldingHvisDenIkkeFinnes(form, feilmeldingskategori, feilmeldingsnavn, feilmelding, referanseTilFeilmeldingslinken, valid, skalVisesAlene);
        }
    }
}

function stringContainsNotCaseSensitive(str, query) {
    return str.toLowerCase().indexOf(query.toLowerCase());
}

(function ($) {
    $.fn.scrollToPos = function (position, speed) {
        if (speed === undefined) {
            speed = 'fast';
        }
        $(this).animate({
            scrollTop: position
        }, speed);
    }
})(jQuery);

function fadeBakgrunnsfarge(element, melding, feilmeldingsklasse, scope) {
    var backgroundColour = [254, 230, 230].join(',') + ',';
    var borderColour = [252, 162, 146].join(',') + ',';
    var meldingColour = [195, 0, 0].join(',') + ',';
    var transparency = 1;
    var timeout = setInterval(function () {
        if (transparency >= 0) {
            element[0].style.backgroundColor = 'rgba(' + backgroundColour + (transparency -= 0.015) + ')';
            element[0].style.borderColor = 'rgba(' + borderColour + (transparency -= 0.015) + ')';
            melding[0].style.color = 'rgba(' + meldingColour + (transparency -= 0.015) + ')';
        } else {
            element.removeClass(feilmeldingsklasse);
            element.removeAttr('style');
            melding.removeAttr('style');
            clearInterval(timeout);
            scope.$apply();
        }
    }, 20);
}
function konverterStringFraNorskDatoformatTilDateObjekt(datoString) {
    var re = new RegExp(/^\d\d\.\d\d\.\d\d\d\d$/);
    if (re.test(datoString) && erGyldigDato(datoString)) {
        var datoKomponenter = datoString.split('.');

        // Måned indekseres fra 0, så må trekke fra 1
        return new Date(datoKomponenter[2], datoKomponenter[1] - 1, datoKomponenter[0]);
    } else {
        return "";
    }
}

// stackoverflow.com/questions/5812220/test-if-date-is-valid
function erGyldigDato(datoString) {
    var bits = datoString.split('.');
    var aar = bits[2];
    var maaned = bits[1];
    var dag = bits[0];

    var dagerIMaaned = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

    // Skuddår
    if ((!(aar % 4) && aar % 100) || !(aar % 400)) {
        dagerIMaaned[1] = 29;
    }

    return dag <= dagerIMaaned[--maaned];
}

function konverterTallTilStringMedToSiffer(tall) {
    var tallMedToSiffer = "0" + tall;
    return tallMedToSiffer.slice(-2);
}

function hentCaretPosisjon(element) {
    var domElement = element[0];
    var posisjon = 0;

    if (document.selection) {
        domElement.focus();

        var oSel = document.selection.createRange();

        oSel.moveStart('character', -domElement.value.length);

        posisjon = oSel.text.length;
    } else if (domElement.selectionStart || domElement.selectionStart == '0') {
        posisjon = domElement.selectionStart;
    }

    return posisjon;
}

function settCaretPosisjon(element, posisjon) {
    var domElement = element[0];

    if (document.selection) {
        domElement.focus();

        var oSel = document.selection.createRange();

        oSel.moveStart('character', posisjon);
    } else if (domElement.selectionStart || domElement.selectionStart == '0') {
        domElement.selectionStart = posisjon;
        domElement.selectionEnd = posisjon;
    }
}

function settFokusTilNesteElement(inputElement) {
    var fokuserbareElementer = $('input, a, select, button, textarea').filter(':visible');
    fokuserbareElementer.eq(fokuserbareElementer.index(inputElement) + 1).focus();
}
