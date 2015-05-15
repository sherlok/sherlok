/**
 * Copyright (C) 2014-2015 Renaud Richardet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sherlok.config;

/**
 * Configuration variable for bundles
 */
public interface ConfigVariable {
    /**
     * Returns the (runtime) path corresponding to this variable
     * 
     * This path should be relative to FileBased.RUTA_RESOURCES_PATH when the
     * RUTA compatibility mode is enabled.
     * 
     * @throws ProcessConfigVariableException
     *             when a runtime error occurs
     */
    public abstract String getProcessedValue()
            throws ProcessConfigVariableException;
}
