angular.module('ot.directive.misc', [

])
    .directive('otNoentry', function () {
        return {
            restrict: 'E',
            templateUrl: 'app/directive/directive.noentry.tpl.html',
            transclude: true,
            scope: {
                list: '='
            }
        };
    })
;