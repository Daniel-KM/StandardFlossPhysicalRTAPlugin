package net.berthereau.exlibris;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

class SimpleNamespaceContext implements NamespaceContext {
    private Map<String, String> urisByPrefix = new HashMap<>();

    private Map<String, Set<String>> prefixesByURI = new HashMap<>();

    public SimpleNamespaceContext() {
        addNamespace(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        addNamespace(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    }

    public synchronized void addNamespace(String prefix, String namespaceURI) {
        urisByPrefix.put(prefix, namespaceURI);
        if (prefixesByURI.containsKey(namespaceURI)) {
            (prefixesByURI.get(namespaceURI)).add(prefix);
        }
        else {
            Set<String> set = new HashSet<>();
            set.add(prefix);
            prefixesByURI.put(namespaceURI, set);
        }
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null)
            throw new IllegalArgumentException("prefix cannot be null");
        return urisByPrefix.containsKey(prefix)
                ? urisByPrefix.get(prefix)
                : XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return getPrefixes(namespaceURI).next();
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        if (namespaceURI == null)
            throw new IllegalArgumentException("namespaceURI cannot be null");
        if (prefixesByURI.containsKey(namespaceURI))
            return prefixesByURI.get(namespaceURI).iterator();
        else
            return Collections.emptyIterator();
    }
}
