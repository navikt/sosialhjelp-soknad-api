angular.module('FormErrors', ['app.services'])

// just put <form-errors><form-errors> wherever you want form errors to be displayed!
.directive('formErrors', ['data', function (data) {
    return {
        // only works if embedded in a form or an ngForm (that's in a form). 
        // It does use its closest parent that is a form OR ngForm
        require: '^form',
        template:
            '<ul class="form-errors" data-ng-show="showErrors">' +
                '<li class="form-error" ng-repeat="error in errors track by $index">' +
                    '{{error}}' +
                '</li>' +
            '</ul>',
        replace: true,
        transclude: true,
        restrict: 'AE',
        link: function postLink(scope, elem, attrs, ctrl) {
            // list of some default error reasons
            var defaultErrorReasons = {
                    required: 'er påkrevd.',
                    minlength: 'er for kort.',
                    maxlength: 'er for langt.',
                    email: 'er ikke en gyldig e-postadresse.',
                    pattern: 'stemmer ikke med forventet verdi.',
                    number: 'er ikke et tall.',

                    fallback: 'er ikke gyldig.'
                },
                // humanize words, turning:
                //     camelCase  --> Camel Case
                //     dash-case  --> Dash Case
                //     snake_case --> Snake Case
                humanize = function (str) {
                    return str.replace(/[-_+]/g, ' ') // turn _ and - into spaces
                              .replace(/([A-Z])/g, ' $1') // put a splace before every capital letter
                              .replace(/^([a-z])|\s+([a-z])/g, // capitalize the first letter of each word
                                    function ($1) { return $1.toUpperCase(); }
                    );
                },
                // this is where we form our message
                errorMessage = function (name, error, props) {
                    // get the nice name if they used the niceName 
                    // directive or humanize the name and call it good
                    var niceName = props.$niceName || humanize(name);

                    // get a reason from our default set
                    var reason = defaultErrorReasons[error] || defaultErrorReasons.fallback;
                    
                    var defaultReason = niceName + ' ' + reason;
                    console.log(" xxx " + elem + reason);
                    // if they used the errorMessages directive, grab that message
                    if(typeof props.$errorMessages === 'object')
                        reason = props.$errorMessages[error];
                    else if(typeof props.$errorMessages === 'string')
                        reason = props.$errorMessages;

                    if(data.tekster[reason] === undefined)
                        return defaultReason;
                    
                    return data.tekster[reason]; // Henter fra cmstekster
                    
                    // return our nicely formatted message
                    
                };

            // only update the list of errors if there was actually a change in $error
            scope.$watch(function() { return ctrl.$error; }, function() {
                // reset error array
                scope.errors = [];
                angular.forEach(ctrl, function(props, name) {
                    // name has some internal properties we don't want to iterate over
                    if(name[0] === '$') return;
                    angular.forEach(props.$error, function(isInvalid, error) {
                        // don't need to even try and get a a message unless it's invalid
                        if(isInvalid) {
                            try{
                                scope.errors.push(errorMessage(name, error, props));
                            }catch(e) {} //duplicate key... 
                        }
                    });
                });
            }, true);
        }
    };
}])

// set a nice name to $niceName on the ngModel ctrl for later use
.directive('niceName', [function () {
    return {
        require: 'ngModel',
        link: function(scope, elem, attrs, ctrl) {
            ctrl.$niceName = attrs.niceName;
        }
    };
}])

// set an errorMessage(s) to $errorMessages on the ngModel ctrl for later use
.directive('errorMessages', [function () {
    return {
        require: 'ngModel',
        link: function(scope, elem, attrs, ctrl) {
            // attrs.errorMessages can be:
            //    1) "must be filled out."
            //    2) "'must be filled out.'"
            //    3) "{ required: 'must be filled out.' }"
            try {
                ctrl.$errorMessages = scope.$eval(attrs.errorMessages);
            } catch(e) {
                ctrl.$errorMessages = attrs.errorMessages;
            }
        }
    };
}]);
