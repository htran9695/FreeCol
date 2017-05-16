/**
 *  Copyright (C) 2002-2015   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A class that makes it slightly tidier to create, parse and format XML
 * documents.
 */
public final class Xml {

	// ------------------------------------------------------ class API methods

	/**
	 * New document.
	 *
	 * @return the document
	 */
	public static Document newDocument() {

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (ParserConfigurationException e) {
			throw new Exception(e);
		}
	}

	/**
	 * Document from.
	 *
	 * @param string
	 *            the string
	 * @return the document
	 */
	public static Document documentFrom(String string) {

		return documentFrom(new InputSource(new StringReader(string)));
	}

	/**
	 * Document from.
	 *
	 * @param stream
	 *            the stream
	 * @return the document
	 */
	public static Document documentFrom(InputStream stream) {

		return documentFrom(new InputSource(stream));
	}

	/**
	 * To string.
	 *
	 * @param document
	 *            the document
	 * @return the string
	 */
	public static String toString(Document document) {

		return document.getDocumentElement().toString();
	}

	/**
	 * Checks for attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @return true, if successful
	 */
	public static boolean hasAttribute(Node xmlElement, String attributeName) {

		return xmlElement.getAttributes().getNamedItem(attributeName) != null;
	}

	/**
	 * Attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @return the string
	 */
	public static String attribute(Node xmlElement, String attributeName) {

		return xmlElement.getAttributes().getNamedItem(attributeName).getNodeValue();
	}

	/**
	 * Attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param otherwise
	 *            the otherwise
	 * @return the string
	 */
	public static String attribute(Node xmlElement, String attributeName, String otherwise) {
		if (hasAttribute(xmlElement, attributeName)) {
			return attribute(xmlElement, attributeName);
		} else {
			return otherwise;
		}
	}

	/**
	 * Array attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param separator
	 *            the separator
	 * @return the string[]
	 */
	public static String[] arrayAttribute(Node xmlElement, String attributeName, String separator) {

		return attribute(xmlElement, attributeName).split(separator);
	}

	/**
	 * Array attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @return the string[]
	 */
	public static String[] arrayAttribute(Node xmlElement, String attributeName) {

		return arrayAttribute(xmlElement, attributeName, ",");
	}

	/**
	 * Array attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param otherwise
	 *            the otherwise
	 * @return the string[]
	 */
	public static String[] arrayAttribute(Node xmlElement, String attributeName, String[] otherwise) {
		if (hasAttribute(xmlElement, attributeName)) {
			return arrayAttribute(xmlElement, attributeName, ",");
		} else {
			return otherwise;
		}
	}

	/**
	 * Char attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @return the char
	 */
	public static char charAttribute(Node xmlElement, String attributeName) {

		return attribute(xmlElement, attributeName).charAt(0);
	}

	/**
	 * Char attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param otherwise
	 *            the otherwise
	 * @return the char
	 */
	public static char charAttribute(Node xmlElement, String attributeName, char otherwise) {
		if (hasAttribute(xmlElement, attributeName)) {
			return charAttribute(xmlElement, attributeName);
		} else {
			return otherwise;
		}
	}

	/*
	 * public static String messageAttribute( Node xmlElement, String
	 * attributeName ) {
	 * 
	 * return Messages.message( attribute(xmlElement, attributeName) ); }
	 */

	/**
	 * Float attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @return the float
	 */
	public static float floatAttribute(Node xmlElement, String attributeName) {

		return Float.parseFloat(attribute(xmlElement, attributeName));
	}

	/**
	 * Float attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param otherwise
	 *            the otherwise
	 * @return the float
	 */
	public static float floatAttribute(Node xmlElement, String attributeName, float otherwise) {
		if (hasAttribute(xmlElement, attributeName)) {
			return floatAttribute(xmlElement, attributeName);
		} else {
			return otherwise;
		}
	}

	/**
	 * Float array attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param separator
	 *            the separator
	 * @return the float[]
	 */
	public static float[] floatArrayAttribute(Node xmlElement, String attributeName, String separator) {
		String[] array = arrayAttribute(xmlElement, attributeName, separator);
		float[] output = new float[array.length];
		for (int i = 0; i < array.length; i++) {
			output[i] = Float.parseFloat(array[i]);
		}
		return output;
	}

	/**
	 * Float array attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @return the float[]
	 */
	public static float[] floatArrayAttribute(Node xmlElement, String attributeName) {

		return floatArrayAttribute(xmlElement, attributeName, ",");
	}

