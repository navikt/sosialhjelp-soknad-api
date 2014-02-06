angular.module('nav.sporsmalferdig', [])
	.directive('spmblokkferdig', ['$timeout', 'data', function ($timeout, data) {
		return {
			require    : '^form',
			replace    : true,
			templateUrl: '../js/app/directives/sporsmalferdig/spmblokkFerdigTemplate.html',
			scope      : {
				submitMethod: '&'
			},
			link: function (scope, element, attrs, form) {
                scope.knappTekst = 'neste';

				var tab = element.closest('.accordion-group');

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
                        }
                    }
                );

				scope.validerOgGaaTilNeste = function () {
					scope.submitMethod();
					if (form.$valid) {
                        var bolkerFaktum = data.finnFaktum('bolker');
                        bolkerFaktum.properties[tab.attr('id')] = "true";
                        bolkerFaktum.$save();
                        form.$setPristine();
                        tab.addClass('validert');
                        lukkTab(tab);

                        var nesteTab;
                        if (scope.knappTekst === 'lagreEndring') {
                            nesteTab = tab.nextAll().not('.validert').first();
                        } else {
                            nesteTab = tab.next();
                        }

                        if (nesteTab.length > 0) {
                            gaaTilTab(nesteTab);
                            apneTab(nesteTab);
                            setFokus(nesteTab);
                        } else {
                            gaaTilTab(angular.element('.accordion-group').last());
                        }
					}
				};

				function gaaTilTab(nyTab) {
					if (nyTab.length > 0) {
						$timeout(function () {
							scrollToElement(nyTab, 0);
						}, 0);
					}
				}

				function apneTab(apneTab) {
					scope.$emit('OPEN_TAB', apneTab.attr('id'));
				}

				function lukkTab(lukkTab) {
					scope.$emit('CLOSE_TAB', lukkTab.attr('id'));
				}

                function setFokus(tab) {
                    tab.closest('.accordion-group').find('a').focus();
                }
			}
		}
	}]);
