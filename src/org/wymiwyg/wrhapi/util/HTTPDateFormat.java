/*
 * Created on Apr 26, 2004
 * 
 * 
 * ====================================================================
 * 
 * The WYMIWYG Software License, Version 1.0
 * 
 * Copyright (c) 2002-2003 WYMIWYG All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowlegement: "This product includes software
 * developed by WYMIWYG." Alternately, this acknowlegement may appear in the
 * software itself, if and wherever such third-party acknowlegements normally
 * appear.
 * 
 * 4. The name "WYMIWYG" or "WYMIWYG.org" must not be used to endorse or promote
 * products derived from this software without prior written permission. For
 * written permission, please contact wymiwyg@wymiwyg.org.
 * 
 * 5. Products derived from this software may not be called "WYMIWYG" nor may
 * "WYMIWYG" appear in their names without prior written permission of WYMIWYG.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL WYMIWYG OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on
 * behalf of WYMIWYG. For more information on WYMIWYG, please see
 * http://www.WYMIWYG.org/.
 * 
 * This licensed is based on The Apache Software License, Version 1.1, see
 * http://www.apache.org/.
 */
package org.wymiwyg.wrhapi.util;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
/** A Date according to section 3.1.1. of RFC2068
 * 
 * @author reto
 */
public class HTTPDateFormat extends SimpleDateFormat {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6639216920987040626L;
	
    private static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
    private static final DateFormatSymbols EN_SYMBOLS = new DateFormatSymbols(new Locale("en"));

    public HTTPDateFormat() {
    		super("EEE, dd MMM yyyy kk:mm:ss zzz",EN_SYMBOLS);
        super.setTimeZone(GMT_TIMEZONE);
    }

}