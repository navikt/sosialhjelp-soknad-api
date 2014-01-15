describe('ArbeidsforholdCtrl', function () {

    beforeEach(
        module('app.services', 'nav.arbeidsforhold', function ($provide) {
            $provide.value("data", []);
        }));
    var scope, $httpBackend, ctrl, routeParams;

    beforeEach(inject(function ($rootScope, _$httpBackend_, $controller, $cookieStore) {
        scope = $rootScope.$new();
        $httpBackend = _$httpBackend_;

        //$httpBackend.expectGET('/sendsoknad/rest/soknad/1/'\/.*/).

        $httpBackend.whenGET(/sendsoknad\/[^\/]*/).
            respond({"soknadId": 1, "gosysId": "Dagpenger", "brukerBehandlingId": "100000000",
                "fakta": {
                    "fornavn": {"soknadId": 1, "key": "fornavn", "value": "Ola"},
                    "mellomnavn": {"soknadId": 1, "key": "mellomnavn", "value": "Johan"},
                    "etternavn": {"soknadId": 1, "key": "etternavn", "value": "Nordmann"},
                    "fnr": {"soknadId": 1, "key": "fnr", "value": "***REMOVED***"},
                    "adresse": {"soknadId": 1, "key": "adresse", "value": "Waldemar Thranes Gt. 98B"},
                    "postnr": {"soknadId": 1, "key": "postnr", "value": "0175"},
                    "poststed": {"soknadId": 1, "key": "poststed", "value": "Oslo"}
                }
            });

        // TODO: result her? kontra backend
        $httpBackend.expectGET('/sendsoknad/rest/soknad/kodeverk/landliste').
            respond({result: ["Norge", "Sverige", "Danmark"]});

        routeParams = {};
        routeParams.soknadId = 1;

        ctrl = $controller('ArbeidsforholdCtrl', {
            $scope: scope,
            $routeParams: routeParams
        });

        $httpBackend.flush();
    }));

})

