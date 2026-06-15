package com.resumeoptimizer.service.document;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.resumeoptimizer.entity.Analysis;
import com.resumeoptimizer.entity.CoverLetter;
import com.resumeoptimizer.entity.Resume;
import com.resumeoptimizer.entity.User;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class DocumentExportService {

    private static final String TIMES_ROMAN = FontFactory.TIMES_ROMAN;
    private static final String TIMES_BOLD = FontFactory.TIMES_BOLD;

    public byte[] generatePdf(Analysis analysis) throws Exception {
        if ("OPTIMIZATION_FAILED_VALIDATION".equals(analysis.getAnalysisStatus())) {
            throw new RuntimeException("Cannot export: Resume Integrity Score is below 95%. Optimization failed.");
        }

        Map<String, Object> data = analysis.getOptimizedResumeJson();
        if (data == null) {
            throw new RuntimeException("No optimized resume data available");
        }

        Resume originalResume = analysis.getResume();
        Map<String, String> header = data.containsKey("header") ? (Map<String, String>) data.get("header") : new java.util.HashMap<>();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        Font nameFont = FontFactory.getFont(TIMES_BOLD, 22);
        Font titleFont = FontFactory.getFont(TIMES_BOLD, 14);
        Font contactFont = FontFactory.getFont(TIMES_ROMAN, 10);
        Font linkFont = FontFactory.getFont(TIMES_ROMAN, 10, Font.UNDERLINE, java.awt.Color.BLUE);
        Font sectionFont = FontFactory.getFont(TIMES_BOLD, 12);
        Font regularFont = FontFactory.getFont(TIMES_ROMAN, 11);
        Font boldFont = FontFactory.getFont(TIMES_BOLD, 11);

        // Header
        Paragraph name = new Paragraph(header.getOrDefault("fullName", "Candidate Name"), nameFont);
        name.setAlignment(Element.ALIGN_CENTER);
        document.add(name);

        String title = header.getOrDefault("title", "Software Engineer");
        Paragraph role = new Paragraph(title, titleFont);
        role.setAlignment(Element.ALIGN_CENTER);
        document.add(role);

        Paragraph contact = new Paragraph();
        contact.setAlignment(Element.ALIGN_CENTER);
        
        contact.add(new Chunk(header.getOrDefault("location", "") + " | ", contactFont));
        contact.add(new Chunk(header.getOrDefault("phone", "") + " | ", contactFont));

        if (header.containsKey("email")) {
            com.lowagie.text.Anchor emailLink = new com.lowagie.text.Anchor(header.get("email"), linkFont);
            emailLink.setReference("mailto:" + header.get("email"));
            contact.add(emailLink);
            contact.add(new Chunk(" | ", contactFont));
        }
        if (header.containsKey("linkedIn")) {
            com.lowagie.text.Anchor linkedinLink = new com.lowagie.text.Anchor("LinkedIn", linkFont);
            linkedinLink.setReference(header.get("linkedIn"));
            contact.add(linkedinLink);
            contact.add(new Chunk(" | ", contactFont));
        }
        if (header.containsKey("gitHub")) {
            com.lowagie.text.Anchor githubLink = new com.lowagie.text.Anchor("GitHub", linkFont);
            githubLink.setReference(header.get("gitHub"));
            contact.add(githubLink);
        }
        
        contact.setSpacingAfter(10);
        document.add(contact);

        LineSeparator line = new LineSeparator();
        line.setLineWidth(1);

        // Professional Summary
        if (data.containsKey("professionalSummary")) {
            addSectionHeader(document, "PROFESSIONAL SUMMARY", sectionFont, line);
            Paragraph summary = new Paragraph((String) data.get("professionalSummary"), regularFont);
            summary.setAlignment(Element.ALIGN_JUSTIFIED);
            summary.setSpacingAfter(10);
            document.add(summary);
        }

        // Professional Experience
        if (data.containsKey("professionalExperience")) {
            addSectionHeader(document, "PROFESSIONAL EXPERIENCE", sectionFont, line);
            List<Map<String, Object>> exps = (List<Map<String, Object>>) data.get("professionalExperience");
            for (Map<String, Object> exp : exps) {
                addExperienceEntry(document, exp, boldFont, regularFont);
            }
        }

        // Virtual Experience
        if (data.containsKey("virtualExperience")) {
            addSectionHeader(document, "VIRTUAL EXPERIENCE", sectionFont, line);
            List<Map<String, Object>> exps = (List<Map<String, Object>>) data.get("virtualExperience");
            for (Map<String, Object> exp : exps) {
                addExperienceEntry(document, exp, boldFont, regularFont);
            }
        }

        // Skills
        if (data.containsKey("skills")) {
            addSectionHeader(document, "SKILLS", sectionFont, line);
            List<Map<String, Object>> skills = (List<Map<String, Object>>) data.get("skills");
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{30, 70});
            for (Map<String, Object> skill : skills) {
                PdfPCell c1 = new PdfPCell(new Phrase((String) skill.get("category"), boldFont));
                c1.setBorder(0);
                PdfPCell c2 = new PdfPCell(new Phrase((String) skill.get("items"), regularFont));
                c2.setBorder(0);
                table.addCell(c1);
                table.addCell(c2);
            }
            document.add(table);
            document.add(new Chunk("\n"));
        }

        // Education
        if (data.containsKey("education")) {
            addSectionHeader(document, "EDUCATION", sectionFont, line);
            List<Map<String, Object>> edus = (List<Map<String, Object>>) data.get("education");
            for (Map<String, Object> edu : edus) {
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                
                PdfPCell c1 = new PdfPCell(new Phrase((String) edu.get("degree"), boldFont));
                c1.setBorder(0);
                PdfPCell c2 = new PdfPCell(new Phrase((String) edu.get("dates"), regularFont));
                c2.setBorder(0);
                c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(c1);
                table.addCell(c2);

                PdfPCell c3 = new PdfPCell(new Phrase((String) edu.get("university"), regularFont));
                c3.setBorder(0);
                PdfPCell c4 = new PdfPCell(new Phrase((String) edu.get("details"), regularFont));
                c4.setBorder(0);
                c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(c3);
                table.addCell(c4);

                document.add(table);
                document.add(new Chunk("\n"));
            }
        }

        // Projects
        if (data.containsKey("projects")) {
            addSectionHeader(document, "PROJECTS", sectionFont, line);
            List<Map<String, Object>> projects = (List<Map<String, Object>>) data.get("projects");
            for (Map<String, Object> proj : projects) {
                Paragraph pTitle = new Paragraph();
                pTitle.add(new Chunk((String) proj.get("title"), boldFont));
                if (proj.containsKey("link") && proj.get("link") != null) {
                    pTitle.add(new Chunk(" | ", regularFont));
                    String linkUrl = (String) proj.get("link");
                    com.lowagie.text.Anchor projLink = new com.lowagie.text.Anchor(linkUrl, linkFont);
                    if (!linkUrl.startsWith("http")) linkUrl = "https://" + linkUrl;
                    projLink.setReference(linkUrl);
                    pTitle.add(projLink);
                }
                document.add(pTitle);

                Paragraph pTech = new Paragraph();
                pTech.add(new Chunk("Tech Stack: ", boldFont));
                pTech.add(new Chunk((String) proj.get("techStack"), boldFont));
                document.add(pTech);

                if (proj.containsKey("bullets")) {
                    com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
                    list.setListSymbol("\u2022 ");
                    List<String> bullets = (List<String>) proj.get("bullets");
                    for (String b : bullets) {
                        list.add(new com.lowagie.text.ListItem(b, regularFont));
                    }
                    document.add(list);
                }
                document.add(new Chunk("\n"));
            }
        }

        // Certifications
        if (data.containsKey("certifications")) {
            addSectionHeader(document, "CERTIFICATIONS", sectionFont, line);
            List<Map<String, Object>> certs = (List<Map<String, Object>>) data.get("certifications");
            for (Map<String, Object> cert : certs) {
                PdfPTable wrapperTable = new PdfPTable(1);
                wrapperTable.setWidthPercentage(100);
                wrapperTable.setKeepTogether(true);

                PdfPCell wrapperCell = new PdfPCell();
                wrapperCell.setBorder(0);

                PdfPTable headerTable = new PdfPTable(2);
                headerTable.setWidthPercentage(100);
                
                PdfPCell c1 = new PdfPCell(new Phrase((String) cert.get("title"), boldFont));
                c1.setBorder(0);
                PdfPCell c2 = new PdfPCell(new Phrase((String) cert.get("date"), regularFont));
                c2.setBorder(0);
                c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                headerTable.addCell(c1);
                headerTable.addCell(c2);
                wrapperCell.addElement(headerTable);

                if (cert.containsKey("bullets")) {
                    com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
                    list.setListSymbol("\u2022 ");
                    List<String> bullets = (List<String>) cert.get("bullets");
                    for (String b : bullets) {
                        list.add(new com.lowagie.text.ListItem(b, regularFont));
                    }
                    wrapperCell.addElement(list);
                }
                
                wrapperTable.addCell(wrapperCell);
                document.add(wrapperTable);
                document.add(new Chunk("\n"));
            }
        }

        // Achievements
        if (data.containsKey("achievements")) {
            addSectionHeader(document, "ACHIEVEMENTS", sectionFont, line);
            List<String> achievements = (List<String>) data.get("achievements");
            com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
            list.setListSymbol("\u2022 ");
            for (String a : achievements) {
                list.add(new com.lowagie.text.ListItem(a, regularFont));
            }
            document.add(list);
            document.add(new Chunk("\n"));
        }

        document.close();
        return out.toByteArray();
    }

    private void addSectionHeader(Document document, String title, Font font, LineSeparator line) throws Exception {
        document.add(line);
        Paragraph p = new Paragraph(title, font);
        p.setSpacingBefore(5);
        p.setSpacingAfter(5);
        document.add(p);
        document.add(line);
        Paragraph space = new Paragraph();
        space.setSpacingAfter(5);
        document.add(space);
    }

    private void addExperienceEntry(Document document, Map<String, Object> exp, Font boldFont, Font regularFont) throws Exception {
        Paragraph pTitle = new Paragraph((String) exp.get("title"), boldFont);
        pTitle.setSpacingAfter(2);
        document.add(pTitle);

        Paragraph pCompany = new Paragraph((String) exp.get("company"), regularFont);
        pCompany.setSpacingAfter(2);
        document.add(pCompany);

        Paragraph pLocation = new Paragraph((String) exp.get("location"), regularFont);
        pLocation.setSpacingAfter(2);
        document.add(pLocation);

        Paragraph pDates = new Paragraph((String) exp.get("dates"), regularFont);
        pDates.setSpacingAfter(5);
        document.add(pDates);

        if (exp.containsKey("bullets")) {
            com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
            list.setListSymbol("\u2022 ");
            List<String> bullets = (List<String>) exp.get("bullets");
            for (String b : bullets) {
                list.add(new com.lowagie.text.ListItem(b, regularFont));
            }
            document.add(list);
        }
        document.add(new Chunk("\n"));
    }

    public byte[] generateDocx(Analysis analysis) throws Exception {
        if ("OPTIMIZATION_FAILED_VALIDATION".equals(analysis.getAnalysisStatus())) {
            throw new RuntimeException("Cannot export: Resume Integrity Score is below 95%. Optimization failed.");
        }

        Map<String, Object> data = analysis.getOptimizedResumeJson();
        if (data == null) {
            throw new RuntimeException("No optimized resume data available");
        }

        XWPFDocument document = new XWPFDocument();
        Map<String, String> header = data.containsKey("header") ? (Map<String, String>) data.get("header") : new java.util.HashMap<>();

        XWPFParagraph namePara = document.createParagraph();
        namePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun nameRun = namePara.createRun();
        nameRun.setBold(true);
        nameRun.setFontSize(22);
        nameRun.setFontFamily("Times New Roman");
        nameRun.setText(header.getOrDefault("fullName", "Candidate Name"));

        String title = header.getOrDefault("title", "Software Engineer");
        XWPFParagraph rolePara = document.createParagraph();
        rolePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun roleRun = rolePara.createRun();
        roleRun.setBold(true);
        roleRun.setFontSize(14);
        roleRun.setFontFamily("Times New Roman");
        roleRun.setText(title);

        XWPFParagraph contactPara = document.createParagraph();
        contactPara.setAlignment(ParagraphAlignment.CENTER);
        
        String location = header.getOrDefault("location", "");
        String phone = header.getOrDefault("phone", "");
        
        XWPFRun textRun1 = contactPara.createRun();
        textRun1.setFontSize(10);
        textRun1.setFontFamily("Times New Roman");
        textRun1.setText(location + " | " + phone + " | ");

        if (header.containsKey("email")) {
            XWPFHyperlinkRun emailRun = contactPara.createHyperlinkRun("mailto:" + header.get("email"));
            emailRun.setText(header.get("email"));
            emailRun.setColor("0000FF");
            emailRun.setUnderline(UnderlinePatterns.SINGLE);
            XWPFRun sep = contactPara.createRun();
            sep.setText(" | ");
        }
        if (header.containsKey("linkedIn")) {
            XWPFHyperlinkRun linkedInRun = contactPara.createHyperlinkRun(header.get("linkedIn"));
            linkedInRun.setText("LinkedIn");
            linkedInRun.setColor("0000FF");
            linkedInRun.setUnderline(UnderlinePatterns.SINGLE);
            XWPFRun sep = contactPara.createRun();
            sep.setText(" | ");
        }
        if (header.containsKey("gitHub")) {
            XWPFHyperlinkRun gitHubRun = contactPara.createHyperlinkRun(header.get("gitHub"));
            gitHubRun.setText("GitHub");
            gitHubRun.setColor("0000FF");
            gitHubRun.setUnderline(UnderlinePatterns.SINGLE);
        }
        
        // Summary
        if (data.containsKey("professionalSummary")) {
            addDocxSectionHeader(document, "PROFESSIONAL SUMMARY");
            XWPFParagraph p = document.createParagraph();
            p.setAlignment(ParagraphAlignment.BOTH);
            XWPFRun r = p.createRun();
            r.setFontFamily("Times New Roman");
            r.setFontSize(11);
            r.setText((String) data.get("professionalSummary"));
        }

        // Experience
        if (data.containsKey("professionalExperience")) {
            addDocxSectionHeader(document, "PROFESSIONAL EXPERIENCE");
            List<Map<String, Object>> exps = (List<Map<String, Object>>) data.get("professionalExperience");
            for (Map<String, Object> exp : exps) {
                addDocxExperienceEntry(document, exp);
            }
        }

        // Skills
        if (data.containsKey("skills")) {
            addDocxSectionHeader(document, "SKILLS");
            List<Map<String, Object>> skills = (List<Map<String, Object>>) data.get("skills");
            for (Map<String, Object> skill : skills) {
                XWPFParagraph p = document.createParagraph();
                XWPFRun rBold = p.createRun();
                rBold.setBold(true);
                rBold.setFontFamily("Times New Roman");
                rBold.setFontSize(11);
                rBold.setText((String) skill.get("category") + ": ");
                
                XWPFRun rReg = p.createRun();
                rReg.setFontFamily("Times New Roman");
                rReg.setFontSize(11);
                rReg.setText((String) skill.get("items"));
            }
        }

        // Education
        if (data.containsKey("education")) {
            addDocxSectionHeader(document, "EDUCATION");
            List<Map<String, Object>> edus = (List<Map<String, Object>>) data.get("education");
            for (Map<String, Object> edu : edus) {
                XWPFParagraph pTitle = document.createParagraph();
                XWPFRun rTitle = pTitle.createRun();
                rTitle.setBold(true);
                rTitle.setFontFamily("Times New Roman");
                rTitle.setFontSize(11);
                rTitle.setText((String) edu.get("degree") + " | " + edu.get("dates"));
                
                XWPFParagraph pDetails = document.createParagraph();
                XWPFRun rDetails = pDetails.createRun();
                rDetails.setFontFamily("Times New Roman");
                rDetails.setFontSize(11);
                rDetails.setText((String) edu.get("university") + " - " + edu.get("details"));
            }
        }

        // Projects
        if (data.containsKey("projects")) {
            addDocxSectionHeader(document, "PROJECTS");
            List<Map<String, Object>> projects = (List<Map<String, Object>>) data.get("projects");
            for (Map<String, Object> proj : projects) {
                XWPFParagraph pTitle = document.createParagraph();
                XWPFRun rTitle = pTitle.createRun();
                rTitle.setBold(true);
                rTitle.setFontFamily("Times New Roman");
                rTitle.setFontSize(11);
                rTitle.setText((String) proj.get("title"));
                
                if (proj.containsKey("link") && proj.get("link") != null) {
                    XWPFRun sep = pTitle.createRun();
                    sep.setFontFamily("Times New Roman");
                    sep.setFontSize(11);
                    sep.setText(" | ");
                    
                    String linkUrl = (String) proj.get("link");
                    if (!linkUrl.startsWith("http")) linkUrl = "https://" + linkUrl;
                    XWPFHyperlinkRun projLink = pTitle.createHyperlinkRun(linkUrl);
                    projLink.setText((String) proj.get("link"));
                    projLink.setColor("0000FF");
                    projLink.setUnderline(UnderlinePatterns.SINGLE);
                }

                XWPFParagraph pTech = document.createParagraph();
                XWPFRun rTech = pTech.createRun();
                rTech.setBold(true);
                rTech.setFontFamily("Times New Roman");
                rTech.setFontSize(11);
                rTech.setText("Tech Stack: " + proj.get("techStack"));

                if (proj.containsKey("bullets")) {
                    List<String> bullets = (List<String>) proj.get("bullets");
                    for (String b : bullets) {
                        XWPFParagraph pBul = document.createParagraph();
                        pBul.setStyle("ListParagraph");
                        XWPFRun rBul = pBul.createRun();
                        rBul.setFontFamily("Times New Roman");
                        rBul.setFontSize(11);
                        rBul.setText("\u2022 " + b);
                    }
                }
            }
        }

        // Certifications
        if (data.containsKey("certifications")) {
            addDocxSectionHeader(document, "CERTIFICATIONS");
            List<Map<String, Object>> certs = (List<Map<String, Object>>) data.get("certifications");
            for (Map<String, Object> cert : certs) {
                XWPFParagraph pTitle = document.createParagraph();
                XWPFRun rTitle = pTitle.createRun();
                rTitle.setBold(true);
                rTitle.setFontFamily("Times New Roman");
                rTitle.setFontSize(11);
                rTitle.setText((String) cert.get("title") + " | " + cert.get("date"));
                
                if (cert.containsKey("bullets")) {
                    List<String> bullets = (List<String>) cert.get("bullets");
                    for (String b : bullets) {
                        XWPFParagraph pBul = document.createParagraph();
                        XWPFRun rBul = pBul.createRun();
                        rBul.setFontFamily("Times New Roman");
                        rBul.setFontSize(11);
                        rBul.setText("\u2022 " + b);
                    }
                }
            }
        }

        // Achievements
        if (data.containsKey("achievements")) {
            addDocxSectionHeader(document, "ACHIEVEMENTS");
            List<String> achievements = (List<String>) data.get("achievements");
            for (String a : achievements) {
                XWPFParagraph pBul = document.createParagraph();
                XWPFRun rBul = pBul.createRun();
                rBul.setFontFamily("Times New Roman");
                rBul.setFontSize(11);
                rBul.setText("\u2022 " + a);
            }
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();
        return out.toByteArray();
    }

    private void addDocxSectionHeader(XWPFDocument document, String title) {
        XWPFParagraph p = document.createParagraph();
        p.setBorderBottom(Borders.SINGLE);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(12);
        r.setFontFamily("Times New Roman");
        r.setText(title);
    }

    private void addDocxExperienceEntry(XWPFDocument document, Map<String, Object> exp) {
        XWPFParagraph pTitle = document.createParagraph();
        XWPFRun rTitle = pTitle.createRun();
        rTitle.setBold(true);
        rTitle.setFontSize(11);
        rTitle.setFontFamily("Times New Roman");
        rTitle.setText((String) exp.get("title"));
        
        XWPFParagraph pComp = document.createParagraph();
        XWPFRun rComp = pComp.createRun();
        rComp.setFontSize(11);
        rComp.setFontFamily("Times New Roman");
        rComp.setText((String) exp.get("company"));
        
        XWPFParagraph pLoc = document.createParagraph();
        XWPFRun rLoc = pLoc.createRun();
        rLoc.setFontSize(11);
        rLoc.setFontFamily("Times New Roman");
        rLoc.setText((String) exp.get("location"));

        XWPFParagraph pDates = document.createParagraph();
        XWPFRun rDates = pDates.createRun();
        rDates.setFontSize(11);
        rDates.setFontFamily("Times New Roman");
        rDates.setText((String) exp.get("dates"));
        
        if (exp.containsKey("bullets")) {
            List<String> bullets = (List<String>) exp.get("bullets");
            for (String b : bullets) {
                XWPFParagraph pBul = document.createParagraph();
                pBul.setStyle("ListParagraph");
                XWPFRun rBul = pBul.createRun();
                rBul.setFontSize(11);
                rBul.setFontFamily("Times New Roman");
                rBul.setText("\u2022 " + b);
            }
        }
        document.createParagraph();
    }

    private void addDocxProjectEntry(XWPFDocument document, Map<String, Object> proj) {
        XWPFParagraph p = document.createParagraph();
        p.setSpacingAfter(0);

        XWPFRun titleRun = p.createRun();
        titleRun.setFontFamily("Times New Roman");
        titleRun.setFontSize(11);
        titleRun.setBold(true);
        titleRun.setText((String) proj.getOrDefault("projectName", ""));

        if (proj.containsKey("technologies")) {
            XWPFRun techRun = p.createRun();
            techRun.setFontFamily("Times New Roman");
            techRun.setFontSize(11);
            techRun.setText(" | " + String.join(", ", (List<String>) proj.get("technologies")));
        }

        if (proj.containsKey("link")) {
            XWPFRun linkRun = p.createRun();
            linkRun.setFontFamily("Times New Roman");
            linkRun.setFontSize(11);
            linkRun.setText(" | " + proj.get("link"));
        }

        if (proj.containsKey("bullets")) {
            List<String> bullets = (List<String>) proj.get("bullets");
            for (String b : bullets) {
                XWPFParagraph bp = document.createParagraph();
                // Removed setNumID call that depended on missing addDocxBulletList
                bp.setSpacingAfter(0);
                XWPFRun br = bp.createRun();
                br.setFontFamily("Times New Roman");
                br.setFontSize(11);
                br.setText("• " + b);
            }
        }
    }

    public byte[] generateCoverLetterPdf(com.resumeoptimizer.entity.CoverLetterVersion version) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        Font nameFont = FontFactory.getFont(TIMES_BOLD, 22);
        Font contactFont = FontFactory.getFont(TIMES_ROMAN, 10);
        Font linkFont = FontFactory.getFont(TIMES_ROMAN, 10, Font.UNDERLINE, java.awt.Color.BLUE);
        Font bodyFont = FontFactory.getFont(TIMES_ROMAN, 11);

        Resume resume = version.getCoverLetter().getResume();
        User user = resume.getUser(); 

        String fullName = user.getFullName() != null ? user.getFullName() : "Candidate Name";
        String email = user.getEmail();
        String location = "";
        String phone = "";
        
        Paragraph name = new Paragraph(fullName, nameFont);
        name.setAlignment(Element.ALIGN_CENTER);
        document.add(name);

        Paragraph contact = new Paragraph();
        contact.setAlignment(Element.ALIGN_CENTER);
        
        if (!location.isEmpty()) contact.add(new Chunk(location + " | ", contactFont));
        if (!phone.isEmpty()) contact.add(new Chunk(phone + " | ", contactFont));

        com.lowagie.text.Anchor emailLink = new com.lowagie.text.Anchor(email, linkFont);
        emailLink.setReference("mailto:" + email);
        contact.add(emailLink);
        
        contact.setSpacingAfter(20);
        document.add(contact);

        LineSeparator line = new LineSeparator();
        line.setLineWidth(1);
        document.add(line);
        
        document.add(new Paragraph(" ", bodyFont)); 

        Paragraph date = new Paragraph(java.time.LocalDate.now().toString(), bodyFont);
        date.setSpacingAfter(15);
        document.add(date);

        String generatedContent = version.getGeneratedContent();
        String[] paragraphs = generatedContent.split("\\n\\n");
        for (String p : paragraphs) {
            Paragraph bodyParagraph = new Paragraph(p, bodyFont);
            bodyParagraph.setSpacingAfter(10);
            bodyParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(bodyParagraph);
        }

        document.close();
        return out.toByteArray();
    }

    public byte[] generateCoverLetterDocx(com.resumeoptimizer.entity.CoverLetterVersion version) throws Exception {
        XWPFDocument document = new XWPFDocument();

        Resume resume = version.getCoverLetter().getResume();
        User user = resume.getUser(); 
        String fullName = user.getFullName() != null ? user.getFullName() : "Candidate Name";
        String email = user.getEmail();

        XWPFParagraph namePara = document.createParagraph();
        namePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun nameRun = namePara.createRun();
        nameRun.setFontFamily("Times New Roman");
        nameRun.setFontSize(22);
        nameRun.setBold(true);
        nameRun.setText(fullName);

        XWPFParagraph contactPara = document.createParagraph();
        contactPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun contactRun = contactPara.createRun();
        contactRun.setFontFamily("Times New Roman");
        contactRun.setFontSize(10);
        contactRun.setText(email);
        
        XWPFParagraph datePara = document.createParagraph();
        XWPFRun dateRun = datePara.createRun();
        dateRun.setFontFamily("Times New Roman");
        dateRun.setFontSize(11);
        dateRun.setText(java.time.LocalDate.now().toString());
        datePara.setSpacingAfter(300);

        String generatedContent = version.getGeneratedContent();
        String[] paragraphs = generatedContent.split("\\n\\n");
        for (String pText : paragraphs) {
            XWPFParagraph p = document.createParagraph();
            XWPFRun r = p.createRun();
            r.setFontFamily("Times New Roman");
            r.setFontSize(11);
            r.setText(pText);
            p.setSpacingAfter(200);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();
        return out.toByteArray();
    }
}
