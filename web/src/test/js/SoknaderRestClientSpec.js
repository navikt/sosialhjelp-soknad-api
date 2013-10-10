describe("SoknaderRestClient", function () {
    "use strict";

    var server;

    beforeEach(function () {
        server = sinon.fakeServer.create();
    });

    afterEach(function () {
        server.restore();
    });

    it("Should return number of applications from rest-service", function () {
        var callback = sinon.spy();
        var soknaderRestClient = new window.Inngangsporten.SoknaderRestClient();
        soknaderRestClient.hentAntallSoknaderUnderArbeid(callback);

        server.requests[0].respond(200, {"Content-Type" : "application/json"}, JSON.stringify({"soknadsoversikt":{"antall":2}}));

        expect(callback.calledOnce).toBeTruthy();
    });

    it("Should not fire callback if rest-service returns error", function () {
        var callback = sinon.spy();
        var soknaderRestClient = new window.Inngangsporten.SoknaderRestClient();
        soknaderRestClient.hentAntallSoknaderUnderArbeid(callback);

        server.requests[0].respond(500, {"Content-Type" : "text/plain"}, "error");

        expect(callback.called).toBeFalsy();
    });

});
