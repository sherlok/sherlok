'use strict';
var app = angular.module('sherlok_editor', [
  'ngSanitize',
  'ngCookies',
  'ngResource',
  'ngMaterial',
  'ngRoute',
  'ui.codemirror',
  'treeControl',
  'angularFileUpload',
  ]);

// ROUTES
app.config(function ($routeProvider) {
  $routeProvider.when('/pipelines', {
    templateUrl: 'views/pipelines.html',
    controller: 'pipelines'
  }).when('/resources', {
    templateUrl: 'views/resources.html',
    controller: 'resources'
  }).when('/bundles', {
    templateUrl: 'views/bundles.html',
    controller: 'bundles'
  }).otherwise({
    redirectTo: '/pipelines'
  })
});

// THEMES
app.config(function($mdThemingProvider) {
  $mdThemingProvider.theme('yellow')
  .primaryPalette('yellow');
  $mdThemingProvider.theme('teal')
  .primaryPalette('teal');
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
      function out(obj) {
        return angular.toJson(obj, 2);
      }
      ngModel.$parsers.push(into);
      ngModel.$formatters.push(out);
    }
  };
});

app.controller('pipelines', function PipelineController($scope, $http, $location, $mdToast, $mdDialog, $cookies) {
  $scope.tabsSelectedIndex = 0;
  $scope.jsonVisible = false;

  $scope.annotate = {};
  $scope.annotate.annotating = false;
  $scope.annotate.text = $cookies.annotateText || "";
  $scope.annotate.types = [];

  // PIPELINE
  var loadPipelines = function(){
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

  var preProcess = function(p){
    // transform json string array & add a few linefeeds
    p.scriptString = p.script.join('\n') + '\n\n\n';
    p.testsOk = 0;
    p.testsFailed = 0;
    for (var i = p.tests.length - 1; i >= 0; i--) {
      p.tests[i].visible = false;
      p.tests[i].actual = p.tests[i].expected;
    }
    return p;
  }

  var postProcess = function(p){
    var pCopy = angular.copy(p);
    if (pCopy.scriptString){
      pCopy.script = p.scriptString.trim().split('\n');
      delete pCopy.scriptString;
    }
    delete pCopy.testsOk;
    delete pCopy.testsFailed;
    return pCopy;
  }

  $scope.openPipe = function(p){
    $scope.activePipe = preProcess(p);
    $scope.annotate.annotating = false;
    $scope.testing = false;
    $cookies.lastPipeline = $scope.pipelines.indexOf(p);
  };

  $scope.newPipe = function() {
    $scope.activePipe = { 'script': '', 'tests': {}};
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
      toast($mdToast, 'pipeline \'' + $scope.activePipe.name + '\' saved!');
      // refresh and set activePipe
      var name = $scope.activePipe.name;
      var version = $scope.activePipe.version;
      loadPipelines();
      for (var pid in $scope.pipelines){
        var p = $scope.pipelines[pid];
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
    if ($scope.activePipe.tests.length == 0 || !$scope.activePipe.tests[0].input){
      toast($mdToast, 'No tests for this pipeline. Click [edit] to add some!');
      $scope.testing = false; // reactivates button
      return;
    }
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
}); ////////////////////////////////////////////////////////////////////////////////////////////////

app.controller('resources', function ResourceController($scope, $route, $http, $location, $mdToast, FileUploader, $mdDialog) {

  $scope.uploader = new FileUploader();
  $scope.uploader.onErrorItem = function(fileItem, response, status, headers) {
    console.info('onErrorItem', fileItem, response, status, headers);
  };
  $scope.uploader.onCompleteAll = function() {
    loadResources();
    toast($mdToast, 'resource sucessfully uploaded');
  };

  var toTree = function(flat){
    var tree = { 'children':[], 'path':''};
    for (var i = 0; i < flat.length; i++) {
      var path    = flat[i];
      var t       = path.split('/');
      var name    = t.slice(-1)[0];
      var parents = t.slice(0, -1);
      var runningNode = tree; // start at 'root'
      for (var j = 0; j < parents.length; j++) { // iterate the parents down the path
        var pName = parents[j];
        // iterating on children and matching on name
        var found = false;
        for (var k = 0; !found & k < runningNode['children'].length; k++) {
          if (runningNode['children'][k]['name'] == pName){
            found = true; // already one node for this folder
            runningNode = runningNode['children'][k]; // update runningNode
          }
        }
        if (!found) { // -> create new folder for this parent
          runningNode['children'].push({
            'children' : [],
            'name': pName,
            'isFolder' : true,
            'path': runningNode['path'] + '/' + pName
          });
          runningNode = runningNode['children'].slice(-1)[0];// last (that we just added)
        }
      }
      // add new node to parent (=runningNode)
      runningNode['children'].push({'path':'/' + path, 'name': name, 'isFolder':false});
    }
    console.log(tree);
    return tree;
  }

  var loadResources = function(){
    $http.get('/resources').success(function (rs) {
      $scope.resources = rs;
      $scope.resourcesTree = toTree(rs);
    }).error(function (data, status) {
      alert(JSON.stringify(data));
    })
  }
  loadResources();

  $scope.showResource = function(node){
    $scope.resource = node;
    if (!node.isFolder) {
      $http.get('/resources' + node.path).success(function (rs) {
        $scope.resource.content = rs;
      }).error(function (data, status) {
        alert(JSON.stringify(data));
      })
    }
  }

  $scope.deleteResource = function(){
    var r = $scope.resource;
    if (confirm('Delete resource '+r.name+'?')){
      $http.delete('/resources/'+ r.path).success(function (data) {
        toast($mdToast, 'resource \''+r.name+'\' deleted!');
        $scope.resource = undefined;
        loadResources();
      }).error(function (data, status) {
        alert('Could not delete resource, '+ JSON.stringify(data));
      })
    }
  }

  $scope.updateResource = function(){
    $http({
      method: 'POST',
      url: '/resources/'+ $scope.resource.path,
      headers: {'Content-Type': undefined}, //multipart/form-data fails...
      transformRequest: function (data) {
        var formData = new FormData();
        formData.append("file", data);
        return formData;
      },
      data: $scope.resource.content
    }).success(function (data) {
      toast($mdToast, 'resource \''+$scope.resource.name+'\' updated');
    }).error(function (data, status) {
      alert('Could not update resource, '+ JSON.stringify(data));
      loadResources(); // refresh
    });
  }

  $scope.showUploadDialog = function(ev) {
    var up = $scope.uploader;
    var parentPath = '';
    if  (typeof $scope.resource === 'undefined'){ // no resource selected -> use root
      parentPath = '/';
    } else if (!$scope.resource.isFolder) { // a file -> use parent path
      parentPath = $scope.resource.path.substring(0, $scope.resource.path.lastIndexOf("/"));
    } else {
      parentPath = $scope.resource.path || '';
    }
    if (parentPath.length == 0){
      parentPath = '/';
    }
    $mdDialog.show({
      controller: function ($scope, $mdDialog, $route) {
        $scope.parentPath = parentPath;
        $scope.up = up;
        $scope.cancel = function() {
          $mdDialog.cancel();
        };
        $scope.uploadResource = function() {
          var f = up.queue[0];
          f.url = '/resources/' + parentPath + '/' + f.file.name;
          up.uploadAll();
          $mdDialog.hide();

        };
      },
      templateUrl: 'views/upload.html',
    });
  };

  $scope.cleanRemoteResources = function(ev) {
    $http({
      method: 'DELETE',
      url: '/clean/remote_resources'
    }).success(function (data) {
      toast($mdToast, 'remote resources cleaned');
    }).error(function (data, status) {
      alert('Could not clean remote resource, '+ JSON.stringify(data));
      loadResources(); // refresh
    })
  };
});

app.controller('bundles', function BundleController($scope, $http, $mdToast) {

  $scope.jsonVisible = true;

  var loadBundles = function(){
    $http.get('/bundles').success(function (data) {
      $scope.bundles = data;
    }).error(function (data, status) {
      alert(JSON.stringify(data));
    })
  }
  loadBundles();

  $scope.openBundle = function(b){
    $scope.bundle = b;
  };

  $scope.newBundle = function() {
    $scope.bundle = {};
  };

  $scope.deleteBundle = function() {
    var name = $scope.bundle.name;
    if (confirm('Delete bundle '+name+'?')){
      $http.delete('/bundles/'+ $scope.bundle.name+'/'+$scope.bundle.version).success(function (data) {
        toast($mdToast, 'bundle \''+$scope.bundle.name+'\' deleted!');
        $scope.bundle = undefined;
        loadBundles();
      }).error(function (data, status) {
        alert('Could not delete bundle, '+ JSON.stringify(data));
      })
    }
  };

  $scope.saveBundle = function() {
    $http.post('/bundles', $scope.bundle).success(function (data) {
      toast($mdToast, 'bundle \'' + $scope.bundle.name + '\' saved!');
    }).error(function (data, status) {
      alert('could not save bundle, '+  JSON.stringify(data));
    })
  };
});

var toast = function(toaster, msg){
  toaster.show({
    template: '<md-toast>'+msg+'</md-toast>',
    hideDelay: 3000,
    position: 'bottom right'
  });
}
