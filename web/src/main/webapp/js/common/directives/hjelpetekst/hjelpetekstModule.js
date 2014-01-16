angular.module('nav.hjelpetekst', ['nav.animation'])
	.directive('navHjelpetekstelement', ['$document', function ($document) {
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
	.directive('navHjelpetekstTooltip', ['$timeout', '$document', function ($timeout, $document) {
		return function (scope, element) {
            $timeout(function() {
                plasserTooltipHorisontalt();
                scrollDersomNodvendig();
            });

            function plasserTooltipHorisontalt() {
                var plassSomMangleTilHoyre = element[0].getBoundingClientRect().right + 40 - (window.innerWidth || document.documentElement.clientWidth);
                if (plassSomMangleTilHoyre > 0) {
                    var venstre = 20 + plassSomMangleTilHoyre
                    element.css({left: -venstre});
                    $('<style/>', {text: '.hjelpetekst .hjelpetekst-tooltip:before {left: ' + venstre + 'px !important};'}).appendTo('head');
                } else if (element.css('left') === '-20px') {
                    $('style:contains(.hjelpetekst .hjelpetekst-tooltip:before)').remove();
                }
            };

            function scrollDersomNodvendig() {
                element.css({top: -element.height() - 30});
                var diff = element[0].getBoundingClientRect().top - 20;
                if (diff < 0) {
                    var animationSpeed = 200;
                    $('body, html').scrollToPos($document.scrollTop() + diff, animationSpeed);
                }
            };
		}
	}]);
