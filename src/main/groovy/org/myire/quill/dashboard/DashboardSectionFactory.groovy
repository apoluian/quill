/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dashboard

import org.gradle.api.Task
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.JDepend
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.reporting.Report
import org.gradle.api.tasks.testing.Test

import org.myire.quill.check.AbstractCheckTaskEnhancer
import org.myire.quill.check.CheckstyleEnhancer
import org.myire.quill.cobertura.CoberturaReportsTask
import org.myire.quill.cpd.CpdTask
import org.myire.quill.javancss.JavaNcssTask
import org.myire.quill.junit.JUnitAdditionsPlugin
import org.myire.quill.scent.ScentTask


/**
 * Factory for default {@code DashboardSection} instances.
 */
class DashboardSectionFactory
{
    // XSL resources with default style sheets for the standard dashboard sections.
    static private final String XSL_RESOURCE_JUNIT = '/org/myire/quill/rsrc/report/junit/junit_summary.xsl'
    static private final String XSL_RESOURCE_COBERTURA = '/org/myire/quill/rsrc/report/cobertura/cobertura_summary.xsl'
    static private final String XSL_RESOURCE_SCENT = '/org/myire/quill/rsrc/report/scent/scent_summary.xsl'
    static private final String XSL_RESOURCE_FINDBUGS= '/org/myire/quill/rsrc/report/findbugs/findbugs_summary.xsl'
    static private final String XSL_RESOURCE_CHECKSTYLE = '/org/myire/quill/rsrc/report/checkstyle/checkstyle_summary.xsl'
    static private final String XSL_RESOURCE_PMD = '/org/myire/quill/rsrc/report/pmd/pmd_summary.xsl'
    static private final String XSL_RESOURCE_JDEPEND = '/org/myire/quill/rsrc/report/jdepend/jdepend_summary.xsl'
    static private final String XSL_RESOURCE_CPD = '/org/myire/quill/rsrc/report/cpd/cpd_summary.xsl'
    static private final String XSL_RESOURCE_JAVANCSS = '/org/myire/quill/rsrc/report/javancss/javancss_summary.xsl'


    private final DashboardTask fTask;
    private final Map<Class<? extends Task>, Closure<DashboardSection>> fSectionCreators = [:];


    /**
     * Create a new {@code DashboardSectionFactory}.
     *
     * @param pTask The task that owns this instance.
     */
    DashboardSectionFactory(DashboardTask pTask)
    {
        fTask = pTask;

        fSectionCreators[Checkstyle.class] = { Checkstyle t -> createCheckstyleSection(t) };
        fSectionCreators[CoberturaReportsTask.class] = { CoberturaReportsTask t -> createCoberturaSection(t) };
        fSectionCreators[CpdTask.class] = { CpdTask t -> createCpdSection(t) };
        fSectionCreators[FindBugs.class] = { FindBugs t -> createFindBugsSection(t) };
        fSectionCreators[JavaNcssTask.class] = { JavaNcssTask t -> createJavaNcssSection(t) };
        fSectionCreators[JDepend.class] = { JDepend t -> createJDependSection(t) };
        fSectionCreators[Pmd.class] = { Pmd t -> createPmdSection(t) };
        fSectionCreators[ScentTask.class] = { ScentTask t -> createScentSection(t) };
        fSectionCreators[Test.class] = { Test t -> createJUnitSection(t) };
    }


    /**
     * Create all default dashboard sections for which the reports exist.
     *
     * @return  The available default sections.
     */
    Map<String, DashboardSection> createAvailableDefaultSections()
    {
        Map<String, DashboardSection> aSections = [:];

        addSectionsForTaskType(aSections, Test.class);
        addSectionsForTaskType(aSections, CoberturaReportsTask.class);
        addSectionsForTaskType(aSections, ScentTask.class);
        addSectionsForTaskType(aSections, JDepend.class, 'jdependMain');
        addSectionsForTaskType(aSections, FindBugs.class, 'findbugsMain');
        addSectionsForTaskType(aSections, Checkstyle.class, 'checkstyleMain');
        addSectionsForTaskType(aSections, Pmd.class, 'pmdMain');
        addSectionsForTaskType(aSections, CpdTask.class);

        return aSections;
    }


