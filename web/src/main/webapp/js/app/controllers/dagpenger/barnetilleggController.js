angular.module('nav.barnetillegg',[])
    .controller('BarnetilleggCtrl', ['$scope', function ($scope) {
        $scope.barn = {
            sammensattnavn: "Jens August Aker Hansen",
            fnr: ***REMOVED***,
            alder: 11,
            barnetillegg: false
        }

        //ønskelig struktur
        /*var myJson= {
            barn: 
            {
                id: 18706,
                soknadId: 275,
                vedleggId: 0,
                key: "barn",
                type: "BRUKERREGISTRERT",
                value: {
                    sammensattnavn: "Jens August Aker Hansen",
                    fnr: ***REMOVED***,
                    alder: 7,
                    barnetillegg: false
                }
            },
            barn:
            {
                id: 18707,
                soknadId: 275,
                vedleggId: 0,
                key: "barn",
                type: "BRUKERREGISTRERT",
                value: {
                    sammensattnavn: "Lisa Kristin Normann Olsen",
                    fnr: 12129623623,
                    alder: 11,
                    barnetillegg: false
                }
            }
        };
        $scope.barn = angular.fromJson(myJson);*/
        

        $scope.validerBarnetillegg = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        $scope.$emit("OPEN_TAB", 'barnetillegg');
    }]);