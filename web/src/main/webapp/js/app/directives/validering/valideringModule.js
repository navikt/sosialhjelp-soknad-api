angular.module('nav.validering', ['nav.cmstekster'])
    .directive('blurValidate', ['cms', function (cms) {
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
                } catch(e) {
                    feil = attrs.errorMessages;
                }

                // Rekkefølgen på setup-metodene bestemmer prioriteten på valideringsmetodene
                RequiredValidator.init(attrs, valideringsMetoder);
                PatternValidator.init(attrs, valideringsMetoder);
                LengthValidator.init(attrs, valideringsMetoder);

                scope.$on(eventString, function() {
                    if (!sjekkOmInputErGyldig()) {
                        formElem.addClass('feil');
                    }
                });

                element.bind('blur', function () {
                    if (!sjekkOmInputErGyldig()) {
                        formElem.addClass('feil');
                    }
                });

                scope.$watch(function() {
                    return ngModel.$viewValue;
                }, function() {
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

                    if(typeof feil === 'object') {
                        feilmeldingsNokkel = feil[feilNokkel];
                    }

                    var feilmeldingTekst = cms.tekster[feilmeldingsNokkel];
                    formElem.find('.melding').text(feilmeldingTekst);
                }
            }
        }
    }]);