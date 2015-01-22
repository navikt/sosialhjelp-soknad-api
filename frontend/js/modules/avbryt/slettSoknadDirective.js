angular.module('nav.avbryt.slettSoknadDirective', [])
    .directive('slettSoknad', function (data, soknadService, $location) {
        return {
            scope: false,
            templateUrl: '../js/modules/avbryt/templates/slettSoknadTemplate.html',
            link: function(scope) {
                scope.fremdriftsindikator = {
                    laster: false

                };
                scope.brukerBehandlingId = data.soknad.brukerBehandlingId;

                scope.krevBekreftelse = data.fakta.filter(function(item) {
                    return item.type==="BRUKERREGISTRERT" && (item.value !== null || Object.keys(item.properties).length > 0);
                }).length > 1;

                scope.submitForm = function () {
                    var start = $.now();
                    scope.fremdriftsindikator.laster = true;

                    soknadService.remove({soknadId: data.soknad.soknadId},
                        function () { // Success
                            var delay = 1500 - ($.now() - start);
                            setTimeout(function () {
                                scope.$apply(function () {
                                    $location.path('/avbrutt');
                                });
                            }, delay);
                        },
                        function () { // Error
                            scope.fremdriftsindikator.laster = false;
                        }
                    );
                };

                if (!scope.krevBekreftelse) {
                    scope.submitForm();
                }
            }
        };
    });