var app = angular.module('sherlok_editor', [
  'ngSanitize',
  'ngCookies',
  'ngResource',
  'ngMaterial',
  'ngRoute',
  'http-post-fix'
  ])
  .filter('joinBy', function () {
    return function (input, delimiter) {
      return (input || []).join(delimiter || ',').trim();
    };
  // })
  // .filter('snippet', function () {
  //   return function (input, maxsize) {
  //     if ((input || "").length < (maxsize - 4)){
  //       return input;
  //     } else {
  //       return (input || "").substring()
  //     }
  //   };
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
      console.log('Could not load pipelines:: '+ data);
    })
  }
  loadPipelines();

  $scope.openPipe = function(item){
    if ($scope.isPipeOpen(item)){
      $scope.activePipe = undefined;
    } else {
      $scope.activePipe = item;
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
    $http.put('/pipelines', $scope.activePipe).success(function (data) {
      toast($mdToast, 'pipeline \''+$scope.activePipe.name+'\' saved!');
      // refresh and set activePipe
      var name = $scope.activePipe.name;
      var version = $scope.activePipe.version;
      loadPipelines();
      for (p in $scope.pipelines){
        if (p.name == name && p.version == version){
          $scope.activePipe = p;
        }
      }
    }).error(function (data, status) {
      alert('could not save pipeline, '+  JSON.stringify(data));
    })
  };

  // RUTA ENGINE
  $scope.openRutaEngine = function(id){
    console.log(id);
  };
  $scope.runAllTests = function(){
    var test = $scope.activePipe.tests[0];
    Sherlok.annotate($scope.activePipe.name, $scope.activePipe.version,
      test['in'], function(txt){
      console.log(txt);
      $scope.activePipe.tests[0].out2 = "<strong>wow</strong>";
      $scope.activePipe.tests[0].out3 = "yep";
    });
  };


  // ENGINE
  $scope.openEngine = function(_engineId){
    console.log(_engineId);
    if ($scope.isEngineOpen(_engineId)){ // close activeEngine
      $scope.activeEngine = undefined;
    } else {
      $scope.activeEngine = _engineId;
    }
  };

  $scope.isEngineOpen = function(item){
    return $scope.activeEngine === item;
  };

  $scope.isRuta = function(_engine){
    return _engine.script != null;
  };

  $scope.hasActiveEngineEngineOpen = function() {
    return $scope.activeEngine !== undefined;
  };

  $scope.closeEngine = function() {
    $scope.activeEngine = undefined;
  };

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

});

toast = function(toaster, msg){
  toaster.show({
    template: '<md-toast>'+msg+'</md-toast>',
    hideDelay: 3000,
    position: 'top right'
  });
}
