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
public interface Response {
    public void setBody(MessageBody body) throws HandlerException;

    /**
     * if the value is not an array its toString()-method is called, otherwise the
     * toString()-methods of the elements are called, and they are comma-separed.
     *
     * @param headerName
     * @param value
     */
    public void setHeader(HeaderName headerName, Object value)
        throws HandlerException;

    /** if the header is already set, this methos adds the new value(s), otherwise
     * it has the same effect as setHeader.
     *
     * This method must be implemented to call setHeader with the new value.
     *
     * @param headerName
     * @param value
     * @throws HandlerException
     */
    public void addHeader(HeaderName headerName, Object value)
        throws HandlerException;

    public void setResponseStatus(ResponseStatus status)
        throws HandlerException;
}
