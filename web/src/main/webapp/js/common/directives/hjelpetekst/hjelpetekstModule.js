angular.module('nav.hjelpetekst', ['nav.animation'])
	.directive('navHjelpetekstelement', ['$document', '$window', function ($document, $window) {
		return {
			replace    : true,
			scope      : {
				tittel: '@',
				tekst : '@'
			},
			templateUrl: '../js/common/directives/hjelpetekst/hjelpetekstTemplate.html',
			link: function (scope) {
                var lukkEventTimestamp = 0;

				scope.visHjelp = false;
				scope.toggleHjelpetekst = function () {
					scope.visHjelp = !scope.visHjelp;
				};

				scope.lukk = function () {
					scope.visHjelp = false;
                    $($window).unbind('resize');
				};

				scope.stoppKlikk = function (event) {
                    lukkEventTimestamp = event.timeStamp;
				};

				$document.bind('click', function (event) {
                    if (lukkEventTimestamp !== event.timeStamp) {
                        scope.visHjelp = false;
                    }
				});
			}
		}
	}])
	.directive('navHjelpetekstTooltip', ['$timeout', '$document', '$window', function ($timeout, $document, $window) {
		return function (scope, element) {
            $('style:contains(.hjelpetekst .hjelpetekst-tooltip:before)').remove();
            $timeout(function() {
                plasserTooltipHorisontalt();
                scrollDersomNodvendig();
            });

            function plasserTooltipVertikalt() {
                element.css({top: -element.height() - 30});
            }

            function plasserTooltipHorisontalt() {
                var plassSomMangleTilHoyre = element[0].getBoundingClientRect().right + 40 - (window.innerWidth || document.documentElement.clientWidth);
                var venstre = Math.min(element.position().left - plassSomMangleTilHoyre, -20);
                element.css({left: venstre});
                settPilStyling(venstre);
            };

            function scrollDersomNodvendig() {
                plasserTooltipVertikalt();
                var diff = element[0].getBoundingClientRect().top - 20;
                if (diff < 0) {
                    var animationSpeed = 200;
                    $('body, html').scrollToPos($document.scrollTop() + diff, animationSpeed);
                }
            };

            function settPilStyling(venstre) {
                if ($('style:contains(.hjelpetekst .hjelpetekst-tooltip:before)').length === 0) {
                    $('<style/>', {text: '.hjelpetekst .hjelpetekst-tooltip:before {left: ' + -venstre + 'px !important};'}).appendTo('head');
                } else {
                    $('style:contains(.hjelpetekst .hjelpetekst-tooltip:before)').text('.hjelpetekst .hjelpetekst-tooltip:before {left: ' + -venstre + 'px !important};');
                }
            };

            $($window).data('over767', $window.innerWidth > 767);
            $($window).bind('resize', function() {
                if ($window.innerWidth > 767) {
                    plasserTooltipHorisontalt();

                    if (!$($window).data('over767')) {
                        plasserTooltipVertikalt();
                    }
                }
                $($window).data('over767', $window.innerWidth > 767);
            });
		}
	}]);
