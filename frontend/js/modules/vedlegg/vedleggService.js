angular.module('nav.vedlegg.service', [])
    .factory('bolk', function () {

        function bolkHarFeil (bolk) {
            return $(bolk).find('.vedlegg-bolk').hasClass('ekstraVedlegg') || !($(bolk).find('.vedlegg-bolk').hasClass('behandlet'));
        }

        return {
            apneFeilBolkerSomIkkeErApenFraFor: function (bolker) {
                for (var i = 0; i < bolker.length; i++) {
                    var bolk = angular.element(bolker[i]);

                    if (!(bolk.hasClass('open'))) {
                        bolk.find('.accordion-toggle').triggerHandler('click');
                    }
                }
            },
            lukkeRiktigBolkerSomIkkErLukketFraFor: function (bolker) {
                for (var i = 0; i < bolker.length; i++) {
                    var bolk = angular.element(bolker[i]);
                    if (bolk.hasClass('open')) {
                        bolk.find('.accordion-toggle').triggerHandler('click');
                    }
                }
            },
            apneForsteBolkMedFeil: function (bolk) {
                var bolkElement = angular.element(bolk);
                if(!(bolkElement.hasClass('open'))) {
                    bolkElement.find('.accordion-toggle').triggerHandler('click');
                }
            },
            finnAlleBolker: function () {
                return $(".accordion-group");
            },
            finnBolkerMedOgUtenFeil: function (bolker) {
                var bolkerMedFeil = [];
                var bolkerUtenfeil = [];

                for (var i = 0; i < bolker.length; i++) {
                    if (bolkHarFeil(bolker[i])) {
                        bolkerMedFeil.push(bolker[i]);
                    } else {
                        bolkerUtenfeil.push(bolker[i]);
                    }
                }
                return {medFeil: bolkerMedFeil, utenFeil: bolkerUtenfeil };
            }
        };
    });
