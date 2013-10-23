angular.module('nav.feilmeldinger',['nav.cmstekster'])
    .directive('feilmeldinger', [ function(){
      return {
          restrict: 'E',
          replace: true,
          scope: {
              nokkel: '@',
              form: '='
          },
          link: function(scope, element, attrs) {
              console.log("Start: " + scope.form.$error);
              scope.$watchCollection('form.$error', function(newCollection) {
                  console.log("Tjohei " + scope.form.$error);
                  scope.errorliste = [];
                  var nokler = Object.keys(newCollection);
                  for (var i = 0; i < nokler.length; i++) {
                      var nokkel = nokler[i];
                      if (newCollection[nokkel] != false) {
                          scope.errorliste.push(nokkel);
                      }
                  }
              });
          },
          templateUrl: '../js/app/directives/feilmeldinger/feilmeldingerTemplate.html'
      }
    }]);
