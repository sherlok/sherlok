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
 * This exception is used when a configuration variable cannot be properly
 * processed (e.g. remote resource cannot be fetched, file cannot be written on
 * disk, ...).
 */
public class ProcessConfigVariableException extends Exception {

    public ProcessConfigVariableException(String msg, Throwable t) {
        super(msg, t);
    }

    public ProcessConfigVariableException(String msg) {
        super(msg);
    }

    private static final long serialVersionUID = -95682363642593272L;

}
