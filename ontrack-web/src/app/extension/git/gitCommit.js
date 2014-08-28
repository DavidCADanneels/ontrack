angular.module('ot.extension.git.commit', [
    'ui.router',
    'ot.service.core'
])
    .config(function ($stateProvider) {
        $stateProvider.state('git-commit', {
            url: '/extension/git/{branch}/commit/{commit}',
            templateUrl: 'app/extension/git/git.commit.tpl.html',
            controller: 'GitCommitCtrl'
        });
    })
    .controller('GitCommitCtrl', function ($stateParams, $scope, $http, $interpolate, ot) {

        var view = ot.view();

//        ot.call(
//            $http.get(
//                $interpolate('extension/git/{{branch}}/commit/{{commit}}')($stateParams)
//            )).then(function (ontrackGitIssueInfo) {
//                $scope.ontrackGitIssueInfo = ontrackGitIssueInfo;
//            });
    })
;