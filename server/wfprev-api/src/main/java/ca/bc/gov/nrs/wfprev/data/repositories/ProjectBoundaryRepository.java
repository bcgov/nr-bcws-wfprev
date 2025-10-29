package ca.bc.gov.nrs.wfprev.data.repositories;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ProjectBoundaryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ProjectBoundaryRepository extends CommonRepository<ProjectBoundaryEntity, String> {

    List<ProjectBoundaryEntity> findByProjectGuid(@NotNull UUID projectGuid);

    Optional<ProjectBoundaryEntity> findByProjectBoundaryGuid(@NotNull UUID id);

    void deleteByProjectBoundaryGuid(@NotNull UUID id);

    @Query(value = """
    WITH tile AS (
    SELECT
    ST_TileEnvelope(:z, :x, :y) AS env_3857,
    ST_Transform(ST_TileEnvelope(:z, :x, :y), 4326) AS env_4326
    ),
    provided AS (
    SELECT unnest(:projectGuids) AS project_guid
    ),
    latest AS (
    SELECT DISTINCT ON (pb.project_guid)
    pb.project_boundary_guid,
    pb.project_guid,
    pb.boundary_geometry,
    pb.system_start_timestamp
    FROM project_boundary pb
    JOIN provided p ON p.project_guid = pb.project_guid
    ORDER BY pb.project_guid,
    pb.system_start_timestamp DESC,
    pb.project_boundary_guid DESC
    ),
    candidates AS (
    SELECT l.*
    FROM latest l
    JOIN tile t ON TRUE
    WHERE l.boundary_geometry && t.env_4326
    AND ST_Intersects(l.boundary_geometry, t.env_4326)
    ),
    src AS (
    SELECT
    CAST(CAST(('x' || substr(md5(CAST(c.project_boundary_guid AS text)), 1, 16)) AS bit(64)) AS bigint) AS id,
    CASE
    WHEN :z <= 6  THEN ST_SimplifyPreserveTopology(ST_Transform(ST_MakeValid(c.boundary_geometry), 3857), 512)
    WHEN :z <= 9  THEN ST_SimplifyPreserveTopology(ST_Transform(ST_MakeValid(c.boundary_geometry), 3857), 64)
    WHEN :z <= 12 THEN ST_SimplifyPreserveTopology(ST_Transform(ST_MakeValid(c.boundary_geometry), 3857), 8)
    ELSE               ST_Transform(ST_MakeValid(c.boundary_geometry), 3857)
    END AS geom_3857,
    c.project_boundary_guid,
    c.project_guid
    FROM candidates c
    ),
    mvt AS (
    SELECT
    s.id,
    ST_AsMVTGeom(s.geom_3857, t.env_3857, 4096, 64, TRUE) AS geom,
    s.project_boundary_guid,
    s.project_guid
    FROM src s
    JOIN tile t ON TRUE
    WHERE s.geom_3857 && t.env_3857
    )
    SELECT COALESCE(
    (SELECT ST_AsMVT(mvt, 'project_boundary', 4096, 'geom', 'id') FROM mvt),
    decode('', 'hex')
    );
    """, nativeQuery = true)
    byte[] getProjectBoundaryTiles(
            @Param("z") int z,
            @Param("x") int x,
            @Param("y") int y,
            @Param("projectGuids") UUID[] projectGuids
    );
}
