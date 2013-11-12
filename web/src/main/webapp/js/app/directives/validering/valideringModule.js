angular.module('nav.validering', ['nav.cmstekster'])
    .directive('blurValidate', ['data', function (data) {
        return {
            require: ['ngModel', '^form'],
            link: function (scope, element, attrs, ctrls) {
                var ngModel = ctrls[0];
                var form = ctrls[1];
                var eventString = 'RUN_VALIDATION' + form.$name;

                var feil;
                try {
                    feil = scope.$eval(attrs.errorMessages);
                } catch(e) {
                    feil = attrs.errorMessages;
                }

                var revaliderFeilMetode;
                var valideringsMetoder = [];

                // Rekkefølgen på setup-metodene bestemmer prioriteten på valideringsmetodene
                RequiredValidator.init(attrs, valideringsMetoder);
                PatternValidator.init(attrs, valideringsMetoder);
                LengthValidator.init(attrs, valideringsMetoder);

                var formElem = element.closest('.form-linje');

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
                    var erGyldig = true;
                    for (var i = 0; i < valideringsMetoder.length; i++) {
                        erGyldig = erGyldig && valideringsMetoder[i](ngModel.$viewValue);

                        if (erGyldig != true) {
                            revaliderFeilMetode = valideringsMetoder[i];
                            settFeilmeldingsTekst(erGyldig);
                            return false;
                        }
                    }
                    return erGyldig;
                }

                function settFeilmeldingsTekst(feilNokkel) {
                    var feilmeldingsNokkel = feil;

                    if(typeof feil === 'object') {
                        feilmeldingsNokkel = feil[feilNokkel];
                    }

                    var feilmeldingTekst = data.tekster[feilmeldingsNokkel];
                    formElem.find('.melding').text(feilmeldingTekst);
                }
            }
        }
    }]);