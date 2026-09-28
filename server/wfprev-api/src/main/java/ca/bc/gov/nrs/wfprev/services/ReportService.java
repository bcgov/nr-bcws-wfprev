package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfone.common.service.api.ServiceException;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectFuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsCulturalPrescribedFireReportEntity;
import ca.bc.gov.nrs.wfprev.data.entities.ResultsFuelManagementReportEntity;
import ca.bc.gov.nrs.wfprev.data.models.ReportRequestModel;
import ca.bc.gov.nrs.wfprev.data.models.ReportType;
import ca.bc.gov.nrs.wfprev.data.params.FeatureQueryParams;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectCulturalPrescribedFireReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProjectFuelManagementReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ProgramAreaRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ResultsCulturalPrescribedFireReportRepository;
import ca.bc.gov.nrs.wfprev.data.repositories.ResultsFuelManagementReportRepository;
import ca.bc.gov.nrs.wfprev.services.spatial.ResultsSpatialExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
public class ReportService {

    @Value("${spring.application.baseUrl}")
    private String baseUrl;

    private static final String PROJECT_URL_PREFIX = "/edit-project?projectGuid=";

    private static final String FISCAL_QUERY_STRING = "&tab=fiscal&fiscalGuid=";

    private final ProjectFuelManagementReportRepository projectFuelManagementReportRepository;
    private final ProjectCulturalPrescribedFireReportRepository projectCulturalPrescribedFireReportRepository;
    private final ResultsFuelManagementReportRepository resultsFuelManagementReportRepository;
    private final ResultsCulturalPrescribedFireReportRepository resultsCulturalPrescribedFireReportRepository;
    private final ProgramAreaRepository programAreaRepository;
    private final FeaturesService featuresService;
    private final CsvReportGenerator csvReportGenerator;
    private final XlsxReportGenerator xlsxReportGenerator;
    private final ResultsSpatialExporter resultsSpatialExporter;

    public ReportService(ProjectFuelManagementReportRepository projectFuelManagementReportRepository,
                         ProjectCulturalPrescribedFireReportRepository projectCulturalPrescribedFireReportRepository,
                         ResultsFuelManagementReportRepository resultsFuelManagementReportRepository,
                         ResultsCulturalPrescribedFireReportRepository resultsCulturalPrescribedFireReportRepository,
                         ProgramAreaRepository programAreaRepository,
                         FeaturesService featuresService,
                         CsvReportGenerator csvReportGenerator,
                         XlsxReportGenerator xlsxReportGenerator,
                         ResultsSpatialExporter resultsSpatialExporter) {
        this.projectFuelManagementReportRepository = projectFuelManagementReportRepository;
        this.projectCulturalPrescribedFireReportRepository = projectCulturalPrescribedFireReportRepository;
        this.resultsFuelManagementReportRepository = resultsFuelManagementReportRepository;
        this.resultsCulturalPrescribedFireReportRepository = resultsCulturalPrescribedFireReportRepository;
        this.programAreaRepository = programAreaRepository;
        this.featuresService = featuresService;
        this.csvReportGenerator = csvReportGenerator;
        this.xlsxReportGenerator = xlsxReportGenerator;
        this.resultsSpatialExporter = resultsSpatialExporter;
    }

    public CsvReportGenerator getCsvReportGenerator() {
        return csvReportGenerator;
    }

    public XlsxReportGenerator getXlsxReportGenerator() {
        return xlsxReportGenerator;
    }

    public static class ProjectReportDataBundle {
        List<ProjectFuelManagementReportEntity> fuel;
        List<ProjectCulturalPrescribedFireReportEntity> crx;

        ProjectReportDataBundle(List<ProjectFuelManagementReportEntity> fuel,
                                List<ProjectCulturalPrescribedFireReportEntity> crx) {
            this.fuel = fuel;
            this.crx = crx;
        }
    }

    public static class ResultsReportDataBundle {
        List<ResultsFuelManagementReportEntity> fuel;
        List<ResultsCulturalPrescribedFireReportEntity> crx;

        ResultsReportDataBundle(List<ResultsFuelManagementReportEntity> fuel,
                                List<ResultsCulturalPrescribedFireReportEntity> crx) {
            this.fuel = fuel;
            this.crx = crx;
        }
    }

