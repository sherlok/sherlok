
var app = angular.module('sherlok_editor', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute'
]);

app.config(function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: 'views/pipelines/list.html',
        controller: 'pipelines-list'
    }).when('/pipelines/create', {
        templateUrl: 'views/pipelines/create.html',
        controller: 'pipelines-create'
     }).when('/pipelines/show', {
        templateUrl: 'views/pipelines/show.html',
        controller: 'pipelines-show'
    }).otherwise({
        redirectTo: '/'
    })
});

app.controller('pipelines-list', function ($scope, $http) {
    $http.get('/pipelines').success(function (data) {
        $scope.pipelines = data;
    }).error(function (data, status) {
        console.log('Error ' + data)
    })
    $scope.showPipeline = function () {
        console.log('showwww');
    }
});

app.controller('pipelines-show', function ($scope, $http, $location) {
    $scope.pipeline = {
        done: false
    };
    $scope.createPipeline = function () {
        console.log($scope.pipeline);
        $http.put('/pipelines', $scope.pipeline).success(function (data) {
            $location.path('/');
        }).error(function (data, status) {
            console.log('Error ' + data)
        })
    }
});

app.controller('pipelines-create', function ($scope, $http, $location) {
    $scope.pipeline = {
        done: false
    };
    $scope.createPipeline = function () {
        console.log($scope.pipeline);
        $http.put('/pipelines', $scope.pipeline).success(function (data) {
            $location.path('/');
        }).error(function (data, status) {
            console.log('Error ' + data)
        })
    }
});
