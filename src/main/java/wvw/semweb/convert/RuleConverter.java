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
package wvw.semweb.convert;

import java.util.ArrayList;
import java.util.List;

import org.topbraid.spin.model.Construct;

import wvw.utils.rule.RuleWrapper;
import wvw.utils.rule.RulesUtils;

public abstract class RuleConverter extends Converter {

	public RuleConverter(String id) {
		super(id);
	}

	public Object convertRules(String rulesStr) throws ConvertException {
		return convertRules(rulesStr, new ConvertConfig(false, false));
	}

	@SuppressWarnings("unchecked")
	public Object convertRules(String rulesStr, ConvertConfig config) throws ConvertException {

		Object results = null;
		if (config.isToString())
			results = new StringBuffer();
		else
			results = new ArrayList<Object>();

		List<RuleWrapper> rules = RulesUtils.splitRules(rulesStr);
		for (int i = 0; i < rules.size(); i++) {
			RuleWrapper rule = rules.get(i);

			List<Object> convRules = convertRule(rule.getRule());
			if (config.isToString()) {
				String result = RulesUtils.mergeRules(rule, convRules, config.isIncludeComments());

				((StringBuffer) results).append(result);

			} else
				((List<Object>) results).addAll(convRules);
		}

		reset();

		if (config.isToString())
			results = results.toString();

		return postProcessRules(results);
	}

	public List<Object> convertRules(List<Object> rules) throws ConvertException {
		List<Object> ret = new ArrayList<Object>();
		for (Object rule : rules)
			ret.addAll(convertRule(rule));

		return ret;
	}

	public abstract List<Object> convertRule(Object rule) throws ConvertException;

	protected Construct genConstruct(String constructQuery) throws ConvertException {

		return RulesUtils.genConstruct(constructQuery);
	}

	protected Object postProcessRules(Object rules) {
		return rules;
	}

	// this method allows resetting internal state after a conversion request
	// (does not need to be implemented)
	public void reset() {
	}
}
