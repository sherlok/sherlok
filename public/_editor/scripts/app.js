//'use strict';
var app = angular.module('sherlok_editor', [
  'ngSanitize',
  'ngCookies',
  'ngResource',
  'ngMaterial',
  'ngRoute',
  'ui.codemirror',
 // 'http-post-fix'
  ])
  .filter('joinBy', function () {
    return function (input, delimiter) {
      return (input || []).join(delimiter || ',').trim();
    };
  });

app.config(function ($routeProvider) {
  $routeProvider.when('/', {
    templateUrl: 'views/pipelines.html',
    controller: 'pipelines'
  // }).when('/pipelines/create', {
  //   templateUrl: 'views/pipelines/create.html',
  //   controller: 'pipelines-create'
  }).otherwise({
    redirectTo: '/'
  })
});

app.controller('pipelines', function PipelineController($scope, $http, $location, $mdToast, $mdDialog) {
  $scope.splash_page = 'splash_page.html'

  // PIPELINE
  loadPipelines = function(){
    $http.get('/pipelines').success(function (data) {
      $scope.pipelines = data;
    }).error(function (data, status) {
      alert(JSON.stringify(data));
    })
  }
  loadPipelines();

  $scope.openPipe = function(item){
    if ($scope.isPipeOpen(item)){
      $scope.activePipe = undefined;
    } else {
      $scope.activePipe = item;
      $scope.activePipe.scriptString = $scope.activePipe.script.join('\n');
      $scope.activeEngine = undefined;
    }
  };

  $scope.isPipeOpen = function(item){
    return $scope.activePipe === item;
  };

  $scope.hasActivePipeOpen = function() {
    return $scope.activePipe !== undefined;
  };

  $scope.newPipe = function() {
    $scope.activePipe = {};
  };

  $scope.closePipe = function() {
    $scope.activePipe = undefined;
    loadPipelines(); // reload in case some changes have been made
  };

  $scope.deletePipe = function() {
    var name = $scope.activePipe.name;
    if (confirm('Delete pipeline '+name+'?')){
      $http.delete('/pipelines/'+ $scope.activePipe.name+'/'+$scope.activePipe.version).success(function (data) {
        toast($mdToast, 'pipeline \''+$scope.activePipe.name+'\' deleted!');
        $scope.activePipe = undefined;
        loadPipelines();
      }).error(function (data, status) {
        alert('Could not delete pipeline, '+ JSON.stringify(data));
      })
    }
  };

  $scope.savePipe = function() {
    console.log($scope.activePipe);

    $scope.activePipe.script = $scope.activePipe.scriptString.split('\n');
    delete $scope.activePipe.scriptString;

    // FIXME /pipelines?testonly=true
    $http.put('/pipelines', $scope.activePipe).success(function (data) {
      toast($mdToast, 'pipeline \''+$scope.activePipe.name+'\' saved!');
      // refresh and set activePipe
      var name = $scope.activePipe.name;
      var version = $scope.activePipe.version;
      loadPipelines();
      for (pid in $scope.pipelines){
        p = $scope.pipelines[pid];
        if (p.name == name && p.version == version){
          $scope.activePipe = p;
          $scope.activePipe.scriptString = $scope.activePipe.script.join('\n');
          $scope.activeEngine = undefined;
        }
      }
    }).error(function (data, status) {
      alert('could not save pipeline, '+  JSON.stringify(data));
    })
  };

  // TESTS
  $scope.runAllTests = function(){
    var ap = $scope.activePipe;
    for (var i = ap.tests.length - 1; i >= 0; i--) {
      runTest($scope.activePipe, i);
    };
    toast($mdToast, ap.tests.length + ' tests successfully completed');
  };
  runTest = function(ap, id){
    var test = ap.tests[id];
    Sherlok.annotate(ap.name, ap.version, test['in'], function(annotatedHtml){
      $('div#test_out_' + id).html(annotatedHtml);
    });
  }

  // ENGINES
  loadEngines = function(){
    $http.get('/engines').success(function (data) {
      $scope.engines = {};
      for (var en in data){
        $scope.engines[en.name+':'+en.version] = en;
      }
    }).error(function (data, status) {
      console.log('Error ' + data)
    })
  }
  loadEngines();

  // RUTA EDITOR
  $scope.editorOptions = {
        lineNumbers: true,
        mode: 'ruta',
  };
});


toast = function(toaster, msg){
  toaster.show({
    template: '<md-toast>'+msg+'</md-toast>',
    hideDelay: 3000,
    position: 'top right'
  });
}
