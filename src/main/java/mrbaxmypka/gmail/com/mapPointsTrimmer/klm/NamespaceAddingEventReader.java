package mrbaxmypka.gmail.com.mapPointsTrimmer.klm;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.EventReaderDelegate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NamespaceAddingEventReader extends EventReaderDelegate {
	
	private final XMLEventFactory factory = XMLEventFactory.newInstance();
	private final String LOCUS_NAMESPACE_URI = "http://www.locusmap.eu";
	private final String LC_PREFIX = "lc";
	
	public NamespaceAddingEventReader(XMLEventReader reader) {
		super(reader);
	}
	
	@Override
	public XMLEvent nextEvent() throws XMLStreamException {
		XMLEvent event = null;
		try {
			event = super.nextEvent();
			if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("attachment")) {
				System.out.println(event.getEventType());
			}
		} catch (XMLStreamException e) {
			System.out.println(e.getMessage());
			event = factory.createStartElement("lc", "http://www.locusmap.eu", "attachment");
		}
		if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("kml")) {
			Iterator<Namespace> namespaces = event.asStartElement().getNamespaces();
			while (namespaces.hasNext()) {
				if (namespaces.next().getName().getLocalPart().equals("lc")) return event;
			}
			return withLocusNamespaceForLcPrefix(event.asStartElement());
		}
		return event;
	}
	
	/**
	 * Duplicate event with additional namespace declaration.
	 *
	 * @param startElement
	 * @return event with namespace
	 */
	private StartElement withLocusNamespaceForLcPrefix(StartElement startElement) {
		List<Namespace> namespaces = new ArrayList<>();
		namespaces.add(factory.createNamespace(LC_PREFIX, LOCUS_NAMESPACE_URI));
		Iterator<Namespace> originalNamespaces = startElement.getNamespaces();
		while (originalNamespaces.hasNext()) {
			namespaces.add(originalNamespaces.next());
		}
		return factory.createStartElement(
			startElement.getName(),
			startElement.getAttributes(),
			namespaces.iterator());
	}
}
