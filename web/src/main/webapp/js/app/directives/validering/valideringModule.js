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
                } catch (e) {
                    feil = attrs.errorMessages;
                }

                // Rekkefølgen på setup-metodene bestemmer prioriteten på valideringsmetodene
                RequiredValidator.init(attrs, valideringsMetoder);
                PatternValidator.init(attrs, valideringsMetoder);
                LengthValidator.init(attrs, valideringsMetoder);

                scope.$on(eventString, function () {
                    if (!sjekkOmInputErGyldig() && element.is(':visible')) {
                        formElem.addClass('feil');
                    }
                });

                element.bind('blur', function () {
                    if (!sjekkOmInputErGyldig()) {
                        formElem.addClass('feil');
                    }
                });

                scope.$watch(function () {
                    return element.is(':visible');
                }, function (erSynlig) {
                    if (!erSynlig) {
                        formElem.removeClass('feil');
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

                    var feilmeldingTekst = cms.tekster[feilmeldingsNokkel];
                    formElem.find('.melding').text(feilmeldingTekst);
                }
            }
        }
    }])

    .directive('clickValidate', ['$timeout', 'cms', function ($timeout, cms) {
        return {
            require: ['ngModel', '^form'],
            link: function (scope, element, attrs, ctrls) {
                var ngModel = ctrls[0];
                var form = ctrls[1];
                var eventString = 'RUN_VALIDATION' + form.$name;

                scope.$on(eventString, function () {
                    if (sjekkOmFeltetErRequired() && !sjekkOmFeltetErSvart()) {
                        element.closest('.form-linje').addClass('feil');
                    }
                });

                scope.$watch(function () {
                    return ngModel.$viewValue;
                }, function () {
                    if (ngModel.$viewValue && element.closest('.form-linje').hasClass('feil')) {
                        fadeBakgrunnsfarge(element.closest('.form-linje'), element.closest('.form-linje').find('.melding'), 'feil', scope);
                    }
                });

                function sjekkOmFeltetErRequired() {
//                    return element[0].hasOwnProperty("required");
                    return element[0].hasAttribute("required");
                }

                function sjekkOmFeltetErSvart() {
                    if (!ngModel.$modelValue) {
                        settFeilmeldingsTekst();
                        return false;
                    }
                    return true;
                }

                function settFeilmeldingsTekst() {
                    var feilmeldingsNokkel = element[0].getAttribute('data-error-messages').toString();
                    //hack for å fjerne dobbeltfnuttene rundt feilmeldingsnokk
                    var feilmeldingTekst = cms.tekster[feilmeldingsNokkel.substring(1, feilmeldingsNokkel.length - 1)];
                    element.closest('.form-linje').find('.melding').text(feilmeldingTekst);
                }
            }
        }
    }])
//    direktivet skal brukes på div-en som ligger rundt en checkboksgruppe og som skal ha inlinevalidering
    .directive('checkboxValidate', ['cms', function (cms) {
        return {
            require: ['^form'],
            link: function (scope, element, attrs, ctrl) {
                var eventString = 'RUN_VALIDATION' + ctrl[0].$name;

                scope.$on(eventString, function () {
                    if (element.find("input:checked").length > 0) {
                        element.closest('.form-linje').removeClass('feil');
                    } else if (element.find("input:checked").length == 0 && element.is(':visible')) {
                        if (element.closest('.form-linje').hasClass('checkbox')) {
                            element.closest('.form-linje').addClass('feilstyling');
                        } else {
                            element.closest('.form-linje').addClass('feil');
                        }
                    }
                })

                scope.$watch(function () {
                    return element.find("input:checked").length;
                }, function () {
                    if (element.find("input:checked").length > 0 && element.closest('.form-linje').hasClass('feil')) {
                        fadeBakgrunnsfarge(element.closest('.form-linje'), element.closest('.form-linje').find('.melding'), 'feil', scope);
                    } else if (element.find("input:checked").length > 0 && element.closest('.form-linje').hasClass('feilstyling')) {
                        fadeBakgrunnsfarge(element.closest('.form-linje'), element.closest('.form-linje.feilstyling').children('.melding'), 'feilstyling', scope);
                    }
                });
            }
        }
    }])
    .directive('dateValidate', ['cms', function (cms) {
        return {
            require: ['ngModel', '^form'],
            link: function (scope, element, attrs, ctrls) {
                var ngModel = ctrls[0];
                var form = ctrls[1];
                var eventString = 'RUN_VALIDATION' + form.$name;

                scope.$on(eventString, function () {
                    if (sjekkOmFeltetErRequired() && !sjekkOmFeltetErSvart()) {
                        element.closest('.form-linje').addClass('feil');
                    }
                });

                element.bind('blur', function () {
                    angular.forEach(form.$error, function (verdi, feilNokkel) {
                        var feilmeldignsnavn = attrs.id.substring(0, attrs.id.lastIndexOf('.'));
                        angular.forEach(verdi, function (feil) {
                            if (feil === undefined && feilNokkel.indexOf(feilmeldignsnavn) !== -1) {
                                settFraTilDatoFeilmeldingstekst(feilNokkel)
                                //fjerner feilmeldingen om at fra må være før til, så ved lagring vil brukeren få beskjed om required-feilmeldingen i stedet
                                form.$setValidity(feilNokkel, true);
                            }
                        })
                    })
                });

                scope.$watch(function () {
                    return ngModel.$viewValue;
                }, function () {
                    if (ngModel.$viewValue) {
                        fjernFeilKlassen();
                    }
                });

                function sjekkOmFeltetErRequired() {
                    return element[0].hasOwnProperty("required");
                }

                function sjekkOmFeltetErSvart() {
                    if (!ngModel.$modelValue) {
                        settRequiredFeilmeldingsTekst();
                        return false;
                    }
                    return true;
                }

                function settRequiredFeilmeldingsTekst() {
                    var feilmeldingsNokkel = element[0].getAttribute('error-messages').toString();
                    //hack for å fjerne dobbeltfnuttene rundt feilmeldingsnokk
                    var feilmeldingTekst = cms.tekster[feilmeldingsNokkel.substring(1, feilmeldingsNokkel.length - 1)];
                    element.closest('.form-linje').find('.melding').text(feilmeldingTekst);
                }

                function settFraTilDatoFeilmeldingstekst(feilNokkel) {
                    var feilmeldingTekst = cms.tekster[feilNokkel];
                    var tilElement = element.closest('.varighet').find('.til');
                    tilElement.find('.melding').text(feilmeldingTekst);
                    tilElement.addClass('feil');
                }

                function fjernFeilKlassen() {
                    element.closest('.form-linje').removeClass('feil');
                }
            }
        }
    }]);