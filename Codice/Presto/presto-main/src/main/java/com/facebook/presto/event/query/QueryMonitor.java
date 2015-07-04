/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.event.query;

import com.facebook.presto.client.FailureInfo;
import com.facebook.presto.execution.QueryInfo;
import com.facebook.presto.execution.QueryStats;
import com.facebook.presto.execution.StageInfo;
import com.facebook.presto.execution.TaskId;
import com.facebook.presto.execution.TaskInfo;
import com.facebook.presto.operator.DriverStats;
import com.facebook.presto.operator.TaskStats;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import io.airlift.event.client.EventClient;
import io.airlift.log.Logger;
import io.airlift.node.NodeInfo;
import io.airlift.units.Duration;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class QueryMonitor
{
    private static final Logger log = Logger.get(QueryMonitor.class);

    private final ObjectMapper objectMapper;
    private final EventClient eventClient;
    private final String environment;

    @Inject
    public QueryMonitor(ObjectMapper objectMapper, EventClient eventClient, NodeInfo nodeInfo)
    {
        this.objectMapper = checkNotNull(objectMapper, "objectMapper is null");
        this.eventClient = checkNotNull(eventClient, "eventClient is null");
        this.environment = checkNotNull(nodeInfo, "nodeInfo is null").getEnvironment();
    }

    public void createdEvent(QueryInfo queryInfo)
    {
        eventClient.post(
                new QueryCreatedEvent(
                        queryInfo.getQueryId(),
                        queryInfo.getSession().getUser(),
                        queryInfo.getSession().getSource(),
                        environment,
                        queryInfo.getSession().getCatalog(),
                        queryInfo.getSession().getSchema(),
                        queryInfo.getSession().getRemoteUserAddress(),
                        queryInfo.getSession().getUserAgent(),
                        queryInfo.getSelf(),
                        queryInfo.getQuery(),
                        queryInfo.getQueryStats().getCreateTime()
                )
        );
    }

    public void completionEvent(QueryInfo queryInfo)
    {
        try {
            QueryStats queryStats = queryInfo.getQueryStats();
            FailureInfo failureInfo = queryInfo.getFailureInfo();

            String failureType = failureInfo == null ? null : failureInfo.getType();
            String failureMessage = failureInfo == null ? null : failureInfo.getMessage();

            eventClient.post(
                    new QueryCompletionEvent(
                            queryInfo.getQueryId(),
                            queryInfo.getSession().getUser(),
                            queryInfo.getSession().getSource(),
                            environment,
                            queryInfo.getSession().getCatalog(),
                            queryInfo.getSession().getSchema(),
                            queryInfo.getSession().getRemoteUserAddress(),
                            queryInfo.getSession().getUserAgent(),
                            queryInfo.getState(),
                            queryInfo.getSelf(),
                            queryInfo.getFieldNames(),
                            queryInfo.getQuery(),
                            queryStats.getCreateTime(),
                            queryStats.getExecutionStartTime(),
                            queryStats.getEndTime(),
                            queryStats.getQueuedTime(),
                            queryStats.getAnalysisTime(),
                            queryStats.getDistributedPlanningTime(),
                            queryStats.getTotalScheduledTime(),
                            queryStats.getTotalCpuTime(),
                            queryStats.getRawInputDataSize(),
                            queryStats.getRawInputPositions(),
                            queryStats.getTotalDrivers(),
                            queryInfo.getErrorCode(),
                            failureType,
                            failureMessage,
                            objectMapper.writeValueAsString(queryInfo.getOutputStage()),
                            objectMapper.writeValueAsString(queryInfo.getFailureInfo()),
                            objectMapper.writeValueAsString(queryInfo.getInputs())
                    )
            );

            logQueryTimeline(queryInfo);
        }
        catch (JsonProcessingException e) {
            throw Throwables.propagate(e);
        }
    }

    private void logQueryTimeline(QueryInfo queryInfo)
    {
        try {
            QueryStats queryStats = queryInfo.getQueryStats();
            DateTime queryStartTime = queryStats.getCreateTime();
            DateTime queryEndTime = queryStats.getEndTime();

            // query didn't finish cleanly
            if (queryStartTime == null || queryEndTime == null) {
                return;
            }

            // planning duration -- start to end of planning
            Duration planning = queryStats.getTotalPlanningTime();
            if (planning == null) {
                planning = new Duration(0, MILLISECONDS);
            }

            List<StageInfo> stages = StageInfo.getAllStages(queryInfo.getOutputStage());
            // long lastSchedulingCompletion = 0;
            long firstTaskStartTime = queryEndTime.getMillis();
            long lastTaskStartTime = queryStartTime.getMillis() + planning.toMillis();
            long lastTaskEndTime = queryStartTime.getMillis() + planning.toMillis();
            for (StageInfo stage : stages) {
                // only consider leaf stages
                if (!stage.getSubStages().isEmpty()) {
                    continue;
                }

                for (TaskInfo taskInfo : stage.getTasks()) {
                    TaskStats taskStats = taskInfo.getStats();

                    DateTime firstStartTime = taskStats.getFirstStartTime();
                    if (firstStartTime != null) {
                        firstTaskStartTime = Math.min(firstStartTime.getMillis(), firstTaskStartTime);
                    }

                    DateTime lastStartTime = taskStats.getLastStartTime();
                    if (lastStartTime != null) {
                        lastTaskStartTime = Math.max(lastStartTime.getMillis(), lastTaskStartTime);
                    }

                    DateTime endTime = taskStats.getEndTime();
                    if (endTime != null) {
                        lastTaskEndTime = Math.max(endTime.getMillis(), lastTaskEndTime);
                    }
                }
            }

            Duration elapsed = millis(queryEndTime.getMillis() - queryStartTime.getMillis());

            Duration scheduling = millis(firstTaskStartTime - queryStartTime.getMillis() - planning.toMillis());

            Duration running = millis(lastTaskEndTime - firstTaskStartTime);

            Duration finishing = millis(queryEndTime.getMillis() - lastTaskEndTime);

            log.info("TIMELINE: Query %s :: elapsed %s :: planning %s :: scheduling %s :: running %s :: finishing %s :: begin %s :: end %s",
                    queryInfo.getQueryId(),
                    elapsed,
                    planning,
                    scheduling,
                    running,
                    finishing,
                    queryStartTime,
                    queryEndTime
            );
        }
        catch (Exception e) {
            log.error(e, "Error logging query timeline");
        }
    }

    public void splitCompletionEvent(TaskId taskId, DriverStats driverStats)
    {
        splitCompletionEvent(taskId, driverStats, null, null);
    }

    public void splitFailedEvent(TaskId taskId, DriverStats driverStats, Throwable cause)
    {
        splitCompletionEvent(taskId, driverStats, cause.getClass().getName(), cause.getMessage());
    }

    private void splitCompletionEvent(TaskId taskId, DriverStats driverStats, @Nullable String failureType, @Nullable String failureMessage)
    {
        Duration timeToStart = null;
        if (driverStats.getStartTime() != null) {
            timeToStart = millis(driverStats.getStartTime().getMillis() - driverStats.getCreateTime().getMillis());
        }
        Duration timeToEnd = null;
        if (driverStats.getEndTime() != null) {
            timeToEnd = millis(driverStats.getEndTime().getMillis() - driverStats.getCreateTime().getMillis());
        }

        try {
            eventClient.post(
                    new SplitCompletionEvent(
                            taskId.getQueryId(),
                            taskId.getStageId(),
                            taskId,
                            environment,
                            driverStats.getQueuedTime(),
                            driverStats.getStartTime(),
                            timeToStart,
                            timeToEnd,
                            driverStats.getRawInputDataSize(),
                            driverStats.getRawInputPositions(),
                            driverStats.getRawInputReadTime(),
                            driverStats.getElapsedTime(),
                            driverStats.getTotalCpuTime(),
                            driverStats.getTotalUserTime(),
                            failureType,
                            failureMessage,
                            objectMapper.writeValueAsString(driverStats)
                    )
            );
        }
        catch (JsonProcessingException e) {
            log.error(e, "Error posting split completion event for task %s", taskId);
        }
    }

    private static Duration millis(long millis)
    {
        if (millis < 0) {
            millis = 0;
        }
        return new Duration(millis, MILLISECONDS);
    }
}
