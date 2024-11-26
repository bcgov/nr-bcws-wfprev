Application:          WFPREV (Wildfire Prevention System)
Repository:           
Version:              1.0.0-00
Author:               Vivid Solutions (dhemsworth@vividsolutions.com)

Description
-------------------------------------------------------------------------------
Assisted Delivery.
 - Create "wildfire" database
 - Create "proxy_wf1_prev" login account
 - Create "proxy_wf1_prev_rest" login account
 - Create "wfprev" schema
 - Create "app_wf1_prev_rest_proxy" role
 - Create "app_wf1_prev_custodian" role
 - Create extensions

This release will create a database called "wfprev". 

Prerequisites
-------------------------------------------------------------------------------
None


-------------------------------------------------------------------------------
1. Assisted Delivery

1.1 Change to the scripts directory. 

    cd ../

1.2 Connect to the target PostgreSQL host server as the postgres user 
    (or equivalent DBA account) for the target environment. You can use either the
    psql terminal or PGAdmin tool.
    
    Where host server is one of the following:
      Integration: volatile   
      Delivery:    transform
      Test:        convert
      Prod:        translate

1.3 Create the "wildfire" database by running the following script:
    /ddl/database/wfprev.ddl.create_database.sql;
    
1.4 Create the wfprev account. 
    Please edit the following scripts to set an md5 hash password for each account:
    
    /ddl/logins/wfprev.ddl.create_login_APP_WF1_prev.sql;
    /ddl/logins/wfprev.ddl.create_login_PROXY_WF1_prev_REST.sql;

    After the scripts have been updated with a password run the scripts above 
    to create the logins.
    
1.5 Create the  roles by running the 
    following script:
    /ddl/roles/wfprev.ddl.create_roles.sql;

1.6 Create the "wfprev" schema within the new "wildfire" database by running the 
    following script:
    /ddl/schema/wfprev.ddl.create_wfprev_schema.sql;
    
    The script will make both the postgres and app_wf1_prev accounts owners of 
     the "wfprev" schema.
    The script will make proxy_wf1_prev_rest a user of the "wfnes" schema.
    

1.7 Confirm that the app_wf1_prev has owner privileges for the "wfprev" schema in the
    "wildfire" database.

1.8 Store the passwords for the app_wf1_prev and proxy_wf1_prev_rest in KeyPass.
    
1.9 Release completed.    

  
  
-------------------------------------------------------------------------------
2. NOTIFICATION
-------------------------------------------------------------------------------
2.1 For Integration and Delivery environments please contact Kevin Guise with the 
     proxy account passwords.
      Kevin Guise
      kcguise@VividSolutions.Com
      
2.2 Update RFD Sub-Tasks to mark release as completed.      


 
