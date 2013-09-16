var INTEGER_REGEX = /^\-?\d*$/;

/*Hva med casene 1-242 osv */
sendsoknad.directive('landskodevalidering', function(){
  return {
    require: 'ngModel',
    link: function(scope, elm, attrs, ctrl){
      ctrl.$parsers.unshift(function(viewValue){
        var kode = viewValue.slice(1, viewValue.length);
        if(viewValue.charAt(0) === '+' && INTEGER_REGEX.test(kode)) {
          ctrl.$setValidity('feil', true);
        } else {
          ctrl.$setValidity('feil', false);
        }
      });
    }
  };

});

sendsoknad.directive('mobilnummer', function(){
	return {
		require: 'ngModel',
		link: function(scope, elm, attrs, ctrl){
			ctrl.$parsers.unshift(function(viewValue){
				if(INTEGER_REGEX.test(viewValue) && viewValue.length === 8) {
					ctrl.$setValidity('feil', true);
					return parseFloat(viewValue.replace(',', '.'));
				} else {
					ctrl.$setValidity('feil', false);
					return undefined;
				}
			});
		}
	};

});


sendsoknad.directive('dato', function(){
  return {
    require: 'ngModel',
    link: function(scope, elm, attrs, ctrl){
      ctrl.$parsers.unshift(function(viewValue){

        if(typeof viewValue === 'undefined') {
          ctrl.$setValidity('dato', false);
          return undefined;
        }
        var date = viewValue.split('.');
        var y = date[2], m = date[1], d = date[0];

        var antallDagerIMnd = [31,28,31,30,31,30,31,31,30,31,30,31];

        if( (!(y%4) && y%100) || !(y % 400)){
          antallDagerIMnd[1] = 29;
        }

        if(typeof y === 'undefined'){
          ctrl.$setValidity('dato', false);
          return undefined;
        } else {
          if( d <= antallDagerIMnd[--m] && y.length === 4){
           ctrl.$setValidity('dato', true);
           return viewValue;
         } else {
           ctrl.$setValidity('dato', false);
           return undefined;
         }
       }
       ctrl.$setValidity('dato', false);
       return undefined;
     });
    }
  };

});

sendsoknad.directive('datotil', function(){
  return {
    replace: true,
    require: 'ngModel',
    
    scope: {
      fraDato: '=',
      fraNavn: '@'
    },
    link: function(scope, elm, attrs, ctrl){
      ctrl.$parsers.unshift(function(viewValue){
        if(typeof scope.fraDato === 'undefined' || typeof viewValue === 'undefined'){
          ctrl.$setValidity('framindre', false);
          return undefined;
        }
        

        var navn = scope.fraNavn;
        var til = viewValue.split('.');
        var fra = scope.fraDato.split('.');

        if(fratil(fra, til)){
          ctrl.$setValidity('framindre', true);
          return viewValue;
        } 
        ctrl.$setValidity('framindre', false);
        return undefined;

      }); 

      scope.$watch('arbeidsforhold.permiteringFra', function(value) { 
        debugger
        if(typeof value === 'undefined' || typeof ctrl.$viewValue === 'undefined') {
          ctrl.$setValidity('framindre', false);
          return undefined;
        }
        var til = ctrl.$viewValue.split('.');
        var fra = value.split('.');
        ctrl.$setValidity('framindre', fratil(fra, til));
      }, true);
    }
  };
});

sendsoknad.directive('mod-verdiendret', function(){
  // Runs during compile
  return {
      restrict: 'A', 
    // name: '',
    // priority: 1,
    // terminal: true,
    // scope: {}, // {} = isolate, true = child, false/undefined = no change
    // cont­rol­ler: function($scope, $element, $attrs, $transclue) {},
    // require: 'ngModel', // Array = multiple requires, ? = optional, ^ = check parent elements
    // restrict: 'A', // E = Element, A = Attribute, C = Class, M = Comment
    // template: '',
    // templateUrl: '',
    // replace: true,
    // transclude: true,
    // compile: function(tElement, tAttrs, function transclude(function(scope, cloneLinkingFn){ return function linking(scope, elm, attrs){}})),
    link: function($scope, attrs) {
      scope.$watch(attrs.ngModel, function (verdi ){
        console.log('verdi endret seg' + verdi);
      });
    }
  };
});

function fratil(fra, til){
  var gyldig = false;
  if(parseInt(til[2]) > parseInt(fra[2])){
    gyldig = true;
  } else if (parseInt(til[2]) === parseInt(fra[2])) {
    if(parseInt(til[1]) > parseInt(fra[1])) {
     gyldig = true;
   } else if (parseInt(til[1]) === parseInt(fra[1])) {
    if (parseInt(til[0]) > parseInt(fra[0])) {
      gyldig = true;
    }
  }
}
return gyldig;
}

sendsoknad.directive('sluttaarsakutfall', function(){
  return {

  }
})
