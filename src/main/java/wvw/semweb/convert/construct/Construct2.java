/**
 * Copyright 2016 William Van Woensel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 * 
 * 
 * @author wvw
 * 
 */
package wvw.semweb.convert.construct;

import java.util.List;

import org.topbraid.spin.model.Construct;

import wvw.semweb.convert.ConvertException;
import wvw.semweb.convert.RuleConverter;

public abstract class Construct2 extends RuleConverter {

	public Construct2(String id) {
		super(id);
	}

	public List<Object> convertRule(Object constructQuery) throws ConvertException {		
		return convert(genConstruct(constructQuery.toString()));
	}

	public abstract List<Object> convert(Construct query) throws ConvertException;
}
