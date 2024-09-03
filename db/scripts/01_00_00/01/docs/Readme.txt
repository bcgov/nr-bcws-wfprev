Application:          WF1_PREV (Wildfire PREV)
Repository:           
Version:              1.0.0-01
Author:               Vivid Solutions (dhemsworth@vividsolutions.com)

Requirements
-------------------------------------------------------------------------------
PostgreSQL Host Servers: Integration: volatile   
                         Delivery:    transform
                         Test:        convert
                         Prod:        translate

Description
-------------------------------------------------------------------------------
Create database objects for the WF PREV system in the 
wfnews schema contained in the "Wildfire One" database.


Prerequisites
-------------------------------------------------------------------------------
Release 1.0.0-00 must be deployed first.
Database objects will be created using the APP_WF1_PREV and PROXY_WF1_PREV_REST 
accounts created in the 1.0.0-00 release.

-------------------------------------------------------------------------------
1. APP_WF1_PREV: Create Database objects.
-------------------------------------------------------------------------------

1.1 Change to the root  directory. 

    cd ../../../../

1.2 set correct values in  liquibase.properties  and exceute liquibase command 

liquibase update --changelog-file=main-changelog.json
    
1.3 Release completed.    

1.4 If need to rollback changes execute liquibase rollback   

liquibase --changelog-file=main-changelog.json  rollbackCount 1

-------------------------------------------------------------------------------
2. NOTIFICATION
-------------------------------------------------------------------------------
2.1 Update RFD Sub-Tasks to mark release as completed.