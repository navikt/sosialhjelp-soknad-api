angular.module('nav.datepicker.constant', [])
    .constant('datepickerConfig', {
        altFormat  : 'dd.MM.yyyy',
        dateFormat : 'dd.mm.yy',
        changeMonth: true,
        changeYear : true,
        yearRange: "c-30:c+10"
    })
    .constant('datepickerInputKeys', [
        47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 190
    ])
    .constant('datepickerUtilityKeys', [
        46, 37, 39, 8, 9, 35, 36, 45
    ])
    .constant('periodKeyCode', 190)
    .constant('ctrlKeyCodes', [65, 67, 88, 86])