    private List<ReportRequestModel.Project> resolveProjectsToReport(ReportRequestModel request) {
        if (request == null || ((request.getProjects() == null || request.getProjects().isEmpty()) && request.getProjectFilter() == null)) {
            throw new IllegalArgumentException("At least one project or a filter is required");
        }

        List<ReportRequestModel.Project> projectsToReport = request.getProjects();

        if (projectsToReport == null || projectsToReport.isEmpty()) {
            // Fetch projects using filter
            FeatureQueryParams filter = request.getProjectFilter();
            var entities = featuresService.findFilteredProjects(filter, 1, Integer.MAX_VALUE, null, null);
            Map<UUID, List<UUID>> fiscalGuidsByProject = featuresService.findFilteredProjectFiscalGuids(
                    entities.stream().map(ProjectEntity::getProjectGuid).toList(),
                    filter.getFiscalYears(),
                    filter.getActivityCategoryCodes(),
                    filter.getPlanFiscalStatusCodes()
            );

            projectsToReport = new ArrayList<>();
            for (var entity : entities) {
                ReportRequestModel.Project p = new ReportRequestModel.Project();
                p.setProjectGuid(entity.getProjectGuid());
                // No matching fiscal means the whole project is reported (see collectRows)
                p.setProjectFiscalGuids(new ArrayList<>(
                        fiscalGuidsByProject.getOrDefault(entity.getProjectGuid(), List.of())));
                projectsToReport.add(p);
            }
        }
        return projectsToReport;
    }

    private ProjectReportDataBundle resolveProjectReportData(ReportRequestModel request) {
        List<ReportRequestModel.Project> projectsToReport = resolveProjectsToReport(request);

        List<ProjectFuelManagementReportEntity> fuel = collectRows(projectsToReport,
                projectFuelManagementReportRepository::findByProjectPlanFiscalGuidIn,
                projectFuelManagementReportRepository::findByProjectGuidIn,
                ProjectFuelManagementReportEntity::getProjectGuid,
                ProjectFuelManagementReportEntity::getProjectPlanFiscalGuid);
        List<ProjectCulturalPrescribedFireReportEntity> crx = collectRows(projectsToReport,
                projectCulturalPrescribedFireReportRepository::findByProjectPlanFiscalGuidIn,
                projectCulturalPrescribedFireReportRepository::findByProjectGuidIn,
                ProjectCulturalPrescribedFireReportEntity::getProjectGuid,
                ProjectCulturalPrescribedFireReportEntity::getProjectPlanFiscalGuid);

        return new ProjectReportDataBundle(fuel, crx);
    }

    private ResultsReportDataBundle resolveResultsReportData(ReportRequestModel request) {
        List<ReportRequestModel.Project> projectsToReport = resolveProjectsToReport(request);

        List<ResultsFuelManagementReportEntity> fuel = collectRows(projectsToReport,
                resultsFuelManagementReportRepository::findByProjectPlanFiscalGuidIn,
                resultsFuelManagementReportRepository::findByProjectGuidIn,
                ResultsFuelManagementReportEntity::getProjectGuid,
                ResultsFuelManagementReportEntity::getProjectPlanFiscalGuid);
        List<ResultsCulturalPrescribedFireReportEntity> crx = collectRows(projectsToReport,
                resultsCulturalPrescribedFireReportRepository::findByProjectPlanFiscalGuidIn,
                resultsCulturalPrescribedFireReportRepository::findByProjectGuidIn,
                ResultsCulturalPrescribedFireReportEntity::getProjectGuid,
                ResultsCulturalPrescribedFireReportEntity::getProjectPlanFiscalGuid);

        return new ResultsReportDataBundle(fuel, crx);
    }

