describe('navFaktum', function () {
    var element;
    var $scope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: [{}],
            soknad: {
                soknadId: 1
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope,data) {
        $scope = $rootScope;

        element = angular.element('<div nav-faktum="mittFaktum">' + '</div>');

        $compile(element)($scope);
        $scope.$apply();
    }));

    describe("navFaktum", function() {
        it("skal kunne setet navFaktum", function() {
            expect(true).toBe(true);
        });

    });
});
describe('navFaktum', function () {
    var element, scope, rootScope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: [{}],
            soknad: {
                soknadId: 1
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope,data) {
        rootScope = $rootScope;

        rootScope.parentFaktum = {
            properties: {
                minproperty: 'en property'
            }
        };

        element = angular.element(
            '<div data-nav-faktum-property="minproperty">' +
            '</div>');

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();

    }));

    describe("navFaktum", function() {
        it("skal kunne setet navFaktum", function() {
            expect(scope.faktum.value).toBe('en property');
            expect(scope.faktum.key).toBe('minproperty');
        });
    });
});
describe('navFaktum', function () {
    var element, scope, rootScope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: [{}],
            soknad: {
                soknadId: 1
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope,data) {
        rootScope = $rootScope;

        rootScope.parentFaktum = {
            properties: {
                minproperty: '2014.01.01'
            }
        };

        element = angular.element(
            '<div data-nav-faktum-property="minproperty">' +
                '</div>');

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();

    }));

    describe("navFaktum", function() {
        it("datoen skal endres til dage", function() {
            expect(rootScope.parentFaktum.properties.minproperty).toBe('2014.03.06');
            expect(scope.faktum.key).toBe('minproperty');
        });
    });
});
describe('navFaktum', function () {
    var element, scope, rootScope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: [{}],
            soknad: {
                soknadId: 1
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope,data) {
        rootScope = $rootScope;

        rootScope.parentFaktum = {
            properties: {
                minproperty: '2014.01.01'
            }
        };

        element = angular.element(
            '<div data-nav-faktum-property="">' +
                '</div>');

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();

    }));

    describe("navFaktum", function() {
        it("minproperty skal ikke endres når attributtet ikke er satt,  selv om det er en dato", function() {
            expect(rootScope.parentFaktum.properties.minproperty).toBe('2014.01.01');
        });
    });
});