	/**
	 * Float array attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param otherwise
	 *            the otherwise
	 * @return the float[]
	 */
	public static float[] floatArrayAttribute(Node xmlElement, String attributeName, float[] otherwise) {
		if (hasAttribute(xmlElement, attributeName)) {
			return floatArrayAttribute(xmlElement, attributeName, ",");
		} else {
			return otherwise;
		}
	}

	/**
	 * Int attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @return the int
	 */
	public static int intAttribute(Node xmlElement, String attributeName) {

		return Integer.parseInt(attribute(xmlElement, attributeName));
	}

	/**
	 * Int attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param otherwise
	 *            the otherwise
	 * @return the int
	 */
	public static int intAttribute(Node xmlElement, String attributeName, int otherwise) {
		if (hasAttribute(xmlElement, attributeName)) {
			return intAttribute(xmlElement, attributeName);
		} else {
			return otherwise;
		}
	}

	/**
	 * Int array attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param separator
	 *            the separator
	 * @return the int[]
	 */
	public static int[] intArrayAttribute(Node xmlElement, String attributeName, String separator) {
		String[] array = arrayAttribute(xmlElement, attributeName, separator);
		/*
		 * For testing for (int k = 0; k < array.length; k++)
		 * logger.info(array[k]);
		 */
		int[] output = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			output[i] = Integer.parseInt(array[i]);
		}
		return output;
	}

	/**
	 * Int array attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @return the int[]
	 */
	public static int[] intArrayAttribute(Node xmlElement, String attributeName) {

		return intArrayAttribute(xmlElement, attributeName, ",");
	}

	/**
	 * Int array attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param otherwise
	 *            the otherwise
	 * @return the int[]
	 */
	public static int[] intArrayAttribute(Node xmlElement, String attributeName, int[] otherwise) {
		if (hasAttribute(xmlElement, attributeName)) {
			return intArrayAttribute(xmlElement, attributeName, ",");
		} else {
			return otherwise;
		}
	}

	/**
	 * Boolean attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @return true, if successful
	 */
	public static boolean booleanAttribute(Node xmlElement, String attributeName) {

		return parseTruth(attribute(xmlElement, attributeName));
	}

	/**
	 * Boolean attribute.
	 *
	 * @param xmlElement
	 *            the xml element
	 * @param attributeName
	 *            the attribute name
	 * @param otherwise
	 *            the otherwise
	 * @return true, if successful
	 */
	public static boolean booleanAttribute(Node xmlElement, String attributeName, boolean otherwise) {
		if (hasAttribute(xmlElement, attributeName)) {
			return booleanAttribute(xmlElement, attributeName);
		} else {
			return otherwise;
		}
	}

	/**
	 * For each child.
	 *
	 * @param xml
	 *            the xml
	 * @param method
	 *            the method
	 */
	public static void forEachChild(Node xml, Method method) {

		NodeList childList = xml.getChildNodes();

		for (int ci = 0, nc = childList.getLength(); ci < nc; ci++) {
			Node child = childList.item(ci);

			if (child instanceof Element) {

				method.invokeOn(child);
			}
		}
	}

	// -------------------------------------------------- class support methods

	/**
	 * Document from.
	 *
	 * @param source
	 *            the source
	 * @return the document
	 */
	private static Document documentFrom(InputSource source) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(source);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new Exception(e);
		}
	}

	/**
	 * Parses the truth.
	 *
	 * @param truthAsString
	 *            the truth as string
	 * @return true, if successful
	 */
	private static boolean parseTruth(String truthAsString) {
		if (null != truthAsString)
			switch (truthAsString) {
			case "yes":
			case "true":
				return true;
			case "no":
				return false;
			}
		throw new RuntimeException("mus be 'yes' or 'no': " + truthAsString);
	}

	// ----------------------------------------------------------- constructors

	/**
	 * Instantiates a new xml.
	 */
	private Xml() {
	}

	// ----------------------------------------------------------- nested types

	/**
	 * The Interface Method.
	 */
	public interface Method {

		/**
		 * Invoke on.
		 *
		 * @param xml
		 *            the xml
		 */
		public void invokeOn(Node xml);
	}

	/**
	 * This class is defined so that exceptions thrown by methods on {@link Xml}
	 * may be filtered from other runtime exceptions such as
	 * {@link NullPointerException} if desired. In the majority of cases, an
	 * exception will mean that the game cannot proceed, which is why this is a
	 * runtime exception so that it percolates up to the {@link Thread#run()}
	 * method of the invoking thread or other controlling loop without the need
	 * for every other method on the way to declare "throws" clauses.
	 */
	public static final class Exception extends RuntimeException {

		/**
		 * Instantiates a new exception.
		 *
		 * @param cause
		 *            the cause
		 */
		Exception(Throwable cause) {

			super(cause);
		}
	}

}