    /**
     * Loads a report view's rows for all the projects in a few set-based queries, then returns them in project
     * order. A project with fiscal GUIDs gets only its rows for those fiscals; a project without any gets all of
     * its rows, including those whose project_plan_fiscal_guid is null.
     */
    private static <E> List<E> collectRows(List<ReportRequestModel.Project> projects,
                                           Function<Collection<UUID>, List<E>> findByFiscalGuids,
                                           Function<Collection<UUID>, List<E>> findByProjectGuids,
                                           Function<E, UUID> projectGuidOf,
                                           Function<E, UUID> fiscalGuidOf) {
        Set<UUID> fiscalGuids = new LinkedHashSet<>();
        Set<UUID> wholeProjectGuids = new LinkedHashSet<>();
        for (ReportRequestModel.Project p : projects) {
            UUID projectGuid = Objects.requireNonNull(p.getProjectGuid(), "projectGuid is required");
            if (p.getProjectFiscalGuids() != null && !p.getProjectFiscalGuids().isEmpty()) {
                fiscalGuids.addAll(p.getProjectFiscalGuids());
            } else {
                wholeProjectGuids.add(projectGuid);
            }
        }

        // A row can be loaded twice when a project is requested both whole and by fiscal; keep one copy.
        Set<E> loaded = new LinkedHashSet<>();
        loaded.addAll(findInChunks(fiscalGuids, findByFiscalGuids));
        loaded.addAll(findInChunks(wholeProjectGuids, findByProjectGuids));

        Map<UUID, List<E>> rowsByProject = new HashMap<>();
        for (E row : loaded) {
            if (row != null) {
                rowsByProject.computeIfAbsent(projectGuidOf.apply(row), k -> new ArrayList<>()).add(row);
            }
        }

        List<E> rows = new ArrayList<>();
        for (ReportRequestModel.Project p : projects) {
            List<E> projectRows = rowsByProject.getOrDefault(p.getProjectGuid(), List.of());
            if (p.getProjectFiscalGuids() != null && !p.getProjectFiscalGuids().isEmpty()) {
                Set<UUID> wanted = new HashSet<>(p.getProjectFiscalGuids());
                projectRows.stream().filter(r -> wanted.contains(fiscalGuidOf.apply(r))).forEach(rows::add);
            } else {
                rows.addAll(projectRows);
            }
        }
        return rows;
    }

    private static <E> List<E> findInChunks(Collection<UUID> guids, Function<Collection<UUID>, List<E>> finder) {
        List<UUID> all = new ArrayList<>(guids);
        List<E> rows = new ArrayList<>();
        for (int from = 0; from < all.size(); from += FeaturesService.IN_CLAUSE_CHUNK_SIZE) {
            rows.addAll(finder.apply(all.subList(from, Math.min(from + FeaturesService.IN_CLAUSE_CHUNK_SIZE, all.size()))));
        }
        return rows;
    }

    private ProjectReportDataBundle getPreparedProjectReportData(ReportRequestModel request) {
        ProjectReportDataBundle data = resolveProjectReportData(request);

        // Remove nulls up front (defensive)
        data.fuel.removeIf(Objects::isNull);
        data.crx.removeIf(Objects::isNull);

        // Pre-process
        data.fuel.forEach(this::setProjectFuelManagementFields);
        data.crx.forEach(this::setProjectCrxFields);

        // If absolutely nothing to write, fail early
        if (data.fuel.isEmpty() && data.crx.isEmpty()) {
            throw new IllegalArgumentException("No fiscal data found for the provided projects");
        }

        return data;
    }

    private ResultsReportDataBundle getPreparedResultsReportData(ReportRequestModel request) {
        ResultsReportDataBundle data = resolveResultsReportData(request);

        // Remove nulls up front (defensive)
        data.fuel.removeIf(Objects::isNull);
        data.crx.removeIf(Objects::isNull);

        // Pre-process
        data.fuel.forEach(this::setResultsFuelManagementFields);
        data.crx.forEach(this::setResultsCrxFields);

        // If absolutely nothing to write, fail early
        if (data.fuel.isEmpty() && data.crx.isEmpty()) {
            throw new IllegalArgumentException("No fiscal data found for the provided projects");
        }

        return data;
    }

    /** The rows for a PROJECT_XLSX or RESULTS_XLSX export, as the report Lambda's input. */
    public XlsxReportGenerator.LambdaReportRequest prepareXlsxLambdaRequest(ReportRequestModel request) {
        if (request != null && ReportType.RESULTS_XLSX.equals(request.getReportType())) {
            ResultsReportDataBundle data = getPreparedResultsReportData(request);
            // Names match the files in the RESULTS_SPATIAL ZIP, which is exported alongside.
            resultsSpatialExporter.applyFileNames(data.fuel, data.crx);
            return xlsxReportGenerator.buildResultsRequest(data.fuel, data.crx);
        }
        ProjectReportDataBundle data = getPreparedProjectReportData(request);
        return xlsxReportGenerator.buildProjectRequest(data.fuel, data.crx);
    }

    /**
     * Writes the ZIP of Shapefiles for the RESULTS export.
     *
     * @return false, having written nothing, when none of the exported activities has a spatial file
     */
    public boolean exportResultsSpatialZip(ReportRequestModel request, OutputStream outputStream) throws IOException {
        ResultsReportDataBundle data = getPreparedResultsReportData(request);
        return resultsSpatialExporter.writeZip(data.fuel, data.crx, outputStream);
    }

