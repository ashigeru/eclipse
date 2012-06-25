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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;

class RefreshWorkspaceOperation extends AbstractOperation {

    public RefreshWorkspaceOperation() {
        super("--refresh-workspace", null);
    }

    @Override
    public void perform(String value) throws Exception {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            System.out.println("[REFRESH] Refresh workspace");
            workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        } finally {
            try {
                workspace.save(true, new NullProgressMonitor());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
