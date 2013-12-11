angular.module('nav.dagpenger', [])
    .controller('DagpengerCtrl', ['$scope', '$location', '$timeout', function ($scope, $location, $timeout) {

        $scope.grupper = [
            {id: 'reell-arbeidssoker', tittel: 'reellarbeidssoker.tittel', template: '../html/templates/reell-arbeidssoker.html', apen: true},
            {id: 'arbeidsforhold', tittel: 'arbeidsforhold.tittel', template: '../html/templates/arbeidsforhold.html', apen: false},
            {id: 'egen-naering', tittel: 'ikkeegennaering.tittel', template: '../html/templates/egen-naering.html', apen: false},
            {id: 'verneplikt', tittel: 'ikkeavtjentverneplikt.tittel', template: '../html/templates/verneplikt.html', apen: false},
            {id: 'utdanning', tittel: 'utdanning.tittel', template: '../html/templates/utdanning.html', apen: false},
            {id: 'ytelser', tittel: 'ytelser.tittel', template: '../html/templates/ytelser.html', apen: false},
            {id: 'personalia', tittel: 'personalia.tittel', template: '../html/templates/personalia.html', apen: false},
            {id: 'barnetillegg', tittel: 'barnetillegg.tittel', template: '../html/templates/barnetillegg.html', apen: false}
        ]

        $scope.validerDagpenger = function (form) {
            $scope.$broadcast('VALIDER_YTELSER', form.ytelserForm);
            $scope.$broadcast('VALIDER_UTDANNING', form.utdanningForm);
            $scope.$broadcast('VALIDER_ARBEIDSFORHOLD', form.arbeidsforholdForm);
            $scope.$broadcast('VALIDER_EGENNAERING', form.egennaeringForm);
            $scope.$broadcast('VALIDER_VERNEPLIKT', form.vernepliktigForm);
//            $scope.$broadcast('VALIDER_FRIVILLIG', form.frivilligForm);
//            $scope.$broadcast('VALIDER_PERSONALIA', form.personaliaForm);
            $scope.$broadcast('VALIDER_REELLARBEIDSSOKER', form.reellarbeidssokerForm);
            $scope.$broadcast('VALIDER_DAGPENGER', form);

            $timeout(function () {
                $scope.validateForm(form.$invalid);
                if (form.$valid) {
                    $location.path("/vedlegg/" + $scope.soknadData.soknadId);
                } else {
                    scrollToElement($('.accordion-group').has('.form-linje.feil, .form-linje.feilstyling').first(), 70);
                }
            }, 400);
        };



        $scope.$on("OPEN_TAB", function (e, ider) {
            settApenStatusForAccordion(true, ider);
        });

        $scope.$on("CLOSE_TAB", function (e, ider) {
            settApenStatusForAccordion(false, ider);
        });

        function settApenStatusForAccordion(apen, ider) {
            if (ider instanceof Array) {
                angular.forEach(ider, function (id) {
                    settApenForId(apen, id);
                });
            } else {
                settApenForId(apen, ider);
            }
        }

        function settApenForId(apen, id) {
            var idx = $scope.grupper.indexByValue(id);
            if (idx > -1) {
                $scope.grupper[idx].apen = apen;
            }
        }
    }])
