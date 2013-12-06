angular.module('nav.barnetillegg',[])
    .controller('BarnetilleggCtrl', ['$scope', function ($scope) {
        if ($scope.soknadData.fakta.barn) {
                $scope.barn = [];
                angular.forEach($scope.soknadData.fakta.barn.valuelist, function(value) { 
                    $scope.barn.push(angular.fromJson(value));
                });
                
            }

        if ($scope.soknadData.fakta.barnetillegg) {
            $scope.barnetillegg = [];
            angular.forEach($scope.soknadData.fakta.barnetillegg.valuelist, function(value) { 
                $scope.barnetillegg.push(angular.fromJson(value).valgt);

            });
            console.log("har no greier her da vettu");
        } else {
            $scope.soknadData.fakta.barnetillegg = {};
             $scope.barnetillegg = [];
        }
        /*$scope.barn =
        {
            id: 18706,
            soknadId: 275,
            key: "barn",
            type: "BRUKERREGISTRERT",
            valuelist: [
                {
                    id: 18707,
                    vedleggId: 0,
                    sammensattnavn: "Jens August Aker Hansen",
                    fnr: ***REMOVED***,
                    kjonn: "gutt",
                    alder: 7,
                    barnetillegg: false
                },
                {
                    id: 18708,
                    vedleggId: 0,
                    sammensattnavn: "Lisa Kristin Normann Olsen",
                    fnr: 12129623623,
                    kjonn: "jente",
                    alder: 11,
                    barnetillegg: false
                }
            ]
        }*/
        
        $scope.lagreBarnetilegg = function(barn, index , event) {
            var result = {};
            result["fnr"] = barn.fnr;
            result["valgt"] = event.target.checked;
            if($scope.soknadData.fakta.barnetillegg.valuelist && $scope.soknadData.fakta.barnetillegg.valuelist.length >= index && $scope.soknadData.fakta.barnetillegg.valuelist[index]) {
                result["faktumId"] = angular.fromJson($scope.soknadData.fakta.barnetillegg.valuelist[index]).faktumId;
            }
           /* result["key"] = 'barnetillegg';
            result["soknadId"] = $scope.soknadData.soknadId;*/

           
                $scope.$emit("LAGRE_BARNETILLEGG", {key: 'barnetillegg', value: angular.toJson(result)});
           

        }

        $scope.erGutt = function(barn) {
            return barn.kjonn == "gutt";
        }

        $scope.erJente = function(barn) {
            return barn.kjonn == "jente";
        }

        $scope.validerBarnetillegg = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        $scope.$emit("OPEN_TAB", 'barnetillegg');
    }]);