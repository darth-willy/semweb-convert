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
package wvw.semweb.convert.servlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import wvw.semweb.convert.Config;
import wvw.semweb.convert.ConvertConfig;
import wvw.semweb.convert.ConvertException;
import wvw.semweb.convert.Converter;
import wvw.semweb.convert.DataConverter;
import wvw.semweb.convert.RuleConverter;
import wvw.semweb.convert.res.ServiceResources;
import wvw.semweb.convert.servlet.msg.ErrorMessage;
import wvw.semweb.convert.servlet.msg.StringResultMessage;
import wvw.utils.OutputUtils;
import wvw.utils.StreamUtils;

public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private Gson gson;
	private GsonBuilder gsonBuilder;

	private Map<String, RuleConverter> spin2s = new HashMap<String, RuleConverter>();
	private Map<String, DataConverter> rdf2s = new HashMap<String, DataConverter>();

	private Logger log;

	private ServiceResources res;

	public MainServlet() {
		super();
	}

	public void init() throws ServletException {
		try {
			initResources();

			initLogger();
			initGson();

			Config.init(res);

			loadConverters("construct2s.txt", spin2s);
			loadConverters("rdf2s.txt", rdf2s);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initResources() {
		this.res = new ServiceResources(getServletContext());
	}

	private void initLogger() throws IOException {
		log = Logger.getLogger(MainServlet.class.getName());

		Properties properties = new Properties();
		try {
			properties.load(res.getInputStream(("log4j.properties")));
			PropertyConfigurator.configure(properties);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initGson() {
		gsonBuilder = new GsonBuilder();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadConverters(String fileName, Map convs) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(res.getInputStream(fileName)));

			String line = null;
			while ((line = reader.readLine()) != null) {
				String className = line.trim();

				try {
					Class<?> clazz = Class.forName(className);
					Constructor<?> constr = clazz.getConstructor();

					Converter conv = (Converter) constr.newInstance();
					conv.setRes(res);

					convs.put(conv.getId(), conv);

				} catch (ClassNotFoundException e) {
					log.error("Error loading converter: converter class " + className + " not found on classpath ("
							+ OutputUtils.toString(e) + ")");

					e.printStackTrace();

				} catch (NoSuchMethodException e) {
					log.error("Error loading converter: no empty constructor " + "found for " + className + " ("
							+ OutputUtils.toString(e) + ")");

					e.printStackTrace();

				} catch (InvocationTargetException e) {
					log.error("Error loading converter for class " + className + ": " + OutputUtils.toString(e));

					e.printStackTrace();

				} catch (IllegalAccessException e) {
					log.error("Error loading converter for class " + className + ": " + OutputUtils.toString(e));

					e.printStackTrace();

				} catch (InstantiationException e) {
					log.error("Error loading converter for class " + className + ": " + OutputUtils.toString(e));

					e.printStackTrace();
				}
			}

			reader.close();

		} catch (IOException e) {
			log.error("Error loading converters: " + OutputUtils.toString(e));

			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.getWriter().write("<html>" + "<body>"
					+ "<h1>Welcome to the SemWebConvert RESTful Web Service!</h1>" + "</body>" + "</html>");

		} catch (IOException e) {
			log.error("Error handling incoming GET: " + OutputUtils.toString(e));

			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log.info("> Received request");

		gson = gsonBuilder.create();

		String url = request.getRequestURL().toString();
		// log.info("url: " + url);

		if (url.matches(".*?convert/.*?"))
			doConversion(url, request, response);

		else {
			String errorMsg = "Expected 'convert/(rules|data)' in URL";
			log.error(errorMsg);

			response.getWriter().write(gson.toJson(new ErrorMessage(errorMsg)));
		}
	}

	private void doConversion(String url, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (url.endsWith("rules"))
			doRuleConversion(request, response);

		else if (url.endsWith("data"))
			doDataConversion(request, response);
	}

	private void doRuleConversion(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log.info(">> Performing rule conversion");

		String errorMsg = null;
		RuleConverter conv = null;

		// NOTE "from" parameter (converting *to* SPIN is future work)
		String from = request.getParameter("from");
		String to = request.getParameter("to");

		boolean includeComments = Boolean.valueOf(request.getParameter("includeComments"));

		if (from == null && to == null)
			errorMsg = "Error converting rules: expected 'from' or 'to' URL parameter";

		else if (from != null) {
			errorMsg = "Error converting rules: converter for '" + from + "' not registered";

		} else if (to != null) {
			conv = spin2s.get(to);

			if (conv == null)
				errorMsg = "Error converting rules: converter for '" + to + "' not registered";
		}

		if (conv != null)
			try {
				String rules = readString(request);
				// Log.d("rules? " + rules);

				String result = (String) conv.convertRules(rules, new ConvertConfig(true, includeComments));

				response.getWriter().write(gson.toJson(new StringResultMessage(result)));

				log.info("> Conversion successful, returned result");

			} catch (IOException | ConvertException e) {
				errorMsg = "Error converting rules: " + OutputUtils.toString(e);

				e.printStackTrace();
			}

		if (errorMsg != null) {
			response.getWriter().write(gson.toJson(new ErrorMessage(errorMsg)));

			log.error(errorMsg);
		}
	}

	private void doDataConversion(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log.info(">> Performing data conversion");

		String errorMsg = null;
		DataConverter conv = null;

		String to = request.getParameter("to");

		if (to == null)
			errorMsg = "Error converting data: expected 'to' URL parameter";

		else if (to != null) {
			conv = rdf2s.get(to);

			if (conv == null)
				errorMsg = "Error converting data: converter for '" + to + "' not registered";
		}

		String syntax = request.getParameter("syntax");
		if (syntax == null)
			syntax = defRDFSyntax;
		else
			syntax = syntax.replace("_", "/"); // for RDF/XML

		if (!isSupportedRDFSyntax(syntax))
			errorMsg = "Error converting data: unsupported RDF syntax (supported: " + getSupportedRDFSyntaxes() + ")";

		if (errorMsg == null) {
			try {
				String data = readString(request);

				String result = conv.convert(data, syntax);
				// System.out.println("data result:\n" + result);

				response.getWriter().write(gson.toJson(new StringResultMessage(result)));

				log.info(">> Conversion successful, returned result");

			} catch (IOException | ConvertException e) {
				errorMsg = "Error converting data: " + OutputUtils.toString(e);

				e.printStackTrace();
			}
		}

		if (errorMsg != null) {
			response.getWriter().write(gson.toJson(new ErrorMessage(errorMsg)));

			log.error(errorMsg);
		}
	}

	private Reader getReader(HttpServletRequest request) throws IOException {
		String url = request.getParameter("url");
		String path = request.getParameter("path");

		if (url != null)
			return new InputStreamReader(new URL(url).openStream());

		else if (path != null)
			return new FileReader(path);

		else
			return request.getReader();
	}

	private String readString(HttpServletRequest request) throws IOException {
		return StreamUtils.readString(getReader(request));
	}

	// source:
	// https://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/rdf/model/Model.html
	private static String defRDFSyntax = "RDF/XML";

	private static Map<String, Boolean> rdfSyntaxes = new HashMap<String, Boolean>();

	static {
		rdfSyntaxes.put("RDF/XML", true);
		rdfSyntaxes.put("N-TRIPLE", true);
		rdfSyntaxes.put("TURTLE", true);
		rdfSyntaxes.put("TTL", true);
		rdfSyntaxes.put("N3", true);
		rdfSyntaxes.put("RDF/XML", true);
		rdfSyntaxes.put("RDF/XML-ABBREV", true);
	}

	private boolean isSupportedRDFSyntax(String syntax) {
		return rdfSyntaxes.containsKey(syntax);
	}

	private String getSupportedRDFSyntaxes() {
		return OutputUtils.keysToString(rdfSyntaxes);
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}
}