    public void writeCsvZipFromEntities(ReportRequestModel request, OutputStream zipOutStream) throws ServiceException {
        if (request != null && ReportType.RESULTS_CSV.equals(request.getReportType())) {
            writeResultsCsvZipFromEntities(request, zipOutStream);
        } else {
            writeProjectCsvZipFromEntities(request, zipOutStream);
        }
    }

    public void writeProjectCsvZipFromEntities(ReportRequestModel request, OutputStream zipOutStream) throws ServiceException {
        ProjectReportDataBundle data = getPreparedProjectReportData(request);
        csvReportGenerator.generateCsvZip(data.fuel, data.crx, zipOutStream);
    }

    public void writeResultsCsvZipFromEntities(ReportRequestModel request, OutputStream zipOutStream) throws ServiceException {
        ResultsReportDataBundle data = getPreparedResultsReportData(request);
        csvReportGenerator.generateResultsCsvZip(data.fuel, data.crx, zipOutStream);
    }

    private void setProjectFuelManagementFields(ProjectFuelManagementReportEntity entity) {
        String urlPrefix = baseUrl + PROJECT_URL_PREFIX;
        if (entity != null) {
            if (entity.getProjectGuid() != null) {
                entity.setLinkToProject(urlPrefix + entity.getProjectGuid());
            }

            if (entity.getProjectPlanFiscalGuid() != null) {
                entity.setLinkToFiscalActivity(
                        urlPrefix + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());
            }

            if (entity.getProgramAreaGuid() != null) {
                programAreaRepository.findById(entity.getProgramAreaGuid())
                        .ifPresent(programArea -> entity.setBusinessArea(programArea.getProgramAreaName()));
            }

            // 2025 -> 2025/26 format
            entity.setFiscalYear(formatFiscalYearIfNumeric(entity.getFiscalYear()));
        }
    }

    private void setProjectCrxFields(ProjectCulturalPrescribedFireReportEntity entity) {
        String urlPrefix = baseUrl + PROJECT_URL_PREFIX;
        if (entity != null) {
            if (entity.getProjectGuid() != null) {
                entity.setLinkToProject(urlPrefix + entity.getProjectGuid());
                if (entity.getProjectPlanFiscalGuid() != null) {
                    entity.setLinkToFiscalActivity(
                            urlPrefix + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());
                }
            }

            if (entity.getProgramAreaGuid() != null) {
                programAreaRepository.findById(entity.getProgramAreaGuid())
                        .ifPresent(programArea -> entity.setBusinessArea(programArea.getProgramAreaName()));
            }

            // 2025 -> 2025/26 format
            entity.setFiscalYear(formatFiscalYearIfNumeric(entity.getFiscalYear()));
        }
    }

    private void setResultsFuelManagementFields(ResultsFuelManagementReportEntity entity) {
        String urlPrefix = baseUrl + PROJECT_URL_PREFIX;
        if (entity != null) {
            if (entity.getProjectGuid() != null) {
                entity.setLinkToProject(urlPrefix + entity.getProjectGuid());
            }

            if (entity.getProjectPlanFiscalGuid() != null) {
                entity.setLinkToFiscalActivity(
                        urlPrefix + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());
            }

            entity.setFiscalYear(formatFiscalYearIfNumeric(entity.getFiscalYear()));
        }
    }

    private void setResultsCrxFields(ResultsCulturalPrescribedFireReportEntity entity) {
        String urlPrefix = baseUrl + PROJECT_URL_PREFIX;
        if (entity != null) {
            if (entity.getProjectGuid() != null) {
                entity.setLinkToProject(urlPrefix + entity.getProjectGuid());
                if (entity.getProjectPlanFiscalGuid() != null) {
                    entity.setLinkToFiscalActivity(
                            urlPrefix + entity.getProjectGuid() + FISCAL_QUERY_STRING + entity.getProjectPlanFiscalGuid());
                }
            }

            entity.setFiscalYear(formatFiscalYearIfNumeric(entity.getFiscalYear()));
        }
    }

    private String formatFiscalYearIfNumeric(String fiscalYear) {
        if (fiscalYear == null)
            return "";
        if (fiscalYear.matches("\\d{4}/\\d{2}"))
            return fiscalYear;
        try {
            int year = Integer.parseInt(fiscalYear);
            return year + "/" + String.format("%02d", (year + 1) % 100);
        } catch (NumberFormatException e) {
            return "";
        }
    }
}
