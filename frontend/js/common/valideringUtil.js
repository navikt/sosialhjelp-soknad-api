(function () {
	var RequiredValidator = function RequiredValidator(scope, attrs) {
		var erRequired;
		if (harAttributt(scope, attrs, 'required')) {
			this.erRequired = true;
		}
	};

	RequiredValidator.prototype.validate = function (verdi) {
        if (!this.erRequired) {
            return true;
        } else if (verdi && verdiErIkkeTom(verdi.trim())) {
			return true;
		}
		return 'required';
	};

	window.RequiredValidator = RequiredValidator;

	var PatternValidator = function PatternValidator(scope, attrs) {
		var pattern;
		var stringPattern = harAttributt(scope, attrs, 'pattern');
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

	var LengthValidator = function LengthValidator(scope, attrs) {
		var minLengde, maxLengde;
		this.minLengde = harAttributt(scope, attrs, 'minlength');

		if (!this.minLengde) {
			this.minLengde = false;
		}

		this.maxLengde = harAttributt(scope, attrs, 'maxlength');

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

    var ValueValidator = function ValueValidator(scope, attrs) {
        var min, max;
        this.min = harAttributt(scope, attrs, 'min');

        if (!this.min) {
            this.min = false;
        } else {
            this.min = parseInt(this.min);
        }

        this.max = harAttributt(scope, attrs, 'max');

        if (!this.max) {
            this.max = false;
        } else {
            this.max = parseInt(this.max);
        }
    };

    ValueValidator.prototype.validate = function (value) {
        if (this.min && parseInt(value.trim()) < this.min) {
            return 'min'; // Invalid
        }

        if (this.max && parseInt(value.trim()) > this.max) {
            return 'max'; // Invalid
        }

        return true;
    };

    window.ValueValidator = ValueValidator;
})();
