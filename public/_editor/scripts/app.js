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
          Sherlok.annotateElement(newTest[0], newTest[1], function(html, types){
            element[0].innerHTML = html;
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
            if (input){
              return JSON.parse(input);
            }
          }
          function out(data) {
            return JSON.stringify(data);
          }
          ngModel.$parsers.push(into);
          ngModel.$formatters.push(out);
        }
    };
});

app.controller('pipelines', function PipelineController($scope, $http, $location, $mdToast, $mdDialog, $cookies) {
  $scope.tabsSelectedIndex = 0;

  $scope.annotate = {};
  $scope.annotate.annotating = false;
  $scope.annotate.text = $cookies.annotateText || "";
  $scope.annotate.types = [];

  // PIPELINE
  loadPipelines = function(){
    $http.get('/pipelines').success(function (data) {
      $scope.pipelines = data;
      // open last viewed pipeline
      var last = $cookies.lastPipeline || 0;
      $scope.openPipe($scope.pipelines[last]);
    }).error(function (data, status) {
      alert(JSON.stringify(data));
    })
  }
  loadPipelines();

  preProcess = function(p){
    // transform json string array & add a few linefeeds
    p.scriptString = p.script.join('\n') + '\n\n\n';
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
    pCopy.script = p.scriptString.trim().split('\n');
    delete pCopy.scriptString;
    delete pCopy.testsOk;
    delete pCopy.testsFailed;
    return pCopy;
  }

  $scope.openPipe = function(p){
    $scope.activePipe = preProcess(p);
    $cookies.lastPipeline = $scope.pipelines.indexOf(p);
  };

  $scope.newPipe = function() {
    $scope.activePipe = {};
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
    $http.post('/pipelines', postProcess($scope.activePipe)).success(function (data) {
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
    $scope.testing = true;
    $http.post('/test', postProcess($scope.activePipe)).success(function (data) {
      toast($mdToast, 'all tests passed!');
      // update ok/fail counts
      $scope.activePipe.testsOk = $scope.activePipe.tests.length;
      $scope.activePipe.testsFailed = 0;
      // update actual field
      var p = data.passed;
      for (var id in p) {
        if (p.hasOwnProperty(id)) {
          $scope.activePipe.tests[id].actual = p[id].system;
        }
      }
      $scope.testing = false; // reactivates button
    }).error(function (testResults, status) {
      toast($mdToast, 'some tests failed');
      console.log(testResults);
      var f = testResults.failed;
      $scope.activePipe.testsFailed = Object.keys(f).length;
      $scope.activePipe.testsOk = $scope.activePipe.tests.length - $scope.activePipe.testsFailed;
      for (var id in f) {
        if (f.hasOwnProperty(id)) {
          $scope.activePipe.tests[id].actual = f[id].system;
        }
      }
      $scope.testing = false;
    })
  };

  $scope.testsStatus = function(){
    if ($scope.activePipe.testsOk + $scope.activePipe.testsFailed == 0){
      return "gray";       //unknown
    } else if ($scope.activePipe.testsFailed > 0){
      return "red";         // fail
    } else return "green";  // ok
  };

  $scope.newTest = function() {
    $scope.activePipe.tests.push({"expected" : {}, "input" : "", "visible": true});
  };

  // copies current pipeline, and set as first test the text to annotate
  $scope.annotateText = function(){
     $scope.annotate.annotating = true;
    var txt = $scope.annotate.text;
    var p = angular.copy(postProcess($scope.activePipe));
    p.tests = {"expected" : {}, "input" : txt};
    $http.post('/test', p).success(function (data) {
      var annotated = data.passed[0].system;
      Sherlok.annotateElement(txt, annotated, function(html, types){
        $scope.annotate.html = html;
        $scope.annotate.types = [];
        for (var t in types) {
          $scope.annotate.types.push({"name": types[t], "activated":true});
        }
        $scope.annotate.annotating = false;
      });
    }).error(function (testResults, status) {
      alert("could not annotate text: ", testResults.failed);
      console.log(testResults);
       $scope.annotate.annotating = false;
    })
  };
  $scope.$watch('annotate.text', function() {
    $cookies.annotateText = $scope.annotate.text || "";
  });

  $scope.toggleType = function(type){
    console.log(type);

    if (!type.activated){// --> reactivate
      $("._inactive" + type.name)
       //.toggleClass("inline-a")
       .toggleClass("np_" + type.name)
       .toggleClass("_inactive" + type.name);
      type.activated = false;

    } else {              // --> deactivate
      $(".np_" + type.name)
      .toggleClass("_inactive" + type.name)
      .toggleClass("np_" + type.name)
      //.toggleClass("inline-a");
      type.activated = true;
    }
  }

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
    position: 'bottom right'
  });
}

$("body").on("focus", ".CodeMirror", function(e){
  // $(".CodeMirror").css("height", "auto");
  // var myTextArea = document.getElementById('myText');
  //   var myCodeMirror = CodeMirror.fromTextArea(myTextArea);
  //   myCodeMirror.setSize(500, 300);
});
