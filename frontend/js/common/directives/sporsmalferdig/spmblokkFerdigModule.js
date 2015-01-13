angular.module('nav.sporsmalferdig', [])
	.directive('spmblokkferdig', ['$timeout', 'data', function ($timeout, data) {
		return {
			require    : '^form',
			replace    : true,
			templateUrl: '../js/common/directives/sporsmalferdig/spmblokkFerdigTemplate.html',
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
            templateUrl: '../js/common/directives/sporsmalferdig/vedleggblokkFerdigTemplate.html',
            link: function (scope, element, attrs, form) {
                var tab = element.closest('.accordion-group');
                scope.gaaTilNeste = function () {
                    if(form.$valid) {
                        var nesteTab = tab.next();
                        apneBolk(nesteTab);
                        lukkBolk(tab);
                        gaaTilTab(nesteTab);
                    } else {
                        angular.forEach(form.$error, function (feil) {
                            var el = finnElementMedFeilOgSettFeilmelding(feil[0], scope);
                            scrollToElement(el, 200);
                            el.focus();
                        });
                    }
                };

                function finnElementMedFeilOgSettFeilmelding(feil, scope) {
                    var selector = "[data-error-messages=\"" + feil.$elementErrorAttr + "\"], " +
                        "[error-messages=\"" + feil.$elementErrorAttr + "\"]";

                    var el = element.closest('[data-ng-form]').find(selector);
                    if(el.is(":visible")) {
                        el.closest(".form-linje").addClass("feil");
                        return el;
                    } else {
                        scope.validert.value = true;
                        scope.skalViseFeil.value = true;
                        return element.closest('[data-ng-form]').find("[type=\"radio\"]").first();
                    }
                }

                function gaaTilTab(nyTab) {
                    if (nyTab.length > 0) {
                        $timeout(function () {
                            scrollToElement(nyTab, 0, 200);
                            settFokus(nyTab);
                        }, 50);
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

                function settFokus(tab) {
                    tab.closest('.accordion-group').find('a').focus();
                }
            }
        };
    }]);
