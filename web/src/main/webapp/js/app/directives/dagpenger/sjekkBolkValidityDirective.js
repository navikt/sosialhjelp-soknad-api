angular.module('nav.sjekkBoklerValiditet', [])
    .directive('sjekkValidert', ['data', '$parse', function (data, $parse) {
        return {
            link: {
                post: function (scope, element, attrs) {
                    var skalSettesTilValidVedForsteApning = scope.$eval(attrs.sjekkValidert);
                    var erValidert = data.finnFaktum('bolker').properties[attrs.id] === "true";
                    if (erValidert) {
                        element.addClass('validert');
                    };

                    scope.$watch(
                        function() {
                            return data.finnFaktum('bolker').properties[attrs.id];
                        },
                        function(newVal, oldVal) {
                            if (newVal === oldVal) {
                                return;
                            }

                            if (newVal === "true") {
                                element.addClass('validert');
                            } else {
                                element.removeClass('validert');
                            }
                        }
                    );

                    scope.$watch(
                        function() {
                            return element.find('[data-ng-form]').length > 0 && element.find('[data-ng-form]').is('.ng-dirty');
                        },
                        function(newVal, oldVal) {
                            if (newVal === oldVal) {
                                return;
                            }
                            if (newVal) {
                                var bolkerFaktum = data.finnFaktum('bolker');
                                bolkerFaktum.properties[attrs.id] = "false";
                                bolkerFaktum.$save();
                            }
                        }
                    );

                    if (skalSettesTilValidVedForsteApning && !erValidert) {
                        var unregister = scope.$watch(
                            function() {
                                return scope.$eval(attrs.isOpen);
                            },
                            function(newVal) {
                                if (newVal) {
                                    var bolkerFaktum = data.finnFaktum('bolker');
                                    bolkerFaktum.properties[attrs.id] = "true";
                                    bolkerFaktum.$save();
                                    unregister();
                                }
                            }
                        );
                    }
                }
            }
        }
    }]);