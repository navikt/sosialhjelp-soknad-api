'use strict';
/**
 *  Module
 *
 * Description
 */

angular.module('app.directives', ['app.services', 'nav.booleanradio', 'nav.cmstekster', 'nav.input', 'nav.feilmeldinger', 'nav.sporsmalferdig', 'nav.markup', 'nav.stegindikator', 'nav.textarea', 'nav.fremdriftsindikator', 'nav.validering', 'nav.sistlagret', 'nav.select', 'nav.hjelpetekst', 'nav.datepicker'])
    /*Hva med casene 1-242 osv? */

    .directive('landskodevalidering', function () {
        return {
            require: 'ngModel',
            link: function (scope, elm, attrs, ctrl) {
                ctrl.$parsers.unshift(function (viewValue) {
                    var INTEGER_REGEX = /^\-?\d*$/;
                    var kode = viewValue.slice(1, viewValue.length);
                    if (viewValue.charAt(0) === '+' && INTEGER_REGEX.test(kode)) {
                        ctrl.$setValidity('feil', true);
                    } else {
                        ctrl.$setValidity('feil', false);
                    }
                });
            }
        };

    })

    .directive('mobilnummer', function () {
        return {
            require: 'ngModel',
            link: function (scope, elm, attrs, ctrl) {
                ctrl.$parsers.unshift(function (viewValue) {
                    var INTEGER_REGEX = /^\-?\d*$/;
                    if (INTEGER_REGEX.test(viewValue) && viewValue.length === 8) {
                        ctrl.$setValidity('feil', true);
                        return parseFloat(viewValue.replace(',', '.'));
                    } else {
                        ctrl.$setValidity('feil', false);
                        return undefined;
                    }
                });
            }
        };

    })

    .directive('prosent', function () {
        return {
            replace: true,
            require: 'ngModel',

            link: function (scope, elm, attrs, ctrl) {
                ctrl.$parsers.unshift(function (viewValue) {
                    var INTEGER_REGEX = /^\-?\d*$/;
                    if (INTEGER_REGEX.test(viewValue) && viewValue <= 100 && viewValue >= 0) {
                        ctrl.$setValidity('prosent', true);
                        return viewValue;
                    } else {
                        ctrl.$setValidity('prosent', false);
                        return undefined;
                    }
                });
            }
        };
    });


function fraMindreEnnTil(fra, til) {
    var gyldig = false;
    if (fra.getTime() < til.getTime()) {
        gyldig = true
    }

    return gyldig;
}