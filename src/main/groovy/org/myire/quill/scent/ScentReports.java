/*
 * Copyright 2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent;

import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.SingleFileReport;

import org.myire.quill.report.TransformingReport;


/**
 * The reports produced by a {@code ScentTask}.
 */
public interface ScentReports extends ReportContainer<Report>
{
    /**
     * Get the XML file report.
     *
     * @return The XML file report.
     */
    SingleFileReport getXml();

    /**
     * Get the HTML file report. This report is produced by applying an XSL transformation on the
     * XML report.
     *
     * @return The HTML file report.
     */
    TransformingReport getHtml();
}
