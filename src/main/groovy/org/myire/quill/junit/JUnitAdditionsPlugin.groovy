/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.junit

import org.gradle.api.Task
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

import org.myire.quill.common.Projects
import org.myire.quill.report.AdditionalReportsTask

/**
 * Gradle plugin for adding enhancements to the JUnit test task.
 */
class JUnitAdditionsPlugin implements Plugin<Project>
{
    static final String SUMMARY_REPORT_NAME = 'junitSummaryReport'


    @Override
    void apply(Project pProject)
    {
        Test aTestTask = Projects.getTask(pProject, "test", Test.class)

        Task aTask = pProject.tasks.getByName("test");
        aTask.logger.debug("ZZZ junitSummaryReport: gradle project test task class")
        aTask.logger.debug(aTask.getClass().getName())

        if (aTestTask != null)
        {
            // Create a JUnit summary report, enable it and add it to the task's convention.
            JUnitSummaryReport aReport = new JUnitSummaryReport(aTestTask);
            aReport.enabled = true;
            aTestTask.convention.add(SUMMARY_REPORT_NAME, aReport);

            // Add the report to the test task's update check to allow triggering the tests if the
            // summary report is out-of-date.
            aTestTask.outputs.upToDateWhen({ aReport.checkUpToDate() });

            // Add a task action to create the summary report.
            aTestTask.doLast({ 
                aTestTask.logger.debug("ZZZ junitSummaryReport: running quill summary report sub-task")
                aReport.createReport()
            });

            // Add the summary report to the additional reports task's report container to make it
            // known to the build dashboard plugin.

            try
            {
                // Add the summary report to the additional reports task's report container to make it
                // known to the build dashboard plugin.
                if (!AdditionalReportsTask.maybeCreate(pProject).reports.addReport(aReport)) {
                    aTestTask.logger.debug("ZZZ junitSummaryReport: Failed to add report \'{}\' to the additional reports task", SUMMARY_REPORT_NAME)
                }
            }
            catch (Exception e)
            {
                aTestTask.logger.debug("ZZZ junitSummaryReport: exception on AdditionalReportsTask creation")
                aTestTask.logger.debug(e.getMessage())
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString();
                aTestTask.logger.debug(sStackTrace);
            }
        }
    }
}
