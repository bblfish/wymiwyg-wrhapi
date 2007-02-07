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
 * Basically a case-insensitive String
 *
 * @author reto
 */
public class HeaderName {
    public static final Object CONTENT_DISPOSITION = new HeaderName(
            "content-disposition");
    public static final HeaderName TRANSFER_ENCODING = new HeaderName(
            "transfer-tncoding");
    public static HeaderName ACCEPT = new HeaderName("accept");
    public static HeaderName HOST = new HeaderName("host");
    public static HeaderName CONTENT_TYPE = new HeaderName("content-type");
    public static HeaderName ACCEPT_LANGUAGE = new HeaderName("accept-language");
    public static HeaderName CONTENT_LENGTH = new HeaderName("content-length");
    public static HeaderName CONTENT_ENCODING = new HeaderName(
            "content-encoding"); //RFC 2616 Section 7.1, like gzip
    public static HeaderName CONTENT_LANGUAGE = new HeaderName(
            "content-language");
    public static HeaderName COOKIE = new HeaderName("cookie");
    public static HeaderName SET_COOKIE = new HeaderName("set-cookie");
    public static HeaderName LOCATION = new HeaderName("location");
    public static HeaderName REFERER = new HeaderName("referer");
    public static final HeaderName PRAGMA = new HeaderName("pragma");
    public static final HeaderName EXPIRES = new HeaderName("expires");
    public static final HeaderName AUTHORIZATION = new HeaderName(
            "authorization");
    public static final HeaderName CACHE_CONTROL = new HeaderName(
            "cache-control");
    public static final HeaderName USER_AGENT = new HeaderName("user-agent");
    public static final HeaderName WWW_AUTHENTICATE = new HeaderName(
            "www-authenticate");
    public static final HeaderName DEPTH = new HeaderName("depth");
    public static final HeaderName DAV = new HeaderName("dav");
    public static final HeaderName ALLOW = new HeaderName("allow");
    public static final HeaderName LAST_MODIFIED = new HeaderName(
            "last-modified");
    public static final HeaderName SERVER = new HeaderName("server");
    public static final HeaderName IF_MODIFIED_SINCE = new HeaderName(
            "if-modified-since");
    public static final HeaderName IF_NONE_MATCH = new HeaderName(
            "if-none-match");
    private String lowerCaseName;

    /**
     *
     */
    private HeaderName(String lowerCaseName) {
        this.lowerCaseName = lowerCaseName;
    }

    public static HeaderName get(String name) {
        return new HeaderName(name.toLowerCase());
    }

    public String toString() {
        return lowerCaseName;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if (!object.getClass().equals(HeaderName.class)) {
            return false;
        } else {
            return lowerCaseName.equals(((HeaderName) object).lowerCaseName);
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return lowerCaseName.hashCode();
    }
}
