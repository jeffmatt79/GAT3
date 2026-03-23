package br.ufc.huwc.gat3.service;

import br.ufc.huwc.gat3.exception.PdfFormNotFoundException;
import br.ufc.huwc.gat3.exception.PdfGenerationException;
import br.ufc.huwc.gat3.exception.PdfTemplateNotFoundException;
import br.ufc.huwc.gat3.model.Discharge;
import br.ufc.huwc.gat3.repositories.DischargeRepository;
import br.ufc.huwc.gat3.repositories.DischargeRepositoryPDF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
// 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service responsible for generating discharge PDF documents.
 *
 * <p>This service loads a predefined PDF template and fills its form fields
 * with patient and discharge data retrieved from the database using
 * {@link DischargeRepository}. The resulting PDF is returned as a byte array
 * and also stored on disk.</p>
 *
 * <p>The PDF template must contain an AcroForm with fields matching the names
 * expected by the service.</p>
 *
 * <p>Technologies used:</p>
 * <ul>
 *   <li>Spring Service layer</li>
 *   <li>Apache PDFBox for PDF manipulation</li>
 *   <li>Spring Data JPA repository for database access</li>
 * </ul>
 *
 * 
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DischargePdfService {

    /**
     * Base directory where generated PDFs will be stored.
     * Can be configured via application properties.
     */
    @Value("${app.discharge.pdf.base-dir:files/discharge}")
    private String baseDir;

    /**
     * Repository used to retrieve discharge data from the database.
     */
    private final DischargeRepositoryPDF DischargeRepositoryPDF;

    /**
     * Optional template stream used for testing purposes.
     */
    InputStream templateStream;

    /**
     * Generates a discharge PDF based on the encounter ID.
     *
     * <p>The service retrieves the discharge entity from the database,
     * loads the PDF template, fills the form fields with discharge data,
     * flattens the form, and returns the generated PDF.</p>
     *
     * @param encounterId the identifier of the medical encounter
     * @return the generated PDF as a byte array
     * @throws PdfGenerationException if an error occurs during PDF generation
     */
    public byte[] generatePdf(Long encounterId) {

        try (PDDocument document = loadTemplate()) {

            // Retrieve discharge data from the database
            Discharge discharge = DischargeRepositoryPDF
                    .findByAghuDischargeId(encounterId)
                    .orElseThrow(() ->
                            new RuntimeException("Discharge not found for encounter: " + encounterId));

            PDAcroForm form = getForm(document);

            // Fill form fields using discharge data
            fillFields(form, discharge);

            // Flatten the form to prevent further editing
            form.flatten();

            return savePdf(document, encounterId);

        } catch (IOException e) {
            throw new PdfGenerationException("Error generating discharge PDF", e);
        }
    }

    /**
     * Loads the PDF template from the classpath or test stream.
     *
     * @return the loaded {@link PDDocument}
     * @throws IOException if the document cannot be loaded
     * @throws PdfTemplateNotFoundException if the template cannot be found
     */
    protected PDDocument loadTemplate() throws IOException {

        InputStream stream = loadTemplateStream();

        if (stream == null) {
           throw new PdfTemplateNotFoundException("PDF template not found");
        }

        return PDDocument.load(stream);
    }

    /**
     * Retrieves the template input stream.
     *
     * <p>If a test template stream is provided, it will be used instead
     * of the default template located in the resources folder.</p>
     *
     * @return the template input stream
     */
    protected InputStream loadTemplateStream() {
        return templateStream != null
                ? templateStream
                : getClass().getResourceAsStream("/templates/alta_template_compressed.pdf");
    }

    /**
     * Retrieves the AcroForm from the PDF document.
     *
     * @param document the loaded PDF document
     * @return the {@link PDAcroForm} object
     * @throws PdfFormNotFoundException if the document does not contain a form
     */
    private PDAcroForm getForm(PDDocument document) {

        PDAcroForm form = document.getDocumentCatalog().getAcroForm();

        if (form == null) {
           throw new PdfFormNotFoundException("PDF does not contain AcroForm");
        }

        return form;
    }

    /**
     * Fills the PDF form fields using data from the {@link Discharge} entity.
     *
     * @param form the PDF form
     * @param discharge the discharge entity containing patient data
     * @throws IOException if a field cannot be written
     */
    private void fillFields(PDAcroForm form, Discharge discharge) throws IOException {

    if (discharge == null) {
        return;
    }

    // ======================
    // PATIENT INFO
    // ======================

    if (discharge.getPatientInfo() != null) {

        Discharge.PatientInfo patient = discharge.getPatientInfo();

        setTextSafe(form, "Text-NPE3SrcX7Z", patient.getFullName(), 10);
        setTextSafe(form, "Text-qzmqWws9el", patient.getMotherName(), 9);
        setTextSafe(form, "Text-rQVfua2bEx", patient.getCnsOrCpf(), 9);
        setTextSafe(form, "Text-66omlA8yiR", patient.getAddress(), 8);
        setTextSafe(form, "Text-aQTo_ujYJ2", patient.getContacts(), 9);
        setTextSafe(form, "Text-Q0L7EuT_mj", patient.getGender(), 9);
        setTextSafe(form, "Text-SuDaQUWLAg", patient.getAghuRecord(), 9);

        if (patient.getBirthDate() != null) {
            setTextSafe(form, "Text-p-uF-nMAwd", patient.getBirthDate().toString(), 9);
        }
    }

    // ======================
    // SERVICE INFO
    // ======================

    if (discharge.getServiceInfo() != null) {

        Discharge.ServiceInfo service = discharge.getServiceInfo();

        setTextSafe(form, "Text-jCtS4NRYhL", service.getAmbulatoryName(), 9);
        setTextSafe(form, "Text-qH9IsGGYdL", service.getSubspecialtyOrServiceChief(), 9);
    }

    // ======================
    // AMBULATORY PROFILE
    // ======================

    if (discharge.getAmbulatoryProfile() != null) {

        Discharge.AmbulatoryProfile profile = discharge.getAmbulatoryProfile();

        setTextSafe(form, "Text-lgp3r9H7U8", profile.getInitialDiagnosisCid10(), 9);
        setTextSafe(form, "Text-5ME6x92Cp7", profile.getDischargeReason(), 9);

        if (profile.getAdequate() != null) {
            setTextSafe(form, "Text-_9bdDYnTpn", profile.getAdequate().toString(), 9);
        }
    }

    // ======================
    // DISCHARGE INFO
    // ======================

    setTextSafe(form, "Text-Am9TOK-aUW", discharge.getStatus(), 9);
    setTextSafe(form, "Text-cvT022DUTy", discharge.getClinicalSummary(), 9);
    setTextSafe(form, "Text-xGV2uI8Yx5", discharge.getTherapeuticGuidance(), 9);
    setTextSafe(form, "Text-MSk10a46O5", discharge.getSpecializedReturnGuidance(), 9);
    setTextSafe(form, "Text-eAAzxZZkUB", discharge.getAdditionalInformation(), 9);
    setTextSafe(form, "Text-ekED2_fSJ4", discharge.getFollowUpSchedulingSuggestion(), 9);

    // ======================
    // PHYSICIAN
    // ======================

    setTextSafe(form, "Text-4QXrEf5JLb", discharge.getResponsiblePhysician(), 9);
    setTextSafe(form, "Text-1oCj91gl1k", discharge.getResponsiblePhysicianRegistration(), 9);

    if (discharge.getReportDate() != null) {
        setTextSafe(form, "Date-URxD_NVfqo", discharge.getReportDate().toString(), 9);
    }

    // ======================
    // PROBLEM LIST
    // ======================

    if (discharge.getProblemList() != null && !discharge.getProblemList().isEmpty()) {

        String problems = String.join("\n", discharge.getProblemList());

        setTextSafe(form, "Paragraph-1LaB2q5yTh", problems, 9);
    }

    // ======================
    // MEDICATIONS
    // ======================

    if (discharge.getMedications() != null && !discharge.getMedications().isEmpty()) {

        StringBuilder meds = new StringBuilder();

        for (Discharge.MedicationEntry m : discharge.getMedications()) {

            meds.append(m.getName() != null ? m.getName() : "")
                .append(" - ")
                .append(m.getDosage() != null ? m.getDosage() : "")
                .append(" - ")
                .append(m.getQuantity() != null ? m.getQuantity() : "")
                .append("\n");
        }

        setTextSafe(form, "Paragraph-LWGzHiMzvv", meds.toString(), 9);
    }
}

    /**
     * Safely assigns a value to a PDF field.
     *
     * <p>If the field does not exist or the value is empty,
     * the method will safely ignore the assignment.</p>
     *
     * @param form the PDF form
     * @param fieldName the name of the field
     * @param value the value to assign
     * @throws IOException if an error occurs while setting the field value
     */
    
    private void setTextSafe(
        PDAcroForm form,
        String fieldName,
        String value,
        int fontSize
) throws IOException {

    if (value == null || value.trim().isEmpty()) {
        return;
    }

    PDField field = form.getField(fieldName);

    if (field == null) {
        log.warn("Campo não encontrado no PDF: {}", fieldName);
        return;
    }

    if (!(field instanceof PDTextField)) {
        log.warn("Campo não é texto: {}", fieldName);
        return;
    }

    PDTextField textField = (PDTextField) field;

    String defaultAppearance = "/Helv " + fontSize + " Tf 0 g";
    textField.setDefaultAppearance(defaultAppearance);

    textField.setValue(value);
}
    /**
     * Saves the generated PDF to disk and returns its byte representation.
     *
     * @param document the PDF document
     * @param encounterId the encounter identifier
     * @return the generated PDF as a byte array
     * @throws IOException if the file cannot be written
     */
    private byte[] savePdf(PDDocument document, Long encounterId) throws IOException {

        Path dir = Paths.get(baseDir);

        // Ensure the directory exists
        Files.createDirectories(dir);

        Path filePath = dir.resolve("discharge-" + encounterId + ".pdf");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            document.save(out);

            byte[] pdfBytes = out.toByteArray();

            Files.write(filePath, pdfBytes);

            return pdfBytes;
        }
    }
} 