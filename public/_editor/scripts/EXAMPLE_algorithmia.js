(function() {
  var AlgorithmError, Proxy, algoPattern, handleApiResponse, proxy;

  window.Algorithmia = window.Algorithmia || {};

  if (Algorithmia.query) {
    console.error("Warning: algorithmia.js loaded twice");
  }

  if (Algorithmia.apiAddress) {
    console.log("Using alternate API server: " + Algorithmia.apiAddress);
  }

  if (Algorithmia.apiAddress === void 0) {
    Algorithmia.apiAddress = "https://api.algorithmia.com";
  }

  proxy = null;

  algoPattern = /^(?:algo:\/\/|\/|)(\w+\/.+)$/;

  Algorithmia.query = function(algo_uri, api_key, data, cb) {
    if (!(typeof algo_uri === "string" && algo_uri.match(algoPattern))) {
      return cb("Invalid Algorithm URI (expected /user/algo)");
    }
    if (proxy === null || proxy.apiAddress !== Algorithmia.apiAddress) {
      proxy = new Proxy();
    }
    proxy.query(algo_uri, api_key, data, cb);
  };

  Algorithmia.queryNative = function(algo_uri, api_key, data, cb) {
    var endpoint_url, xhr;
    Algorithmia.startTask();
    if (!(typeof algo_uri === "string" && algo_uri.match(algoPattern))) {
      return cb("Invalid Algorithm URI (expected /user/algo)");
    }
    endpoint_url = Algorithmia.apiAddress + "/api" + algo_uri;
    xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
      var error, responseJson;
      if (xhr.readyState === 4) {
        Algorithmia.finishTask();
        if (xhr.status === 0) {
          if (xhr.responseText) {
            cb("API connection error: " + xhr.responseText);
          } else {
            cb("API connection error");
          }
        } else if (xhr.status === 502) {
          cb("API error, bad gateway");
        } else if (xhr.status === 503) {
          cb("API error, service unavailable");
        } else if (xhr.status === 504) {
          cb("API error, server timeout");
        } else {
          try {
            responseJson = JSON.parse(xhr.responseText);
            handleApiResponse(responseJson, cb);
          } catch (_error) {
            error = _error;
            console.error("API error", xhr.responseText);
            cb("API error (status " + xhr.status + "): " + error);
          }
        }
      }
    };
    xhr.open("POST", endpoint_url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.setRequestHeader("Accept", "application/json, text/javascript");
    xhr.setRequestHeader("Authorization", api_key);
    xhr.send(JSON.stringify(data));
  };

  handleApiResponse = function(response, cb) {
    if (response.error) {
      cb(new AlgorithmError(response.error, response.stacktrace));
    } else {
      cb(response.error, response.result);
    }
  };

  AlgorithmError = function(message, stacktrace) {
    this.error = message;
    this.stacktrace = stacktrace;
  };

  AlgorithmError.prototype.toString = function() {
    return this.error;
  };

  Proxy = function() {
    var div, proxy_url, timeoutDuration, timeoutFunction, windowOnload;
    this.connected = false;
    this.queue = [];
    this.pending = {};
    this.apiAddress = Algorithmia.apiAddress;
    proxy_url = this.apiAddress + "/api/proxy";
    div = document.createElement("div");
    div.innerHTML = "<iframe id=\"aiFrame\" src=\"" + proxy_url + "\" height=\"0\" width=\"0\" tabindex=\"-1\" style=\"display: none;\">";
    this.iframe = div.firstChild;
    if (document.readyState === "complete") {
      document.body.appendChild(this.iframe);
    } else {
      windowOnload = window.onload;
      window.onload = (function(_this) {
        return function() {
          document.body.appendChild(_this.iframe);
          if (windowOnload) {
            return windowOnload();
          }
        };
      })(this);
    }
    window.addEventListener("message", (function(_this) {
      return function(event) {
        var cb;
        if (event.origin === Algorithmia.apiAddress) {
          if (event.data.proxy === "ready") {
            _this.connected = true;
            _this.onReady();
          } else if (event.data.queryId && _this.pending[event.data.queryId]) {
            cb = _this.pending[event.data.queryId];
            delete _this.pending[event.data.queryId];
            Algorithmia.finishTask();
            handleApiResponse(event.data, cb);
          }
        }
      };
    })(this));
    timeoutDuration = 10000;
    timeoutFunction = (function(_this) {
      return function() {
        if (!_this.connected) {
          _this.queue.forEach(function(request) {
            var cb;
            cb = _this.pending[request.queryId];
            delete _this.pending[request.queryId];
            Algorithmia.finishTask();
            cb("Failed to initialize Algorithmia API Proxy");
          });
        }
      };
    })(this);
    setTimeout(timeoutFunction, timeoutDuration);
    this.query = function(algo_uri, api_key, data, cb) {
      var queryId, request;
      Algorithmia.startTask();
      if (typeof data === 'function') {
        data = data.toString();
      }
      queryId = Math.floor(Math.random() * 0x10000000);
      this.pending[queryId] = cb;
      request = {
        algo_uri: algo_uri,
        api_key: api_key,
        input: data,
        queryId: queryId
      };
      if (this.connected) {
        this.sendRequest(request);
      } else {
        this.queue.push(request);
      }
    };
    this.sendRequest = function(request) {
      var iwindow;
      iwindow = this.iframe.contentWindow || this.iframe.contentDocument;
      iwindow.postMessage(request, "*");
    };
    this.onReady = function() {
      this.queue.forEach((function(_this) {
        return function(request) {
          _this.sendRequest(request);
        };
      })(this));
    };
  };

  Algorithmia.tasksInProgress = 0;

  Algorithmia.startTask = function() {
    var spinner, _i, _len, _ref;
    if (Algorithmia.tasksInProgress === 0) {
      _ref = document.getElementsByClassName("algo-spinner");
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        spinner = _ref[_i];
        spinner.style.visibility = "visible";
      }
    }
    Algorithmia.tasksInProgress++;
  };

  Algorithmia.finishTask = function() {
    var spinner, _i, _len, _ref;
    Algorithmia.tasksInProgress--;
    if (Algorithmia.tasksInProgress === 0) {
      _ref = document.getElementsByClassName("algo-spinner");
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        spinner = _ref[_i];
        spinner.style.visibility = "hidden";
      }
    } else if (Algorithmia.tasksInProgress < 0) {
      console.error("Algorithmia task error (unknown task finished)");
    }
  };

}).call(this);

//# sourceMappingURL=algorithmia.js.map
