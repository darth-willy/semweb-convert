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

public class ConvertConfig {

	public boolean toString;
	public boolean includeComments;

	public ConvertConfig(boolean toString) {
		this.toString = false;
		
		includeComments = false;
	}
	
	public ConvertConfig(boolean toString, boolean includeComments) {
		this.toString = toString;
		this.includeComments = includeComments;
	}

	public boolean isToString() {
		return toString;
	}

	public void setToString(boolean toString) {
		this.toString = toString;
	}

	public boolean isIncludeComments() {
		return includeComments;
	}

	public void setIncludeComments(boolean includeComments) {
		this.includeComments = includeComments;
	}

}
