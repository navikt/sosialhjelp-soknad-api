angular.module('nav.textarea', [])
    .directive('navtextarea', [function () {
        var linker = function (scope, element, attrs) {
            if (scope.attr("data-obligatorisk")) {
                return '../js/common/directives/navtextarea/navtextareaObligatoriskTemplate.html';
            } else {
                return '../js/common/directives/navtextarea/navtextareaTemplate.html';
            }
        }

        return {
            restrict: "A",
            replace: true,
            scope: true,
            link: {
                pre: function (scope, element, attr) {
                    scope.nokkel = attr.nokkel;
                    scope.sporsmal = attr.nokkel + ".sporsmal";
                    scope.feilmelding = attr.nokkel + ".feilmelding";
                    scope.tellertekst = attr.nokkel + ".tellertekst";
                    scope.maxlengde = attr.maxlengde;
                    scope.counter = attr.maxlengde;

                },
                post: function (scope, element) {
                    scope.feil = false;

                    var harFokus = false;

                    scope.hvisSynlig = function () {
                        return element.is(':visible');
                    };

                    element.find('textarea').bind('focus', function() {
                        harFokus = true;
                    });

                    element.find('textarea').bind('blur', function() {
                        harFokus = false;
                    });

                    scope.harFokusOgFeil = function() {
                        return scope.feil || harFokus;
                    };
                }},
            templateUrl: linker
        };


    }])
    .directive('validateTextarea', ['$timeout', 'cms', function ($timeout, cms) {
        return {
            require: ['ngModel', '^form'],
            link: function (scope, element, attrs, ctrls) {
                var ngModel = ctrls[0];
                var form = ctrls[1];

                var settStorrelse = function () {
                    element.css('height', '0px');
                    element.height(element[0].scrollHeight);

                }
                var validerOgOppdater = function (viewValue) {
                    if (viewValue != undefined) {
                        settStorrelse();
                        scope.counter = scope.maxlengde - viewValue.length;
                        var valid = validerAntallTegn();
                        valid = valid && validerTom(viewValue);
                        if (valid) {
                            ngModel.$setValidity(scope.nokkel, true);
                        } else {
                            ngModel.$setValidity(scope.nokkel, false);
                        }
                    } else {
                        scope.counter = scope.maxlengde;
                    }

                }
                ngModel.$formatters.push(function (viewValue) {
                    validerOgOppdater(viewValue);
                    return viewValue;
                });
                ngModel.$parsers.unshift(function (viewValue) {
                    validerOgOppdater(viewValue);
                    return viewValue;
                });
                $timeout(settStorrelse);

                function validerAntallTegn() {
                    if (scope.counter < 0) {
                        scope.feil = true;
                        settFeilmeldingsTekst();
                        return false;

                    } else {
                        scope.feil = false;
                        element.closest('.form-linje').removeClass('feil');
                        return true;
                    }
                }

                function validerTom(viewValue) {
                    if (viewValue == undefined || viewValue.length == 0) {
                        ngModel.$setValidity(scope.nokkel, true);
                        settFeilmeldingsTekst();
                        element.closest('.form-linje').addClass('feil');
                        return false;
                    }
                    return true;
                }

                function settFeilmeldingsTekst() {
                    var feilmeldingTekst = cms.tekster['textarea.feilmleding'];
                    if (scope.counter > -1) {
                        var feilmeldingsNokkel = element[0].getAttribute('data-error-messages').toString();
                        //hack for å fjerne dobbeltfnuttene rundt feilmeldingsnokk
                        feilmeldingTekst = cms.tekster[feilmeldingsNokkel.substring(1, feilmeldingsNokkel.length - 1)];
                    }
                    element.closest('.form-linje').find('.melding').text(feilmeldingTekst);
                }

                var eventString = 'RUN_VALIDATION' + form.$name;
                scope.$on(eventString, function () {
                    validerAntallTegn();
                    validerTom(ngModel.$viewValue);
                    if (ngModel.$invalid) {
                        element.closest('.form-linje').addClass('feil');
                    } else {
                        element.closest('.form-linje').removeClass('feil');
                    }
                })

                scope.mistetFokus = function () {
                    validerAntallTegn();
                    validerTom(ngModel.$viewValue);
                    if (ngModel.$invalid) {
                        element.closest('.form-linje').addClass('feil');
                    } else {
                        element.closest('.form-linje').removeClass('feil');
                    }
                    scope.lagreFaktum();
                };
            }
        }
    }]);
