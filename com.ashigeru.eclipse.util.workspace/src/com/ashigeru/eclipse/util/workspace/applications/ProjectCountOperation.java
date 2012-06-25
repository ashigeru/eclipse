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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

class ProjectCountOperation extends AbstractOperation {

    public ProjectCountOperation() {
        super("--project-count", "number-of-projects");
    }

    @Override
    public void perform(String value) throws Exception {
        int expected = Integer.parseInt(value);
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        System.out.println("[CHECK] Counting available projects");
        IProject[] projects = workspace.getRoot().getProjects();
        int actual = 0;
        for (IProject project : projects) {
            if (project.exists()) {
                System.out.println(MessageFormat.format(
                        "[CHECK] Found project: {0} ({1})",
                        project.getName(),
                        project.getLocationURI()));
                actual++;
            }
        }
        if (expected != actual) {
            throw new AssertionError(MessageFormat.format(
                    "Inconsistent project count: expected={0}, actual={1}",
                    expected,
                    actual));
        }
    }
}
