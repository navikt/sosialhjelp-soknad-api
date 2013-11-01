describe('brukerdata domene', function () {

    beforeEach(
        module('app.services', 'app.brukerdata')
    );

    describe('soknaddata controller', function () {

        var scope, ctrl, $httpBackend, routeParams;

        beforeEach(inject(function (_$httpBackend_, $rootScope, $controller) {
            routeParams = {};
            $httpBackend = _$httpBackend_;
            $httpBackend.whenGET(/sendsoknad\/.*/).
                respond({"soknadId": 1, "gosysId": "Dagpenger", "brukerBehandlingId": "100000000",
                    "fakta": {
                        "fornavn": {"soknadId": 1, "key": "fornavn", "value": "Ola"},
                        "mellomnavn": {"soknadId": 1, "key": "mellomnavn", "value": "Johan"},
                        "etternavn": {"soknadId": 1, "key": "etternavn", "value": "Nordmann"},
                        "fnr": {"soknadId": 1, "key": "fnr", "value": "01015245464"},
                        "adresse": {"soknadId": 1, "key": "adresse", "value": "Waldemar Thranes Gt. 98B"},
                        "postnr": {"soknadId": 1, "key": "postnr", "value": "0175"},
                        "poststed": {"soknadId": 1, "key": "poststed", "value": "Oslo"}
                    }
                });
            scope = $rootScope.$new();
            routeParams.soknadId = 1;
            ctrl = $controller('SoknadDataCtrl', {
                $scope: scope,
                $routeParams: routeParams
            });
        }));

        it('skal returnere soknaddata', function () {
            $httpBackend.flush();
            expect(scope.soknadData.soknadId).toEqual(1);
            expect(scope.soknadData.fakta.fornavn.soknadId).toEqual(1);
            expect(scope.soknadData.fakta.fornavn.value).toEqual('Ola');
        });


        it('skal legge til en et nytt faktum i soknaddata', function () {
            $httpBackend.flush();
            $httpBackend.whenPOST('/sendsoknad/rest/soknad').respond('200');
            //cope.lagre();
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

        it('skal returnere true dersom form ikke validerer', function () {
            scope.validateForm(true);
            expect(scope.hvisIkkeFormValiderer()).toBe(true);
        });
    })

});


