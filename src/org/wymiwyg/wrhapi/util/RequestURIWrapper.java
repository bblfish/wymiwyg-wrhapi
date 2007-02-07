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

import org.wymiwyg.wrhapi.RequestURI;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;


/**
 * @author reto
 */
public class RequestURIWrapper implements RequestURI {
    private RequestURI wrapped;

    public RequestURIWrapper(RequestURI wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * @see org.wymiwyg.wrhapi.RequestURI#getPath()
     */
    public String getPath() {
        return wrapped.getPath();
    }

    /**
     * @see org.wymiwyg.wrhapi.RequestURI#getParameterNames()
     */
    public String[] getParameterNames() {
        return wrapped.getParameterNames();
    }

    /**
     * @see org.wymiwyg.wrhapi.RequestURI#getParamterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        return wrapped.getParameterValues(name);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(getPath());
        boolean first = true;
        String[] parameterNames = getParameterNames();

        for (int i = 0; i < parameterNames.length; i++) {
            String[] values = getParameterValues(parameterNames[i]);

            for (int j = 0; j < values.length; j++) {
                if (first) {
                    buffer.append('?');
                    first = false;
                } else {
                    buffer.append('&');
                }

                buffer.append(parameterNames[i]);
                buffer.append('=');

                try {
                    buffer.append(URLEncoder.encode(values[j], "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return buffer.toString();
    }
}
