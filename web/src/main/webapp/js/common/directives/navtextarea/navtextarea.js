angular.module('nav.textarea', [])
    .directive('navtextarea', [function () {
        var linker = function(scope,element, attrs){
            if(scope.attr("data-obligatorisk")) {
                return '../js/common/directives/navtextarea/navtextareaObligatoriskTemplate.html';
            } else {                
                return '../js/common/directives/navtextarea/navtextareaTemplate.html';
            }
        }

        return {
            restrict: "A",
            replace: true,
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                modus: '=',
                nokkel: '@',
                maxlengde: '@',
                inputname: '@',
                feilmelding: '@',
                obligatorisk: '@'
            },
            controller: function ($scope) {
                $scope.sporsmal = $scope.nokkel + ".sporsmal";
                $scope.feilmelding = $scope.nokkel + ".feilmelding";
                $scope.tellertekst = $scope.nokkel + ".tellertekst";
            },

            link: function (scope, element) {
                scope.counter = scope.maxlengde;
                scope.fokus = false;
                scope.feil = false;

                var tmpElementName = 'tmpName';
                fiksNavn(element, scope.inputname, tmpElementName);

                scope.hvisIRedigeringsmodus = function () {
                    return scope.modus;
                }

                scope.hvisIOppsummeringsmodus = function () {
                    return !scope.hvisIRedigeringsmodus();
                }

                scope.hvisSynlig = function () {
                    return element.is(':visible');
                }
            },
            templateUrl: linker
        };


         
    }])
    .directive('validateTextarea',['cms', function (cms) {
        return {
            require: 'ngModel',
            link: function(scope, element, attrs, ctrl) {
                scope.$watch(function() {
                    return ctrl.$viewValue;
                }, function() {
                    if (ctrl.$viewValue != undefined) {
                        element.css('height', '0px');
                        element.height(element[0].scrollHeight);
                        scope.counter = scope.maxlengde - ctrl.$viewValue.length;
                        validerAntallTegn();
                    }
                });

                function validerAntallTegn() {
                    if (scope.counter < 0) {
                        ctrl.$setValidity(scope.nokkel, false);
                        scope.feil = true;
                        element.closest('.form-linje').addClass('feil');
                        settFeilmeldingsTekst();

                    } else {
                        ctrl.$setValidity(scope.nokkel, true);
                        scope.feil = false;
                        element.closest('.form-linje').removeClass('feil');
                    }
                }

                element.bind('focus', function () {
                    scope.fokus = true;
                    scope.$apply(attrs.onFocus);
                })

                element.bind('blur', function () {
                    scope.fokus = false;
                    scope.$apply(attrs.onBlur)
                    validerAntallTegn();
                    var verdi = element.val().toString();
                    if(ctrl.$viewValue == undefined || ctrl.$viewValue.length == 0) {
                        settFeilmeldingsTekst();
                        element.closest('.form-linje').addClass('feil');
                    }
                    scope.$emit("OPPDATER_OG_LAGRE", {key: element.attr('name'), value: verdi});
                })

                function settFeilmeldingsTekst() {
                    var feilmeldingTekst = cms.tekster['textarea.feilmleding'];
                    if(scope.counter > -1) {
                       var feilmeldingsNokkel = element[0].getAttribute('data-error-messages').toString();
                        //hack for å fjerne dobbeltfnuttene rundt feilmeldingsnokk
                        feilmeldingTekst = cms.tekster[feilmeldingsNokkel.substring(1, feilmeldingsNokkel.length - 1)];
                    }
                    element.closest('.form-linje').find('.melding').text(feilmeldingTekst);
                }
            }
        }
    }]);
