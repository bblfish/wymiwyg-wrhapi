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
package org.wymiwyg.wrhapi.jetty.test;

import org.wymiwyg.wrhapi.WebServerFactory;
import org.wymiwyg.wrhapi.jetty.JettyWebServerFactory;
import org.wymiwyg.wrhapi.test.BaseTests;


/**
 * @author reto
 *
 */
public class DefaultTests extends BaseTests {
    /* (non-Javadoc)
     * @see org.wymiwyg.wrhapi.test.BaseTests#createServer()
     */
    @Override
    protected WebServerFactory createServer() {
        return new JettyWebServerFactory();
    }
}
