(function () {
    var RequiredValidator = function RequiredValidator(attrs) {
        var erRequired;

        if (harAttributt(attrs, 'required')) {
            this.erRequired = true;
        }
    };

    RequiredValidator.prototype.validate = function (verdi) {
        if (this.erRequired && verdi && verdiErIkkeTom(verdi.trim())) {
            return true;
        }

        return 'required';
    };

    window.RequiredValidator = RequiredValidator;

    var PatternValidator = function PatternValidator(attrs) {
        var pattern;

        var stringPattern = harAttributt(attrs, 'pattern');
        if (stringPattern) {
            // Tatt fra angular for å bygge regexp fra string
            var match = stringPattern.match(/^\/(.*)\/([gim]*)$/);
            this.pattern = new RegExp(match[1], match[2]);
        }
    };

    PatternValidator.prototype.validate = function (verdi) {
        if (this.pattern && !this.pattern.test(verdi)) {
            return 'pattern';
        }
        return true;
    };

    window.PatternValidator = PatternValidator;


    var LengthValidator = function LengthValidator(attrs) {
        var minLengde, maxLengde;

        this.minLengde = harAttributt(attrs, 'minlength');
        if (!this.minLengde) {
            this.minLengde = false;
        }

        this.maxLengde = harAttributt(attrs, 'maxlength');
        if (!this.maxLengde) {
            this.maxLengde = false;
        }
    };


    LengthValidator.prototype.validate = function (value) {
        if (this.minLengde && value.trim().length < this.minLengde) {
            return 'minlength'; // Invalid
        }

        if (this.maxLengde && value.trim().length > this.maxLengde) {
            return 'maxlength'; // Invalid
        }

        return true;
    };

    window.LengthValidator = LengthValidator;
})();
