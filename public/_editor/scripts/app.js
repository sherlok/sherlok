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

app.directive('renderAnnotations', function($compile) {
  return {
    link: function(scope, element, attr) {
      scope.$watchGroup(['test.input', 'test.expected'], function(newTest, oldValue) {
        if (newTest) {
          Sherlok.annotateElement(newTest[0], newTest[1], function(annotatedHtml){
            element[0].innerHTML = annotatedHtml;
            $compile(element.contents())(scope);
          });
        }
      });
    }
  };
});
// Binds a (JSON) object to a textarea. Otherwise, displays [object - Object]
app.directive('jsonText', function() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attr, ngModel) {
          function into(input) {
            return JSON.parse(input);
          }
          function out(data) {
            return JSON.stringify(data);
          }
          ngModel.$parsers.push(into);
          ngModel.$formatters.push(out);
        }
    };
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

  preProcess = function(p){
    p.scriptString = p.script.join('\n');
    p.testsOk = 0;
    p.testsFailed = 0;
    for (var i = p.tests.length - 1; i >= 0; i--) {
      p.tests[i].visible = false;
      p.tests[i].actual = p.tests[i].expected;
    };
    return p;
  }

  postProcess = function(p){
    pCopy = angular.copy(p);
    pCopy.script = p.scriptString.split('\n');
    delete pCopy.scriptString;
    delete pCopy.testsOk;
    delete pCopy.testsFailed;
    return pCopy;
  }

  $scope.openPipe = function(p){
    if ($scope.isPipeOpen(p)){ // user clicked on pipeline again?
      $scope.activePipe = undefined; // then close active pipeline
    } else {
      $scope.activePipe = preProcess(p);
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
    $http.put('/pipelines', postProcess($scope.activePipe)).success(function (data) {
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
        }
      }
    }).error(function (data, status) {
      alert('could not save pipeline, '+  JSON.stringify(data));
    })
  };

  // TESTS
  $scope.runAllTests = function(){
    $http.post('/test', postProcess($scope.activePipe)).success(function (data) {
      toast($mdToast, 'all tests passed!');
      $scope.activePipe.testsOk = $scope.activePipe.tests.length;
      $scope.activePipe.testsFailed = 0;
    }).error(function (testResults, status) {
      toast($mdToast, 'some tests failed');
      console.log(testResults);
      $scope.activePipe.testsFailed = Object.keys(testResults).length;
      $scope.activePipe.testsOk = $scope.activePipe.tests.length - $scope.activePipe.testsFailed;
      for (var testId in testResults) {
        if (testResults.hasOwnProperty(testId)) {
            $scope.activePipe.tests[testId].actual = testResults[testId].system;
        }
      }
    })
  };

  $scope.testsStatus = function(){
    if ($scope.activePipe.testsOk + $scope.activePipe.testsFailed == 0){
      return "gray";       //unknown
    } else if ($scope.activePipe.testsFailed > 0){
      return "red";         // fail
    } else return "green";  // ok
  };

  // // ENGINES
  // loadEngines = function(){
  //   $http.get('/engines').success(function (data) {
  //     $scope.engines = {};
  //     for (var en in data){
  //       $scope.engines[en.name+':'+en.version] = en;
  //     }
  //   }).error(function (data, status) {
  //     console.log('Error ' + data)
  //   })
  // }
  // loadEngines();

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

$("body").on("focus", ".CodeMirror", function(e){
  // $(".CodeMirror").css("height", "auto");
  // var myTextArea = document.getElementById('myText');
  //   var myCodeMirror = CodeMirror.fromTextArea(myTextArea);
  //   myCodeMirror.setSize(500, 300);
});
