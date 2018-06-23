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
package wvw.semweb.convert.construct.datalog;

import java.util.ArrayList;
import java.util.List;

import org.topbraid.spin.model.Filter;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.TripleTemplate;

import wvw.semweb.convert.ConvertException;
import wvw.semweb.convert.construct.Construct2;
import wvw.semweb.convert.construct.Construct2Visitor;
import wvw.semweb.convert.datalog.DatalogUtils;

public class Construct2DatalogVisitor extends Construct2Visitor {

	private List<String> rules = new ArrayList<String>();

	private StringBuilder rightPart = new StringBuilder();

	public Construct2DatalogVisitor(Construct2 converter) {
		super(converter);
	}

	public void visit(Filter arg0) {
		Construct2DatalogFilterVisitor filterVisit = new Construct2DatalogFilterVisitor();

		try {
			String filterStr = filterVisit.visit(arg0.getExpression());
			if (filterStr != null)
				append(rightPart, filterStr);

		} catch (Exception e) {
			this.exc = new ConvertException("Error generating rule head", e);
		}
	}

	public void visit(TriplePattern arg0) {		
		try {
			append(rightPart, DatalogUtils.toString(arg0));

		} catch (Exception e) {
			this.exc = new ConvertException("Error generating rule head", e);
		}
	}

	public void visit(List<TripleTemplate> templates) {
		for (TripleTemplate template : templates) {
			String left = visit(template);
			String right = rightPart.toString();

			rules.add(left + ":-" + right + ".");
		}
	}

	private String visit(TripleTemplate template) {
		return DatalogUtils.toString(template);
	}

	private void append(StringBuilder part, String str) {
		if (part.length() > 0)
			part.append(",");

		part.append(str);
	}

	private void checkExc() throws ConvertException {
		if (exc != null)
			throw (ConvertException) exc;
	}

	public List<String> getRules() throws ConvertException {
		checkExc();

		return rules;
	}
}