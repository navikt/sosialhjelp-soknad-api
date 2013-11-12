var RequiredValidator = (function() {
    var init = function(attrs, valideringsMetoder) {
        if (harAttributt(attrs, 'required')) {
            valideringsMetoder.push(validate);
        }
    }

    var validate = function(value) {
        if (value && verdiErIkkeTom(value.trim())) {
            return true;
        }
        return 'required';
    }

    return {
        init: init
    };
})();

var PatternValidator = (function() {
    var pattern;

    var init = function(attrs, valideringsMetoder) {
        var stringPattern = harAttributt(attrs, 'pattern');
        if (stringPattern) {
            valideringsMetoder.push(validate);

            // Tatt fra angular for å bygge regexp fra string
            var match = stringPattern.match(/^\/(.*)\/([gim]*)$/);
            pattern = new RegExp(match[1], match[2]);
        }
    }

    var validate = function(value) {
        if (pattern.test(value)) {
            return true;
        }
        return 'pattern';
    }

    return {
        init: init
    };
})();

var LengthValidator = (function() {
    var minLengde, maxLengde;

    var init = function(attrs, valideringsMetoder) {
        minLengde = harAttributt(attrs, 'minlength');

        if (minLengde) {
            valideringsMetoder.push(validateMinimum);
        }

        maxLengde = harAttributt(attrs, 'maxlength');

        if (maxLengde) {
            valideringsMetoder.push(validateMaximum);
        }

    }

    var validateMinimum = function(value) {
        if (value.trim().length >= minLengde) {
            return true;
        }

        return 'minlength';
    }

    var validateMaximum = function(value) {
        if (value.trim().length <= maxLengde) {
            return true;
        }

        return 'maxlength';
    }

    return {
        init: init
    };
})();