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

import java.net.InetAddress;


/**
 * @author reto
 */
public interface Request {
    public Method getMethod() throws HandlerException;

    /**
     * This methods returns the request-uri including the get parameters.
     * @return
     */
    public RequestURI getRequestURI() throws HandlerException;
    
    public MessageBody getMessageBody() throws HandlerException;

    /**
     * @deprecated use getMessageBody instead
     * @return
     * @throws HandlerException
     */
    public Object getBody() throws HandlerException;

    /**
     *
     * @return the header-names as lower-case strings
     */
    public HeaderName[] getHeaderNames() throws HandlerException;

    /** Returns the value of a header-field, if the header-field is not present an array
     * of size 0 is returned
     *
     * @param headerName
     * @return
     */
    public String[] getHeaderValues(HeaderName headerName)
        throws HandlerException;

    public int getPort() throws HandlerException;

    public String getScheme() throws HandlerException;

    public InetAddress getRemoteHost() throws HandlerException;
}
