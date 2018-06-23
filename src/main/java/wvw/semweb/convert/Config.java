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

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.PropertyConfigurator;

import wvw.semweb.convert.res.ServiceResources;

public class Config {

	public static ServletContext context;
	public static Properties properties;

	public static void init(ServiceResources res) {
		Config.context = res.getCtx();

		properties = new Properties();
		try {
			properties.load(res.getInputStream("config.properties"));
			PropertyConfigurator.configure(properties);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean initialized() {
		return properties != null;
	}

	public static String get(String propertyName) {
		return properties.getProperty(propertyName);
	}
}
