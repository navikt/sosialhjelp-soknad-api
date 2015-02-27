describe('navFaktumProperty', function () {
    var element, scope, rootScope;

    beforeEach(module('sendsoknad.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: [{}],
            soknad: {
                soknadId: 1
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        rootScope = $rootScope;

        rootScope.faktum = {
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

    it("simulerer et faktum", function () {
        expect(scope.faktum.value).toBe('en property');
        expect(scope.faktum.key).toBe('minproperty');
    });
});

describe('navFaktum', function () {
    var element, scope, rootScope;

    beforeEach(module('sendsoknad.services', 'nav.navfaktum'));

    describe("når faktum ikke finnes,", function () {
        beforeEach(module(function ($provide) {
            var fakta = [{}];

            $provide.value("data", {
                fakta: fakta,
                soknad: {
                    soknadId: 1
                }
            });
        }));

        beforeEach(inject(function ($compile, $rootScope, data) {
            rootScope = $rootScope;
            element = angular.element(
                '<div data-nav-faktum="etnavfaktum">' +
                '</div>');

            $compile(element)(rootScope);
            rootScope.$apply();
            scope = element.scope();

        }));

        it("opprettes faktum med gitt nøkkel", function () {
            expect(scope.faktum.key).toBe('etnavfaktum');
            expect(scope.faktum.soknadId).toBe(1);
        });
    });

    describe("når faktum finnes,", function () {
        beforeEach(module(function ($provide) {
            var fakta = [
                {
                    key: 'etnavfaktum',
                    value: "valuen",
                    soknadId: 2,
                    properties: {},
                    $save: function(){}
                }
            ];

            $provide.value("data", {
                fakta: fakta,
                soknad: {
                    soknadId: 1
                }
            });
        }));

        beforeEach(inject(function ($compile, $rootScope, data) {
            rootScope = $rootScope;
            element = angular.element(
                '<div data-nav-faktum="etnavfaktum">' +
                '</div>');

            $compile(element)(rootScope);
            rootScope.$apply();
            scope = element.scope();

        }));

        it("skal sette scope faktumet til faktumet som ligger i data, og soknadIden til faktummet", function () {
            expect(scope.faktum.key).toBe('etnavfaktum');
            expect(scope.faktum.soknadId).toBe(2);
        });

        it("så vil lagreFaktum kalle save metoden til faktumet", function () {
            spyOn(scope.faktum, '$save');
            scope.lagreFaktum();
            expect(scope.faktum.$save).toHaveBeenCalled();
        });
    });
});


describe('navFaktum med ikkeAutoLagre', function () {
    var element, scope, rootScope;

    beforeEach(module('sendsoknad.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        $provide.value("data", {
            fakta: [{}],
            soknad: {
                soknadId: 1
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope, data) {
        rootScope = $rootScope;
        element = angular.element(
            '<div data-nav-faktum data-ikke-auto-lagre="true" data-nav-nytt-faktum="true">' +
            '</div>');

        rootScope.etnavfaktum = {
            value: 'enverdi',
            $save: function () {
            }
        };

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();

    }));

    it("skal ikke kalle $save metoden ", function () {
        spyOn(rootScope.etnavfaktum, '$save');
        scope.lagreFaktum();
        expect(rootScope.etnavfaktum.$save).wasNotCalled();
    });

});