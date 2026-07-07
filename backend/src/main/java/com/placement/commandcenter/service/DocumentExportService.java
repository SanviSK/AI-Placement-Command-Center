package com.placement.commandcenter.service;

import com.placement.commandcenter.dto.GeneratedContentDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentExportService {

    public byte[] generateDocx(String studentName, GeneratedContentDto content) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Title (Name)
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(studentName);
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily("Arial");

            // Professional Summary
            if (content.getSummary() != null && !content.getSummary().trim().isEmpty()) {
                addDocxSectionHeader(document, "PROFESSIONAL SUMMARY");
                XWPFParagraph p = document.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(content.getSummary());
                run.setFontSize(11);
                run.setFontFamily("Arial");
            }

            // Skills Section
            if (content.getSkills() != null && !content.getSkills().isEmpty()) {
                addDocxSectionHeader(document, "SKILLS & EXPERTISE");
                XWPFParagraph p = document.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(String.join(" | ", content.getSkills()));
                run.setFontSize(11);
                run.setFontFamily("Arial");
            }

            // Experience Section
            if (content.getExperience() != null && !content.getExperience().isEmpty()) {
                addDocxSectionHeader(document, "PROFESSIONAL EXPERIENCE");
                for (GeneratedContentDto.ExperienceItem exp : content.getExperience()) {
                    XWPFParagraph expHeader = document.createParagraph();
                    XWPFRun expRun = expHeader.createRun();
                    expRun.setText(exp.getCompany() + "  -  " + exp.getRole());
                    expRun.setBold(true);
                    expRun.setFontSize(11);
                    expRun.setFontFamily("Arial");

                    XWPFRun durRun = expHeader.createRun();
                    durRun.setText(" (" + exp.getDuration() + ")");
                    durRun.setItalic(true);
                    durRun.setFontSize(11);
                    durRun.setFontFamily("Arial");

                    if (exp.getDescription() != null) {
                        for (String line : exp.getDescription().split("\n")) {
                            if (line.trim().isEmpty()) continue;
                            XWPFParagraph bullet = document.createParagraph();
                            bullet.setIndentationLeft(360); // 0.25 inch indent
                            XWPFRun bulletRun = bullet.createRun();
                            bulletRun.setText("• " + line.replaceAll("^\\s*[•\\-*]\\s*", ""));
                            bulletRun.setFontSize(10);
                            bulletRun.setFontFamily("Arial");
                        }
                    }
                }
            }

            // Projects Section
            if (content.getProjects() != null && !content.getProjects().isEmpty()) {
                addDocxSectionHeader(document, "ACADEMIC & PERSONAL PROJECTS");
                for (GeneratedContentDto.ProjectItem proj : content.getProjects()) {
                    XWPFParagraph projHeader = document.createParagraph();
                    XWPFRun projRun = projHeader.createRun();
                    projRun.setText(proj.getName());
                    projRun.setBold(true);
                    projRun.setFontSize(11);
                    projRun.setFontFamily("Arial");

                    if (proj.getTechnologies() != null && !proj.getTechnologies().isEmpty()) {
                        XWPFRun techRun = projHeader.createRun();
                        techRun.setText(" | Tech: " + proj.getTechnologies());
                        techRun.setItalic(true);
                        techRun.setFontSize(10);
                        techRun.setFontFamily("Arial");
                    }

                    if (proj.getDescription() != null) {
                        for (String line : proj.getDescription().split("\n")) {
                            if (line.trim().isEmpty()) continue;
                            XWPFParagraph bullet = document.createParagraph();
                            bullet.setIndentationLeft(360);
                            XWPFRun bulletRun = bullet.createRun();
                            bulletRun.setText("• " + line.replaceAll("^\\s*[•\\-*]\\s*", ""));
                            bulletRun.setFontSize(10);
                            bulletRun.setFontFamily("Arial");
                        }
                    }
                }
            }

            // Education Section
            if (content.getEducation() != null && !content.getEducation().isEmpty()) {
                addDocxSectionHeader(document, "EDUCATION");
                for (GeneratedContentDto.EducationItem edu : content.getEducation()) {
                    XWPFParagraph eduPara = document.createParagraph();
                    XWPFRun eduRun = eduPara.createRun();
                    eduRun.setText(edu.getSchool() + " -- " + edu.getDegree() + " (" + edu.getYear() + ")");
                    eduRun.setFontSize(11);
                    eduRun.setFontFamily("Arial");
                }
            }

            document.write(out);
            return out.toByteArray();
        }
    }

    private void addDocxSectionHeader(XWPFDocument document, String title) {
        XWPFParagraph space = document.createParagraph();
        space.createRun().setFontSize(6); // minor spacing

        XWPFParagraph header = document.createParagraph();
        XWPFRun run = header.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(13);
        run.setFontFamily("Arial");
        run.setColor("2B4C7E"); // deep steel blue
    }

    // PDF Generation State Tracking Helper
    private static class PdfState {
        PDDocument doc;
        PDPage currentPage;
        PDPageContentStream stream;
        float y;
        float margin = 50;
        float width = PDRectangle.LETTER.getWidth() - 100;
        float leading = 14;

        PdfState() {
            doc = new PDDocument();
            addNewPage();
        }

        void addNewPage() {
            try {
                if (stream != null) {
                    stream.close();
                }
                currentPage = new PDPage(PDRectangle.LETTER);
                doc.addPage(currentPage);
                stream = new PDPageContentStream(doc, currentPage);
                y = PDRectangle.LETTER.getHeight() - margin;
            } catch (IOException e) {
                throw new RuntimeException("Failed to add PDF page", e);
            }
        }

        void writeLine(String text, PDFont font, float fontSize, boolean indent) throws IOException {
            if (y < margin + leading * 2) {
                addNewPage();
            }

            List<String> lines = wrapText(text, width - (indent ? 15 : 0), font, fontSize);
            for (String line : lines) {
                if (y < margin + leading) {
                    addNewPage();
                }
                stream.beginText();
                stream.setFont(font, fontSize);
                stream.newLineAtOffset(margin + (indent ? 15 : 0), y);
                stream.showText(line);
                stream.endText();
                y -= leading;
            }
        }

        void addSpacing(float height) {
            y -= height;
        }

        byte[] closeAndGet() throws IOException {
            if (stream != null) {
                stream.close();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            doc.close();
            return out.toByteArray();
        }

        private List<String> wrapText(String text, float maxW, PDFont font, float size) throws IOException {
            List<String> wrapped = new ArrayList<>();
            String[] words = text.split("\\s+");
            StringBuilder builder = new StringBuilder();

            for (String word : words) {
                if (word.isEmpty()) continue;
                String testStr = builder.length() == 0 ? word : builder.toString() + " " + word;
                float widthPoints = font.getStringWidth(testStr) / 1000 * size;
                if (widthPoints > maxW) {
                    wrapped.add(builder.toString());
                    builder = new StringBuilder(word);
                } else {
                    if (builder.length() > 0) builder.append(" ");
                    builder.append(word);
                }
            }
            if (builder.length() > 0) {
                wrapped.add(builder.toString());
            }
            return wrapped;
        }
    }

    public byte[] generatePdf(String studentName, GeneratedContentDto content) throws IOException {
        PdfState state = new PdfState();

        // Title
        state.writeLine(studentName, PDType1Font.HELVETICA_BOLD, 18, false);
        state.addSpacing(10);

        // Summary
        if (content.getSummary() != null && !content.getSummary().trim().isEmpty()) {
            addPdfSectionHeader(state, "PROFESSIONAL SUMMARY");
            state.writeLine(content.getSummary(), PDType1Font.HELVETICA, 10, false);
            state.addSpacing(10);
        }

        // Skills
        if (content.getSkills() != null && !content.getSkills().isEmpty()) {
            addPdfSectionHeader(state, "SKILLS & EXPERTISE");
            state.writeLine(String.join("  |  ", content.getSkills()), PDType1Font.HELVETICA, 10, false);
            state.addSpacing(10);
        }

        // Experience
        if (content.getExperience() != null && !content.getExperience().isEmpty()) {
            addPdfSectionHeader(state, "PROFESSIONAL EXPERIENCE");
            for (GeneratedContentDto.ExperienceItem exp : content.getExperience()) {
                String header = exp.getCompany() + "  -  " + exp.getRole() + " (" + exp.getDuration() + ")";
                state.writeLine(header, PDType1Font.HELVETICA_BOLD, 10, false);
                
                if (exp.getDescription() != null) {
                    for (String line : exp.getDescription().split("\n")) {
                        if (line.trim().isEmpty()) continue;
                        String bulletText = "• " + line.replaceAll("^\\s*[•\\-*]\\s*", "");
                        state.writeLine(bulletText, PDType1Font.HELVETICA, 9.5f, true);
                    }
                }
                state.addSpacing(5);
            }
            state.addSpacing(5);
        }

        // Projects
        if (content.getProjects() != null && !content.getProjects().isEmpty()) {
            addPdfSectionHeader(state, "ACADEMIC & PERSONAL PROJECTS");
            for (GeneratedContentDto.ProjectItem proj : content.getProjects()) {
                String header = proj.getName() + (proj.getTechnologies() != null && !proj.getTechnologies().isEmpty() ? " (" + proj.getTechnologies() + ")" : "");
                state.writeLine(header, PDType1Font.HELVETICA_BOLD, 10, false);

                if (proj.getDescription() != null) {
                    for (String line : proj.getDescription().split("\n")) {
                        if (line.trim().isEmpty()) continue;
                        String bulletText = "• " + line.replaceAll("^\\s*[•\\-*]\\s*", "");
                        state.writeLine(bulletText, PDType1Font.HELVETICA, 9.5f, true);
                    }
                }
                state.addSpacing(5);
            }
            state.addSpacing(5);
        }

        // Education
        if (content.getEducation() != null && !content.getEducation().isEmpty()) {
            addPdfSectionHeader(state, "EDUCATION");
            for (GeneratedContentDto.EducationItem edu : content.getEducation()) {
                String val = edu.getSchool() + "  -  " + edu.getDegree() + " (" + edu.getYear() + ")";
                state.writeLine(val, PDType1Font.HELVETICA, 10, false);
            }
        }

        return state.closeAndGet();
    }

    private void addPdfSectionHeader(PdfState state, String title) throws IOException {
        state.addSpacing(6);
        state.writeLine(title, PDType1Font.HELVETICA_BOLD, 12, false);
        state.addSpacing(4);
    }
}
