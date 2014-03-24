angular.module('nav.sporsmalferdig', [])
	.directive('spmblokkferdig', ['$timeout', 'data', function ($timeout, data) {
		return {
			require    : '^form',
			replace    : true,
			templateUrl: '../js/dagpenger/directives/sporsmalferdig/spmblokkFerdigTemplate.html',
			link: function (scope, element, attrs, form) {
                var tab = element.closest('.accordion-group');

                scope.knappTekst = 'neste';
                scope.leggTilValideringsmetode(tab.attr('id'), scope.valider);

                scope.$watch(
                    function() {
                        return tab.hasClass('validert');
                    },
                    function(newVal, oldVal) {
                        if (newVal === oldVal) {
                            return;
                        }
                        if (!newVal) {
                            scope.knappTekst = 'lagreEndring';
                        } else {
                            scope.knappTekst = 'neste';
                        }
                    }
                );

				scope.validerOgGaaTilNeste = function () {
					scope.valider(true);
					if (form.$valid) {
                        var bolkerFaktum = data.finnFaktum('bolker');
                        bolkerFaktum.properties[tab.attr('id')] = "true";
                        bolkerFaktum.$save();
                        form.$setPristine();
                        tab.addClass('validert');

                        var nesteTab;
                        if (scope.knappTekst === 'lagreEndring') {
                            nesteTab = tab.nextAll().not('.validert').first();
                        } else {
                            nesteTab = tab.next();
                        }

                        if (nesteTab.length > 0) {
                            gaaTilTab(nesteTab);
                            apneBolk(nesteTab);
                            setFokus(nesteTab);
                        } else {
                            gaaTilTab(angular.element('.accordion-group').last());
                        }
					}
				};

				function gaaTilTab(nyTab) {
					if (nyTab.length > 0) {
						$timeout(function () {
                            scrollToElement(nyTab, 120);
						}, 0);
					}
				}

				function apneBolk(apneTab) {
					scope.apneTab(apneTab.attr('id'));
				}

                function setFokus(tab) {
                    tab.closest('.accordion-group').find('a').focus();
                }
			}
		};
	}])
    .directive('vedleggblokkferdig', ['$timeout', function ($timeout) {
        return {
            require    : '^form',
            replace    : true,
            templateUrl: '../js/dagpenger/directives/sporsmalferdig/vedleggblokkFerdigTemplate.html',
            link: function (scope, element, attrs, form) {
                var tab = element.closest('.accordion-group');
                scope.gaaTilNeste = function () {
                    var nesteTab = tab.next();
                    lukkBolk(tab);
                    gaaTilTab(nesteTab);
                    apneBolk(nesteTab);
                    setFokus(nesteTab);
                };

                function gaaTilTab(nyTab) {
                    if (nyTab.length > 0) {
                        $timeout(function () {
                            scrollToElement(nyTab, 120);
                        }, 0);
                    }
                }

                function toggleBolk(tab) {
                    $timeout(function() {
                        tab.find('.accordion-toggle').trigger('click');

                    });
                }

                function apneBolk(tab) {
                    if (!tab.find('.accordion-body').hasClass('in')) {
                        toggleBolk(tab);
                    }
                }

                function lukkBolk(tab) {
                    if (tab.find('.accordion-body').hasClass('in')) {
                        toggleBolk(tab);
                    }
                }

                function setFokus(tab) {
                    tab.closest('.accordion-group').find('a').focus();
                }
            }
        };
    }]);
