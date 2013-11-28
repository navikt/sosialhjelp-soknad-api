'use strict';
angular.module('app.directives', ['app.services', 'nav.booleanradio', 'nav.cmstekster', 'nav.input', 'nav.feilmeldinger', 'nav.sporsmalferdig', 'nav.markup', 'nav.stegindikator', 'nav.textarea', 'nav.fremdriftsindikator', 'nav.validering', 'nav.sistlagret', 'nav.select', 'nav.hjelpetekst', 'nav.datepicker'])
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