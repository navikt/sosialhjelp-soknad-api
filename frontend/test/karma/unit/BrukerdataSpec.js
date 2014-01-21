describe('brukerdata domene', function () {

    beforeEach(
        module('app.services', 'app.brukerdata', function ($provide) {
            $provide.value("data", {
                soknad: {"soknadId": 1, "skjemaNummer": "Dagpenger", "brukerBehandlingId": "100000000",
                    "fakta": {
                        "fornavn": {"soknadId": 1, "key": "fornavn", "value": "Ola"},
                        "mellomnavn": {"soknadId": 1, "key": "mellomnavn", "value": "Johan"},
                        "etternavn": {"soknadId": 1, "key": "etternavn", "value": "Nordmann"},
                        "fnr": {"soknadId": 1, "key": "fnr", "value": "01015245464"},
                        "adresse": {"soknadId": 1, "key": "adresse", "value": "Waldemar Thranes Gt. 98B"},
                        "postnr": {"soknadId": 1, "key": "postnr", "value": "0175"},
                        "poststed": {"soknadId": 1, "key": "poststed", "value": "Oslo"}
                    }
                }
            });
    }));

    describe('soknaddata controller', function () {

        var scope, ctrl;

        beforeEach(inject(function ($rootScope, $controller) {
            scope = $rootScope.$new();
            ctrl = $controller('SoknadDataCtrl', {
                $scope: scope
            });
        }));

        it('skal returnere soknaddata', function () {
            expect(scope.soknadData.soknadId).toEqual(1);
            expect(scope.soknadData.fakta.fornavn.soknadId).toEqual(1);
            expect(scope.soknadData.fakta.fornavn.value).toEqual('Ola');
        });
    })

    describe('modus controller', function () {

        var scope, ctrl;

        beforeEach(inject(function ( $rootScope, $controller) {
            scope = $rootScope.$new();
            ctrl = $controller('ModusCtrl', {
                $scope: scope
            });
        }));

        it('skal returnere true for redigeringsmodus', function () {
            expect(scope.hvisIRedigeringsmodus()).toBe(true);
        });

        it('skal endre til oppsummeringsmodus dersom form er validert', function () {
            scope.validateForm(false);
            expect(scope.hvisIOppsummeringsmodus()).toBe(true);
        });

        it('skal endre til redigeringsmodus', function () {
            scope.gaTilRedigeringsmodus();
            expect(scope.hvisIRedigeringsmodus()).toBe(true);
        });
    })

});


