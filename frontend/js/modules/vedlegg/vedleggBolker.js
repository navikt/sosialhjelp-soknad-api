angular.module('nav.vedlegg.bolker', [])
    .directive('triggBolker', function ($timeout, bolk) {
        return {
            require: 'form',
            link: function ($scope, element, attrs, ctrl) {
                $timeout(function () {
                    var bolker = $(".accordion-group");
                    var bolkerMedFeil = bolk.finnBolkerMedOgUtenFeil(bolker);
                    bolk.apneForsteBolkMedFeil(bolkerMedFeil.medFeil[0]);
                }, 500);

                //Bolker med feil og som er lukket skal åpnes ved validering av hele vedleggsiden
                element.find('#til-oppsummering').bind('click', function () {
                    if (ctrl.$invalid) {
                        var alleBolker = bolk.finnAlleBolker();
                        var inndeltBolker = bolk.finnBolkerMedOgUtenFeil(alleBolker);
                        bolk.apneFeilBolkerSomIkkeErApenFraFor(inndeltBolker.medFeil);
                        bolk.lukkeRiktigBolkerSomIkkErLukketFraFor(inndeltBolker.utenFeil);
                    }
                });
            }
        };
    })
    .directive('apneAnnetVedlegg', function ($interval, $timeout) {
        return {
            link: function ($scope, element) {
                element.bind('click', function () {
                    var lastElementId = $(".accordion-toggle").last().attr('id');
                    var interval = $interval(function () {
                        var lastElement = $(".accordion-toggle").last();
                        if (lastElement.attr('id') !== lastElementId) {
                            $timeout(function() {
                                // Fysj og fy...
                                $('.accordion-group.open .accordion-toggle').not('#' + lastElement.attr('id')).trigger('click');
                                lastElement.trigger('click');
                            });
                            $interval.cancel(interval);
                        }
                    }, 100, 30);
                });
            }
        };
    });
