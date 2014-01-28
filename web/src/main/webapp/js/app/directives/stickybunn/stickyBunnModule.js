angular.module('nav.stickybunn', [])
	.directive('sistLagret', ['data', '$window', '$timeout', function (data, $window, $timeout) {
		return {
			replace    : true,
            scope: 	{
                navtilbakelenke: '@'
            },
            templateUrl: '../js/app/directives/stickybunn/stickyBunnTemplate.html',
			link       : function (scope, element) {
				scope.soknadId = data.soknad.soknadId;
                scope.lenke = {
                    value: ""
                }

                if(scope.navtilbakelenke.indexOf('vedlegg') > -1) {
                    scope.lenke.value="#/vedlegg";
                } else if (scope.navtilbakelenke.indexOf('soknad') > -1) {
                    scope.lenke.value="#/soknad";
                }

				scope.hentSistLagretTid = function () {
					return data.soknad.sistLagret;
				};

				scope.soknadHarBlittLagret = function () {
					return data.soknad.sistLagret !== null;
				};

				scope.soknadHarAldriBlittLagret = function () {
					return !scope.soknadHarBlittLagret();
				};

				angular.element($window).bind('scroll', function () {
					settStickySistLagret();
				});



				// Litt hacky måte å få smooth overgang mellom sticky og non-sticky...
				var nonStickyHeightCompensation = 16;
				var stickyHeightCompensation = 56;
				var stickyHeight = nonStickyHeightCompensation;

				function settStickySistLagret() {
					var elementTop = element.find('#sticky-bunn-anchor')[0].getBoundingClientRect().bottom + stickyHeight;
					var windowTop = this.innerHeight;

					if (elementTop > windowTop) {
						stickyHeight = stickyHeightCompensation;
						element.find('.sticky-bunn').addClass('stick');
					} else {
						stickyHeight = nonStickyHeightCompensation;
						element.find('.sticky-bunn').removeClass('stick');
					}
				}

				$timeout(function () {
					settStickySistLagret();
				});
			}
		}
	}]);
