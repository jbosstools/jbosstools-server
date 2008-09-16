/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package org.jboss.ide.eclipse.archives.core.model.internal.xb;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.jboss.xb.binding.AbstractMarshaller;
import org.jboss.xb.binding.AttributesImpl;
import org.jboss.xb.binding.Constants;
import org.jboss.xb.binding.Content;
import org.jboss.xb.binding.ContentWriter;
import org.jboss.xb.binding.DelegatingObjectModelProvider;
import org.jboss.xb.binding.GenericObjectModelProvider;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.xb.binding.Marshaller;
import org.jboss.xb.binding.MarshallingContext;
import org.jboss.xb.binding.NamespaceRegistry;
import org.jboss.xb.binding.ObjectLocalMarshaller;
import org.jboss.xb.binding.ObjectModelProvider;
import org.jboss.xb.binding.SimpleTypeBindings;
import org.jboss.xb.binding.Util;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1958 $</tt>
 */
public class StrictXercesXSMarshaller
   extends AbstractMarshaller
{
   private Stack stack = new StackImpl();

   /**
    * ObjectModelProvider for this marshaller
    */
   private GenericObjectModelProvider provider;

   private Object root;

   /**
    * Whether NULL values should be ignored or marshalled as xsi:nil='1'
    */
   private boolean supportNil = true;

   private QName rootTypeQName;

   private SchemaBindingResolver schemaResolver;

   private XSModel model;

   private boolean ignoreUnresolvedWildcard;

   private XSAttributeUse currentAttribute;
   private XSTypeDefinition currentElementType;

   private String simpleContentProperty = "value";
   
   private MarshallingContext ctx = new MarshallingContext()
   {
      private ContentHandler ch;

      public boolean isAttributeRequired()
      {
         if(currentAttribute == null)
         {
            throw new JBossXBRuntimeException("There is no current attribute!");
         }
         return currentAttribute.getRequired();
      }

      public boolean isTypeComplex()
      {
         if(currentElementType == null)
         {
            throw new JBossXBRuntimeException("There is no current element!");
         }
         return currentElementType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE;
      }

      public String getSimpleContentProperty()
      {
         return simpleContentProperty;
      }

      public ContentHandler getContentHandler()
      {
         if(ch == null)
         {
            ch = new ContentHandlerAdaptor();
         }
         return ch;
      }

      public NamespaceRegistry getNamespaceContext()
      {
         return nsRegistry;
      }
   };

   public String getSimpleContentProperty()
   {
      return simpleContentProperty;
   }

   public void setSimpleContentProperty(String simpleContentProperty)
   {
      this.simpleContentProperty = simpleContentProperty;
   }

   public boolean isIgnoreUnresolvedWildcard()
   {
      return ignoreUnresolvedWildcard;
   }

   public void setIgnoreUnresolvedWildcard(boolean ignoreUnresolvedWildcard)
   {
      this.ignoreUnresolvedWildcard = ignoreUnresolvedWildcard;
   }

   public SchemaBindingResolver getSchemaResolver()
   {
      return schemaResolver;
   }

   public void setSchemaResolver(SchemaBindingResolver schemaResolver)
   {
      this.schemaResolver = schemaResolver;
   }

   public QName getRootTypeQName()
   {
      return rootTypeQName;
   }

   public void setRootTypeQName(QName rootTypeQName)
   {
      this.rootTypeQName = rootTypeQName;
   }

   public boolean isSupportNil()
   {
      return supportNil;
   }

   public void setSupportNil(boolean supportNil)
   {
      this.supportNil = supportNil;
   }

   /**
    * Adds an attribute to the top most elements.
    * First, we check whether there is a namespace associated with the passed in prefix.
    * If the prefix was not declared, an exception is thrown.
    *
    * @param prefix    the prefix of the attribute to be declared
    * @param localName local name of the attribute
    * @param type      the type of the attribute
    * @param value     the value of the attribute
    */
   public void addAttribute(String prefix, String localName, String type, String value)
   {
      // todo addAttribute(String prefix, String localName, String type, String value)
   }

   // AbstractMarshaller implementation

   public void marshal(Reader xsdReader, ObjectModelProvider provider, Object root, Writer writer)
      throws IOException, SAXException, ParserConfigurationException
   {
      XSModel model = Util.loadSchema(xsdReader, null, schemaResolver);
      marshallInternal(provider, root, model, writer);
   }

   public void marshal(String xsdURL, ObjectModelProvider provider, Object root, Writer writer) throws IOException,
      SAXException
   {
      XSModel model = Util.loadSchema(xsdURL, schemaResolver);
      marshallInternal(provider, root, model, writer);
   }

   public void marshal(XSModel model, ObjectModelProvider provider, Object root, Writer writer) throws IOException,
      SAXException
   {
      marshallInternal(provider, root, model, writer);
   }

   private void marshallInternal(ObjectModelProvider provider, Object root, XSModel model, Writer writer)
      throws IOException, SAXException
   {
      if(model == null)
      {
         throw new JBossXBRuntimeException("XSModel is not available!");
      }

      this.model = model;
      this.provider = provider instanceof GenericObjectModelProvider ?
         (GenericObjectModelProvider)provider : new DelegatingObjectModelProvider(provider);

      this.root = root;

      content.startDocument();

      if(rootTypeQName != null)
      {
         if(rootQNames.isEmpty())
         {
            throw new JBossXBRuntimeException("If type name (" +
               rootTypeQName +
               ") for the root element is specified then the name for the root element is required!"
            );
         }
         QName rootQName = (QName)rootQNames.get(0);

         XSTypeDefinition type = model.getTypeDefinition(rootTypeQName.getLocalPart(),
            rootTypeQName.getNamespaceURI()
         );
         if(type == null)
         {
            throw new JBossXBRuntimeException("Global type definition is not found: " + rootTypeQName);
         }

         if(isArrayWrapper(type))
         {
            Object o = provider.getRoot(root, null, rootQName.getNamespaceURI(), rootQName.getLocalPart());
            stack.push(o);
            marshalComplexType(rootQName.getNamespaceURI(),
               rootQName.getLocalPart(),
               (XSComplexTypeDefinition)type,
               true,
               false
            );
            stack.pop();
         }
         else
         {
            Object o = provider.getRoot(root, null, rootQName.getNamespaceURI(), rootQName.getLocalPart());
            marshalElementOccurence(rootQName.getNamespaceURI(),
               rootQName.getLocalPart(),
               type,
               o,
               false,
               false,
               true
            );
         }
      }
      else if(rootQNames.isEmpty())
      {
         XSNamedMap components = model.getComponents(XSConstants.ELEMENT_DECLARATION);
         if(components.getLength() == 0)
         {
            throw new JBossXBRuntimeException("The schema doesn't contain global element declarations.");
         }

         for(int i = 0; i < components.getLength(); ++i)
         {
            XSElementDeclaration element = (XSElementDeclaration)components.item(i);
            Object o = provider.getRoot(root, null, element.getNamespace(), element.getName());
            marshalElementOccurence(element.getNamespace(),
               element.getName(),
               element.getTypeDefinition(),
               o,
               element.getNillable(),
               false,
               true
            );
         }
      }
      else
      {
         for(int i = 0; i < rootQNames.size(); ++i)
         {
            QName qName = (QName)rootQNames.get(i);
            XSElementDeclaration element = model.getElementDeclaration(qName.getLocalPart(), qName.getNamespaceURI());
            if(element == null)
            {
               XSNamedMap components = model.getComponents(XSConstants.ELEMENT_DECLARATION);
               String roots = "";
               for(int j = 0; j < components.getLength(); ++j)
               {
                  XSObject xsObject = components.item(j);
                  if(j > 0)
                  {
                     roots += ", ";
                  }
                  roots += "{" + xsObject.getNamespace() + "}" + xsObject.getName();
               }
               throw new IllegalStateException("Root element not found: " + qName + " among " + roots);
            }

            Object o = provider.getRoot(root, null, element.getNamespace(), element.getName());
            marshalElementOccurence(element.getNamespace(),
               element.getName(),
               element.getTypeDefinition(),
               o,
               element.getNillable(),
               false,
               true
            );
         }
      }

      content.endDocument();

      // version & encoding
      writeXmlVersion(writer);

      ContentWriter contentWriter = new ContentWriter(writer,
         propertyIsTrueOrNotSet(Marshaller.PROP_OUTPUT_INDENTATION)
      );
      content.handleContent(contentWriter);

      if(log.isTraceEnabled())
      {
         java.io.StringWriter traceWriter = new java.io.StringWriter();
         contentWriter = new ContentWriter(traceWriter,
            propertyIsTrueOrNotSet(Marshaller.PROP_OUTPUT_INDENTATION)
         );
         content.handleContent(contentWriter);
         log.trace("marshalled:\n" + traceWriter.getBuffer().toString());
      }
   }

   private boolean marshalElement(String elementNs, String elementLocal,
                                  XSTypeDefinition type,
                                  boolean optional,
                                  boolean nillable,
                                  boolean declareNs,
                                  boolean declareXsiType)
   {
      Object value = stack.peek();
      boolean result = value != null || value == null && (optional || nillable);
      boolean trace = log.isTraceEnabled() && result;
      if(trace)
      {
         String prefix = getPrefix(elementNs);
         log.trace("started element ns=" + elementNs + ", local=" + elementLocal + ", prefix=" + prefix);
      }

      if(value != null)
      {
         marshalElementType(elementNs, elementLocal, type, declareNs, nillable, declareXsiType);
      }
      else if(nillable)
      {
         writeNillable(elementNs, elementLocal, nillable);
      }

      if(trace)
      {
         log.trace("finished element ns=" + elementNs + ", local=" + elementLocal);
      }

      return result;
   }

   private void marshalElementType(String elementNs,
                                   String elementLocal,
                                   XSTypeDefinition type,
                                   boolean declareNs,
                                   boolean nillable,
                                   boolean declareXsiType)
   {
      switch(type.getTypeCategory())
      {
         case XSTypeDefinition.SIMPLE_TYPE:
            marshalSimpleType(elementNs,
               elementLocal,
               (XSSimpleTypeDefinition)type,
               declareNs,
               nillable,
               declareXsiType
            );
            break;
         case XSTypeDefinition.COMPLEX_TYPE:
            marshalComplexType(elementNs, elementLocal, (XSComplexTypeDefinition)type, declareNs, declareXsiType);
            break;
         default:
            throw new IllegalStateException("Unexpected type category: " + type.getTypeCategory());
      }
   }

   private void marshalSimpleType(String elementUri,
                                  String elementLocal,
                                  XSSimpleTypeDefinition type,
                                  boolean declareNs,
                                  boolean nillable,
                                  boolean declareXsiType)
   {
      Object value = stack.peek();
      if(value != null)
      {
         String prefix = getPrefix(elementUri);
         boolean genPrefix = prefix == null && elementUri != null && elementUri.length() > 0;
         if(genPrefix)
         {
            prefix = "ns_" + elementLocal;
         }

         AttributesImpl attrs = null;
         String typeName = type.getName();
         if(SimpleTypeBindings.XS_QNAME_NAME.equals(typeName) ||
            SimpleTypeBindings.XS_NOTATION_NAME.equals(typeName) ||
            type.getItemType() != null &&
            (SimpleTypeBindings.XS_QNAME_NAME.equals(type.getItemType().getName()) ||
            SimpleTypeBindings.XS_NOTATION_NAME.equals(type.getItemType().getName())
            )
         )
         {
            attrs = new AttributesImpl(5);
         }

         String marshalled = marshalCharacters(elementUri, prefix, type, value, attrs);

         if((declareNs || declareXsiType) && nsRegistry.size() > 0)
         {
            if(attrs == null)
            {
               attrs = new AttributesImpl(nsRegistry.size() + 1);
            }
            declareNs(attrs);
         }

         if(declareXsiType)
         {
            declareXsiType(type, attrs);
         }

         if(genPrefix)
         {
            if(attrs == null)
            {
               attrs = new AttributesImpl(1);
            }
            attrs.add(null, prefix, "xmlns:" + prefix, null, (String)elementUri);
         }

         String qName = prefixLocalName(prefix, elementLocal);

         content.startElement(elementUri, elementLocal, qName, attrs);
         content.characters(marshalled.toCharArray(), 0, marshalled.length());
         content.endElement(elementUri, elementLocal, qName);
      }
      else
      {
         writeNillable(elementUri, elementLocal, nillable);
      }
   }

   private void marshalComplexType(String elementNsUri,
                                   String elementLocalName,
                                   XSComplexTypeDefinition type,
                                   boolean declareNs,
                                   boolean declareXsiType)
   {
      Object o = stack.peek();
      XSParticle particle = type.getParticle();

      XSObjectList attributeUses = type.getAttributeUses();
      int attrsTotal = declareNs || declareXsiType ?
         nsRegistry.size() + attributeUses.getLength() + 1 :
         attributeUses.getLength();
      AttributesImpl attrs = attrsTotal > 0 ? new AttributesImpl(attrsTotal) : null;

      if(declareNs && nsRegistry.size() > 0)
      {
         declareNs(attrs);
      }

      String generatedPrefix = null;
      if(declareXsiType)
      {
         generatedPrefix = declareXsiType(type, attrs);
         if(generatedPrefix != null)
         {
            String typeNsWithGeneratedPrefix = type.getNamespace();
            declareNs(attrs, generatedPrefix, typeNsWithGeneratedPrefix);
            declareNamespace(generatedPrefix, typeNsWithGeneratedPrefix);
         }
      }

      String prefix = getPrefix(elementNsUri);
      boolean genPrefix = prefix == null && elementNsUri != null && elementNsUri.length() > 0;
      if(genPrefix)
      {
         // todo: it's possible that the generated prefix already mapped. this should be fixed
         prefix = "ns_" + elementLocalName;
         declareNamespace(prefix, elementNsUri);
         if(attrs == null)
         {
            attrs = new AttributesImpl(1);
         }
         attrs.add(null, prefix, "xmlns:" + prefix, null, elementNsUri);
      }

      for(int i = 0; i < attributeUses.getLength(); ++i)
      {
         currentAttribute = (XSAttributeUse)attributeUses.item(i);
         XSAttributeDeclaration attrDec = currentAttribute.getAttrDeclaration();
         String attrNs = attrDec.getNamespace();
         String attrLocal = attrDec.getName();
         Object attrValue = provider.getAttributeValue(o, ctx, attrNs, attrLocal);

         if(attrValue != null)
         {
            if(attrs == null)
            {
               attrs = new AttributesImpl(5);
            }

            String attrPrefix = null;
            if(attrNs != null)
            {
               attrPrefix = getPrefix(attrNs);
               if(attrPrefix == null && attrNs != null && attrNs.length() > 0)
               {
                  attrPrefix = "ns_" + attrLocal;
                  attrs.add(null, attrPrefix, "xmlns:" + attrPrefix, null, attrNs);
               }
            }

            String qName = attrPrefix == null || attrPrefix.length() == 0 ? attrLocal : attrPrefix + ":" + attrLocal;

            // todo: this is a quick fix for boolean pattern (0|1 or true|false) should be refactored
            XSSimpleTypeDefinition attrType = attrDec.getTypeDefinition();
            if(attrType.getItemType() != null)
            {
               XSSimpleTypeDefinition itemType = attrType.getItemType();
               if(Constants.NS_XML_SCHEMA.equals(itemType.getNamespace()))
               {
                  List list;
                  if(attrValue instanceof List)
                  {
                     list = (List)attrValue;
                  }
                  else if(attrValue.getClass().isArray())
                  {
                     list = Arrays.asList((Object[])attrValue);
                  }
                  else
                  {
                     throw new JBossXBRuntimeException("Expected value for list type is an array or " +
                        List.class.getName() +
                        " but got: " +
                        attrValue
                     );
                  }

                  if(Constants.QNAME_QNAME.getLocalPart().equals(itemType.getName()))
                  {
                     for(int listInd = 0; listInd < list.size(); ++listInd)
                     {
                        QName item = (QName)list.get(listInd);
                        String itemNs = item.getNamespaceURI();
                        if(itemNs != null && itemNs.length() > 0)
                        {
                           String itemPrefix;
                           if(itemNs.equals(elementNsUri))
                           {
                              itemPrefix = prefix;
                           }
                           else
                           {
                              itemPrefix = getPrefix(itemNs);
                              if(itemPrefix == null)
                              {
                                 itemPrefix = attrLocal + listInd;
                                 declareNs(attrs, itemPrefix, itemNs);
                              }
                           }
                           item = new QName(item.getNamespaceURI(), item.getLocalPart(), itemPrefix);
                           list.set(listInd, item);
                        }
                     }
                  }

                  attrValue = SimpleTypeBindings.marshalList(itemType.getName(), list, null);
               }
               else
               {
                  throw new JBossXBRuntimeException("Marshalling of list types with item types not from " +
                     Constants.NS_XML_SCHEMA + " is not supported."
                  );
               }
            }
            else if(attrType.getLexicalPattern().item(0) != null
               &&
               attrType.derivedFrom(Constants.NS_XML_SCHEMA,
                  Constants.QNAME_BOOLEAN.getLocalPart(),
                  XSConstants.DERIVATION_RESTRICTION
               ))
            {
               String item = attrType.getLexicalPattern().item(0);
               if(item.indexOf('0') != -1 && item.indexOf('1') != -1)
               {
                  attrValue = ((Boolean)attrValue).booleanValue() ? "1" : "0";
               }
               else
               {
                  attrValue = ((Boolean)attrValue).booleanValue() ? "true" : "false";
               }
            }
            else if(Constants.QNAME_QNAME.getNamespaceURI().equals(attrType.getNamespace()) &&
               Constants.QNAME_QNAME.getLocalPart().equals(attrType.getName()))
            {
               QName qNameValue = (QName)attrValue;

               String qNamePrefix = null;
               boolean declarePrefix = false;
               String ns = qNameValue.getNamespaceURI();
               if(ns != null && ns.length() > 0)
               {
                  qNamePrefix = getPrefix(ns);
                  if(qNamePrefix == null)
                  {
                     qNamePrefix = qNameValue.getPrefix();
                     if(qNamePrefix == null || qNamePrefix.length() == 0)
                     {
                        qNamePrefix = "ns_" + qNameValue.getLocalPart();
                     }
                     declareNs(attrs, qNamePrefix, ns);
                     nsRegistry.addPrefixMapping(qNamePrefix, ns);
                     declarePrefix = true;
                  }
               }

               attrValue = SimpleTypeBindings.marshalQName(qNameValue, nsRegistry);

               if(declarePrefix)
               {
                  nsRegistry.removePrefixMapping(qNamePrefix);
               }
            }
            else
            {
               attrValue = attrValue.toString();
            }

            attrs.add(attrNs,
               attrLocal,
               qName,
               attrDec.getTypeDefinition().getName(),
               attrValue.toString()
            );
         } // end if attrValue != null 
         else if( currentAttribute.getRequired()){
        	 // its required and is not present. Must throw exception
        	 String name = currentAttribute.getAttrDeclaration().getName();
        	 throw new JBossXBRuntimeException("Required Attribute " + name + " is not present");
         }
      }
      currentAttribute = null;

      String characters = null;
      if(type.getSimpleType() != null)
      {
         Object value = getSimpleContentValue(elementNsUri, elementLocalName, type);
         if(value != null)
         {
            XSSimpleTypeDefinition simpleType = type.getSimpleType();
            String typeName = simpleType.getName();
            if(attrs == null && (SimpleTypeBindings.XS_QNAME_NAME.equals(typeName) ||
               SimpleTypeBindings.XS_NOTATION_NAME.equals(typeName) ||
               simpleType.getItemType() != null &&
               (SimpleTypeBindings.XS_QNAME_NAME.equals(simpleType.getItemType().getName()) ||
               SimpleTypeBindings.XS_NOTATION_NAME.equals(simpleType.getItemType().getName())
               )
               )
            )
            {
               attrs = new AttributesImpl(5);
            }

            characters = marshalCharacters(elementNsUri, prefix, simpleType, value, attrs);
         }
      }

      String qName = prefixLocalName(prefix, elementLocalName);
      content.startElement(elementNsUri, elementLocalName, qName, attrs);

      if(particle != null)
      {
         marshalParticle(particle, false);
      }

      if(characters != null)
      {
         content.characters(characters.toCharArray(), 0, characters.length());
      }
      content.endElement(elementNsUri, elementLocalName, qName);

      if(genPrefix)
      {
         removePrefixMapping(prefix);
      }

      if(generatedPrefix != null)
      {
         removePrefixMapping(generatedPrefix);
      }
   }

   private boolean marshalParticle(XSParticle particle, boolean declareNs)
   {
      boolean marshalled;
      XSTerm term = particle.getTerm();
      Object o;
      Iterator i;
      switch(term.getType())
      {
         case XSConstants.MODEL_GROUP:
            o = stack.peek();
            i = o != null && isRepeatable(particle) ? getIterator(o) : null;
            if(i != null)
            {
               marshalled = true;
               while(i.hasNext() && marshalled)
               {
                  Object value = i.next();
                  stack.push(value);
                  marshalled = marshalModelGroup(particle, declareNs);
                  stack.pop();
               }
            }
            else
            {
               marshalled = marshalModelGroup(particle, declareNs);
            }
            break;
         case XSConstants.WILDCARD:
            o = stack.peek();

            boolean popWildcardValue = false;
            ObjectLocalMarshaller marshaller = null;
            FieldToWildcardMapping mapping = (FieldToWildcardMapping)field2WildcardMap.get(o.getClass());
            if(mapping != null)
            {
               marshaller = mapping.marshaller;
               o = mapping.fieldInfo.getValue(o);
               stack.push(o);
               popWildcardValue = true;
            }

            i = o != null && isRepeatable(particle) ? getIterator(o) : null;
            if(i != null)
            {
               marshalled = true;
               while(i.hasNext() && marshalled)
               {
                  Object value = i.next();
                  marshalled = marshalWildcardOccurence(particle, marshaller, value, declareNs);
               }
            }
            else
            {
               marshalled = marshalWildcardOccurence(particle, marshaller, o, declareNs);
            }

            if(popWildcardValue)
            {
               stack.pop();
            }

            break;
         case XSConstants.ELEMENT_DECLARATION:
            XSElementDeclaration element = (XSElementDeclaration)term;
            XSTypeDefinition type = element.getTypeDefinition();
            o = getElementValue(element.getNamespace(), element.getName(), type);

            i = o != null && isRepeatable(particle) ? getIterator(o) : null;
            if(i != null)
            {
               marshalled = true;
               while(i.hasNext() && marshalled)
               {
                  Object value = i.next();
                  marshalled =
                     marshalElementOccurence(element.getNamespace(),
                        element.getName(),
                        type,
                        value,
                        element.getNillable(),
                        particle.getMinOccurs() == 0,
                        declareNs
                     );
               }
            }
            else
            {
               marshalled =
                  marshalElementOccurence(element.getNamespace(),
                     element.getName(),
                     type,
                     o,
                     element.getNillable(),
                     particle.getMinOccurs() == 0,
                     declareNs
                  );
            }
            break;
         default:
            throw new IllegalStateException("Unexpected term type: " + term.getType());
      }
      return marshalled;
   }

   private boolean marshalElementOccurence(String elementNs,
                                           String elementLocal,
                                           XSTypeDefinition type,
                                           Object value,
                                           boolean nillable,
                                           boolean optional,
                                           boolean declareNs)
   {
      boolean declareXsiType = false;
      QName xsiTypeQName = null;
      if(value != null)
      {
         xsiTypeQName = (QName)cls2TypeMap.get(value.getClass());
         if(xsiTypeQName != null &&
            !(type.getName().equals(xsiTypeQName.getLocalPart()) &&
            type.getNamespace().equals(xsiTypeQName.getNamespaceURI())
            ))
         {
            declareXsiType = true;
            if(log.isTraceEnabled())
            {
               log.trace(value.getClass() + " is mapped to xsi:type " + xsiTypeQName);
            }

            XSTypeDefinition xsiType = model.getTypeDefinition(xsiTypeQName.getLocalPart(),
               xsiTypeQName.getNamespaceURI()
            );

            if(xsiType == null)
            {
               log.warn("Class " +
                  value.getClass() +
                  " is mapped to type " +
                  xsiTypeQName +
                  " but the type is not found in schema."
               );
            }
            // todo should check derivation also, i.e. if(xsiType.derivedFrom())
            else
            {
               type = xsiType;
            }
         }
      }

      stack.push(value);
      boolean marshalled = marshalElement(elementNs,
         elementLocal,
         type,
         optional,
         nillable,
         declareNs,
         declareXsiType
      );
      stack.pop();

      return marshalled;
   }

   private boolean marshalWildcardOccurence(XSParticle particle,
                                            ObjectLocalMarshaller marshaller,
                                            Object value,
                                            boolean declareNs)
   {
      boolean marshalled = true;
      if(marshaller != null)
      {
         marshaller.marshal(ctx, value);
      }
      else
      {
         stack.push(value);
         marshalled = marshalWildcard(particle, declareNs);
         stack.pop();
      }
      return marshalled;
   }

   private boolean marshalWildcard(XSParticle particle, boolean declareNs)
   {
      XSWildcard wildcard = (XSWildcard)particle.getTerm();
      Object o = stack.peek();
      ClassMapping mapping = getClassMapping(o.getClass());
      if(mapping == null)
      {
         // todo: YAH (yet another hack)
         QName autoType = SimpleTypeBindings.typeQName(o.getClass());
         if(autoType != null)
         {
            String marshalled = SimpleTypeBindings.marshal(autoType.getLocalPart(), o, null);
            content.characters(marshalled.toCharArray(), 0, marshalled.length());
            return true;
         }
         else
         {
            if(ignoreUnresolvedWildcard)
            {
               log.warn("Failed to marshal wildcard. Class mapping not found for " +
                  o.getClass() +
                  "@" +
                  o.hashCode() +
                  ": " + o
               );
               return true;
            }
            else
            {
               throw new IllegalStateException("Failed to marshal wildcard. Class mapping not found for " +
                  o.getClass() +
                  "@" +
                  o.hashCode() +
                  ": " + o
               );
            }
         }
      }

      GenericObjectModelProvider parentProvider = this.provider;
      Object parentRoot = this.root;
      Stack parentStack = this.stack;
      XSModel parentModel = this.model;

      this.root = o;
      this.stack = new StackImpl();
      this.model = mapping.schemaUrl == null ? this.model : Util.loadSchema(mapping.schemaUrl, schemaResolver);
      if(mapping.provider != null)
      {
         this.provider = mapping.provider;
      }

      boolean marshalled;
      if(mapping.elementName != null)
      {
         XSElementDeclaration elDec = model.getElementDeclaration(mapping.elementName.getLocalPart(),
            mapping.elementName.getNamespaceURI()
         );

         if(elDec == null)
         {
            throw new JBossXBRuntimeException("Element " + mapping.elementName + " is not declared in the schema.");
         }

         Object elementValue = provider.getRoot(root, null, elDec.getNamespace(), elDec.getName());
         marshalled = marshalElementOccurence(elDec.getNamespace(),
            elDec.getName(),
            elDec.getTypeDefinition(),
            elementValue,
            elDec.getNillable(),
            particle.getMinOccurs() == 0,
            declareNs
         );
      }
      else if(mapping.typeName != null)
      {
         XSTypeDefinition typeDef = model.getTypeDefinition(mapping.typeName.getLocalPart(),
            mapping.typeName.getNamespaceURI()
         );

         if(typeDef == null)
         {
            List typeNames = new ArrayList();
            XSNamedMap types = model.getComponents(XSConstants.TYPE_DEFINITION);
            for(int i = 0; i < types.getLength(); ++i)
            {
               XSObject type = types.item(i);
               if(!Constants.NS_XML_SCHEMA.equals(type.getNamespace()))
               {
                  typeNames.add(new QName(type.getNamespace(), type.getName()));
               }
            }
            throw new JBossXBRuntimeException("Type " +
               mapping.typeName +
               " is not defined in the schema." +
               " Defined types are: " + typeNames
            );
         }

         Object elementValue = provider.getRoot(root, null, wildcard.getNamespace(), wildcard.getName());
         marshalled =
            marshalElementOccurence(wildcard.getNamespace(),
               wildcard.getName(),
               typeDef,
               elementValue,
               true,
               particle.getMinOccurs() == 0,
               declareNs
            );
      }
      else
      {
         throw new JBossXBRuntimeException("Class mapping for " +
            mapping.cls +
            " is associated with neither global element name nor global type name."
         );
      }

      this.root = parentRoot;
      this.provider = parentProvider;
      this.stack = parentStack;
      this.model = parentModel;

      return marshalled;
   }

   private boolean marshalModelGroup(XSParticle particle, boolean declareNs)
   {
      XSModelGroup modelGroup = (XSModelGroup)particle.getTerm();
      boolean marshalled;
      switch(modelGroup.getCompositor())
      {
         case XSModelGroup.COMPOSITOR_ALL:
            marshalled = marshalModelGroupAll(modelGroup.getParticles(), declareNs);
            break;
         case XSModelGroup.COMPOSITOR_CHOICE:
            marshalled = marshalModelGroupChoice(modelGroup.getParticles(), declareNs);
            break;
         case XSModelGroup.COMPOSITOR_SEQUENCE:
            marshalled = marshalModelGroupSequence(modelGroup.getParticles(), declareNs);
            break;
         default:
            throw new IllegalStateException("Unexpected compsitor: " + modelGroup.getCompositor());
      }
      return marshalled;
   }

   private boolean marshalModelGroupAll(XSObjectList particles, boolean declareNs)
   {
      boolean marshalled = false;
      for(int i = 0; i < particles.getLength(); ++i)
      {
         XSParticle particle = (XSParticle)particles.item(i);
         marshalled |= marshalParticle(particle, declareNs);
      }
      return marshalled;
   }

   private boolean marshalModelGroupChoice(XSObjectList particles, boolean declareNs)
   {
      boolean marshalled = false;
      Content mainContent = this.content;
      for(int i = 0; i < particles.getLength() && !marshalled; ++i)
      {
         XSParticle particle = (XSParticle)particles.item(i);
         this.content = new Content();
         marshalled = marshalParticle(particle, declareNs);
      }

      if(marshalled)
      {
         mainContent.append(this.content);
      }
      this.content = mainContent;

      return marshalled;
   }

   private boolean marshalModelGroupSequence(XSObjectList particles, boolean declareNs)
   {
      boolean marshalled = true;
      for(int i = 0; i < particles.getLength(); ++i)
      {
         XSParticle particle = (XSParticle)particles.item(i);
         marshalled &= marshalParticle(particle, declareNs);
      }
      return marshalled;
   }

   private String marshalCharacters(String elementUri,
                                    String elementPrefix,
                                    XSSimpleTypeDefinition type,
                                    Object value,
                                    AttributesImpl attrs)
   {
      String marshalled;
      if(type.getItemType() != null)
      {
         XSSimpleTypeDefinition itemType = type.getItemType();
         if(Constants.NS_XML_SCHEMA.equals(itemType.getNamespace()))
         {
            List list;
            if(value instanceof List)
            {
               list = (List)value;
            }
            else if(value.getClass().isArray())
            {
               list = asList(value);
            }
            else
            {
               // todo: qname are also not yet supported
               throw new JBossXBRuntimeException(
                  "Expected value for list type is an array or " + List.class.getName() + " but got: " + value
               );
            }

            marshalled = SimpleTypeBindings.marshalList(itemType.getName(), list, null);
         }
         else
         {
            throw new JBossXBRuntimeException("Marshalling of list types with item types not from " +
               Constants.NS_XML_SCHEMA + " is not supported."
            );
         }
      }
      else if(Constants.NS_XML_SCHEMA.equals(type.getNamespace()))
      {
         String typeName = type.getName();

         String prefix = null;
         boolean removePrefix = false;
         if(SimpleTypeBindings.XS_QNAME_NAME.equals(typeName) ||
            SimpleTypeBindings.XS_NOTATION_NAME.equals(typeName))
         {
            QName qName = (QName)value;
            if(qName.getNamespaceURI() != null && qName.getNamespaceURI().length() > 0)
            {
               prefix = nsRegistry.getPrefix(qName.getNamespaceURI());
               if(prefix == null)
               {
                  prefix = qName.getPrefix();
                  if(prefix == null || prefix.length() == 0)
                  {
                     prefix = qName.getLocalPart() + "_ns";
                  }
                  nsRegistry.addPrefixMapping(prefix, qName.getNamespaceURI());
                  declareNs(attrs, prefix, qName.getNamespaceURI());

                  removePrefix = true;
               }
            }
         }
         marshalled = SimpleTypeBindings.marshal(typeName, value, nsRegistry);

         if(removePrefix)
         {
            nsRegistry.removePrefixMapping(prefix);
         }
      }
      // todo: this is a quick fix for boolean pattern (0|1 or true|false) should be refactored
      else if(type.getLexicalPattern().item(0) != null
         &&
         type.derivedFrom(Constants.NS_XML_SCHEMA,
            Constants.QNAME_BOOLEAN.getLocalPart(),
            XSConstants.DERIVATION_RESTRICTION
         ))
      {
         String item = type.getLexicalPattern().item(0);
         if(item.indexOf('0') != -1 && item.indexOf('1') != -1)
         {
            marshalled = ((Boolean)value).booleanValue() ? "1" : "0";
         }
         else
         {
            marshalled = ((Boolean)value).booleanValue() ? "true" : "false";
         }
      }
      else
      {
         StringList lexicalEnumeration = type.getLexicalEnumeration();
         if(lexicalEnumeration != null && lexicalEnumeration.getLength() > 0)
         {
            Method getValue;
            try
            {
               getValue = value.getClass().getMethod("value", null);
            }
            catch(NoSuchMethodException e)
            {
               try
               {
                  getValue = value.getClass().getMethod("getValue", null);
               }
               catch(NoSuchMethodException e1)
               {
                  List values = new ArrayList(lexicalEnumeration.getLength());
                  for(int i = 0; i < lexicalEnumeration.getLength(); ++i)
                  {
                     values.add(lexicalEnumeration.item(i));
                  }

                  throw new JBossXBRuntimeException("Failed to find neither value() nor getValue() in " +
                     value.getClass() +
                     " which is bound to enumeration type (" +
                     type.getNamespace() +
                     ", " +
                     type.getName() + "): " + values
                  );
               }
            }

            try
            {
               value = getValue.invoke(value, null);
            }
            catch(Exception e)
            {
               throw new JBossXBRuntimeException(
                  "Failed to invoke getValue() on " + value + " to get the enumeration value", e
               );
            }
         }

         marshalled = marshalCharacters(elementUri,
            elementPrefix,
            (XSSimpleTypeDefinition)type.getBaseType(),
            value, attrs
         );
      }
      return marshalled;
   }

   /**
    * Adds xsi:type attribute and optionally declares namespaces for xsi and type's namespace.
    * @param type  the type to declare xsi:type attribute for
    * @param attrs  the attributes to add xsi:type attribute to
    * @return  prefix for the type's ns if it was generated
    */
   private String declareXsiType(XSTypeDefinition type, AttributesImpl attrs)
   {
      String result = null;
      String xsiPrefix = nsRegistry.getPrefix(Constants.NS_XML_SCHEMA_INSTANCE);
      if(xsiPrefix == null)
      {
         attrs.add(Constants.NS_XML_SCHEMA, "xmlns", "xmlns:xsi", null, Constants.NS_XML_SCHEMA_INSTANCE);
         xsiPrefix = "xsi";
      }

      String pref = getPrefix(type.getNamespace());
      if(pref == null)
      {
         // the ns is not declared
         result = pref = type.getName() + "_ns";
      }

      String typeQName = pref == null ? type.getName() : pref + ':' + type.getName();
      attrs.add(Constants.NS_XML_SCHEMA_INSTANCE, "type", xsiPrefix + ":type", null, typeQName);
      return result;
   }

   private Object getElementValue(String elementNs, String elementLocal, XSTypeDefinition type)
   {
      Object value;
      Object peeked = stack.isEmpty() ? root : stack.peek();
      if(peeked == null)
      {
         value = null;
      }
      else if(peeked instanceof Collection || peeked.getClass().isArray())
      {
         // collection is the provider
         value = peeked;
      }
      else
      {
         XSTypeDefinition parentType = currentElementType;
         currentElementType = type;

         value = provider.getChildren(peeked, ctx, elementNs, elementLocal);
         if(value == null)
         {
            value = provider.getElementValue(peeked, ctx, elementNs, elementLocal);
         }

         currentElementType = parentType;
      }
      return value;
   }

   private Object getSimpleContentValue(String elementNs, String elementLocal, XSTypeDefinition type)
   {
      Object value;
      Object peeked = stack.isEmpty() ? root : stack.peek();
      if(peeked == null)
      {
         value = null;
      }
      else
      {
         XSTypeDefinition parentType = currentElementType;
         currentElementType = type;
         value = provider.getElementValue(peeked, ctx, elementNs, elementLocal);
         currentElementType = parentType;
      }
      return value;
   }

   private void writeNillable(String elementNs, String elementLocal, boolean nillable)
   {
      if(!supportNil)
      {
         return;
      }

      if(!nillable)
      {
         throw new JBossXBRuntimeException("Failed to marshal " +
            new QName(elementNs, elementLocal) +
            ": Java value is null but the element is not nillable."
         );
      }

      AttributesImpl attrs;
      String prefix = getPrefix(elementNs);
      if(prefix == null && elementNs != null && elementNs.length() > 0)
      {
         prefix = "ns_" + elementLocal;
         attrs = new AttributesImpl(2);
         attrs.add(null, prefix, "xmlns:" + prefix, null, elementNs);
      }
      else
      {
         attrs = new AttributesImpl(1);
      }

      String xsiPrefix = getPrefix(Constants.NS_XML_SCHEMA_INSTANCE);
      if(xsiPrefix == null)
      {
         xsiPrefix = "xsi";
         attrs.add(null,
            xsiPrefix,
            "xmlns:xsi",
            null,
            Constants.NS_XML_SCHEMA_INSTANCE
         );
      }

      String nilQName = xsiPrefix + ":nil";
      attrs.add(Constants.NS_XML_SCHEMA_INSTANCE, "nil", nilQName, null, "1");

      String qName = prefixLocalName(prefix, elementLocal);
      content.startElement(elementNs, elementLocal, qName, attrs);
      content.endElement(elementNs, elementLocal, qName);
   }

   private static boolean isArrayWrapper(XSTypeDefinition type)
   {
      boolean is = false;
      if(XSTypeDefinition.COMPLEX_TYPE == type.getTypeCategory())
      {
         XSComplexTypeDefinition cType = (XSComplexTypeDefinition)type;
         XSParticle particle = cType.getParticle();
         if(particle != null)
         {
            is = particle.getMaxOccursUnbounded() || particle.getMaxOccurs() > 1;
         }
      }
      return is;
   }

   private Iterator getIterator(Object value)
   {
      Iterator i = null;
      if(value instanceof Collection)
      {
         i = ((Collection)value).iterator();
      }
      else if(value.getClass().isArray())
      {
         final Object arr = value;
         i = new Iterator()
         {
            private int curInd = 0;
            private int length = Array.getLength(arr);

            public boolean hasNext()
            {
               return curInd < length;
            }

            public Object next()
            {
               return Array.get(arr, curInd++);
            }

            public void remove()
            {
               throw new UnsupportedOperationException("remove is not implemented.");
            }
         };
      }
      else if(value instanceof Iterator)
      {
         i = (Iterator)value;
      }
      else
      {
         //throw new JBossXBRuntimeException("Unexpected type for children: " + value.getClass());
      }
      return i;
   }

   private static boolean isRepeatable(XSParticle particle)
   {
      return particle.getMaxOccursUnbounded() || particle.getMaxOccurs() > 1 || particle.getMinOccurs() > 1;
   }

   private static final List asList(final Object arr)
   {
      return new AbstractList()
      {
         private final Object array = arr;

         public Object get(int index)
         {
            return Array.get(array, index);
         }

         public int size()
         {
            return Array.getLength(array);
         }
      };
   }
}
