angular.module('nav.textarea', [])
    .directive('navtextarea', [function () {
        return {
            replace: true,
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                nokkel: '@',
                maxlengde: '@'
            },
            controller: function ($scope) {
                $scope.sporsmal = $scope.nokkel + ".sporsmal";
                $scope.feilmelding = $scope.nokkel + ".feilmelding";
                $scope.tellertekst = $scope.nokkel + ".tellertekst";
                $scope.counter = $scope.maxlengde;
                $scope.fokus = false;
                $scope.oppdaterTeller = function () {
                    if ($scope.model) {
                        $scope.counter = $scope.maxlengde - $scope.model.length;
                    } else {
                        $scope.counter = $scope.maxlengde;
                    }
                }
            },
            templateUrl: '../js/app/directives/navtextarea/navtextareaTemplate.html',
            link: function ($scope, element, attrs) {
                $(element).on('keyup', 'textarea', function (e) {
                    $(this).css('height', '0px');
                    $(this).height(this.scrollHeight);
                });
                $(element).find('textarea').keyup();

                element.find('textarea').bind('focus', function () {
                    $scope.fokus = true;
                    $scope.$apply(attrs.onFocus);
                })
                element.find('textarea').bind('blur', function () {
                    $scope.fokus = false;
                    $scope.$apply(attrs.onBlur)
                })


            }
        };
    }]);
