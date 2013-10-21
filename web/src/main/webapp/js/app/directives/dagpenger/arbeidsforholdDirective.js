angular.module('nav.arbeidsforhold.directive',[])
	.directive('lagreArbeidsforhold', function () {
        return function ($scope, element, attrs) {
            var eventType;
            switch (element.attr('type')) {
                case "radio":
                case "checkbox":
                    eventType = "change";
                    break;
                default:
                    eventType = "blur";
            }

            element.bind(eventType, function () {
                var verdi = element.val();
                if (element.attr('type') === "checkbox") {
                    verdi = element.is(':checked');
                }
                $scope.$emit("OPPDATER_OG_LAGRE_ARBEIDSFORHOLD");
            });
        };
    })

    .directive('legg-til-arbeidsforhold', function () {
        return function ($scope, element, attrs) {
            element.click(function () {
                
            })
        }

    })