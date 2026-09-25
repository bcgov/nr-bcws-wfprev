/* ---------------------------------------------------- */
/*  DBMS       : PostgreSQL                             */
/* ---------------------------------------------------- */

/* Create Tables */

CREATE TABLE "wfprev"."report_export_job"
(
	"report_export_job_guid" UUID NOT NULL,    -- report_export_job_guid is a unique identifier for the record.
	"export_group_guid" UUID NOT NULL,    -- Export Group Guid groups the jobs started by one download request.
	"retried_from_job_guid" UUID NULL,    -- Retried From Job Guid is the job this one retries or re-runs.
	"owner_user_id" varchar(64)	 NOT NULL,    -- Owner User Id is the user who requested the export.
	"report_type_code" varchar(32)	 NOT NULL,    -- Report Type Code is the kind of file the job produces.
	"request_json" jsonb NOT NULL,    -- Request Json is the original report request, reused by retries.
	"description" varchar(4000)	 NOT NULL,    -- Description is the filter summary shown to the user.
	"file_name" varchar(255)	 NOT NULL,    -- File Name is the name the file is saved under.
	"status_code" varchar(16)	 NOT NULL,    -- Status Code is the job status: PREPARING, READY, NO_FILES or FAILED.
	"s3_object_key" varchar(1024)	 NULL,    -- S3 Object Key is the location of the finished file in the export bucket.
	"error_code" varchar(16)	 NULL,    -- Error Code is the failure category: TEMPORARY, DATA or LIMIT.
	"error_message" varchar(1000)	 NULL,    -- Error Message is the plain-language failure message shown to the user.
	"retryable_ind" boolean NULL,    -- Retryable Ind is true when retrying the failed job can succeed.
	"request_timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- Request Timestamp is when the export was requested.
	"started_timestamp" TIMESTAMP NULL,    -- Started Timestamp is when a worker claimed the job.
	"completed_timestamp" TIMESTAMP NULL,    -- Completed Timestamp is when the job reached a final status.
	"downloaded_timestamp" TIMESTAMP NULL,    -- Downloaded Timestamp is when a download link was first issued.
	"dismissed_ind" boolean NOT NULL DEFAULT false,    -- Dismissed Ind is true once the user has cleared the job from the download tray.
	"revision_count" decimal(10) NOT NULL DEFAULT 0,    -- REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.
	"create_user" varchar(64)	 NOT NULL,    -- CREATE_USER is an audit column that indicates the user that created the record.
	"create_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- CREATE_DATE is the date and time the row of data was created.
	"update_user" varchar(64)	 NOT NULL,    -- UPDATE_USER is an audit column that indicates the user that updated the record.
	"update_date" DATE NOT NULL DEFAULT CURRENT_TIMESTAMP    -- UPDATE_DATE is the date and time the row of data was updated.
)
TABLESPACE	PG_DEFAULT
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON TABLE "wfprev"."report_export_job"
	IS 'Report Export Job tracks one file produced by a report download request: its status, where the file is stored, and the original request so it can be retried.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."report_export_job_guid"
	IS 'report_export_job_guid is a unique identifier for the record.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."export_group_guid"
	IS 'Export Group Guid groups the jobs started by one download request, for example the RESULTS spreadsheet and its spatial ZIP.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."retried_from_job_guid"
	IS 'Retried From Job Guid is the job this one retries or re-runs. It is null for a job the user requested directly.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."owner_user_id"
	IS 'Owner User Id is the user who requested the export. Only this user can see the job or download its file.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."report_type_code"
	IS 'Report Type Code is the kind of file the job produces: PROJECT_CSV, PROJECT_XLSX, RESULTS_CSV, RESULTS_XLSX or RESULTS_SPATIAL.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."request_json"
	IS 'Request Json is the original report request (report type and filters). Retries reuse it so they produce what the user first asked for.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."description"
	IS 'Description is the filter summary shown to the user in the download tray.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."file_name"
	IS 'File Name is the name the file is saved under.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."status_code"
	IS 'Status Code is the job status: PREPARING, READY, NO_FILES (nothing to include) or FAILED.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."s3_object_key"
	IS 'S3 Object Key is the location of the finished file in the export bucket. Files are deleted from the bucket 24 hours after they are created.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."error_code"
	IS 'Error Code is the failure category: TEMPORARY, DATA or LIMIT.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."error_message"
	IS 'Error Message is the plain-language failure message shown to the user. Technical details are only logged.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."retryable_ind"
	IS 'Retryable Ind is true when retrying the failed job can succeed.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."request_timestamp"
	IS 'Request Timestamp is when the export was requested.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."started_timestamp"
	IS 'Started Timestamp is when a worker claimed the job. It is null while the job is waiting to start.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."completed_timestamp"
	IS 'Completed Timestamp is when the job reached a final status.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."downloaded_timestamp"
	IS 'Downloaded Timestamp is when a download link for the file was first issued.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."dismissed_ind"
	IS 'Dismissed Ind is true once the user has cleared the job from the download tray.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."revision_count"
	IS 'REVISION_COUNT is the number of times that the row of data has been changed. The column is used for optimistic locking via application code.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."create_user"
	IS 'CREATE_USER is an audit column that indicates the user that created the record.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."create_date"
	IS 'CREATE_DATE is the date and time the row of data was created.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."update_user"
	IS 'UPDATE_USER is an audit column that indicates the user that updated the record.'
;

COMMENT ON COLUMN "wfprev"."report_export_job"."update_date"
	IS 'UPDATE_DATE is the date and time the row of data was updated.'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."report_export_job" ADD CONSTRAINT "rptexpjob_pk"
	PRIMARY KEY ("report_export_job_guid")
;

ALTER TABLE "wfprev"."report_export_job" ADD CONSTRAINT "rptexpjob_status_code_chk"
	CHECK ("status_code" IN ('PREPARING', 'READY', 'NO_FILES', 'FAILED'))
;

ALTER TABLE "wfprev"."report_export_job" ADD CONSTRAINT "rptexpjob_error_code_chk"
	CHECK ("error_code" IS NULL OR "error_code" IN ('TEMPORARY', 'DATA', 'LIMIT'))
;

ALTER TABLE "wfprev"."report_export_job" ADD CONSTRAINT "rptexpjob_report_type_code_chk"
	CHECK ("report_type_code" IN ('PROJECT_CSV', 'PROJECT_XLSX', 'RESULTS_CSV', 'RESULTS_XLSX', 'RESULTS_SPATIAL'))
;

CREATE INDEX "rptexpjob_owner_request_idx" ON "wfprev"."report_export_job" ("owner_user_id" ASC, "request_timestamp" DESC)
;

CREATE INDEX "rptexpjob_status_idx" ON "wfprev"."report_export_job" ("status_code" ASC)
;
