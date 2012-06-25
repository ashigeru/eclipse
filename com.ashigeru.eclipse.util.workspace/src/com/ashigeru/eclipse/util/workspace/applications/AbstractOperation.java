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

/**
 * Abstract super implementation of {@link Operation}.
 */
public abstract class AbstractOperation implements Operation {

    private final String prefix;

    private final String valueDescription;

    /**
     * Creates a new instance.
     * @param prefix operation command line prefix
     * @param valueDescription argument description, or {@code null} if not used
     */
    public AbstractOperation(String prefix, String valueDescription) {
        this.prefix = prefix;
        this.valueDescription = valueDescription;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getValueDescription() {
        return valueDescription;
    }

    @Override
    public abstract void perform(String value) throws Exception;
}
