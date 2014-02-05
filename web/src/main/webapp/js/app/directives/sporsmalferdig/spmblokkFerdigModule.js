angular.module('nav.sporsmalferdig', [])
	.directive('spmblokkferdig', ['$timeout', 'data', function ($timeout, data) {
		return {
			require    : '^form',
			replace    : true,
			templateUrl: '../js/app/directives/sporsmalferdig/spmblokkFerdigTemplate.html',
			scope      : {
				nokkel      : '@',
				submitMethod: '&'
			},
			link       : function (scope, element, attrs, form) {
				var tab = element.closest('.accordion-group');

				scope.validerOgGaaTilNeste = function () {
					scope.submitMethod();
					if (form.$valid) {
                        var bolkerFaktum = data.finnFaktum('bolker');
                        bolkerFaktum.properties[tab.attr('id')] = "true";
                        bolkerFaktum.$save();
                        form.$setPristine();
                        lukkTab(tab);

                        var nesteInvalidTab = tab.nextAll().not('.validert').first();
                        if (nesteInvalidTab.length > 0) {
                            gaaTilTab(nesteInvalidTab.prev());
                            apneTab(nesteInvalidTab);
                            setFokus(nesteInvalidTab);
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
