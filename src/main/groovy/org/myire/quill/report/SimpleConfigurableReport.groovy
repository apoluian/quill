/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

package org.myire.quill.report

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.Report
import org.gradle.util.ConfigureUtil

import org.myire.quill.common.ProjectAware;

import java.io.File;


/**
 * Implementation of {@code org.gradle.api.reporting.ConfigurableReport}.
 */
class SimpleConfigurableReport extends ProjectAware implements ConfigurableReport
{
    static private final boolean USE_CONFIGURE_SELF = configureSelfAvailable();


    private final String fName;
    private final String fDisplayName;
    private final Report.OutputType fOutputType;

    //Code taken from
    //https://github.com/tc214/WebRTC_Android/blob/512d1fde2ae0a77adfe155b2541f8f9fc7e666bf/examples/androidtests/third_party/gradle/subprojects/reporting/src/main/java/org/gradle/api/reporting/internal/SimpleReport.java
    private final Property<File> destination;
    private final Property<Boolean> enabled;
    private final Project project;


    /**
     * Create a new {@code SimpleReport}.
     *
     * @param pProject      The project for which the report will be produced.
     * @param pName         The report's symbolic name.
     * @param pDisplayName  The report's descriptive name.
     * @param pOutputType   The type of output the report produces.
     */
    SimpleConfigurableReport(
            Project pProject,
            String pName,
            String pDisplayName,
            Report.OutputType pOutputType)
    {
        super(pProject);
        fName = pName;
        fDisplayName = pDisplayName;
        fOutputType = pOutputType;

        this.project = pProject;
        destination = project.getObjects().property(File.class);
        enabled = project.getObjects().property(Boolean.class);
    }


    String getName()
    {
        return fName;
    }


    String getDisplayName()
    {
        return fDisplayName;
    }


    Report.OutputType getOutputType()
    {
        return fOutputType;
    }


    Report configure(Closure pConfigureClosure)
    {
        if (USE_CONFIGURE_SELF)
            return ConfigureUtil.configureSelf(pConfigureClosure, this);
        else
            return ConfigureUtil.configure(pConfigureClosure, this, false);
    }


    String toString()
    {
        return "Report " + getName();
    }


    /**
     * Check if the {@code configureSelf} method is available in the {@code ConfigureUtil} class.
     * This method, introduced in Gradle 2.14, is, if available, the preferred way to configure
     * an entity with a closure.
     *
     * @return  True if the method is available, false if not.
     */
    static private boolean configureSelfAvailable()
    {
        try
        {
            ConfigureUtil.class.getMethod("configureSelf", Closure.class, Object.class) != null;
            return true;
        }
        catch (ReflectiveOperationException e)
        {
            return false;
        }
    }

    public File getDestination() {
        return destination.getOrNull();
    }

    @Override
    public void setDestination(File file) {
        this.destination.set(file);
    }

    @Override
    public void setDestination(Provider<File> provider) {
        this.destination.set(provider);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    @Override
    public void setEnabled(Provider<Boolean> enabled) {
        this.enabled.set(enabled);
    }
}
