/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2014 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Tab;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.Logging;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.file.database.FilePathItem;
import com.jaeksoft.searchlib.request.AbstractRequest;
import com.jaeksoft.searchlib.result.AbstractResult;
import com.jaeksoft.searchlib.scheduler.JobItem;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.StartStopListener;
import com.jaeksoft.searchlib.web.Version;
import com.jaeksoft.searchlib.web.servlet.restv1.AbstractServlet;

public abstract class CommonController implements EventInterface,
		EventListener<Event> {

	protected transient Component component;

	public CommonController() throws SearchLibException {
		super();
		reset();
	}

	@AfterCompose
	public void afterCompose(
			@ContextParam(ContextType.COMPONENT) Component component,
			@ContextParam(ContextType.VIEW) Component view) {
		this.component = component;
		EventQueues.lookup(PushEvent.QUEUE_NAME, EventQueues.DESKTOP, true)
				.subscribe(this);
		EventQueues.lookup(PushEvent.QUEUE_NAME, EventQueues.APPLICATION, true)
				.subscribe(this);
		EventQueues.lookup(PushEvent.QUEUE_NAME, EventQueues.SESSION, true)
				.subscribe(this);
	}

	@Override
	public void onEvent(Event event) throws Exception {
		Object data = event.getData();
		if (Logging.isDebug)
			Logging.debug(this + " -> " + "Event: " + event.getName()
					+ " data: " + data);
		switch (PushEvent.valueOf(event.getName())) {
		case eventClientChange:
			eventClientChange();
			break;
		case eventClientSwitch:
			eventClientSwitch((Client) data);
			break;
		case eventDocumentUpdate:
			eventDocumentUpdate((Client) data);
			break;
		case eventEditFileRepository:
			eventEditFileRepository((FilePathItem) data);
			break;
		case eventEditRequest:
			eventEditRequest((AbstractRequest) data);
			break;
		case eventEditRequestResult:
			eventEditRequestResult((AbstractResult<?>) data);
			break;
		case eventEditScheduler:
			eventEditScheduler((JobItem) data);
			break;
		case eventFlushPrivileges:
			eventFlushPrivileges((User) data);
			break;
		case eventLogout:
			eventLogout((User) data);
			break;
		case eventRequestListChange:
			eventRequestListChange((Client) data);
			break;
		case eventSchemaChange:
			eventSchemaChange((Client) data);
			break;
		}
	}

	final protected String getExecutionParameter(String name) {
		return Executions.getCurrent().getParameter(name);
	}

	final protected Desktop getDesktop() {
		return Executions.getCurrent().getDesktop();
	}

	final protected Session getSession() {
		return Executions.getCurrent().getSession();
	}

	final protected static StringBuilder getBaseUrl(Execution exe) {
		int port = exe.getServerPort();
		StringBuilder sb = new StringBuilder();
		sb.append(exe.getScheme());
		sb.append("://");
		sb.append(exe.getServerName());
		if (port != 80) {
			sb.append(":");
			sb.append(port);
		}
		sb.append(exe.getContextPath());
		return sb;

	}

	final public static StringBuilder getBaseUrl() {
		Execution exe = Executions.getCurrent();
		return getBaseUrl(exe);
	}

	final public static StringBuilder getApiUrl(String servletPathName)
			throws UnsupportedEncodingException {
		Execution exe = Executions.getCurrent();
		StringBuilder sb = getBaseUrl();
		Client client = (Client) exe.getSession().getAttribute(
				ScopeAttribute.CURRENT_CLIENT.name());
		User user = (User) exe.getSession().getAttribute(
				ScopeAttribute.LOGGED_USER.name());
		return AbstractServlet.getApiUrl(sb, servletPathName, client, user);
	}

	final public static String getRestApiUrl(String path)
			throws UnsupportedEncodingException {
		Execution exe = Executions.getCurrent();
		StringBuilder sb = getBaseUrl();
		Client client = (Client) exe.getSession().getAttribute(
				ScopeAttribute.CURRENT_CLIENT.name());
		sb.append("/services/rest");
		sb.append(StringUtils.replace(path, "{index_name}",
				URLEncoder.encode(client.getIndexName(), "UTF-8")));
		sb.append("?_type=json");
		User user = (User) exe.getSession().getAttribute(
				ScopeAttribute.LOGGED_USER.name());
		if (user != null)
			user.appendApiCallParameters(sb);
		return sb.toString();
	}

	protected Object getAttribute(ScopeAttribute scopeAttribute,
			Object defaultValue) {
		Object o = scopeAttribute.get(getSession());
		return o == null ? defaultValue : o;
	}

	protected Object getAttribute(ScopeAttribute scopeAttribute) {
		return scopeAttribute.get(getSession());
	}

	protected void setAttribute(ScopeAttribute scopeAttribute, Object value) {
		scopeAttribute.set(getSession(), value);
	}

	public Version getVersion() throws IOException {
		return StartStopListener.getVersion();
	}

	public Client getClient() throws SearchLibException {
		Client client = (Client) getAttribute(ScopeAttribute.CURRENT_CLIENT);
		if (client == null)
			return null;
		if (client.isClosed()) {
			client = ClientCatalog.getClient(client.getIndexName());
			setClient(client);
		}
		return client;
	}

	protected void setClient(Client client) {
		setAttribute(ScopeAttribute.CURRENT_CLIENT, client);
		PushEvent.eventClientChange.publish();
	}

	public List<String> getIndexedFieldList() throws SearchLibException,
			IOException {
		List<String> fields = new ArrayList<String>(0);
		Client client = getClient();
		if (client == null)
			return fields;
		client.getSchema().getFieldList().getIndexedFields(fields);
		return fields;
	}

	public boolean isInstanceValid() throws SearchLibException {
		return getClient() != null;
	}

	public boolean isInstanceNotValid() throws SearchLibException {
		return getClient() == null;
	}

	public User getLoggedUser() {
		return (User) getAttribute(ScopeAttribute.LOGGED_USER);
	}

	public boolean isAdmin() throws SearchLibException {
		User user = getLoggedUser();
		if (user == null)
			return false;
		return user.isAdmin();
	}

	public String getRequestParameter(String name) {
		return Executions.getCurrent().getParameter(name);
	}

	public boolean isNoUserList() throws SearchLibException {
		return ClientCatalog.getUserList().isEmpty();
	}

	public boolean isAdminOrNoUser() throws SearchLibException {
		if (isNoUserList())
			return true;
		return isAdmin();
	}

	public boolean isAdminOrMonitoringOrNoUser() throws SearchLibException {
		if (isNoUserList())
			return true;
		User user = getLoggedUser();
		if (user == null)
			return false;
		return user.isAdmin() || user.isMonitoring();
	}

	public boolean isLogged() throws SearchLibException {
		if (isNoUserList())
			return true;
		return getLoggedUser() != null;
	}

	final protected void resize() {
		if (component != null)
			Clients.resize(component);
	}

	@Command
	@GlobalCommand
	public void reload() throws SearchLibException {
		BindUtils.postNotifyChange(null, null, this, "*");
		resize();
		if (Logging.isDebug)
			Logging.debug("reload " + this + " " + component);
	}

	@Command
	@GlobalCommand
	public void refresh() throws SearchLibException {
		reset();
		reload();
	}

	public LanguageEnum[] getLanguageEnum() {
		return LanguageEnum.values();
	}

	public List<String> getAnalyzerNameList() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		List<String> analyzerNameList = new ArrayList<String>(0);
		analyzerNameList.add("");
		client.getSchema().getAnalyzerList()
				.populateNameCollection(analyzerNameList);
		return analyzerNameList;
	}

	protected void flushPrivileges(User user) {
		PushEvent.eventFlushPrivileges.publish(user);
	}

	@Command
	public void onLogout() {
		for (ScopeAttribute attr : ScopeAttribute.values())
			setAttribute(attr, null);
		PushEvent.eventLogout.publish();
		Executions.sendRedirect("/");
	}

	protected abstract void reset() throws SearchLibException;

	@Override
	public void eventClientChange() throws SearchLibException {
		Logging.debug("eventClientChange " + this);
		refresh();
	}

	@Override
	public void eventEditRequest(AbstractRequest request)
			throws SearchLibException {
		Logging.debug("eventEditRequest " + this);
	}

	@Override
	public void eventEditScheduler(JobItem jobItem) throws SearchLibException {
		Logging.debug("eventEditScheduler " + this);
	}

	@Override
	public void eventEditFileRepository(FilePathItem filePathItem)
			throws SearchLibException {
		Logging.debug("eventEditFileRepository " + this);
	}

	@Override
	public void eventEditRequestResult(AbstractResult<?> result)
			throws SearchLibException {
		Logging.debug("eventEditRequestResult " + this);
	}

	@Override
	public void eventClientSwitch(Client client) throws SearchLibException {
		if (client == null)
			return;
		Client currentClient = getClient();
		if (currentClient == null)
			return;
		String indexName = client.getIndexName();
		if (!indexName.equals(currentClient.getIndexName()))
			return;
		Client newClient = ClientCatalog.getClient(indexName);
		setClient(newClient);
		refresh();
	}

	@Override
	public void eventFlushPrivileges(User user) throws SearchLibException {
		Logging.debug("eventFlushPrivileges " + this);
		refresh();
	}

	@Override
	public void eventDocumentUpdate(Client client) throws SearchLibException {
		Logging.debug("eventDocumentUpdate " + this);
	}

	@Override
	public void eventRequestListChange(Client client) throws SearchLibException {
		Logging.debug("eventRequestListChange " + this);
	}

	@Override
	public void eventSchemaChange(Client client) throws SearchLibException {
		Logging.debug("eventSchemaChange " + this);
	}

	@Override
	public void eventLogout(User user) throws SearchLibException {
		Logging.debug("eventLogout " + this);
		refresh();
	}

	protected String getIndexName() throws SearchLibException {
		Client client = getClient();
		if (client == null)
			return null;
		return getClient().getIndexName();
	}

	public boolean isQueryRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(), Role.GROUP_INDEX);
	}

	public boolean isUpdateRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(), Role.INDEX_UPDATE);
	}

	public boolean isSchemaRights() throws SearchLibException {
		if (!isLogged() || !isInstanceValid())
			return false;
		if (isNoUserList())
			return true;
		return getLoggedUser().hasAnyRole(getIndexName(), Role.INDEX_SCHEMA);
	}

	protected final static void buildTabPath(Component component,
			List<String> tabPath) throws SearchLibException {
		if (component == null)
			return;
		if (!component.isVisible())
			return;
		if (component instanceof Tab) {
			Tab tab = (Tab) component;
			if (tab.isSelected()) {
				String lbl = tab.getTooltiptext();
				if (lbl == null || lbl.length() == 0)
					lbl = tab.getLabel();
				tabPath.add(lbl);
			}
		}
		List<Component> children = component.getChildren();
		if (children == null)
			return;
		for (Component comp : children)
			buildTabPath(comp, tabPath);
	}

	@Command
	final public void onHelp(@BindingParam("target") Component component)
			throws SearchLibException, UnsupportedEncodingException {
		List<String> tabPath = new ArrayList<String>();
		buildTabPath(component.getRoot(), tabPath);
		String path = URLEncoder.encode(StringUtils.join(tabPath, " - "),
				"UTF-8");
		Executions.getCurrent().sendRedirect(
				"http://www.open-search-server.com/confluence/display/EN/Inline+help+-+"
						+ path, "_blank");
	}

}
