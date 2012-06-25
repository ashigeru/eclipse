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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;

class MavenRepositoryOperation extends AbstractOperation {

    public MavenRepositoryOperation() {
        super("--maven-repository", "/path/to/maven-repository");
    }

    @Override
    public void perform(String value) throws Exception {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            IPath path = Path.fromOSString(value);
            System.out.println(MessageFormat.format(
                    "[M2REPO] Registering M2_REPO: {0}",
                    path));
            JavaCore.setClasspathVariable("M2_REPO", path, new NullProgressMonitor());
            workspace.save(true, new NullProgressMonitor());
        } finally {
            try {
                workspace.save(true, new NullProgressMonitor());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
