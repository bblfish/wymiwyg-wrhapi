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
 * A request Method (GET, POST, PUT, DELETE, ...).
 *
 * @author reto
 */
public class Method {
    public static final Method POST = new Method("POST");
    public static final Method GET = new Method("GET");
    public static final Method PUT = new Method("PUT");
    public static final Method DELETE = new Method("DELETE");
    public static final Method OPTIONS = new Method("OPTIONS");
    public static final Method TRACE = new Method("TRACE");
    public static final Method CONNECT = new Method("CONNECT");
    public static final Method HEAD = new Method("HEAD");

    //URIQUA
    public static final Method MGET = new Method("MGET");

    //WEBDAV
    public static final Method PROPFIND = new Method("PROPFIND");
    public static final Method PROPPATCH = new Method("PROPPATCH");
    public static final Method MKCOL = new Method("MKCOL");
    public static final Method COPY = new Method("COPY");
    public static final Method MOVE = new Method("MOVE");
    public static final Method LOCK = new Method("LOCK");
    public static final Method UNLOCK = new Method("UNLOCK");
    private String name;

    private Method(String name) {
        this.name = name;
    }

    public static Method get(String name) {
        return new Method(name.toUpperCase());
    }

    public String toString() {
        return "Method: " + name;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof Method)) {
            return false;
        }

        return name.equals(((Method) object).name);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return name.hashCode();
    }

    public String getName() {
        return name;
    }
}
