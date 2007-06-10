/*
 * Copyright  2002-2006 WYMIWYG (http://wymiwyg.org)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wymiwyg.wrhapi;


/**
 * @author reto
 */
public interface RequestURI {
    /**
     * @return the path without parameters.
     */
    public String getPath();

    /**
     * @return the parameter names in the order they first appear in the request
     */
    public String[] getParameterNames();

    /**
     *
     * @param name the name of the parameter
     * @return an array with the values of the ger parameter or null if the parameter is not present
     */
    public String[] getParameterValues(String name);
}
