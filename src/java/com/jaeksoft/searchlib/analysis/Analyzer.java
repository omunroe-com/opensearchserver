/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of Jaeksoft OpenSearchServer.
 *
 * Jaeksoft OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.TokenStream;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;

public class Analyzer extends org.apache.lucene.analysis.Analyzer {

	private TokenizerFactory tokenizer;
	private List<FilterFactory> filters;
	private String name;
	private String lang;

	public Analyzer(String name, String lang, String tokenizerFactoryClassName)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		if (tokenizerFactoryClassName == null)
			tokenizerFactoryClassName = "StandardTokenizer";
		this.tokenizer = (TokenizerFactory) Class.forName(
				"com.jaeksoft.searchlib.analysis." + tokenizerFactoryClassName)
				.newInstance();
		this.filters = new ArrayList<FilterFactory>();
		this.name = name;
		this.lang = lang;
	}

	public void add(String filterFactoryClassName, XPathParser xpp, Node node)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, IOException {
		FilterFactory filter = (FilterFactory) Class.forName(
				"com.jaeksoft.searchlib.analysis." + filterFactoryClassName)
				.newInstance();
		filter.setParams(xpp, node);
		this.filters.add(filter);
	}

	@Override
	public TokenStream tokenStream(String fieldname, Reader reader) {
		TokenStream ts = tokenizer.create(reader);
		for (FilterFactory filter : this.filters)
			ts = filter.create(ts);
		return ts;
	}

	public String getName() {
		return name;
	}

	public String getLang() {
		return lang;
	}

	public String getTokenizer() {
		return tokenizer.getClass().getSimpleName();
	}

	public List<FilterFactory> getFilters() {
		return filters;
	}

	/**
	 * 
	 * @param xpp
	 * @param parentNode
	 * @param attr
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws DOMException
	 */
	public static Analyzer fromXmlConfig(XPathParser xpp, Node node)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, XPathExpressionException, DOMException,
			IOException {
		if (node == null)
			return null;
		Analyzer analyzer = new Analyzer(XPathParser.getAttributeString(node,
				"name"), XPathParser.getAttributeString(node, "lang"),
				XPathParser.getAttributeString(node, "tokenizer"));
		NodeList nodes = xpp.getNodeList(node, "filter");
		for (int i = 0; i < nodes.getLength(); i++)
			analyzer.add(
					XPathParser.getAttributeString(nodes.item(i), "class"),
					xpp, nodes.item(i));
		return analyzer;
	}

	public void writeXmlConfig(XmlWriter writer) throws SAXException {
		writer.startElement("analyzer", "name", getName(), "tokenizer",
				tokenizer != null ? tokenizer.getClassName() : null, "lang",
				lang != null ? lang : null);
		if (filters != null && filters.size() > 0)
			for (FilterFactory filter : filters)
				filter.writeXmlConfig(writer);
		writer.endElement();
	}
}
