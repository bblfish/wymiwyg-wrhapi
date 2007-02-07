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
package org.wymiwyg.wrhapi.util;

import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.RequestURI;


/**
 * @author reto
 * @date Jul 5, 2004
 */
public class EnhancedRequestURI extends RequestURIWrapper {
    /**
     *
     */
    public EnhancedRequestURI(RequestURI wrapped) {
        super(wrapped);
    }

    public String getParameter(String name) throws HandlerException {
        try {
            return getParameterValues(name)[0];
        } catch (Exception ex) {
            return null;
        }
    }
}
