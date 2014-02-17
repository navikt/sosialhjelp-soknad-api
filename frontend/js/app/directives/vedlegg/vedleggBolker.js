angular.module('nav.vedleggbolker', [])
    .directive('triggBolker', ['$timeout', function ($timeout) {
        return {
            require: 'form',
            link: function ($scope, element, attrs, ctrl) {
                $timeout(function () {
                    var bolker = $(".accordion-group");
                    var bolkerMedFeil = finnBolkerMedOgUtenFeil(bolker);

                    apneForsteBolkMedFeil(bolkerMedFeil.medFeil[0]);
                }, 500);

                //Bolker med feil og som er lukket skal åpnes ved validering av hele vedleggsiden
                element.find('#til-oppsummering').bind('click', function () {
                    if (ctrl.$invalid) {
                        var alleBolker = finnAlleBolker();
                        var inndeltBolker = finnBolkerMedOgUtenFeil(alleBolker);
                        apneFeilBolkerSomIkkeErApenFraFor(inndeltBolker.medFeil);
                        lukkeRiktigBolkerSomIkkErLukketFraFor(inndeltBolker.utenFeil);
                    }
                });

                function apneFeilBolkerSomIkkeErApenFraFor(bolker) {
                    for (var i = 0; i < bolker.length; i++) {
                        var bolk = $(bolker[i]);
                        if (!(bolk.hasClass('open'))) {
                            bolk.find('.accordion-toggle').trigger('click');
                        }
                    }
                }

                function lukkeRiktigBolkerSomIkkErLukketFraFor(bolker) {
                    for (var i = 0; i < bolker.length; i++) {
                        var bolk = $(bolker[i]);
                        if (bolk.hasClass('open')) {
                            bolk.find('.accordion-toggle').trigger('click');
                        }
                    }
                }

                function apneForsteBolkMedFeil(bolk) {
                    if(!($(bolk).hasClass('open'))) {
                        $(bolk).find('.accordion-toggle').trigger('click');
                    }
                }

                function bolkHarFeil(bolk) {
                    return !($(bolk).find('.vedlegg-bolk').hasClass('behandlet'));
                }

                function finnAlleBolker() {
                    return $(".accordion-group");
                }

                function finnBolkerMedOgUtenFeil(bolker) {
                    var bolkerMedFeil = [];
                    var bolkerUtenfeil = [];

                    for (var i = 0; i < bolker.length; i++) {
                        if (bolkHarFeil(bolker[i])) {
                            bolkerMedFeil.push(bolker[i]);
                        } else {
                            bolkerUtenfeil.push(bolker[i]);
                        }
                    }
                    var resultatBolker = {medFeil: bolkerMedFeil, utenFeil: bolkerUtenfeil };
                    return resultatBolker;
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
                    }, 200);
                });
            }
        };
    }]);
