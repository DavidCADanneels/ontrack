angular.module('ot.extension.svn.changelog', [
    'ui.router',
    'ot.service.core',
    'ot.service.structure'
])
    .config(function ($stateProvider) {
        // SVN configurations
        $stateProvider.state('svn-changelog', {
            url: '/extension/svn/changelog?branch&from&to',
            templateUrl: 'app/extension/svn/svn.changelog.tpl.html',
            controller: 'SVNChangeLogCtrl'
        });
    })
    .controller('SVNChangeLogCtrl', function ($q, $log, $interpolate, $anchorScroll, $location, $stateParams, $scope, $http, ot, otStructureService) {

        // The build request
        $scope.buildDiffRequest = {
            branch: $stateParams.branch,
            from: $stateParams.from,
            to: $stateParams.to
        };

        // The view
        var view = ot.view();
        view.title = "Subversion change log";

        // Loading the branch
        otStructureService.getBranch($stateParams.branch).then(function (branch) {
            view.breadcrumbs = ot.branchBreadcrumbs(branch);
        });

        /**
         * The REST end point to contact is contained by the current path, with the leading
         * slash being removed.
         */
        var path = $location.path().substring(1);

        /**
         * Loads the change log
         */

        ot.pageCall($http.get(path, {params: $scope.buildDiffRequest})).then(function (changeLog) {
            $scope.changeLog = changeLog;

            $scope.revisionsCommand = "Revisions";

            // Loading the revisions if needed
            $scope.changeLogRevisions = function () {
                // Loads the revisions if needed
                if (!$scope.revisions) {
                    $scope.revisionsLoading = true;
                    $scope.revisionsCommand = "Loading the revisions...";
                    ot.pageCall($http.get($scope.changeLog._revisions)).then(function (revisions) {
                        $scope.revisions = revisions;
                        $log.info('Revisions loaded: ', $scope.revisions);
                        // TODO Navigates to the revisions section (after it has been displayed)
                        $location.hash('revisions');
                        $anchorScroll();
                        $scope.revisionsLoading = false;
                        $scope.revisionsCommand = "Revisions";
                    });
                }
            };
        });

    })
;