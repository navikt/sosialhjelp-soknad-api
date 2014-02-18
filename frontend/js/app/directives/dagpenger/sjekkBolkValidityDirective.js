angular.module('nav.sjekkBoklerValiditet', [])
    .directive('sjekkValidert', ['data', function (data) {
        return {
            link: function (scope, element, attrs) {
                var skalSettesTilValidVedForsteApning = scope.$eval(attrs.sjekkValidert);
                var erValidert = data.finnFaktum('bolker').properties[attrs.id] === "true";
                if (erValidert) {
                    element.addClass('validert');
                }

                if (!skalSettesTilValidVedForsteApning) {
                    scope.$watch(
                        function() {
                            var form = element.find('[data-ng-form]');
                            return form.length > 0 && form.is('.ng-dirty');
                        },
                        function(newVal, oldVal) {
                            if (newVal === oldVal) {
                                return;
                            }
                            if (newVal) {
                                var bolkerFaktum = data.finnFaktum('bolker');
                                bolkerFaktum.properties[attrs.id] = "false";
                                bolkerFaktum.$save();

                                if (element.hasClass('validert')) {
                                    element.removeClass('validert');
                                }
                            }
                        }
                    );
                } else if (!erValidert) {
                    var unregister = scope.$watch(
                        function() {
                            return scope.$eval(attrs.isOpen);
                        },
                        function(newVal) {
                            if (newVal) {
                                var bolkerFaktum = data.finnFaktum('bolker');
                                bolkerFaktum.properties[attrs.id] = "true";
                                bolkerFaktum.$save();
                                element.addClass('validert');
                                unregister();
                            }
                        }
                    );
                }
            }
        };
    }]);