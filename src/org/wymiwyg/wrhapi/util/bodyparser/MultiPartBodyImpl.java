/*
 * Copyright  2002-2004 WYMIWYG (www.wymiwyg.org)
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
package org.wymiwyg.wrhapi.util.bodyparser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.activation.MimeTypeParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.commons.mediatypes.MimeType;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Request;

/**
 * @author reto
 * 
 */
public class MultiPartBodyImpl implements MultiPartBody {

	/**
	 * @author reto
	 * 
	 */
	public class FormFileImpl implements FormFile {

		private byte[] content;
		private String fileName;
		private MimeType type;
		/**
		 * @param fileName
		 * @param type
		 * @param content
		 */
		public FormFileImpl(String fileName, MimeType type, byte[] content) {
			this.type = type;
			this.fileName = fileName;
			this.content = content;
		}

		public byte[] getContent() {
			return content;
		}

		/* (non-Javadoc)
		 * @see org.wymiwyg.wrhapi.util.bodyparser.FormFile#getFileName()
		 */
		public String getFileName() {
			return fileName;
		}

		/* (non-Javadoc)
		 * @see org.wymiwyg.wrhapi.util.bodyparser.FormFile#getMimeType()
		 */
		public MimeType getMimeType() {
			return type;
		}

	}

	/**
	 * @author reto
	 * 
	 */
	class Disposition {

		private String fileName;

		private String name;

		/**
		 * @param dispositionString
		 * @throws IOException
		 */
		public Disposition(String dispositionString) throws IOException {
			if (dispositionString == null) {
				throw new RuntimeException(
						"No content-disposition string - contact your browser vendor");
			}
			StringTokenizer tokens = new StringTokenizer(dispositionString, ";");
			while (tokens.hasMoreTokens()) {
				String current = tokens.nextToken();
				current = current.trim();
				if (current.startsWith("name")) {
					name = readTokenValue(current);
				} else {
					if (current.startsWith("filename")) {
						fileName = readTokenValue(current);
					}
				}
			}
		}

		/**
		 * @return
		 */
		public String getFileName() {
			return fileName;
		}

		/**
		 * @return
		 */
		public String getName() {
			return name;
		}

		/**
		 * returns teh value between "
		 * 
		 * @param current
		 * @return
		 */
		private String readTokenValue(String token) throws IOException {
			StringWriter out = new StringWriter();
			StringReader in = new StringReader(token);
			boolean inValueSection = false;
			;
			for (int ch = in.read(); ch != -1; ch = in.read()) {
				// assume " cannot be escaped in names
				if (ch == '\"') {
					if (inValueSection) {
						return out.toString();
					} else {
						inValueSection = true;
					}
				} else {
					if (inValueSection) {
						out.write(ch);
					}
				}
			}
			throw new IOException("token-value not terminated with \"");
		}

	}

	protected static int maxSize = 1024 * 1024 * 50; // 50MB

	final static byte[] DOUBLE_LINE_BREAK = { 13, 10, 13, 10 };

	final static byte[] LINE_BREAK = { 13, 10 };

	final static Log log = LogFactory.getLog(MultiPartBodyImpl.class);

	List<String> allFieldNames = new ArrayList<String>();

	List<KeyValuePair<FormFile>> formFiles = new ArrayList<KeyValuePair<FormFile>>();

	List<KeyValuePair<String>> formTexts = new ArrayList<KeyValuePair<String>>();
	
	/**
	 * @param request
	 * @param type
	 * @throws HandlerException
	 */
	public MultiPartBodyImpl(Request request, MimeType type)
			throws HandlerException {
		try {
			if (Long.parseLong(request
					.getHeaderValues(HeaderName.CONTENT_LENGTH)[0]) > maxSize) {
				throw new IOException(
						"Content too long - sent files are limited in size");
			}

			String boundary = type.getParameter("boundary");

			if (boundary == null) {
				throw new IOException("boundary is not set");
			}
			InputStream rawIn;
			rawIn = Channels.newInputStream(request.getMessageBody().read());
			DelimiterInputStream in = new DelimiterInputStream(rawIn);

			ByteArrayOutputStream delimiterBaos = new ByteArrayOutputStream();
			delimiterBaos.write(45);// dash
			delimiterBaos.write(45);
			delimiterBaos.write(boundary.getBytes("utf-8"));
			byte[] delimiter = delimiterBaos.toByteArray();
			in.readTill(delimiter);
			in.read(); // 13
			in.read(); // 10
			delimiterBaos = new ByteArrayOutputStream();
			delimiterBaos.write(LINE_BREAK);
			delimiterBaos.write(delimiter);
			readFields(in, delimiterBaos.toByteArray());
			// from rfc 2616: a bare CR
			// or LF MUST NOT be substituted for CRLF within any of the HTTP
			// control
			// structures (such as header fields and multipart boundaries)
			// this means we are strict in expecting CRLF
		} catch (IOException e) {
			throw new HandlerException(e);
		}
	}

