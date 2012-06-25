/*
 * Copyright 2012 @ashigeru.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ashigeru.eclipse.util.workspace.applications;

import java.text.MessageFormat;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

class CheckErrorsOperation extends AbstractOperation {

    public CheckErrorsOperation() {
        super("--check-errors", null);
    }

    @Override
    public void perform(String value) throws Exception {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        System.out.println("[CHECK] Searching for errors");
        IMarker[] markers = workspace.getRoot().findMarkers(
                IMarker.PROBLEM,
                true,
                IResource.DEPTH_INFINITE);

        int errorCount = 0;
        for (IMarker marker : markers) {
            int severity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
            if (severity == IMarker.SEVERITY_ERROR) {
                errorCount++;
                System.out.println(MessageFormat.format(
                        "[ERROR] {0} ({1})",
                        marker.getAttribute(IMarker.MESSAGE, "(null)"),
                        marker.getResource().getLocationURI()));
            }
        }
        if (errorCount > 0) {
            throw new AssertionError(MessageFormat.format(
                    "Errors detected: {0}",
                    errorCount));
        }
    }
}
