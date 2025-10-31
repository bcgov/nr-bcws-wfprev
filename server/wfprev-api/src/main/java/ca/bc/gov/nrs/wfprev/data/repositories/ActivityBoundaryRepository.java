package ca.bc.gov.nrs.wfprev.data.repositories;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ca.bc.gov.nrs.wfprev.common.repository.CommonRepository;
import ca.bc.gov.nrs.wfprev.data.entities.ActivityBoundaryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ActivityBoundaryRepository extends CommonRepository<ActivityBoundaryEntity, String> {

    List<ActivityBoundaryEntity> findByActivityGuid(@NotNull UUID activityGuid);

    Optional<ActivityBoundaryEntity> findByActivityBoundaryGuid(@NotNull UUID id);

    void deleteByActivityBoundaryGuid(@NotNull UUID id);

    @Query(value = """
    WITH tile AS (
      SELECT
        ST_TileEnvelope(CAST(:z AS integer), CAST(:x AS integer), CAST(:y AS integer))                     AS env_3857,
        ST_Transform(ST_TileEnvelope(CAST(:z AS integer), CAST(:x AS integer), CAST(:y AS integer)), 4326) AS env_4326
    ),
    provided AS (
      SELECT unnest(:projectGuids) AS project_guid
    ),
    latest AS (
      SELECT DISTINCT ON (ab.activity_guid)
             ab.activity_boundary_guid,
             ab.activity_guid,
             ab.geometry,
             ab.system_start_timestamp,
             pf.fiscal_year
      FROM wfprev.activity_boundary ab
      JOIN wfprev.activity a             ON a.activity_guid = ab.activity_guid
      JOIN wfprev.project_plan_fiscal pf ON pf.project_plan_fiscal_guid = a.project_plan_fiscal_guid
      JOIN wfprev.project p              ON p.project_guid = pf.project_guid
      JOIN provided pr                   ON pr.project_guid = p.project_guid
      ORDER BY ab.activity_guid,
               ab.system_start_timestamp DESC,
               ab.activity_boundary_guid DESC
    ),
    
    candidates AS (
      SELECT l.*
      FROM latest l
      JOIN tile t ON TRUE
      WHERE l.geometry && t.env_4326
        AND ST_Intersects(l.geometry, t.env_4326)
    ),
    
    src AS (
      SELECT
        CAST(CAST(
          ('x' || substr(md5(CAST(c.activity_boundary_guid AS text)), 1, 16))
          AS bit(64)
        ) AS bigint) AS id,
        CASE
          WHEN CAST(:z AS integer) <=  6 THEN ST_SimplifyPreserveTopology(ST_Transform(ST_MakeValid(c.geometry), 3857), 512)
          WHEN CAST(:z AS integer) <=  9 THEN ST_SimplifyPreserveTopology(ST_Transform(ST_MakeValid(c.geometry), 3857), 64)
          WHEN CAST(:z AS integer) <= 12 THEN ST_SimplifyPreserveTopology(ST_Transform(ST_MakeValid(c.geometry), 3857), 8)
          ELSE                                ST_Transform(ST_MakeValid(c.geometry), 3857)
        END AS geom_3857,
        c.activity_boundary_guid,
        c.activity_guid,
        c.fiscal_year
      FROM candidates c
    ),
    
    mvt AS (
      SELECT
        s.id,
        ST_AsMVTGeom(s.geom_3857, t.env_3857, 4096, 64, TRUE) AS geom,
        s.activity_boundary_guid,
        s.activity_guid,
        s.fiscal_year
      FROM src s
      JOIN tile t ON TRUE
      WHERE s.geom_3857 && t.env_3857
    )
    
    SELECT COALESCE(
      (SELECT ST_AsMVT(mvt, 'activity_boundary', 4096, 'geom', 'id') FROM mvt),
      decode('', 'hex')
    );
    """, nativeQuery = true)
    byte[] getActivityBoundaryTiles(
            @Param("z") int z,
            @Param("x") int x,
            @Param("y") int y,
            @Param("projectGuids") UUID[] projectGuids
    );

}