	/**
	 * @param result
	 * @param disposition
	 * @param string
	 * @param data
	 * @throws IOException
	 */
	private void addFileField(Disposition disposition, String mimeType,
			byte[] data) throws IOException {
		String name = disposition.getName();
		try {
			formFiles.add(new KeyValuePair<FormFile>(name,new FormFileImpl(disposition
					.getFileName(), new MimeType(mimeType), data)));
		} catch (MimeTypeParseException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}


	}

	/**
	 * @param result
	 * @param disposition
	 * @param data
	 */
	private void addTextField(Disposition disposition, byte[] data) {
		String name = disposition.getName();
		try {
			formTexts.add(new KeyValuePair<String>(name,new String(data, "utf-8")));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

	}

//	/**
//	 * @see org.wymiwyg.rwcf.form.MultiPartBody#getFileContents(java.lang.String)
//	 */
//	private byte[][] getFileContents(String parametername) {
//		Object[] descriptors = (Object[]) fields.get(parametername);
//		if (descriptors == null) {
//			return null;
//		}
//		byte[][] datas = new byte[descriptors.length][];
//		for (int i = 0; i < descriptors.length; i++) {
//			try {
//				datas[i] = descriptors[i].getData();
//			} catch (ClassCastException ex) {
//			}
//		}
//		return datas;
//	}

//	/**
//	 * @see org.wymiwyg.rwcf.form.MultiPartBody#getFileContentType(java.lang.String)
//	 */
//	private MimeType getFileContentType(String parametername) {
//		MimeType[] values = getFileContentTypes(parametername);
//		if (values != null) {
//			return values[0];
//		} else {
//			return null;
//		}
//	}
//
//	/**
//	 * @see org.wymiwyg.rwcf.form.MultiPartBody#getFileContentTypes(java.lang.String)
//	 */
//	private MimeType[] getFileContentTypes(String parametername) {
//		Object[] descriptors = (Object[]) fields.get(parametername);
//		if (descriptors == null) {
//			return null;
//		}
//		MimeType[] types = new MimeType[descriptors.length];
//		for (int i = 0; i < descriptors.length; i++) {
//			try {
//				types[i] = ((FormFileImpl) descriptors[i]).getMimeType();
//			} catch (ClassCastException ex) {
//			}
//		}
//		return types;
//	}
//
//	/**
//	 * @see org.wymiwyg.rwcf.form.MultiPartBody#getFileName(java.lang.String)
//	 */
//	private String getFileName(String parametername) {
//		String[] values = getFileNames(parametername);
//		if (values != null) {
//			return values[0];
//		} else {
//			return null;
//		}
//	}
//
//	/**
//	 * @see org.wymiwyg.rwcf.form.MultiPartBody#getFileNames(java.lang.String)
//	 */
//	private String[] getFileNames(String parametername) {
//		Object[] descriptors = (Object[]) fields.get(parametername);
//		if (descriptors == null) {
//			return null;
//		}
//		String[] fileNames = new String[descriptors.length];
//		for (int i = 0; i < descriptors.length; i++) {
//			try {
//				fileNames[i] = ((FormFileImpl) descriptors[i]).getFileName();
//			} catch (ClassCastException ex) {
//			}
//		}
//		return fileNames;
//	}
//
//	/**
//	 * @see org.wymiwyg.rwcf.form.MultiPartBody#getParameterNames()
//	 */
//	private String[] getParameterNames() {
//		return (String[]) fields.keySet().toArray(new String[fields.size()]);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.wymiwyg.rwcf.form.MultiPartBody#getParameterObjects(java.lang.String)
//	 */
//	private Object[] getParameterObjects(String name) {
//		return (Object[]) fields.get(name);
//	}
//
//	/**
//	 * @see org.wymiwyg.rwcf.form.MultiPartBody#getParameterValues(java.lang.String)
//	 */
//	private String[] getParameterValues(String name) {
//		Object[] elements = (Object[]) fields.get(name);
//		if (elements == null) {
//			return null;
//		}
//		String[] result = new String[elements.length];
//		for (int i = 0; i < result.length; i++) {
//			result[i] = elements[i].toString();
//		}
//		return result;
//
//	}

	/**
	 * @param in
	 * @param bs
	 * @return
	 * @throws IOException
	 * @throws IOException
	 */
	private void readFields(DelimiterInputStream in, byte[] delimiter)
			throws IOException {
		while (true) {
			Map<HeaderName, String> partHeaders;
			partHeaders = readHeaders(in);

			String dispositionString = (String) partHeaders
					.get(HeaderName.CONTENT_DISPOSITION);
			byte[] data;
			data = in.readTill(delimiter);

			Disposition disposition = new Disposition(dispositionString);
			allFieldNames.add(disposition.name);
			if (disposition.getFileName() != null) {
				addFileField(disposition, (String) partHeaders
						.get(HeaderName.CONTENT_TYPE), data);
			} else {
				addTextField(disposition, data);
			}
			byte[] twoBytes = new byte[2];
			in.read(twoBytes);
			if (twoBytes[0] == '-') {
				// the terminating two dashed (instead of CRLF
				break;
			}
		}

	}

	/**
	 * 
	 * This reads the headers, it stops after having read CRLFCRLF
	 * 
	 * @param in
	 * @return
	 * @throws
	 * @throws IOException
	 */
	private Map<HeaderName, String> readHeaders(DelimiterInputStream in) throws IOException {
		Map<HeaderName, String> result = new HashMap<HeaderName, String>();
		for (byte[] line = in.readTill(LINE_BREAK); line.length > 0; line = in
				.readTill(LINE_BREAK)) {
			String string = new String(line, "utf-8");
			int colonPos = string.indexOf(':');
			if (colonPos == -1) {
				log.error("Header conatins no colon: " + string);
				continue;
			}
			int startPos = 0;
			while ((string.charAt(startPos) == '\n')
					|| (string.charAt(startPos) == '\r')) {
				startPos++;
			}
			String headerName = string.substring(startPos, colonPos);

			String value = string.substring(colonPos + 1);
			result.put(HeaderName.get(headerName), value);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.wymiwyg.wrhapi.util.bodyparser.MultiPartBody#getFileParameterNames()
	 */
	public String[] getFileParameterNames() {
		String[] result = new String[formFiles.size()];
		int i = 0;
		for (KeyValuePair<FormFile> keyValue : formFiles) {
			result[i++] = keyValue.key;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.wymiwyg.wrhapi.util.bodyparser.MultiPartBody#getFormFileParameterValues(java.lang.String)
	 */
	public FormFile[] getFormFileParameterValues(String name) {
		List<FormFile> values = new ArrayList<FormFile>();
		for (KeyValuePair<FormFile> keyValue : formFiles) {
			if (keyValue.key.equals(name)) {
				values.add(keyValue.value);
			}
		}
		return values.toArray(new FormFile[values.size()]);
	}

	/* (non-Javadoc)
	 * @see org.wymiwyg.wrhapi.util.bodyparser.MultiPartBody#getParameterNames()
	 */
	public String[] getParameterNames() {
		return allFieldNames.toArray(new String[allFieldNames.size()]);
	}

	/* (non-Javadoc)
	 * @see org.wymiwyg.wrhapi.util.bodyparser.MultiPartBody#getTextParameterNames()
	 */
	public String[] getTextParameterNames() {
		String[] result = new String[formTexts.size()];
		int i = 0;
		for (KeyValuePair<String> keyValue : formTexts) {
			result[i++] = keyValue.key;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.wymiwyg.wrhapi.util.bodyparser.MultiPartBody#getTextParameterValues(java.lang.String)
	 */
	public String[] getTextParameterValues(String name) {
		List<String> values = new ArrayList<String>();
		for (KeyValuePair<String> keyValue : formTexts) {
			if (keyValue.key.equals(name)) {
				values.add(keyValue.value);
			}
		}
		return values.toArray(new String[values.size()]);
	}

}