    /**
     * Create a default dashboard section for the JUnit summary report of a test task.
     *
     * @param pTask The test task.
     *
     * @return  A new {@code DashboardSection}, or null if the task has no JUnit summary report in
     *          its convention.
     */
    DashboardSection createJUnitSection(Test pTask)
    {
        Report aReport = (Report) pTask.convention?.findByName(JUnitAdditionsPlugin.SUMMARY_REPORT_NAME);
        if (aReport != null)
        {
            return new DashboardSection(fTask, pTask.name, aReport, pTask.reports?.html, XSL_RESOURCE_JUNIT);
        }
        else
        {
            fTask.logger.debug('Task \'{}\' has no \'{}\' report, cannot create dashboard section for it',
                               pTask.name,
                               JUnitAdditionsPlugin.SUMMARY_REPORT_NAME);
            return null;
        }
    }


    /**
     * Create a default dashboard section for the XML report of a Cobertura report task.
     *
     * @param pTask The Cobertura report task.
     *
     * @return  A new {@code DashboardSection}.
     */
    DashboardSection createCoberturaSection(CoberturaReportsTask pTask)
    {
        return new DashboardSection(fTask,
                                    pTask.name,
                                    pTask.reports.getXml(),
                                    pTask.reports.getHtml(),
                                    XSL_RESOURCE_COBERTURA);
    }


    /**
     * Create a default dashboard section for the XML report of a Scent task.
     *
     * @param pTask The Scent task.
     *
     * @return  A new {@code DashboardSection}.
     */
    DashboardSection createScentSection(ScentTask pTask)
    {
        return new DashboardSection(fTask,
                                    pTask.name,
                                    pTask.reports.getXml(),
                                    pTask.reports.getHtml(),
                                    XSL_RESOURCE_SCENT);
    }


    /**
     * Create a default dashboard section for the XML report of a CPD task.
     *
     * @param pTask The CPD task.
     *
     * @return  A new {@code DashboardSection}, or null if the task's primary report is not on the
     *          XML format.
     */
    DashboardSection createCpdSection(CpdTask pTask)
    {
        Report aReport = pTask.reports.getPrimary();
        if ("xml" == aReport.format)
        {
            return new DashboardSection(fTask, pTask.name, aReport, pTask.reports.getHtml(), XSL_RESOURCE_CPD);
        }
        else
        {
            fTask.logger.debug('Task \'{}\' creates a \'{}\' report, cannot create dashboard section for it',
                               pTask.name,
                               aReport.format);
            return null;
        }
    }


    /**
     * Create a default dashboard section for the XML report of a FindBugs task.
     *
     * @param pTask The FindBugs task.
     *
     * @return  A new {@code DashboardSection}.
     */
    DashboardSection createFindBugsSection(FindBugs pTask)
    {
        Report aDetailedReport = (Report) pTask.convention?.findByName(AbstractCheckTaskEnhancer.TRANSFORMING_REPORT_NAME);
        if (aDetailedReport == null)
            // This is currently (Gradle version <= 2.9) futile; both the XML and the HTML report
            // cannot be enabled at the same time.
            aDetailedReport = pTask.reports.getHtml();

        return new DashboardSection(fTask, pTask.name, pTask.reports.getXml(), aDetailedReport, XSL_RESOURCE_FINDBUGS);
    }


    /**
     * Create a default dashboard section for the XML report of a Checkstyle task.
     *
     * @param pTask The Checkstyle task.
     *
     * @return  A new {@code DashboardSection}.
     */
    DashboardSection createCheckstyleSection(Checkstyle pTask)
    {
        Report aDetailedReport = (Report) pTask.convention?.findByName(AbstractCheckTaskEnhancer.TRANSFORMING_REPORT_NAME);
        if (aDetailedReport == null)
            aDetailedReport = CheckstyleEnhancer.getHtmlReport(pTask.reports);

        return new DashboardSection(fTask, pTask.name, pTask.reports.getXml(), aDetailedReport, XSL_RESOURCE_CHECKSTYLE);
    }


