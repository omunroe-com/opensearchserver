/*
 * Copyright 2017-2018 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaeksoft.opensearchserver.services;

import com.jaeksoft.opensearchserver.model.TaskRecord;

import javax.ws.rs.NotSupportedException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface ProcessingService<T extends TaskRecord, S> {

	ProcessingService DEFAULT = new ProcessingService() {
	};

	default Class<T> getTaskRecordClass() {
		throw new NotSupportedException();
	}

	static NotSupportedException notSupportedException(String taskId) {
		return new NotSupportedException("Action not supported for this task: " + taskId);
	}

	/**
	 * Check if the task is running. The TasksExecutorService may decide to start the task.
	 *
	 * @param taskRecord
	 */
	default TaskRecord.Status checkIsRunning(final String schema, final TaskRecord taskRecord) throws Exception {
		throw notSupportedException(taskRecord.getTaskId());
	}

	default boolean isRunning(final String taskId) {
		throw notSupportedException(taskId);
	}

	default S getStatus(final String taskId) {
		throw notSupportedException(taskId);
	}

	default void abort(final String taskId) {
		throw notSupportedException(taskId);
	}

	static Builder of() {
		return new Builder();
	}

	class Builder {

		final Map<Class<? extends TaskRecord>, ProcessingService<?, ?>> processorMap;

		private Builder() {
			processorMap = new LinkedHashMap<>();
		}

		public Builder register(final ProcessingService<?, ?> tasksExecutorService) {
			processorMap.put(tasksExecutorService.getTaskRecordClass(), tasksExecutorService);
			return this;
		}

		public Map<Class<? extends TaskRecord>, ProcessingService> build() {
			return Collections.unmodifiableMap(processorMap);
		}
	}
}
