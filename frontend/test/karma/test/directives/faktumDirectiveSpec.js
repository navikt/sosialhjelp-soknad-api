describe('navFaktumProperty', function () {
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
describe('navFaktumPropertyDato', function () {
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
describe('navFaktumPropertyDato', function () {
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
describe('navFaktum', function () {
    var element, scope, rootScope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        var fakta = [{}];

        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: fakta,
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
            '<div data-nav-faktum="etnavfaktum">' +
                '</div>');

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();

    }));

    describe("navFaktum", function() {
        it("Ikke satt, så blir et faktum opprettet med keyen som blir sendt inn", function() {
            expect(scope.faktum.key).toBe('etnavfaktum');
            expect(scope.faktum.soknadId).toBe(1);
        });
    });
});
describe('navFaktum', function () {
    var element, scope, rootScope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {key: 'etnavfaktum',
                value: "valuen",
                soknadId: 2,
                properties: {}
            }
        ];

        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: fakta,
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
            '<div data-nav-faktum="etnavfaktum">' +
                '</div>');

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();

    }));

    describe("navFaktum", function() {
        it("", function() {
            expect(scope.faktum.key).toBe('etnavfaktum');
            expect(scope.faktum.soknadId).toBe(2);
        });
    });
});
describe('navFaktumMedDatoProperty', function () {
    var element, scope, rootScope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {key: 'etnavfaktum',
                value: "valuen",
                soknadId: 2,
                properties: {
                    varighetFra: '2014.01.01',
                    varighetTil: '2014.01.05'
                },
                $save: function(){}
            }
        ];

        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: fakta,
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
            '<div data-nav-faktum="etnavfaktum" data-nav-property="[\'varighetFra\', \'varighetTil\']">' +
                '</div>');

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();

    }));

    describe("navFaktum", function() {
        it("navfaktum med nav-property som matcher dato patternet, skal det opprettes en dato og settes til navproperties", function() {
            expect(scope.navproperties.varighetFra.getTime()).toBe(1394108384263);
        });
        it("navfaktum med nav-property som matcher dato patternet, skal det opprettes en dato og settes til navproperties", function() {
            scope.lagreFaktum();
            expect(scope.navproperties.varighetFra.getTime()).toBe(1394108384263);
        });
    });
});
describe('navFaktumMedDatoProperty', function () {
    var element, scope, rootScope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {key: 'etnavfaktum',
                value: "valuen",
                soknadId: 2,
                properties: {
                    varighetFra: undefined,
                    varighetTil: undefined
                },
                $save: function(){}
            }
        ];

        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: fakta,
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
            '<div data-nav-faktum="etnavfaktum" data-nav-property="[\'varighetFra\', \'varighetTil\']">' +
                '</div>');

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();
    }));

    describe("navFaktum", function() {
        it("navfaktum med nav-property som matcher dato patternet, skal det opprettes en dato og settes til navproperties", function() {
            scope.lagreFaktum();
            expect(scope.parentFaktum.properties.varighetFra).toBe(undefined);
        });
    });
});
describe('navFaktumMedDatoProperty', function () {
    var element, scope, rootScope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {key: 'enkey',
                value: "valuen",
                soknadId: 2,
                properties: {
                    varighetFra: undefined,
                    varighetTil: undefined
                },
                $save: function(){}
            }
        ];

        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: fakta,
            soknad: {
                soknadId: 1
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope,data) {
        rootScope = $rootScope;


        element = angular.element(
            '<div data-nav-faktum="enkey">' +
                '</div>');

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();
        scope.$parent = {
            faktum: {
                key: 'enkey',
                faktumId: 111
            }
        }

    }));

    describe("navFaktum", function() {
        it("parentFaktum.key lik som faktum.key, parentFaktum få samme faktumId (med en skriveleif) ", function() {
            scope.lagreFaktum();
            expect(scope.parentFaktum.parrentFaktum).toBe(111);
        });
    });
});
describe('navFaktumMedProperty', function () {
    var element, scope, rootScope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {key: 'etnavfaktum',
                value: "valuen",
                soknadId: 2,
                properties: {
                    varighetFra: 'ikkedato',
                    varighetTil: 'ikkedato2'
                },
                $save: function(){}
            }
        ];

        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: fakta,
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
            '<div data-nav-faktum="etnavfaktum" data-nav-property="[\'varighetFra\', \'varighetTil\']">' +
                '</div>');

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();

    }));

    describe("navFaktum", function() {
        it("navfaktum med nav-property som ikke matcher dato patternet, skal navproperties settes til dens verdi", function() {
            expect(scope.navproperties.varighetFra).toBe('ikkedato');
        });
        it('lagreFaktum skal sette parenFaktum.properties til nav-propertyen som ble sendt inn med direktivet', function() {
            scope.lagreFaktum();
            expect(scope.parentFaktum.properties.varighetTil).toBe('ikkedato2');
            expect(scope.parentFaktum.properties.varighetFra).toBe('ikkedato');
        });
    });
});
describe('navFaktumUtenProps', function () {
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
            '<div data-nav-faktum="etnavfaktum">' +
                '</div>');

        rootScope.etnavfaktum = {
            value: 'enverdi',
            $save: function(){}
        };

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.scope();

    }));

    describe("navFaktum", function() {
        it("Ikke satt, så blir et faktum opprettet med keyen som blir sendt inn", function() {
            expect(scope.faktum.value).toBe('enverdi');
        });
        it("lagreFaktum skal kalle save metoden til etnavfaktum", function() {
            spyOn(rootScope.etnavfaktum, '$save');
            scope.lagreFaktum();
            expect(rootScope.etnavfaktum.$save).toHaveBeenCalled();
        });
    });
});