angular.module('ot.dialog.form', [])
    .controller('otDialogForm', function ($scope, $modalInstance, config) {
        // General configuration
        $scope.config = config;
        // Form data
        $scope.data = {};
        $scope.dates = {};
        $scope.times = {};
        angular.forEach(config.form.fields, function (field) {
            $scope.data[field.name] = field.value;
            if (field.regex) {
                field.pattern = new RegExp(field.regex);
            }
            // Date-time handling
            if (field.type == 'dateTime') {
                if (field.value) {
                    var dateTime = new Date(field.value);
                    $scope.dates[field.name] = dateTime;
                    $scope.times[field.name] = dateTime;
                }
            }
        });
        // Cancelling the dialog
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
        // Submitting the dialog
        $scope.submit = function (isValid) {
            if (isValid) {
                config.formConfig.submit($scope.data).then(
                    function success() {
                        $modalInstance.close('ok');
                    },
                    function error(message) {
                        $scope.message = message;
                    });
            }
        };
    })
;