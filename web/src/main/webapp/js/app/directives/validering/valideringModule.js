angular.module('nav.validering', ['nav.cmstekster'])
    .directive('blurValidate', ['data', function (data) {
        return {
            require: ['ngModel', '^form'],
            link: function (scope, element, attrs, ctrls) {
                var feil, revaliderFeilMetode;

                var ngModel = ctrls[0];
                var form = ctrls[1];
                var eventString = 'RUN_VALIDATION' + form.$name;
                var valideringsMetoder = [];
                var formElem = element.closest('.form-linje');

                try {
                    feil = scope.$eval(attrs.errorMessages);
                } catch (e) {
                    feil = attrs.errorMessages;
                }

                // Rekkefølgen på setup-metodene bestemmer prioriteten på valideringsmetodene
                RequiredValidator.init(attrs, valideringsMetoder);
                PatternValidator.init(attrs, valideringsMetoder);
                LengthValidator.init(attrs, valideringsMetoder);

                scope.$on(eventString, function () {
                    if (!sjekkOmInputErGyldig()) {
                        formElem.addClass('feil');
                    }
                });

                element.bind('blur', function () {
                    if (!sjekkOmInputErGyldig()) {
                        formElem.addClass('feil');
                    }
                });

                scope.$watch(function () {
                    return ngModel.$viewValue;
                }, function () {
                    if (revaliderFeilMetode && revaliderFeilMetode(ngModel.$viewValue) == true) {
                        formElem.removeClass('feil');
                    }
                });

                function sjekkOmInputErGyldig() {
                    for (var i = 0; i < valideringsMetoder.length; i++) {
                        var valideringReturVerdi = valideringsMetoder[i](ngModel.$viewValue);

                        if (valideringReturVerdi != true) {
                            revaliderFeilMetode = valideringsMetoder[i];
                            settFeilmeldingsTekst(valideringReturVerdi);
                            return false;
                        }
                    }
                    return true;
                }

                function settFeilmeldingsTekst(feilNokkel) {
                    var feilmeldingsNokkel = feil;

                    if (typeof feil === 'object') {
                        feilmeldingsNokkel = feil[feilNokkel];
                    }

                    var feilmeldingTekst = data.tekster[feilmeldingsNokkel];
                    formElem.find('.melding').text(feilmeldingTekst);
                }
            }
        }
    }])
    .directive('radiobuttonValidate', ['data', function (data) {
        return {
            require: ['ngModel', '^form'],
            link: function (scope, element, attrs, ctrls) {
                var ngModel = ctrls[0];
                var form = ctrls[1];
                var eventString = 'RUN_VALIDATION' + form.$name;

                scope.$on(eventString, function () {
                    if(sjekkOmFeltetErRequired() &&!sjekkOmFeltetErSvart() ) {
                        element.closest('.form-linje').addClass('feil');
                    }
                });

                scope.$watch(function () {
                    return ngModel.$viewValue;
                }, function () {
                    if (ngModel.$viewValue) {
                        element.closest('.form-linje').removeClass('feil');
                    }
                });

                function sjekkOmFeltetErRequired() {
                    return element[0].hasOwnProperty("required");
                }

                function sjekkOmFeltetErSvart() {
                    if (!ngModel.$modelValue) {
                            settFeilmeldingsTekst();
                            return false;
                        }
                    return true;
                }

                function settFeilmeldingsTekst() {
                    var feilmeldingsNokkel = element[0].getAttribute('error-messages').toString();
                    //hack for å fjerne dobbeltfnuttene rundt feilmeldingsnokk
                    var feilmeldingTekst = data.tekster[feilmeldingsNokkel.substring(1, feilmeldingsNokkel.length-1)];
                   element.closest('.form-linje').find('.melding').text(feilmeldingTekst);
                }
            }
        }
    }]);