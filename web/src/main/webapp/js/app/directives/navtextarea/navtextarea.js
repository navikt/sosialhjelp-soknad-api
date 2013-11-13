angular.module('nav.textarea', [])
    .directive('navtextarea', [function () {
        return {
            replace: true,
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                nokkel: '@',
                maxlengde: '@',
                inputname: '@',
                feilmelding: '@'
            },
            controller: function ($scope) {
                $scope.sporsmal = $scope.nokkel + ".sporsmal";
                $scope.feilmelding = $scope.nokkel + ".feilmelding";
                $scope.tellertekst = $scope.nokkel + ".tellertekst";
            },

            link: function (scope, element, attrs, ctrl) {
                scope.counter = scope.maxlengde;
                scope.fokus = false;
                scope.feil = false;

                var tmpElementName = 'tmpName';
                fiksNavn(element, scope.inputname, tmpElementName);
            },
            templateUrl: '../js/app/directives/navtextarea/navtextareaTemplate.html'
        };
    }])
    .directive('validateTextarea', [function() {
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
                    } else {
                        ctrl.$setValidity(scope.nokkel, true);
                        scope.feil = false;
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
                    scope.$emit("OPPDATER_OG_LAGRE", {key: element.attr('name'), value: verdi});
                })
            }
        }
    }]);