    /**
     * Create a default dashboard section for the XML report of a PMD task.
     *
     * @param pTask The PMD task.
     *
     * @return  A new {@code DashboardSection}.
     */
    DashboardSection createPmdSection(Pmd pTask)
    {
        Report aDetailedReport = (Report) pTask.convention?.findByName(AbstractCheckTaskEnhancer.TRANSFORMING_REPORT_NAME);
        if (aDetailedReport == null)
            aDetailedReport = pTask.reports.getHtml();

        return new DashboardSection(fTask, pTask.name, pTask.reports.getXml(), aDetailedReport, XSL_RESOURCE_PMD);
    }


    /**
     * Create a default dashboard section for the XML report of a JDepend task.
     *
     * @param pTask The JDepend task.
     *
     * @return  A new {@code DashboardSection}.
     */
    DashboardSection createJDependSection(JDepend pTask)
    {
        Report aDetailedReport = (Report) pTask.convention?.findByName(AbstractCheckTaskEnhancer.TRANSFORMING_REPORT_NAME);
        return new DashboardSection(fTask, pTask.name, pTask.reports.getXml(), aDetailedReport, XSL_RESOURCE_JDEPEND);
    }


    /**
     * Create a default dashboard section for the XML report of a JavaNCSS task.
     *
     * @param pTask The JavaNCSS task.
     *
     * @return  A new {@code DashboardSection}, or null if the task's primary report is not on the
     *          XML format.
     */
    DashboardSection createJavaNcssSection(JavaNcssTask pTask)
    {
        Report aReport = pTask.reports.getPrimary();
        if ("xml" == aReport.format)
        {
            return new DashboardSection(fTask, pTask.name, aReport, pTask.reports.getHtml(), XSL_RESOURCE_JAVANCSS);
        }
        else
        {
            fTask.logger.debug('Task \'{}\' creates a \'{}\' report, cannot create dashboard section for it',
                               pTask.name,
                               aReport.format);
            return null;
        }
    }


    /**
     * Add a dashboard section for a task if the factory has a built-in creator for the task's type.
     *
     * @param pSections The map to add the section to.
     * @param pTask     The task to create the built-in dashboard section for.
     *
     * @return  True if a built-in section was added, false if the factory doesn't have a built-in
     *          creator for the task's type.
     */
    boolean addBuiltInSection(Map<String, DashboardSection> pSections, Task pTask)
    {
        Map.Entry<Class<? extends Task>, Closure<DashboardSection>> aEntry =
                fSectionCreators.find { k, v -> k.isInstance(pTask) };
        DashboardSection aSection = aEntry?.value?.call(pTask);
        if (aSection != null)
        {
            pSections.put(aSection.name, aSection)
            return true;
        }
        else
            return false;
    }


    /**
     * Add dashboard sections for all tasks of a certain type in the owning task's project by
     * invoking the closure from {@code fSectionCreators} on each task.
     *
     * @param pSections The map to add the sections to.
     * @param pType     The task type to create default dashboard sections for.
     */
    private void addSectionsForTaskType(
            Map<String, DashboardSection> pSections,
            Class<? extends Task> pType)
    {
        Closure<DashboardSection> aClosure = fSectionCreators[pType];
        if (aClosure == null)
            return;

        fTask.project.tasks.withType(pType)
        {
            DashboardSection aSection = aClosure.call(it);
            if (aSection != null)
                pSections.put(aSection.name, aSection);
        }
    }


    /**
     * Add dashboard sections for all tasks of a certain type and name in the owning task's project
     * by invoking the closure from {@code fSectionCreators} on each task.
     *
     * @param pSections The map to add the sections to.
     * @param pType     The task type to create default dashboard sections for.
     * @param pName     The name that the tasks must match.
     */
    private void addSectionsForTaskType(
            Map<String, DashboardSection> pSections,
            Class<? extends Task> pType,
            String pName)
    {
        Closure<DashboardSection> aClosure = fSectionCreators[pType];
        if (aClosure == null)
            return;

        fTask.project.tasks.withType(pType)
        {
            if (pName == it.name)
            {
                DashboardSection aSection = aClosure.call(it);
                if (aSection != null)
                    pSections.put(aSection.name, aSection);
            }
        }
    }
}
