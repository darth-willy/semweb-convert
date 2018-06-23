/**
 * Copyright 2016 William Van Woensel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * 
 * @author wvw
 * 
 */

// most code should probably be replaced by library calls (e.g., jQuery)
doLog = function(msg) {
	console.log(msg);
}

httpRequest = {

	post : function(url, headers, data, callback) {
		var self = this;

		var timeout = config.webService.timeout;
		var xmlHttp = new XMLHttpRequest();
		xmlHttp.overrideMimeType("text/plain");

		// check for timeouts manually
		// (xmlHttp.ontimeout does not work on phonegap (Android))
		setTimeout(function() {
			if (xmlHttp.readyState != 4) {
				// xmlHttp readyState ends up at 4 when aborted
				xmlHttp.aborted = true;
				xmlHttp.abort();

				var errorMsg = "Error contacting service: request timed out";
				console.log(errorMsg);
			}

		}, timeout);

		xmlHttp.onreadystatechange = function() {
			if (xmlHttp.readyState == 4) {

				if (xmlHttp.aborted)
					return;

				switch (xmlHttp.status) {

				case 200:
					if (xmlHttp.responseText) {
						var res = JSON.parse(xmlHttp.responseText);

						callback(res);

					} else
						doLog("Error contacting service: no response text");

					break;

				default:
					var errorMsg = "Error contacting service: status '"
							+ xmlHttp.statusText + "' (" + xmlHttp.status + ")";

					throw errorMsg;

					break;
				}
			}
		};

		xmlHttp.open('POST', url, true);

		xmlHttp.setRequestHeader("Content-Type", "text/plain");
		xmlHttp.setRequestHeader("Accept", "text/plain");
		xmlHttp.setRequestHeader("Access-Control-Allow-Origin", "*");

		xmlHttp.send(data);
	}
};

function read(path) {
	var req = new XMLHttpRequest();
	req.overrideMimeType("text/plain");

	try {
		req.open('GET', path, false);
		req.send(null);

		if (req.status == 200)
			return req.responseText;

		else {
			// throw "error retrieving resource (" + path + "): status code "
			// + req.status;

			return null;
		}

	} catch (e) {
		return null;
	}
}

var loadDone = null;

function loadScript(path, callback) {
	// doLog("loadScript: " + path);

	var code = read(path);

	loadDone = callback;
	code += "\nwindow.loadDone();";

	$.globalEval(code);
}

function loadSelection(name) {
	var path = "res/owl/owl2rl/" + name + "/";

	var config = {};

	var resources = read(path + "res").split("\n");
	for (var i = 0; i < resources.length; i++) {
		var resource = resources[i].trim();

		if (resource == "")
			continue;

		config[resource] = read(path + resource);
	}

	return config;
}

function convert(content, type, format, callback) {
	var urlParams = [];

	if (format.to)
		urlParams.push([ 'to', format.to ]);

	if (format.from)
		urlParams.push([ 'from', format.from ]);

	if (format.syntax)
		urlParams.push([ 'syntax', format.syntax ]);

	if (format.includeComments)
		urlParams.push([ 'includeComments', format.includeComments ]);

	contactWebService(content, 'convert/' + type, urlParams, callback);
}

function contactWebService(postData, urlPath, urlParams, callback) {
	var baseUrl = config.webService.url;
	var timeout = config.webService.timeout;

	function buildUrl(baseUrl, urlPath, urlParams) {
		var url = baseUrl + urlPath;
		if (urlParams.length > 0) {

			url += "?";
			for (var i = 0; i < urlParams.length; i++) {

				if (i > 0)
					url += "&";

				url += urlParams[i][0] + "=" + urlParams[i][1];
			}
		}

		return url;
	}

	var url = buildUrl(baseUrl, urlPath, urlParams);
	doLog("url: " + url);

	httpRequest.post(url, [], postData, function(response) {
		switch (response.type) {

		case 'SUCCESS':
			callback(response.result);

			break;

		case 'ERROR':
			var errorMsg = response.msg;
			// throw errorMsg;
			doLog(errorMsg);

			break;
		}
	});
}

function prepareResults(results) {
	return results.replace(/\r/g, "");
}

function showRules(rules) {
	rules = prepareResults(rules);

	document.body.innerHTML += "RULES (# " + rules.split("\n\n#").length
			+ ")<br /><br />";

	showResults(rules);
}

function showOntology(ontology) {
	ontology = prepareResults(ontology);

	document.body.innerHTML += "ONTOLOGY<br /><br />";

	showResults(ontology);
}

function showResults(results) {
	document.body.innerHTML += results.replace(/</g, "&lt;").replace(/>/,
			"&gt;").split("\n").join("<br />")
			+ "<br /><br /><br />";
}