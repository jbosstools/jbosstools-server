/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core.model.internal.xb;


/**
 * Unmarshaller implementation.
 * WARNING: this implementation is not thread-safe.
 *
 * Taken from JBoss XB. Changed to let getParser() be public.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 2365 $</tt>
 */
public class ArchivesUnmarshallerImpl {
//	implements Unmarshaller {
//	private ObjectModelBuilder builder = new ObjectModelBuilder();
//	private final JBossXBParser parser;
//
//	// Constructor
//
//	/**
//	 * The constructor for DTD and XSD client awareness.
//	 */
//	public ArchivesUnmarshallerImpl() throws JBossXBException {
//		parser = new SaxJBossXBParser();
//	}
//
//	public void setValidation(boolean validation) throws JBossXBException {
//		parser.setFeature(VALIDATION, validation);
//		/*
//		 * Only set DYNAMIC_VALIDATION to false. Setting this to true if its not
//		 * already requires a document to have a DOCTYPE declaring the root
//		 * element
//		 */
//		if (validation == false)
//			parser.setFeature(DYNAMIC_VALIDATION, false);
//	}
//
//	public void setSchemaValidation(boolean validation) throws JBossXBException {
//		parser.setFeature(SCHEMA_VALIDATION, validation);
//	}
//
//	public void setFeature(String feature, boolean value)
//			throws JBossXBException {
//		parser.setFeature(feature, value);
//	}
//
//	public void setNamespaceAware(boolean namespaces) throws JBossXBException {
//		parser.setFeature(NAMESPACES, namespaces);
//	}
//
//	public void setEntityResolver(EntityResolver entityResolver)
//			throws JBossXBException {
//		parser.setEntityResolver(entityResolver);
//	}
//
//	public void setErrorHandler(ErrorHandler errorHandler) {
//		// todo reader.setErrorHandler(errorHandler);
//	}
//
//	public void mapFactoryToNamespace(ObjectModelFactory factory,
//			String namespaceUri) {
//		if (builder == null) {
//			builder = new ObjectModelBuilder();
//		}
//		builder.mapFactoryToNamespace(factory, namespaceUri);
//	}
//
//	public Object unmarshal(String xmlFile) throws JBossXBException {
//		// todo
//		throw new UnsupportedOperationException();
//	}
//
//	public Object unmarshal(String xmlFile, JBossXBParser.ContentHandler handler)
//			throws JBossXBException {
//		parser.parse(xmlFile, handler);
//		return handler.getRoot();
//	}
//
//	public Object unmarshal(String xml, SchemaBinding schemaBinding)
//			throws JBossXBException {
//		JBossXBParser.ContentHandler cHandler = new SundayContentHandler(
//				schemaBinding);
//		parser.parse(xml, cHandler);
//		return cHandler.getRoot();
//	}
//
//	public Object unmarshal(Reader xmlReader, SchemaBinding schemaBinding)
//			throws JBossXBException {
//		JBossXBParser.ContentHandler cHandler = new SundayContentHandler(
//				schemaBinding);
//		parser.parse(xmlReader, cHandler);
//		return cHandler.getRoot();
//	}
//
//	public Object unmarshal(InputStream xmlStream, SchemaBinding schemaBinding)
//			throws JBossXBException {
//		JBossXBParser.ContentHandler cHandler = new SundayContentHandler(
//				schemaBinding);
//		parser.parse(xmlStream, cHandler);
//		return cHandler.getRoot();
//	}
//
//	public Object unmarshal(String xml, SchemaBindingResolver schemaResolver)
//			throws JBossXBException {
//		JBossXBParser.ContentHandler cHandler = new SundayContentHandler(
//				schemaResolver);
//		parser.parse(xml, cHandler);
//		return cHandler.getRoot();
//	}
//
//	public Object unmarshal(Reader xmlReader,
//			SchemaBindingResolver schemaResolver) throws JBossXBException {
//		JBossXBParser.ContentHandler cHandler = new SundayContentHandler(
//				schemaResolver);
//		parser.parse(xmlReader, cHandler);
//		return cHandler.getRoot();
//	}
//
//	public Object unmarshal(InputStream xmlStream,
//			SchemaBindingResolver schemaResolver) throws JBossXBException {
//		JBossXBParser.ContentHandler cHandler = new SundayContentHandler(
//				schemaResolver);
//		parser.parse(xmlStream, cHandler);
//		return cHandler.getRoot();
//	}
//
//	public Object unmarshal(Reader reader, ObjectModelFactory factory,
//			Object root) throws JBossXBException {
//		if (builder == null) {
//			builder = new ObjectModelBuilder();
//		}
//		builder.init(factory, root);
//		parser.parse(reader, builder);
//		return builder.getRoot();
//	}
//
//	public Object unmarshal(InputStream is, ObjectModelFactory factory,
//			Object root) throws JBossXBException {
//		if (builder == null) {
//			builder = new ObjectModelBuilder();
//		}
//		builder.init(factory, root);
//		parser.parse(is, builder);
//		return builder.getRoot();
//	}
//
//	public Object unmarshal(String systemId, ObjectModelFactory factory,
//			Object root) throws JBossXBException {
//		if (builder == null) {
//			builder = new ObjectModelBuilder();
//		}
//		builder.init(factory, root);
//		parser.parse(systemId, builder);
//		return builder.getRoot();
//	}
//
//	public Object unmarshal(String systemId, ObjectModelFactory factory,
//			DocumentBinding binding) throws JBossXBException {
//		if (binding != null) {
//			throw new IllegalStateException(
//					"DocumentBinding API is not supported anymore!"); //$NON-NLS-1$
//		}
//		return unmarshal(systemId, factory, (Object) null);
//	}
//
//	public Object unmarshal(Reader reader, ObjectModelFactory factory,
//			DocumentBinding binding) throws JBossXBException {
//		if (binding != null) {
//			throw new IllegalStateException(
//					"DocumentBinding API is not supported anymore!"); //$NON-NLS-1$
//		}
//		return unmarshal(reader, factory, (Object) null);
//	}
//
//	public JBossXBParser getParser() {
//		return parser;
//	}
}
