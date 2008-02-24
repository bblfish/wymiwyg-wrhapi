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
package org.wymiwyg.wrhapi.util.parameterparser;

import javax.activation.MimeTypeParseException;

import org.wymiwyg.commons.mediatypes.MimeType;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Request;

/**
 * This class provides utility method to access both GET and POST parameters.
 * 
 * @author reto
 * 
 */
public class ParameterUtil {

	/**
	 * parses the content of the request an MultiPartBody
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	public static MultiPartBody parseMultipart(Request request)
			throws HandlerException {
		try {
			return new MultiPartBodyImpl(request, new MimeType(request
					.getHeaderValues(HeaderName.CONTENT_TYPE)[0]));
		} catch (MimeTypeParseException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * parses both get-parameters in the request-uri as well as for the method
	 * POST parameters contained in the body
	 * 
	 * @param request
	 *            the parameters, parameters in the URL first
	 * @return
	 * @throws HandlerException
	 */
	public static ParameterCollection getAllParameters(Request request)
			throws HandlerException {
		ParameterCollection uriParamters = new URLEncodedParameterCollection(
				request.getRequestURI().getQuery());
		ParameterCollection postParameters = getBodyPameters(request);
		return new UnionParameterCollection(uriParamters, postParameters);
	}

	/**
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	public static ParameterCollection getBodyPameters(Request request)
			throws HandlerException {
		MimeType type;
		try {
			type = new MimeType(request
					.getHeaderValues(HeaderName.CONTENT_TYPE)[0]);
		} catch (MimeTypeParseException e) {
			type = null;
		} catch (ArrayIndexOutOfBoundsException e) {
			type = null;
		}
		if (type != null) {
			String boundary = type.getParameter("boundary");
			if (boundary != null) {
				return new MultiPartBodyImpl(request.getMessageBody(), boundary);
			}
		}
		return new URLEncodedParameterCollection(request.getMessageBody());
	}

}
