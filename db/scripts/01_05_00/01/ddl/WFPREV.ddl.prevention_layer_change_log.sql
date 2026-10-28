/* ---------------------------------------------------- */
/*  DBMS       : PostgreSQL                             */
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."prevention_layer_change_log"
(
	"project_guid" UUID NOT NULL,
	"last_updated" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)
TABLESPACE PG_DEFAULT
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON TABLE "wfprev"."prevention_layer_change_log"
	IS 'Prevention Layer Change Log tracks the last modification timestamp for each project to support geoserver layer sync jobs.'
;

COMMENT ON COLUMN "wfprev"."prevention_layer_change_log"."project_guid"
	IS 'project_guid is the unique identifier of the project whose layer features or attributes have changed.'
;

COMMENT ON COLUMN "wfprev"."prevention_layer_change_log"."last_updated"
	IS 'last_updated is the timestamp when the project or any of its child entities was last inserted, updated, or deleted.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."prevention_layer_change_log" ADD CONSTRAINT "prevention_layer_change_log_pk"
	PRIMARY KEY ("project_guid")
;

