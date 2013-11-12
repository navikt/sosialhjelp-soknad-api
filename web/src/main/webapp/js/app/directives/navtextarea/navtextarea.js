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

                $(element).on('keyup', 'textarea', function (e) {
                    $(this).css('height', '0px');
                    $(this).height(this.scrollHeight);
                });
                $(element).find('textarea').keyup();

                element.find('textarea').bind('focus', function () {
                    scope.fokus = true;
                    scope.$apply(attrs.onFocus);

                    $(this).css('height', '0px');
                    $(this).height(this.scrollHeight);

                })
                element.find('textarea').bind('blur', function () {
                    scope.fokus = false;
                    scope.$apply(attrs.onBlur)
                    validerAntallTegn();
                    var verdi = element.find('textarea').val().toString();

                    if (scope.counter > -1) {
                        scope.$emit("OPPDATER_OG_LAGRE", {key: element.find('textarea').attr('name'), value: verdi});
                    }
                })

                scope.oppdaterTeller = function () {
                    if (scope.model) {
                        scope.counter = scope.maxlengde - scope.model.length;
                        validerAntallTegn();
                    } else {
                        scope.counter = scope.maxlengde;
                    }
                }

                function validerAntallTegn() {
                    if (scope.counter < 0) {
                        ctrl.$setValidity(scope.nokkel, false);
                        scope.feil = true;
                    } else {
                        ctrl.$setValidity(scope.nokkel, true);
                        scope.feil = false;
                    }
                }

                validerAntallTegn();
            },
            templateUrl: '../js/app/directives/navtextarea/navtextareaTemplate.html'
        };
    }]);
