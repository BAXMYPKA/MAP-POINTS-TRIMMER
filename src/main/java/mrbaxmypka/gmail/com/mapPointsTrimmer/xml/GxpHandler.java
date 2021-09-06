package mrbaxmypka.gmail.com.mapPointsTrimmer.xml;

import mrbaxmypka.gmail.com.mapPointsTrimmer.entitiesDto.MultipartDto;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.FileService;
import mrbaxmypka.gmail.com.mapPointsTrimmer.services.GoogleIconsService;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;

@Component
public class GxpHandler extends XmlHandler {

    public GxpHandler(HtmlHandler htmlHandler, GoogleIconsService googleIconsService, FileService fileService) {
        super(htmlHandler, googleIconsService, fileService);
    }

    @Override
    public String processXml(InputStream inputStream, MultipartDto multipartDto) throws IOException,
            ParserConfigurationException,
            SAXException, TransformerException {
        return null;
    }
}
