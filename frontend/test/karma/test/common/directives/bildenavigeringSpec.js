describe('stickybunn', function () {
    var scope, element;

    beforeEach(module('nav.bildenavigering', 'nav.cms', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {
            soknad: {
                soknadId: 1
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form"> ' +
                '<div data-bildenavigering data-vedlegg="forventning" data-selvstendig="true"> ' +
                '</div>' +
                '</form>');

        $compile(element)($rootScope);

        $rootScope.$apply();
        scope = element.find('div').isolateScope();
        scope.vedlegg = {
            antallSider: 3
        };

    }));
    describe("Sist lagret", function () {
        it('sette riktig side', function() {
            expect(scope.side).toBe(0);
            expect(scope.bilder.length).toBe(0);
        });
        it('range sin returverdi sin lengde skal vaere det samme som antallet som blir sendt med funksjonen', function() {
            expect(scope.range(2).length).toBe(2);
        });
        it('index 2 for returverdien til range(3) skal vaere 2', function() {
            var rangearray = scope.range(3);
            expect(rangearray[2]).toBe(2);
        });
        it('sideErSynlig skal returnere true for index samme som side', function() {
            expect(scope.side).toBe(0);
            expect(scope.sideErSynlig(0)).toBe(true);
        });
        it('sideErSynlig skal returnere false for ulik index og side', function() {
            expect(scope.side).toBe(0);
            expect(scope.sideErSynlig(1)).toBe(false);
        });
        it('naviger skal skal oke side med 1 hvis retning er 1', function() {
            expect(scope.side).toBe(0);
            scope.naviger(1);
            expect(scope.side).toBe(1);
        });
        it('naviger skal minske side med 1 hvis retning er -1', function() {
            scope.side = 1;
            expect(scope.side).toBe(1);
            scope.naviger(-1);
            expect(scope.side).toBe(0);
        });
        it('naviger skal vise siste bilde hvis har kommet til første og navigerer til venstre', function() {
            scope.side = 0;
            expect(scope.side).toBe(0);
            scope.naviger(-1);
            expect(scope.side).toBe(2);
        });
        it('naviger skal vise forste bilde hvis har kommet til siste og navigerer til hoyre', function() {
            scope.side = 2;
            expect(scope.side).toBe(2);
            scope.naviger(1);
            expect(scope.side).toBe(0);
        });
        it('hentTimestamp skal vaere satt', function() {
            expect(scope.hentTimestamp).toNotBe(undefined);
        });
    });
});
