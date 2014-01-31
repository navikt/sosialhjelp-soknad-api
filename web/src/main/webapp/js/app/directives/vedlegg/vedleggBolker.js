angular.module('nav.vedleggbolker', [])
    .directive('triggBolker', ['$timeout', function ($timeout) {

        return {
            link: function($scope, element) {
                $timeout(function() {
                   var bolker = $(".accordion-group");
                   var bolkerMedFeil = finnBolkerMedFeil(bolker);

                    apneForsteBolkMedFeil(bolkerMedFeil[0]);
                }, 500);


                $scope.$on('APNE_VEDLEGGBOLKER', function () {
                    var alleBolker = finnAlleBolker();
                    apneBolker(alleBolker);
                });

                function apneBolker(bolker) {
                    for(var i =0; i<bolker.length; i++) {
                        $(bolker[i]).find('.accordion-toggle').trigger('click');
                    }
                }

                function apneForsteBolkMedFeil(bolk) {
                    $(bolk).find('.accordion-toggle').trigger('click');
                }

                function bolkHarFeil(bolk) {
                    return !($(bolk).hasClass('behandlet') || $(bolk).hasClass('lastetopp'));
                }

                function finnAlleBolker() {
                    return $(".accordion-group");
                }
                function finnBolkerMedFeil(bolker) {
                    var bolkerMedFeil = [];

                    for(var i = 0; i<bolker.length; i++) {
                        if(bolkHarFeil(bolker[i])) {
                            bolkerMedFeil.push(bolker[i]);
                        }
                    }
                    return bolkerMedFeil;
                }
            }
        };
    }]);
