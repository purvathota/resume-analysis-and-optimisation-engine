package com.resumeoptimizer.service.document;

import com.resumeoptimizer.exception.DocumentParsingException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class DocxParserService {

    public String parseDocx(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (Exception e) {
            throw new DocumentParsingException("Failed to parse DOCX document", e);
        }
    }
}
