package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import lombok.NoArgsConstructor;
import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;

@NoArgsConstructor
@Component
public class GxpHandler extends XmlHandler {
	
	@Override
	public String processXml(InputStream inputStream, MultipartDto multipartDto) throws IOException,
		ParserConfigurationException,
		SAXException, TransformerException {
		return null;
	}
}
