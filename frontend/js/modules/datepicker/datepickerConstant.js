angular.module('nav.datepicker.constant', [])
    .constant('datepickerConfig', {
        altFormat  : 'dd.MM.yyyy',
        dateFormat : 'dd.mm.yy',
        changeMonth: true,
        changeYear : true,
        yearRange: "c-30:c+10"
    });