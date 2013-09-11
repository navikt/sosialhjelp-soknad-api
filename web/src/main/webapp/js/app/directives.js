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
            var date = viewValue.split('.');
            var y = date[2], m = date[1], d = date[0];

            var antallDagerIMnd = [31,28,31,30,31,30,31,31,30,31,30,31];

            if( (!(y%4) && y%100) || !(y % 400)){
              antallDagerIMnd[1] = 29;
            }

            if( d <= antallDagerIMnd[--m] && y.length === 4) {
              ctrl.$setValidity('feil', true);
          } else {
            ctrl.$setValidity('feil', false);
          }
        });
      }
    };

  });