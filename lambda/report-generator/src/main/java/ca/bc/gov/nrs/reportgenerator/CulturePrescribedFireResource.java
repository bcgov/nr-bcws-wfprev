package ca.bc.gov.nrs.reportgenerator;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import ca.bc.gov.nrs.reportgenerator.model.CulturePrescribedFireReportData;
import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Path("/culture-prescribed-fire")
public class CulturePrescribedFireResource extends AbstractJasperResource {

    private static final String TEST_REPORT_NAME = "WFPREV_CULTURE_PRESCRIBED_FIRE_JASPER.jasper";

    @Inject
    ReadOnlyStreamingService repo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(ExtendedMediaType.APPLICATION_XLSX)
    public Response generateXlsx(List<CulturePrescribedFireReportData> fields) {
        System.out.println("Received XLSX generation request with fields: " + fields);
        try {
            System.out.println("Compiled JasperReport successfully.");
            JRDataSource dataSource = new JRBeanCollectionDataSource(fields);
            JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME, new HashMap<>(), dataSource);
            System.out.println("Filled JasperReport successfully.");
            ByteArrayOutputStream xlsxStream = exportXlsx(jasperPrint);
            System.out.println("Exported XLSX successfully.");
            return Response.ok(xlsxStream.toByteArray())
                .type(ExtendedMediaType.APPLICATION_XLSX)
                .header("Content-Disposition", "attachment; filename=wfprev_culture_prescribed_fire.xlsx")
                .build();
        } catch (Exception e) {
            e.printStackTrace();
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Error: ").append(e.getMessage()).append("\n");
            for (StackTraceElement ste : e.getStackTrace()) {
                errorMsg.append(ste.toString()).append("\n");
            }
            return Response.serverError().entity(errorMsg.toString()).build();
        }
    }
}
