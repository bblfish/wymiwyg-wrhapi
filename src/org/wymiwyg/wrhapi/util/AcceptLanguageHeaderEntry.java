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

import java.util.Locale;
import java.util.StringTokenizer;


/**
 * @author reto
 * @date Jun 4, 2004
 */
public class AcceptLanguageHeaderEntry implements Comparable {
    private LocaleRange localeRange;
    private float q;

    /**
     *
     */
    public AcceptLanguageHeaderEntry(String headerEntry) {
        StringTokenizer tokens = new StringTokenizer(headerEntry, ";");
        String firstToken = tokens.nextToken();

        if (firstToken.equals("*")) {
            localeRange = new LocaleRange();
        } else {
            localeRange = new LocaleRange(parseLocale(firstToken));
        }

        if (tokens.hasMoreTokens()) {
            q = readQ(tokens.nextToken());
        } else {
            q = 1;
        }
    }

    /**
     * @param string
     * @return
     */
    private float readQ(String string) {
        int eqPos = string.indexOf('=');

        return Float.parseFloat(string.substring(eqPos + 1));
    }

    /**
     * @param language
     * @return
     */
    private Locale parseLocale(String language) {
        int underscorePos = language.indexOf('_');

        if (underscorePos > -1) {
            return new Locale(language.substring(0, underscorePos),
                language.substring(underscorePos + 1));
        } else {
            int dashPos = language.indexOf('-');

            if (dashPos > -1) {
                return new Locale(language.substring(0, dashPos),
                    language.substring(dashPos + 1));
            } else {
                return new Locale(language);
            }
        }
    }

    /**
     * @return Returns the locale.
     */
    public LocaleRange getLocaleRange() {
        return localeRange;
    }

    /**
     * @return Returns the q.
     */
    public float getQ() {
        return q;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object other) {
        AcceptLanguageHeaderEntry otherA = (AcceptLanguageHeaderEntry) other;

        if (q > otherA.q) {
            return -1;
        }

        if (q < otherA.q) {
            return 1;
        }

        return localeRange.toString().compareTo(otherA.localeRange.toString());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other) {
        if (other.getClass().equals(AcceptLanguageHeaderEntry.class)) {
            return (q == ((AcceptLanguageHeaderEntry) other).q) &&
            (localeRange == ((AcceptLanguageHeaderEntry) other).localeRange);
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return localeRange.hashCode();
    }
}
