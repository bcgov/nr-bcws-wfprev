package ca.bc.gov.nrs.wfprev;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.nimbusds.jose.shaded.gson.JsonDeserializer;
import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import com.nimbusds.jose.shaded.gson.JsonSerializer;

import ca.bc.gov.nrs.wfprev.controllers.ActivityBoundaryController;
import ca.bc.gov.nrs.wfprev.data.models.ActivityBoundaryModel;
import ca.bc.gov.nrs.wfprev.data.models.ActivityModel;
import ca.bc.gov.nrs.wfprev.services.ActivityBoundaryService;
import ca.bc.gov.nrs.wfprev.services.ActivityService;
import ca.bc.gov.nrs.wfprev.services.CoordinatesService;
import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(ActivityBoundaryController.class)
@Import({ TestSpringSecurity.class, TestcontainersConfiguration.class })
@MockBean(JpaMetamodelMappingContext.class)
class ActivityBoundaryControllerTest {

        @MockBean
        private ActivityBoundaryService activityBoundaryService;

        @MockBean
        private CoordinatesService coordinatesService;

        @MockBean
        private ActivityService activityService;

        @Autowired
        private MockMvc mockMvc;

        private Gson gson;

        @MockBean(name = "springSecurityAuditorAware")
        private AuditorAware<String> auditorAware;

        String activityBoundaryJson = """
                            {
                                "activityBoundaryGuid": "%s",
                                "activityGuid": "%s",
                                "systemStartTimestamp": %d,
                                "systemEndTimestamp": %d,
                                "collectionDate": %d,
                                "boundarySizeHa": 10.5,
                                "geometry": {
                                    "type": "MultiPolygon",
                                    "coordinates": [[[
                                        [-123.3656, 48.4284],
                                        [-123.3657, 48.4285],
                                        [-123.3658, 48.4284],
                                        [-123.3656, 48.4284]
                                    ]]]
                                }
                            }
                        """;

        @BeforeEach
        void setup() {
                GsonBuilder builder = new GsonBuilder();
                builder.serializeNulls()
                                .registerTypeAdapter(Date.class,
                                                (JsonSerializer<Date>) (src, typeOfSrc,
                                                                context) -> new JsonPrimitive(src.getTime()))
                                .registerTypeAdapter(Date.class,
                                                (JsonDeserializer<Date>) (json, typeOfT,
                                                                context) -> new Date(json.getAsLong()))
                                .serializeSpecialFloatingPointValues();
                gson = builder.create();
        }

