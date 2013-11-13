angular.module('nav.sistlagret', [])
    .directive('sistLagret', ['data', '$window', function (data, $window) {
        return {
            replace: true,
            templateUrl: '../js/app/directives/sistlagret/sistLagretTemplate.html',
            link: function(scope, element) {
                scope.hentSistLagretTid = function() {
                    return data.soknad.fakta.sistLagret.value;
                }

                scope.soknadHarBlittLagret = function() {
                    return data.soknad.fakta.sistLagret !== undefined;
                }

                scope.soknadHarAldriBlittLagret = function() {
                    return !scope.soknadHarBlittLagret();
                }

                // Litt hacky måte å få smooth overgang mellom sticky og non-sticky...
                var nonStickyHeightCompensation = 16;
                var stickyHeightCompensation = 56;
                var stickyHeight = nonStickyHeightCompensation;
                angular.element($window).bind('scroll', function() {
                    var elementTop = element.find('#sist-lagret-anchor')[0].getBoundingClientRect().bottom + stickyHeight;
                    var windowTop = this.innerHeight;

                    if (elementTop > windowTop) {
                        stickyHeight = stickyHeightCompensation;
                        element.find('.sist-lagret').addClass('stick');
                    } else {
                        stickyHeight = nonStickyHeightCompensation;
                        element.find('.sist-lagret').removeClass('stick');
                    }
                });
            }
        }
    }]);