angular.module('ot.service.branch.copy', [
    'ot.service.core',
    'ot.service.form',
    'ot.dialog.branch.copy',
    'ot.dialog.branch.clone'
])
    .service('otBranchCopyService', function ($modal, $http, ot) {
        var self = {};

        /**
         * Copies another branch's configuration to the given target branch.
         *
         * The user must first select a project and a branch.
         */
        self.copyFrom = function (targetBranch) {
            return $modal.open({
                templateUrl: 'app/dialog/dialog.branch.copy.tpl.html',
                controller: 'otDialogBranchCopy',
                resolve: {
                    config: function () {
                        return {
                            targetBranch: targetBranch,
                            submit: function (copy) {
                                var request = {
                                    sourceBranchId: copy.branch.id,
                                    propertyReplacements: copy.propertyReplacements,
                                    promotionLevelReplacements: copy.promotionLevelReplacements,
                                    validationStampReplacements: copy.validationStampReplacements
                                };
                                return ot.call($http.put(targetBranch._copy, request));
                            }
                        };
                    }
                }
            }).result;
        };

        /**
         * Clones a branch into another one.
         */
        self.cloneBranch = function (sourceBranch) {
            return $modal.open({
                templateUrl: 'app/dialog/dialog.branch.clone.tpl.html',
                controller: 'otDialogBranchClone',
                resolve: {
                    config: function () {
                        return {
                            sourceBranch: sourceBranch,
                            submit: function (specs) {
                                var request = {
                                    name: specs.name,
                                    propertyReplacements: specs.propertyReplacements,
                                    promotionLevelReplacements: specs.promotionLevelReplacements,
                                    validationStampReplacements: specs.validationStampReplacements
                                };
                                return ot.call($http.post(sourceBranch._clone, request));
                            }
                        };
                    }
                }
            }).result;
        };

        return self;
    })
;
