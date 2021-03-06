/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cobertura

import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.SingleFileReport


/**
 * The reports produced by a {@code CoberturaReportTask}.
 */
interface CoberturaReports extends ReportContainer<Report>
{
    /**
     * Get the XML file report.
     *
     * @return  The XML file report.
     */
    SingleFileReport getXml();

    /**
     * Get the directory containing the HTML report.
     *
     * @return  The HTML directory report.
     */
    DirectoryReport getHtml();
}
