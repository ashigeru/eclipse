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
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * Import projects in specified paths.
 */
public class Main implements IApplication {

    static final Map<String, Operation> OPERATIONS = new TreeMap<String, Operation>();
    static {
        register(new MavenRepositoryOperation());
        register(new ImportProjectOperation());
        register(new ProjectCountOperation());
        register(new RefreshWorkspaceOperation());
        register(new CleanWorkspaceOperation());
        register(new BuildWorkspaceOperation());
        register(new CheckErrorsOperation());
    }

    private static void register(Operation operation) {
        OPERATIONS.put(operation.getPrefix(), operation);
    }

    @Override
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        Object exitCode = run(args);
        if (exitCode != EXIT_OK) {
            System.exit(1);
        }
        return exitCode;
    }

    private Object run(String[] args) {
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            for (Operation o : OPERATIONS.values()) {
                if (o.getValueDescription() == null) {
                    System.err.println(o.getPrefix());
                } else {
                    System.err.println(MessageFormat.format(
                            "{0} <{1}>",
                            o.getPrefix(),
                            o.getValueDescription()));
                }
            }
            return args.length == 0 ? 1 : EXIT_OK;
        }

        System.out.println(MessageFormat.format(
                "Starting workspace util: {0}",
                ResourcesPlugin.getWorkspace().getRoot().getLocationURI()));

        for (int i = 0; i < args.length; i++) {
            Operation operation = OPERATIONS.get(args[i]);
            if (operation == null) {
                System.err.println(MessageFormat.format(
                        "Invalid argument: {0}",
                        args[i]));
                return 1;
            }
            System.out.println(MessageFormat.format(
                    "Operation: {0}",
                    operation.getPrefix()));
            try {
                if (operation.getValueDescription() == null) {
                    operation.perform(null);
                } else {
                    if (i + 1 == args.length) {
                        System.err.println(MessageFormat.format(
                                "Missing option value: {0} <{1}>",
                                args[i],
                                operation.getValueDescription()));
                        return 1;
                    }
                    i++;
                    String argument = args[i];
                    operation.perform(argument);
                }
            } catch (Throwable e) {
                System.err.println(MessageFormat.format(
                        "Assertion failed in {0}",
                        operation.getPrefix()));
                System.err.println(e.getMessage());
                return 1;
            }
        }
        return EXIT_OK;
    }

    @Override
    public void stop() {
        return;
    }
}
