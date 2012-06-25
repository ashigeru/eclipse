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

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

class ImportProjectOperation extends AbstractOperation {

    static final FileFilter DIRECTORIES = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    };

    public ImportProjectOperation() {
        super("--import-project", "/path/to/folder");
    }

    @Override
    public void perform(String value) throws Exception {
        File root = new File(value);
        System.out.println(MessageFormat.format(
                "[IMPORT] Searching for projects in \"{0}\"",
                root));
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            workspace.save(true, new NullProgressMonitor());
            List<IProjectDescription> projects = collect(root);

            for (IProjectDescription desc : projects) {
                String name = desc.getName();
                IProject project = workspace.getRoot().getProject(name);
                if (project.exists()) {
                    System.out.println(MessageFormat.format(
                            "[IMPORT] SKIP: Project \"{0}\" already exists ({1})",
                            name,
                            desc.getLocationURI()));
                } else {
                    System.out.println(MessageFormat.format(
                            "[IMPORT] Project \"{0}\" ({1})",
                            name,
                            desc.getLocationURI()));
                    project.create(
                            desc,
                            new NullProgressMonitor());
                }
                project.open(new NullProgressMonitor());
            }

            System.out.println("[IMPORT] Refresh workspace");
            workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        } finally {
            try {
                workspace.save(true, new NullProgressMonitor());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<IProjectDescription> collect(File root) throws CoreException {
        assert root != null;
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        List<IProjectDescription> results = new ArrayList<IProjectDescription>();
        LinkedList<File> work = new LinkedList<File>();
        work.add(root);
        while (work.isEmpty() == false) {
            File next = work.removeFirst();
            if (next.isDirectory()) {
                File descFile = new File(next, IProjectDescription.DESCRIPTION_FILE_NAME);
                if (descFile.exists()) {
                    IPath path = Path.fromOSString(descFile.getAbsolutePath());
                    IProjectDescription description = workspace.loadProjectDescription(path);
                    results.add(description);
                } else {
                    for (File child : next.listFiles(DIRECTORIES)) {
                        work.add(child);
                    }
                }
            }
        }

        return results;
    }
}
