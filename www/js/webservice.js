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

// - rule conversion

// read rules from file
var rules = read("res/owl/owl2rl/full/rules.sparql");

// convert rules via web service
// (options: Datalog, Jena, Nools)

convert(rules, 'rules', {
	to : 'Datalog' // convert to this format

}, function(rules) {
	showRules(rules);
});


// - data conversion

// // read data from file
// var data = read("res/owl/ontology/example.nt");
//
// // convert data via web service
// // (options: Datalog, Nools)
// 
// convert(data, 'data', {
//	 syntax : 'N-TRIPLE', // (syntax of incoming data)
//	 to : 'Datalog' // convert to this format
//
// }, function(data) {
//	 showOntology(data);
// });
