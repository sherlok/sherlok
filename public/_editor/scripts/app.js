var app = angular.module('sherlok_editor', [
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute'
]);
app.config(function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: 'views/pipelines.html',
        controller: 'pipelines'
    // }).when('/pipelines/create', {
    //     templateUrl: 'views/pipelines/create.html',
    //     controller: 'pipelines-create'
    }).otherwise({
        redirectTo: '/'
    })
});

// app.controller('pipelines', function ($scope, $http, $location) {
//     $http.get('/pipelines').success(function (data) {
//         $scope.pipelines = data;
//     }).error(function (data, status) {
//         console.log('Error ' + data)
//     })

//     $scope.showPipeline = function () {
//         console.log('showwww');
//     }
//     $scope.createPipeline = function () {
//         console.log($scope.pipeline);
//         $http.put('/pipelines', $scope.pipeline).success(function (data) {
//             $location.path('/');
//         }).error(function (data, status) {
//             console.log('Error ' + data)
//         })
//     }
// });

app.controller('pipelines', function PipelineController($scope, $http, $location) {
    $http.get('/pipelines').success(function (data) {
        $scope.pipelines = data;
    }).error(function (data, status) {
        console.log('Error ' + data)
    })

    $scope.open = function(item){
        if ($scope.isOpen(item)){
            $scope.activepipe = undefined;
        } else {
            $scope.activepipe = item;
        }
    };

    $scope.isOpen = function(item){
        return $scope.activepipe === item;
    };

    $scope.hasActivePipeOpen = function() {
        return $scope.activepipe !== undefined;
    };

    $scope.close = function() {
        $scope.activepipe = undefined;
    };

    $scope.beep = function() {
        alert('beep from '+$scope.activepipe.name);
    };
});
