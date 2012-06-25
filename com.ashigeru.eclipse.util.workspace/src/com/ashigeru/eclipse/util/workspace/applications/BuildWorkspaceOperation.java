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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspace.ProjectOrder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;

class BuildWorkspaceOperation extends AbstractOperation {

    public BuildWorkspaceOperation() {
        super("--build-workspace", null);
    }

    @Override
    public void perform(String value) throws Exception {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
            ProjectOrder order = workspace.computeProjectOrder(workspace.getRoot().getProjects());
            for (IProject project : order.projects) {
                System.out.println(MessageFormat.format(
                        "[BUILD] Building project: {0}",
                        project.getName()));
                project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
            }
        } finally {
            try {
                workspace.save(true, new NullProgressMonitor());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
