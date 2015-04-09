/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.core.internal.utils;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class StopWatch {

    private static Logger logger = Logger.getLogger(StopWatch.class);

    /**
     * Identifier of this stop watch. Handy when we have output from multiple stop watches and need to distinguish
     * between them in log or console output.
     */
    private final String id;

    private boolean keepTaskList = true;

    /** List of TaskInfo objects */
    private final List<TaskInfo> taskList = new LinkedList<>();
    private TaskInfo lastTaskInfo = null;

    /**
     * Construct a new stop watch. Does not start any task.
     */
    public StopWatch() {
        this.id = "";
    }

    /**
     * Construct a new stop watch with the given id. Does not start any task.
     * 
     * @param id
     *            identifier for this stop watch. Handy when we have output from multiple stop watches and need to
     *            distinguish between them.
     */
    public StopWatch(String id) {
        this.id = id;
    }

    /**
     * Determine whether the TaskInfo array is built over time. Set this to "false" when using a StopWatch for millions
     * of intervals, or the task info structure will consume excessive memory. Default is "true".
     */
    public void setKeepTaskList(boolean keepTaskList) {
        this.keepTaskList = keepTaskList;
    }

    /**
     * Start a named task. The results are undefined if {@link #stop()} or timing methods are called without invoking
     * this method.
     * 
     * @param taskName
     *            the name of the task to start
     * @see #stop()
     */
    public void start(String taskName) throws IllegalStateException {
        if (Utils.isEmpty(taskName)) {
            logger.warn("Can't start StopWatch: task name must be provided");
            return;
        }

        TaskInfo taskInfo = getTaskInfo(taskName, true);
        taskInfo.start();
    }

    /**
     * Stop the current task. The results are undefined if timing methods are called without invoking at least one pair
     * {@link #start()} / {@link #stop()} methods.
     * 
     * @see #start()
     */
    public void stop(String taskName) throws IllegalStateException {
        if (Utils.isEmpty(taskName)) {
            throw new IllegalStateException("Can't stop StopWatch: task name must be provided");
        }

        TaskInfo taskInfo = getTaskInfo(taskName, false);

        if (taskInfo == null) {
            logger.warn("Can't start StopWatch: not existing task found for '" + taskName + "'");
            return;
        }

        taskInfo.stop();

        this.lastTaskInfo = taskInfo;
    }

    private TaskInfo getTaskInfo(String taskName, boolean create) {
        if (Utils.isNotEmpty(taskList)) {
            for (TaskInfo tmpTaskInfo : taskList) {
                if (tmpTaskInfo.getTaskName().equals(taskName)) {
                    return tmpTaskInfo;
                }
            }
        }

        TaskInfo taskInfo = null;
        if (create) {
            taskInfo = new TaskInfo(taskName);
            taskList.add(taskInfo);
        }

        return taskInfo;
    }

    /**
     * Return the time taken by the last task.
     */
    public long getLastTaskTimeMillis() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tests run: can't get last interval");
        }
        return this.lastTaskInfo.getTimeMillis();
    }

    private long getTotalTimeMillis() {
        long totalTime = 0;
        if (Utils.isNotEmpty(taskList)) {
            for (TaskInfo taskInfo : taskList) {
                totalTime += taskInfo.getTimeMillis();
            }
        }

        return totalTime;
    }

    /**
     * Return the total time in seconds for all tasks.
     */
    public double getTotalTimeSeconds() {
        return getTotalTimeMillis() / 1000.0;
    }

    /**
     * Return an array of the data for tasks performed.
     */
    public TaskInfo[] getTaskInfo() {
        if (!this.keepTaskList) {
            throw new UnsupportedOperationException("Task info is not being kept!");
        }
        return this.taskList.toArray(new TaskInfo[this.taskList.size()]);
    }

    /**
     * Return a short description of the total running time.
     */
    public String shortSummary() {
        return "StopWatch '" + this.id + "': running time (millis) = " + getTotalTimeMillis();
    }

    /**
     * Return a string with a table describing all tasks performed. For custom reporting, call getTaskInfo() and use the
     * task info directly.
     */
    public String prettyPrint() {
        StringBuffer sb = new StringBuffer(shortSummary());
        sb.append('\n');
        if (!this.keepTaskList) {
            sb.append("No task info kept");
        } else {
            TaskInfo[] tasks = getTaskInfo();
            Arrays.sort(tasks, new Comparator<TaskInfo>() {
                @Override
                public int compare(TaskInfo o1, TaskInfo o2) {
                    if (o1 == o2 || o1.getAverage() == o2.getAverage()) {
                        return 0;
                    } else if (o1.getAverage() > o2.getAverage()) {
                        return -1;
                    } else if (o1.getAverage() < o2.getAverage()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }

            });

            headerPrettyPrint(sb);

            for (int i = 0; i < tasks.length; i++) {
                taskPrettyPrint(sb, tasks[i]);
            }
            sb.append("\nTotal time of all tasks: ").append(getTotalTimeSeconds()).append(" secs\n");
        }
        return sb.toString();
    }

    public String prettyPrint(String taskName) {
        StringBuffer sb = new StringBuffer(shortSummary());
        sb.append('\n');
        if (!this.keepTaskList) {
            sb.append("No task info kept");
        } else {
            TaskInfo task = getTaskInfo(taskName, false);
            if (task == null) {
                sb.append("No task info kept for task '" + taskName + "'");
            } else {
                headerPrettyPrint(sb);
                taskPrettyPrint(sb, task);
                sb.append("\nTotal time of all tasks: ").append(getTotalTimeSeconds()).append(" secs\n");
            }

        }
        return sb.toString();
    }

    private static void headerPrettyPrint(StringBuffer strBuff) {
        strBuff.append("-----------------------------------------\n").append("ms     avg     %     Task name (cnt)\n")
        .append("-----------------------------------------\n");
    }

    private void taskPrettyPrint(StringBuffer strBuff, TaskInfo task) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumIntegerDigits(5);
        nf.setGroupingUsed(false);
        NumberFormat pf = NumberFormat.getPercentInstance();
        pf.setMinimumIntegerDigits(3);
        pf.setGroupingUsed(false);

        strBuff.append(nf.format(task.getTimeMillis()) + "  ").append(nf.format(task.getAverage()) + "  ").append(
            pf.format(task.getTimeSeconds() / getTotalTimeSeconds()) + "  ").append(task.getTaskName()).append(" (")
            .append(task.getCount()).append(")\n");
    }

    /**
     * Return an informative string describing all tasks performed For custom reporting, call <code>getTaskInfo()</code>
     * and use the task info directly.
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(shortSummary());
        if (this.keepTaskList) {
            TaskInfo[] tasks = getTaskInfo();
            for (int i = 0; i < tasks.length; i++) {
                sb.append("; [" + tasks[i].getTaskName() + "] took " + tasks[i].getTimeMillis());
                long percent = Math.round((100.0 * tasks[i].getTimeSeconds()) / getTotalTimeSeconds());
                sb.append(" = " + percent + "%");
            }
        } else {
            sb.append("; no task info kept");
        }
        return sb.toString();
    }

    /**
     * Inner class to hold data about one task executed within the stop watch.
     */
    public static class TaskInfo {

        private final String taskName;
        private long startTimeMillis;
        private boolean running;
        private long totalTimeMillis = 0;
        private long count = 0;

        TaskInfo(String taskName) {
            this.taskName = taskName;
        }

        /**
         * Return the name of this task.
         */
        public String getTaskName() {
            return taskName;
        }

        /**
         * Return the time in milliseconds this task took.
         */
        public long getTimeMillis() {
            return totalTimeMillis;
        }

        public void addTimeMillis(long timeMillis) {
            this.totalTimeMillis += timeMillis;
            count++;
        }

        /**
         * Return the time in seconds this task took.
         */
        public double getTimeSeconds() {
            return totalTimeMillis / 1000.0;
        }

        public long getCount() {
            return count;
        }

        public long getAverage() {
            if (getCount() < 1) {
                return 0;
            }
            return getTimeMillis() / getCount();
        }

        public void start() throws IllegalStateException {
            if (Utils.isEmpty(taskName)) {
                logger.warn("Task name cannot be null");
                return;
            }
            this.startTimeMillis = System.currentTimeMillis();
            this.running = true;
        }

        /**
         * Stop the current task. The results are undefined if timing methods are called without invoking at least one
         * pair {@link #start()} / {@link #stop()} methods.
         * 
         * @see #start()
         */
        public void stop() throws IllegalStateException {
            if (!this.running) {
                logger.warn("Can't stop StopWatch: it's not running");
                return;
            }

            long lastTime = System.currentTimeMillis() - this.startTimeMillis;
            addTimeMillis(lastTime);
            this.running = false;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final TaskInfo other = (TaskInfo) obj;
            if (taskName == null) {
                if (other.taskName != null)
                    return false;
            } else if (!taskName.equals(other.taskName))
                return false;
            return true;
        }
    }

}
