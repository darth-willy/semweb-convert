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
package wvw.semweb.convert.construct.jena;

import org.topbraid.spin.model.Filter;
import org.topbraid.spin.model.TriplePattern;

import wvw.semweb.convert.ConvertException;
import wvw.semweb.convert.construct.Construct2;
import wvw.semweb.convert.construct.Construct2Visitor;
import wvw.utils.RDFUtils;

public class Construct2JenaVisitor extends Construct2Visitor {

	private String leftPart = "";

	public Construct2JenaVisitor(Construct2 converter) {
		super(converter);
	}

	public void visit(Filter arg0) {
		Construct2JenaFilterVisitor filterVisit = new Construct2JenaFilterVisitor();
		
		try {
			leftPart += "\n" + filterVisit.visit(arg0.getExpression());

		} catch (Exception e) {
			this.exc = new ConvertException("Error generating rule head", e);
		}
	}

	public void visit(TriplePattern arg0) {
		try {
			leftPart += "\n(" + RDFUtils.toString(arg0) + ")";

		} catch (Exception e) {
			this.exc = new ConvertException("Error generating rule head", e);
		}
	}

	public String getLeftPart() throws ConvertException {
		if (exc != null)
			throw (ConvertException) exc;

		return leftPart;
	}
}
