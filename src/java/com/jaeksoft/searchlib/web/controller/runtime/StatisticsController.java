/**   
 * License Agreement for Jaeksoft OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

package com.jaeksoft.searchlib.web.controller.runtime;

import java.util.List;

import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.statistics.StatisticTypeEnum;
import com.jaeksoft.searchlib.statistics.StatisticsAbstract;
import com.jaeksoft.searchlib.web.controller.CommonController;

public class StatisticsController extends CommonController {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2420054165684926096L;

	private StatisticsAbstract selectedStat;

	private StatisticTypeEnum selectedType;

	private List<StatisticsAbstract> statList;

	public StatisticsController() throws SearchLibException {
		super();
		this.selectedType = StatisticTypeEnum.SEARCH;
		selectedStat = null;
		statList = null;
	}

	public boolean isStatList() throws SearchLibException {
		synchronized (this) {
			return getStatList() != null;
		}
	}

	public List<StatisticsAbstract> getStatList() throws SearchLibException {
		synchronized (this) {
			if (statList != null)
				return statList;
			statList = getClient().getStatisticsList()
					.getStatList(selectedType);
			if (statList == null)
				return null;
			if (selectedStat == null && statList.size() > 0)
				selectedStat = statList.get(0);
			return statList;
		}
	}

	public StatisticsAbstract getSelectedStat() {
		synchronized (this) {
			return selectedStat;
		}
	}

	public void setSelectedStat(StatisticsAbstract selectedStat) {
		synchronized (this) {
			this.selectedStat = selectedStat;
			statList = null;
		}
	}

	public StatisticTypeEnum getSelectedType() {
		synchronized (this) {
			return selectedType;
		}
	}

	public void setSelectedType(StatisticTypeEnum selectedType) {
		synchronized (this) {
			this.selectedType = selectedType;
			reloadPage();
		}
	}

	public StatisticTypeEnum[] getTypeList() {
		synchronized (this) {
			return StatisticTypeEnum.values();
		}
	}

	@Override
	public void reloadPage() {
		synchronized (this) {
			selectedStat = null;
			statList = null;
			super.reloadPage();
		}
	}

}
