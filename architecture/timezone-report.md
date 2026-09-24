# BC Permanent Pacific Time (PCT, UTC-7): Impact Report for nr-bcws-wfprev

| | |
|---|---|
| **Prepared** | 2026-09-23 |
| **Repo state** | [bcgov/nr-bcws-wfprev](https://github.com/bcgov/nr-bcws-wfprev) `main` @ `e01a1fa` (2026-09-23) |
| **Deadline** | **Sunday 2026-11-01, 02:00 local (09:00 UTC)** |
| **Bottom line** | **Lowest risk of the three repos reviewed.** There are **no** references to `America/Los_Angeles`, `America/Vancouver`, PST/PDT or fixed −7/−8 offsets anywhere. No container sets `TZ`, so the API (GraalVM native image), the report Lambda and PostgreSQL sessions all run in **UTC**. The client sends and receives UTC ISO timestamps. **Nothing fails on Nov 1.** The main side effect: several places turn a UTC instant into a *calendar date*, so anything done after **5:00 p.m. BC time** is dated the next day. This already happens (after 4 p.m. in winter, 5 p.m. in summer). From Nov 1 the cut-off is **5 p.m. all year**. It matters most near the **March 31 / April 1 fiscal-year boundary** and quarterly reporting deadlines. |

---

## 1. What changed (same basis as the bcws-wfhr and wfnews reports)

- **Government rule.** BC stopped changing clocks after 2026-03-08. On **2026-11-01 clocks do not fall back.** BC stays at **UTC-7** all year, named *Pacific time (PCT)*. ([gov.bc.ca](https://www2.gov.bc.ca/gov/content/governments/celebrating-british-columbia/daylight-saving-time))
- **IANA tzdata 2026b** (2026-04-22) models `America/Vancouver` as permanent UTC-7 from 2026-11-01 02:00. The abbreviation is **`MST`** (not "PCT") and may change later. `America/Los_Angeles` keeps PST/PDT. ([tzdb NEWS](https://data.iana.org/time-zones/tzdb/NEWS))
- **Minimum versions with the rule:**

  | Runtime | Minimum version |
  |---|---|
  | JDK 21 | **21.0.12** |
  | PostgreSQL | **18.4 / 17.10 / 16.14 / 15.18** (2026-05-14 minors) |
  | Browsers and OS | any build with tzdata/ICU 2026b or later |

  **tzdata 2026a is not enough.**

**What "UTC calendar date" means for BC users:**

| BC local time of action | UTC date (before Nov 1, PDT) | UTC date (after Nov 1, PCT) | Same as BC date? |
|---|---|---|---|
| 3:30 p.m. | same day | same day | Yes |
| 4:30 p.m. (winter, pre-change years) | *next day* (under PST) | same day | Changed |
| 5:30 p.m. | next day | next day | **No** (as before) |

So after the change, winter behaves like summer always has. Users who noticed "yesterday's" dates after 4 p.m. in past winters will now see it after 5 p.m.

---

## 2. Summary of findings

| # | Area | Severity | Fails on Nov 1? | Fix effort |
|---|---|---|---|---|
| P1 | UTC-derived calendar dates: CSV export, Jasper reports, DB `DATE` defaults, client `toISOString().split('T')[0]` | **Medium** (pre-existing; fiscal-year and quarter edge cases) | No (cut-off moves from 4 p.m. to 5 p.m. in winter) | Low–Med |
| P2 | Client display depends on the user's device tz data (`| date`, `Intl`, `toLocale*`) | Medium (internal staff app) | Only on un-updated devices | Outside the repo, plus a small code option |
| P3 | Client fiscal-year default uses the device clock (`getMonth() >= 3`) | Low | No | n/a (correct on updated devices) |
| P4 | API is a GraalVM **native image**: tzdb is frozen at build time; floating builder tag `graalvm-community:21` | Low (JVM zone is UTC) | No | Low (rebuild, pin) |
| P5 | RDS PostgreSQL: version from `DB_POSTGRES_VERSION` (not in repo), **`auto_minor_version_upgrade = false`** | Low | No (UTC sessions, no tz-aware SQL) | Low (manual minor upgrade) |
| P6 | Report-generator Lambda (Quarkus native / JasperReports) and GDB extractor (Node 20) | Low | No | Covered by P1 and P4 |
| P7 | Local dev / CI (no `TZ`), schedules, legacy Dockerfiles | None / info | No | n/a |

---

## 3. Areas of failure

**None identified.** No code path depends on BC's offset or on `America/Los_Angeles`, and every server runtime is in UTC. The findings below are concerns and hardening items, not breakages.

---

## 4. Areas of concern

### P1. Calendar dates derived in UTC: MEDIUM (pre-existing, fiscal-sensitive)

Each place below turns an instant into a `yyyy-MM-dd` date using **UTC** (the server's, the Lambda's or the database's default zone), not BC time. An action taken after 5:00 p.m. PCT is dated the following day.

| Location | What happens |
|---|---|
| `server/wfprev-api/.../services/CsvReportGenerator.java:27-28` (`DATE_FORMAT … withZone(ZoneId.systemDefault())`), used at `:179`, `:227`, `:424` | The CSV export's `activityEndDate` and **`endorsementTimestamp`** are formatted in the container zone (UTC) |
| `lambda/report-generator/src/main/jasperreports/WFPREV_FUEL_MANAGEMENT_JASPER.jrxml:1828,1870`, `WFPREV_RESULTS_JASPER.jrxml:1118` (`pattern="yyyy-MM-dd"`) | Jasper renders `endorsementTimestamp` and `approvedTimestamp` in the Lambda's default zone (UTC). No `REPORT_TIME_ZONE` or `net.sf.jasperreports.default.timezone` is set in `jasperreports.properties` |
| DB: about 160 `create_date`/`update_date` columns (both columns on about 80 tables) declared **`DATE DEFAULT CURRENT_TIMESTAMP`** (for example `db/scripts/01_00_05/00/ddl/WFPREV.activity_category_code.sql:18,20`; mirrored in the JPA `columnDefinition`s) | The timestamp is truncated to a date in the session zone (UTC), so rows created after 5 p.m. PCT carry tomorrow's date |
| Server-set timestamps: `ProjectFiscalController.java:395,483`, `ProjectFiscalService.java:288` (`setSubmittedTimestamp(new Date())`) | Stored correctly as instants, but show as the next day wherever they're formatted as UTC dates (CSV, Jasper) |
| Client `project-files.component.ts:344,397,504,542` (`collectionDate: now.toISOString().split('T')[0]`) | The attachment "collection date" becomes tomorrow after 5 p.m. PCT |
| Client `fiscal-year-projects.component.ts:95-99` (`formatDate` → `toISOString().split('T')[0]`) | A date string parsed as a local `Date` and re-split in UTC can shift by a day depending on the input format and time of day |

(Client paths are relative to `client/wfprev-war/src/main/angular/src/app/components/edit-project/project-details/`.)

**Why it matters:**

- **Fiscal year:** a performance update, closeout or endorsement submitted **after 5 p.m. on March 31** will be dated **April 1** in the exports and reports. That places it in the next fiscal year.
- **Quarterly deadlines:** the same happens for quarterly reporting-period deadlines.
- **Year-round:** in winter the cut-off used to be 4 p.m. From Nov 1 it is 5 p.m. all year. That's slightly better, but still wrong.

**Resolution:**

- **Reports (server):** format business dates in BC time:

  ```java
  private static final ZoneId BC_ZONE = ZoneId.of("America/Vancouver");
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(BC_ZONE);
  ```

  Do this **only on a runtime with tzdata 2026b or later** (see P4). On older data, `America/Vancouver` would wrongly switch to UTC-8 from Nov 1. That's still closer than UTC, but not correct.
- **Jasper:** pass `REPORT_TIME_ZONE = TimeZone.getTimeZone("America/Vancouver")` when filling the reports. Alternatively, set `net.sf.jasperreports.default.timezone=America/Vancouver` in `jasperreports.properties`.
- **Client:** replace `toISOString().split('T')[0]` for "today" with a local-date helper (for example `formatDate(now, 'yyyy-MM-dd', 'en-CA')` from `@angular/common`, or `Intl.DateTimeFormat('en-CA', { timeZone: 'America/Vancouver' })`).
- **DB defaults:** low priority, since the API sets these fields explicitly in most paths. Consider `timestamp` columns, or `DEFAULT (now() AT TIME ZONE 'America/Vancouver')::date`, the next time the schema is touched.
- **Decide with the business** whether fiscal assignment should follow **BC local date**. That's almost certainly the intent, and it should be written down.

### P2. Client display depends on the user's device

Timestamps come from the API as instants and are rendered in the browser's zone. For example:

- `shared/timestamp/timestamp.component.html:5` → `updateDate | date:'yyyy-MM-dd HH:mm'`
- `performance-update-header.component.html:26,37` → `submittedTimestamp | date:'yyyy-MM-dd'`
- `utils/tools.ts:188-205` → `LOCAL_ISO_FORMAT` (`timeZone: undefined`)

`fiscalCloseout.updateDate` is explicitly shown in `'UTC'` (`performance-update-header.component.html:31,42`). That's inconsistent with its neighbours: a closeout at 6 p.m. PCT shows tomorrow's date, while the submitted date beside it shows today.

- **Failure mode on stale devices:** from Nov 1, a staff machine without tzdata 2026b shows times one hour early (as UTC-8). WFPREV is an internal tool on mostly managed government devices, so the exposure is smaller than for the public wfnews site.
- **Resolution:** Confirm through desktop support that managed OS and browser updates are deployed. Optionally pin display to `'America/Vancouver'` (Angular `DatePipe` accepts a timezone argument, and `DATE_PIPE_DEFAULT_OPTIONS` can set it app-wide). Make the closeout date consistent with its neighbours: either all BC time or all labelled UTC.

### P3. Fiscal-year default from the device clock

`search-filter.component.ts:445-447`, `map.component.ts:58`, `fiscal-map.component.ts:21` and `project-details.component.ts:913` compute the current fiscal year as `new Date().getMonth() >= 3`. The result uses the device's local date, which is the correct behaviour. It is unaffected by the change, apart from the stale-device hour on the night of March 31.

### P4. API native image: tzdb frozen at build time

Production builds use `server/wfprev-api/Dockerfile.graalvm` (`.github/workflows/mvn-build.yml:160`). The builder is `ghcr.io/graalvm/graalvm-community:21`, and the runtime is `ubuntu:22.04`. A native image **embeds the time-zone rules of the JDK it was built with**. The runtime OS `tzdata` package has no effect, and only a rebuild picks up new rules.

- The API runs in UTC and doesn't use named zones today, so there is **no impact**.
- **It becomes a dependency as soon as P1's `America/Vancouver` fix is applied.**

**Action:**

1. Verify the builder's JDK (`docker run --rm ghcr.io/graalvm/graalvm-community:21 java -version`). It needs **21.0.12 or later** (tzdata 2026b). Check that GraalVM Community for JDK 21 still receives updates. If not, move the build to a maintained distribution (for example Liberica NIK or Mandrel, or GraalVM CE for the current LTS).
2. Pin the builder by version or digest for reproducibility.
3. Add a startup or unit assertion that `ZoneId.of("America/Vancouver")` has offset −07:00 on 2026-12-01. With it, a stale builder fails CI.

The same applies to the **report-generator Lambda** (`lambda/report-generator/src/main/docker/Dockerfile.lambda`, same GraalVM builder). The other Dockerfiles use `tomcat:10.1-jdk21` (`Dockerfile`), `eclipse-temurin:21-*` (`.dev`, `.local`) and `ubi9/openjdk-21:1.21` (`Dockerfile.jvm`). They are not the production path, but should be rebuilt if they are used.

### P5. RDS PostgreSQL: minor upgrades are manual

`terraform/rds.tf:1-24`:

- `engine_version = var.DB_POSTGRES_VERSION`, with the value held outside the repo.
- **`auto_minor_version_upgrade = false`**.
- The default parameter group is used, so server `TimeZone = UTC`.

The SQL uses no `AT TIME ZONE`, `timestamptz` or local-time functions, only `now()` in audit-trigger warning messages. **There is no impact today.**

**Action:** Because minors never upgrade automatically, confirm the running version is at least the 2026-05-14 minor for its major (18.4 / 17.10 / 16.14 / 15.18). Schedule the upgrade if not. It's required before any `AT TIME ZONE 'America/Vancouver'` is introduced (see P1 DB defaults). (A commented-out `wfone_pgsqlDB` at PostgreSQL 13.4 sits in the same file. PG 13 is end-of-life and will never get tzdata 2026b, so don't revive it as written.)

### P6. Lambdas

- **`report_generator`:** image Lambda (Quarkus native plus JasperReports). The date formatting concerns are covered in P1, and the build-time tzdb in P4. Lambda's runtime zone is UTC.
- **`gdb_processor`** (`node/wfprev-gdb-extractor`, `public.ecr.aws/lambda/nodejs:20`): parses geodatabase uploads, with no date or time-zone logic found.
- There are no EventBridge `cron()` schedules. `terraform/eventbridge.tf` only captures AWS Health events.

### P7. Development, CI, tests (information only)

- `docker-compose*.yml` sets no `TZ`, and GitHub Actions runners are UTC, so dev, CI and production behave the same (all UTC).
- The entity tests named `test_date_fields_timezone_conversion` (for example `RiskRatingCodeEntityTest.java:56`) only assert getter/setter round-trips of `new Date()`. They don't test time zones despite the name.
- **Recommendation:** When P1 is implemented, add tests with a fixed `Clock` at `2027-03-31T17:30-07:00` (BC time) asserting that the CSV and Jasper date is **2027-03-31** and the fiscal year is **2026/27**.

---

## 5. Recommended actions and timeline

| When | Action | Findings |
|---|---|---|
| **Before Nov 1** (low effort, do regardless) | Confirm `DB_POSTGRES_VERSION` ≥ the May-2026 minor and upgrade manually if needed. Verify or rebuild the GraalVM builder at JDK ≥ 21.0.12 and pin it. Confirm managed desktops have OS/browser tz updates. | P2, P4, P5 |
| **Before fiscal year-end (Mar 31, 2027)** | Switch CSV and Jasper date formatting to `America/Vancouver`. Fix the client `collectionDate` and `formatDate` helpers. Make closeout display consistent. Add fiscal-boundary tests. | P1, P2, P7 |
| **Opportunistic** | Revisit the `DATE DEFAULT CURRENT_TIMESTAMP` audit columns. Add a tzdb canary assertion to CI. | P1, P4 |

**Rollback note:** none of the recommended changes alters stored data. They only change how instants are rendered as dates, so rolling back is a normal redeploy.

---

## 6. Method and scope

- Shallow clone of `main` @ `e01a1fa`. Static scan excluding `node_modules`. Searched for zone IDs, fixed offsets, `TZ`/`user.timezone`/Jackson/Hibernate time-zone properties, Java and JS date APIs, `Intl`/`DatePipe`/`toISOString`, SQL time functions and column types, JRXML patterns, Dockerfiles (including native-image builds), Terraform ECS/RDS/Lambda/EventBridge, and GitHub workflows.
- **Not verified:** the actual `DB_POSTGRES_VERSION`. The JDK version inside the `graalvm-community:21` builder at the last production build. The business rule for which date counts for fiscal-year assignment.

**Sources:** [gov.bc.ca: Daylight saving time](https://www2.gov.bc.ca/gov/content/governments/celebrating-british-columbia/daylight-saving-time) · [IANA tzdb NEWS](https://data.iana.org/time-zones/tzdb/NEWS) · [JDK tzdata versions](https://www.oracle.com/java/technologies/tzdata-versions.html) · [PostgreSQL 15.18 release notes](https://www.postgresql.org/docs/release/15.18/) · [PostgreSQL 18.4 release notes](https://www.postgresql.org/docs/release/18.4/)
