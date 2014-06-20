angular.module('nav.stickybunn', [])
	.directive('sticky', ['data', '$window', '$timeout', function (data, $window, $timeout) {
        return {
            replace: true,
            transclude: true,
            templateUrl: '../js/common/directives/stickybunn/stickyBunnTemplate.html',
            link: function(scope, element) {
                angular.element($window).bind('scroll', function () {
                    if (breddeErMerEnn767()) {
                        settPosisjon();
                    }
                });

                var tastaturErApent = false;

                if (erTouchDevice() && getIEVersion() < 0) {
                    document.addEventListener('focusin', function(e) {
                        var type = e.target.type;
                        if (type === 'text' || type === 'textarea' || type === 'date') {
                            tastaturErApent = true;
                            scope.$apply();
                        }
                    });

                    document.addEventListener('focusout', function(e) {
                        tastaturErApent = false;
                        if (breddeErMerEnn767()) {
                            settPosisjon();
                        }
                        scope.$apply();
                    });
                }

                scope.hvisTouchTastaturVises = function() {
                    return !tastaturErApent;
                };


                // Litt hacky måte å få smooth overgang mellom sticky og non-sticky...
                var nonStickyHeightCompensation = 16;
                var stickyHeightCompensation = 56;
                var stickyHeight = nonStickyHeightCompensation;

                function settPosisjon() {
                    var elementTop = element.find('#sticky-bunn-anchor')[0].getBoundingClientRect().bottom + stickyHeight;
                    var windowTop = this.innerHeight;

                    if (elementTop > windowTop) {
                        stickyHeight = stickyHeightCompensation;
                        element.find('.sticky-bunn').addClass('stick');
                        return true;
                    } else {
                        stickyHeight = nonStickyHeightCompensation;
                        element.find('.sticky-bunn').removeClass('stick');
                        return false;
                    }
                }

                $timeout(function () {
                    if (breddeErMerEnn767()) {
                        settPosisjon();
                    }
                });

                function breddeErMerEnn767() {
                    return $window.outerWidth > 767;
                }
            }
        };
    }]);