        @Test
        @WithMockUser
        void testGetAllActivityBoundaries_Success() throws Exception {
                List<ActivityBoundaryModel> boundaries = List.of(
                                ActivityBoundaryModel.builder().activityBoundaryGuid(UUID.randomUUID().toString())
                                                .build(),
                                ActivityBoundaryModel.builder().activityBoundaryGuid(UUID.randomUUID().toString())
                                                .build());

                when(activityBoundaryService.getAllActivityBoundaries(anyString(), anyString(), anyString()))
                                .thenReturn(CollectionModel.of(boundaries));

                mockMvc.perform(get(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void testGetAllActivityBoundaries_RuntimeException() throws Exception {
                when(activityBoundaryService.getAllActivityBoundaries(anyString(), anyString(), anyString()))
                                .thenThrow(new RuntimeException("Unexpected error"));

                mockMvc.perform(get(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser
        void testGetAllActivityBoundaries_Exception() throws Exception {
                when(activityBoundaryService.getAllActivityBoundaries(anyString(), anyString(), anyString()))
                                .thenThrow(new RuntimeException("Runtime exception"));

                mockMvc.perform(get(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser
        void testGetActivityBoundary_Success() throws Exception {
                UUID boundaryId = UUID.randomUUID();
                ActivityBoundaryModel model = ActivityBoundaryModel.builder()
                                .activityBoundaryGuid(boundaryId.toString()).build();

                when(activityBoundaryService.getActivityBoundary(anyString(), anyString(), anyString(), anyString()))
                                .thenReturn(model);

                mockMvc.perform(get(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.activityBoundaryGuid").value(model.getActivityBoundaryGuid()));
        }

        @Test
        @WithMockUser
        void testGetActivityBoundary_Exception() throws Exception {
                UUID boundaryId = UUID.randomUUID();

                when(activityBoundaryService.getActivityBoundary(anyString(), anyString(), anyString(), anyString()))
                                .thenThrow(new RuntimeException("Runtime exception"));

                mockMvc.perform(get(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser
        void testGetActivityBoundary_NotFound() throws Exception {
                UUID boundaryId = UUID.randomUUID();

                when(activityBoundaryService.getActivityBoundary(anyString(), anyString(), anyString(), anyString()))
                                .thenThrow(new EntityNotFoundException("Boundary not found"));

                mockMvc.perform(get(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        void testGetActivityBoundary_EntityNotFoundException() throws Exception {
                UUID boundaryId = UUID.randomUUID();

                when(activityBoundaryService.getActivityBoundary(anyString(), anyString(), anyString(), anyString()))
                                .thenThrow(new EntityNotFoundException("Boundary not found"));

                mockMvc.perform(get(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        void testCreateActivityBoundary_Success() throws Exception {
                ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();
                ActivityModel activity = new ActivityModel();
                activity.setActivityGuid(requestModel.getActivityGuid());

                when(activityBoundaryService.createActivityBoundary(anyString(), anyString(), anyString(),
                                any(ActivityBoundaryModel.class)))
                                .thenReturn(requestModel);

                String requestJson = activityBoundaryJson.formatted(
                                requestModel.getActivityBoundaryGuid(),
                                requestModel.getActivityGuid(),
                                requestModel.getSystemStartTimestamp().getTime(),
                                requestModel.getSystemEndTimestamp().getTime() + 1000,
                                requestModel.getCollectionDate().getTime());

                mockMvc.perform(post(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.activityBoundaryGuid")
                                                .value(requestModel.getActivityBoundaryGuid()));

                // WFPREV-1223: is_spatial_added_ind is maintained by a database trigger, so the
                // controller must no longer touch ActivityService at all.
                verifyNoInteractions(activityService);
        }

        @Test
        @WithMockUser
        void testCreateActivityBoundary_DataIntegrityViolationException() throws Exception {
                ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

                when(activityBoundaryService.createActivityBoundary(anyString(), anyString(), anyString(),
                                any(ActivityBoundaryModel.class)))
                                .thenThrow(new DataIntegrityViolationException("Data Integrity Violation"));

                String requestJson = activityBoundaryJson.formatted(
                                requestModel.getActivityBoundaryGuid(),
                                requestModel.getActivityGuid(),
                                requestModel.getSystemStartTimestamp().getTime(),
                                requestModel.getSystemEndTimestamp().getTime() + 1000,
                                requestModel.getCollectionDate().getTime());

                mockMvc.perform(post(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        void testCreateActivityBoundary_Exception() throws Exception {
                ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

                when(activityBoundaryService.createActivityBoundary(anyString(), anyString(), anyString(),
                                any(ActivityBoundaryModel.class)))
                                .thenThrow(new RuntimeException("Runtime exception"));

                String requestJson = activityBoundaryJson.formatted(
                                requestModel.getActivityBoundaryGuid(),
                                requestModel.getActivityGuid(),
                                requestModel.getSystemStartTimestamp().getTime(),
                                requestModel.getSystemEndTimestamp().getTime() + 1000,
                                requestModel.getCollectionDate().getTime());

                mockMvc.perform(post(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser
        void testCreateActivityBoundary_IllegalArgumentException() throws Exception {
                ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

                when(activityBoundaryService.createActivityBoundary(anyString(), anyString(), anyString(),
                                any(ActivityBoundaryModel.class)))
                                .thenThrow(new IllegalArgumentException("Illegal Argument exception"));

                String requestJson = activityBoundaryJson.formatted(
                                requestModel.getActivityBoundaryGuid(),
                                requestModel.getActivityGuid(),
                                requestModel.getSystemStartTimestamp().getTime(),
                                requestModel.getSystemEndTimestamp().getTime() + 1000,
                                requestModel.getCollectionDate().getTime());

                mockMvc.perform(post(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        void testUpdateActivityBoundary_Success() throws Exception {
                ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();
                ActivityModel activity = new ActivityModel();
                activity.setActivityGuid(requestModel.getActivityGuid());
                activity.setIsSpatialAddedInd(false);

                when(activityBoundaryService.updateActivityBoundary(anyString(), anyString(), anyString(),
                                any(ActivityBoundaryModel.class)))
                                .thenReturn(requestModel);
                when(activityService.getActivity(anyString(), anyString(), anyString()))
                                .thenReturn(activity);

                String requestJson = activityBoundaryJson.formatted(
                                requestModel.getActivityBoundaryGuid(),
                                requestModel.getActivityGuid(),
                                requestModel.getSystemStartTimestamp().getTime(),
                                requestModel.getSystemEndTimestamp().getTime() + 1000,
                                requestModel.getCollectionDate().getTime());

                mockMvc.perform(put(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                                requestModel.getActivityBoundaryGuid())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.activityBoundaryGuid")
                                                .value(requestModel.getActivityBoundaryGuid()));

                // WFPREV-1223: the flag is now database-owned; the controller does not update
                // it.
                verifyNoInteractions(activityService);
        }

        @Test
        @WithMockUser
        void testUpdateActivityBoundary_EntityNotFoundException() throws Exception {
                UUID boundaryId = UUID.randomUUID();
                ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

                when(activityBoundaryService.updateActivityBoundary(anyString(), anyString(), anyString(),
                                any(ActivityBoundaryModel.class)))
                                .thenThrow(new EntityNotFoundException("Boundary not found"));

                String requestJson = activityBoundaryJson.formatted(
                                requestModel.getActivityBoundaryGuid(),
                                requestModel.getActivityGuid(),
                                requestModel.getSystemStartTimestamp().getTime(),
                                requestModel.getSystemEndTimestamp().getTime() + 1000,
                                requestModel.getCollectionDate().getTime());

                mockMvc.perform(put(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), boundaryId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        void testUpdateActivityBoundary_Exception() throws Exception {
                ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

                when(activityBoundaryService.updateActivityBoundary(anyString(), anyString(), anyString(),
                                any(ActivityBoundaryModel.class)))
                                .thenThrow(new RuntimeException("Runtime exception"));

                String requestJson = activityBoundaryJson.formatted(
                                requestModel.getActivityBoundaryGuid(),
                                requestModel.getActivityGuid(),
                                requestModel.getSystemStartTimestamp().getTime(),
                                requestModel.getSystemEndTimestamp().getTime() + 1000,
                                requestModel.getCollectionDate().getTime());

                mockMvc.perform(put(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                                requestModel.getActivityBoundaryGuid())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser
        void testUpdateActivityBoundary_IllegalArgumentException() throws Exception {
                ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

                when(activityBoundaryService.updateActivityBoundary(anyString(), anyString(), anyString(),
                                any(ActivityBoundaryModel.class)))
                                .thenThrow(new IllegalArgumentException("Runtime exception"));

                String requestJson = activityBoundaryJson.formatted(
                                requestModel.getActivityBoundaryGuid(),
                                requestModel.getActivityGuid(),
                                requestModel.getSystemStartTimestamp().getTime(),
                                requestModel.getSystemEndTimestamp().getTime() + 1000,
                                requestModel.getCollectionDate().getTime());

                mockMvc.perform(put(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                                requestModel.getActivityBoundaryGuid())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        void testUpdateActivityBoundary_DataIntegrityViolationException() throws Exception {
                ActivityBoundaryModel requestModel = buildActivityBoundaryRequestModel();

                when(activityBoundaryService.updateActivityBoundary(anyString(), anyString(), anyString(),
                                any(ActivityBoundaryModel.class)))
                                .thenThrow(new DataIntegrityViolationException("Data Integrity Violation"));

                String requestJson = activityBoundaryJson.formatted(
                                requestModel.getActivityBoundaryGuid(),
                                requestModel.getActivityGuid(),
                                requestModel.getSystemStartTimestamp().getTime(),
                                requestModel.getSystemEndTimestamp().getTime() + 1000,
                                requestModel.getCollectionDate().getTime());

                mockMvc.perform(put(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                                requestModel.getActivityBoundaryGuid())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        void testDeleteActivityBoundary_Success() throws Exception {
                UUID boundaryId = UUID.randomUUID();
                UUID projectId = UUID.randomUUID();
                UUID fiscalId = UUID.randomUUID();
                UUID activityId = UUID.randomUUID();

                ActivityModel updatedActivity = new ActivityModel();
                updatedActivity.setIsSpatialAddedInd(false);

                when(activityService.updateActivity(eq(projectId.toString()), eq(fiscalId.toString()),
                                any(ActivityModel.class)))
                                .thenReturn(updatedActivity);

                doNothing().when(activityBoundaryService).deleteActivityBoundary(
                                eq(projectId.toString()), eq(fiscalId.toString()), eq(activityId.toString()),
                                eq(boundaryId.toString()), anyBoolean());

                doNothing().when(coordinatesService).updateProjectCoordinates(eq(projectId.toString()));

                mockMvc.perform(delete(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                projectId, fiscalId, activityId, boundaryId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                                .andExpect(status().isNoContent());

                verify(activityBoundaryService).deleteActivityBoundary(eq(projectId.toString()),
                                eq(fiscalId.toString()), eq(activityId.toString()), eq(boundaryId.toString()),
                                eq(false));
                verify(coordinatesService).updateProjectCoordinates(eq(projectId.toString()));

                // WFPREV-1223: clearing the flag is the database's job now. The controller must
                // not
                // re-read the boundary list or write the activity - that recompute used to run
                // after
                // deleteActivityBoundary had already committed, which is how the flag got
                // stranded.
                verify(activityBoundaryService, never()).getAllActivityBoundaries(anyString(), anyString(),
                                anyString());
                verifyNoInteractions(activityService);
        }

        @Test
        @WithMockUser
        void testDeleteActivityBoundary_EntityNotFoundException() throws Exception {
                UUID boundaryId = UUID.randomUUID();
                UUID projectId = UUID.randomUUID();
                UUID fiscalId = UUID.randomUUID();
                UUID activityId = UUID.randomUUID();

                doThrow(new EntityNotFoundException("Activity Boundary not found"))
                                .when(activityBoundaryService).deleteActivityBoundary(
                                                anyString(), anyString(), anyString(), anyString(), anyBoolean());

                mockMvc.perform(delete(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                projectId, fiscalId, activityId, boundaryId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                                .andExpect(status().isNotFound());

                verify(coordinatesService, never()).updateProjectCoordinates(anyString());
        }

        @Test
        @WithMockUser
        void testDeleteActivityBoundary_Exception() throws Exception {
                UUID boundaryId = UUID.randomUUID();
                UUID projectId = UUID.randomUUID();
                UUID fiscalId = UUID.randomUUID();
                UUID activityId = UUID.randomUUID();

                doThrow(new RuntimeException("Runtime exception"))
                                .when(activityBoundaryService).deleteActivityBoundary(
                                                anyString(), anyString(), anyString(), anyString(), anyBoolean());

                mockMvc.perform(delete(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                projectId, fiscalId, activityId, boundaryId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser
        void testDeleteActivityBoundary_CoordinateUpdateFailureStillReportsError() throws Exception {
                UUID boundaryId = UUID.randomUUID();
                UUID projectId = UUID.randomUUID();
                UUID fiscalId = UUID.randomUUID();
                UUID activityId = UUID.randomUUID();

                doNothing().when(activityBoundaryService).deleteActivityBoundary(
                                anyString(), anyString(), anyString(), anyString(), anyBoolean());
                doThrow(new RuntimeException("centroid failure"))
                                .when(coordinatesService).updateProjectCoordinates(anyString());

                mockMvc.perform(delete(
                                "/projects/{projectId}/projectFiscals/{projectFiscalId}/activities/{activityId}/activityBoundary/{id}",
                                projectId, fiscalId, activityId, boundaryId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                                .andExpect(status().isInternalServerError());

                // WFPREV-1223: a failure *after* the boundary is deleted used to strand
                // is_spatial_added_ind, because the flag was recomputed here, downstream of an
                // already-committed delete. The database now maintains it inside that same
                // transaction, so this path can no longer leave the flag inconsistent.
                verify(activityBoundaryService).deleteActivityBoundary(
                                anyString(), anyString(), anyString(), anyString(), anyBoolean());
                verifyNoInteractions(activityService);
        }

        ActivityBoundaryModel buildActivityBoundaryRequestModel() {
                GeometryFactory geometryFactory = new GeometryFactory();

                Coordinate[] coordinates = {
                                new Coordinate(-123.3656, 48.4284),
                                new Coordinate(-123.3657, 48.4285),
                                new Coordinate(-123.3658, 48.4284),
                                new Coordinate(-123.3656, 48.4284)
                };

                LinearRing shell = geometryFactory.createLinearRing(coordinates);
                Polygon polygon = geometryFactory.createPolygon(shell);
                MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(new Polygon[] { polygon });

                return ActivityBoundaryModel.builder()
                                .activityBoundaryGuid(UUID.randomUUID().toString())
                                .activityGuid(UUID.randomUUID().toString())
                                .systemStartTimestamp(new Date())
                                .systemEndTimestamp(new Date())
                                .collectionDate(new Date())
                                .boundarySizeHa(new BigDecimal("10.5"))
                                .geometry(multiPolygon)
                                .build();
        }

}
