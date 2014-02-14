angular.module('nav.vedleggbolker', [])
    .directive('triggBolker', ['$timeout', function ($timeout) {
        return {
            require: 'form',
            link: function ($scope, element, attrs, ctrl) {
                $timeout(function () {
                    var bolker = $(".accordion-group");
                    var bolkerMedFeil = finnBolkerMedFeil(bolker);

                    apneForsteBolkMedFeil(bolkerMedFeil[0]);
                }, 500);

                //Bolker med feil og som er lukket skal åpnes ved validering av hele vedleggsiden
                element.find('#til-oppsummering').bind('click', function () {
                    if (ctrl.$invalid) {
                        var alleBolker = finnAlleBolker();
                        var alleBolkerMedFeil = finnBolkerMedFeil(alleBolker);
                        apneBolkerSomIkkeErApenFraFor(alleBolkerMedFeil);
                    }
                });

                function apneBolkerSomIkkeErApenFraFor(bolker) {
                    for (var i = 0; i < bolker.length; i++) {
                        var bolk = $(bolker[i]);
                        if (!(bolk.hasClass('open'))) {
                            bolk.find('.accordion-toggle').trigger('click');
                        }
                    }
                }

                function apneForsteBolkMedFeil(bolk) {
                    $(bolk).find('.accordion-toggle').trigger('click');
                }

                function bolkHarFeil(bolk) {
                    return !($(bolk).find('.vedlegg-bolk').hasClass('behandlet'));
                }

                function finnAlleBolker() {
                    return $(".accordion-group");
                }

                function finnBolkerMedFeil(bolker) {
                    var bolkerMedFeil = [];

                    for (var i = 0; i < bolker.length; i++) {
                        if (bolkHarFeil(bolker[i])) {
                            bolkerMedFeil.push(bolker[i]);
                        }
                    }
                    return bolkerMedFeil;
                }
            }
        };
    }])
    .directive('apneAnnetVedlegg', ['$timeout', function ($timeout) {
        return {
            link: function ($scope, element) {
                element.bind('click', function () {
                    $timeout(function () {
                        $(".accordion-toggle").last().trigger('click');
                    }, 150)
                })
            }
        };
    }]);
