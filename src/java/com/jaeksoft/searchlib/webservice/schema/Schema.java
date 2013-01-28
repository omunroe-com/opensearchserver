/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.webservice.schema;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.webservice.CommonResult;

@WebService
public interface Schema {
	@WebResult(name = "createIndex")
	public CommonResult createIndex(@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "indexName") String indexName,
			@WebParam(name = "indexTemplateName") TemplateList indexTemplateName);

	@WebResult(name = "deleteIndex")
	public CommonResult deleteIndex(@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "indexName") String indexName);

	@WebResult(name = "indexList")
	public List<String> indexList(@WebParam(name = "login") String login,
			@WebParam(name = "key") String key);

	@WebResult(name = "setField")
	public CommonResult setField(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "schemaField") SchemaFieldRecord schemaFieldRecord);

	@WebResult(name = "deletefield")
	public CommonResult deletefield(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "deleteField") String deleteField);

	@WebResult(name = "defaultField")
	public CommonResult setDefaultField(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "defaultField") String defaultField);

	@WebResult(name = "uniqueField")
	public CommonResult setUniqueField(@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key,
			@WebParam(name = "uniqueField") String uniqueField);

	@WebResult(name = "fieldList")
	public List<SchemaFieldRecord> getFieldList(
			@WebParam(name = "use") String use,
			@WebParam(name = "login") String login,
			@WebParam(name = "key") String key);
}