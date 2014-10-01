angular.module('ot.dialog.branch.clone', [
    'ot.service.core',
    'ot.service.structure',
    'ot.directive.branch.replacements'
])
    .controller('otDialogBranchClone', function ($scope, $modalInstance, $http, config, ot, otStructureService) {
        // Inject the configuration into the scope
        $scope.config = config;
        // Selection object
        $scope.data = {
            propertyReplacements: [{
                regex: config.sourceBranch.name
            }],
            promotionLevelReplacements: [{
                regex: config.sourceBranch.name
            }],
            validationStampReplacements: [{
                regex: config.sourceBranch.name
            }]
        };
        // Cancelling the dialog
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
        // Submitting the dialog
        $scope.submit = function (isValid) {
            if (isValid) {
                $modalInstance.close($scope.data);
            }
        };
    })
;