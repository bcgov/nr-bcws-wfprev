/* Create Tables */

CREATE TABLE "wfprev"."example_table"
(
	"example_guid" VARCHAR(36) NOT NULL,
	"example_code" varchar(10)	 NULL,
	"example_text" text NULL,
	"example_var" varchar(25)	 NULL,
	"example_num" decimal(10,3) NULL,
	"example_ind" varchar(1)	 NOT NULL DEFAULT 'N',
	"last_updated_timestamp" bigint NULL,
	"revision_count" decimal(10) NOT NULL DEFAULT 0,
	"create_user" varchar(64)	 NOT NULL,
	"create_date" timestamp NOT NULL DEFAULT current_date,
	"update_user" varchar(64)	 NOT NULL,
	"update_date" timestamp NOT NULL DEFAULT current_date
)
;

/* Create Table Comments, Sequences for Autonumber Columns */

COMMENT ON TABLE "wfprev"."example_table"
	IS 'And example table definition. Please update and add column comments too!'
;

/* Create Primary Keys, Indexes, Uniques, Checks */

ALTER TABLE "wfprev"."example_table" ADD CONSTRAINT "exmpl_pk"
	PRIMARY KEY ("example_guid")
;

/* Create Foreign Key Constraints */

ALTER TABLE "wfprev"."example_table" ADD CONSTRAINT "exmpl_fk"
	FOREIGN KEY ("example_code") REFERENCES "wfprev"."example_code" ("example_code") ON DELETE No Action ON UPDATE No Action
;