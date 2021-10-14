## WarnMyChild


## Table of Contents

 * [Introduction](#introduction)
 * [Configuration ](#configuration )
 * [Supported Environments](#supported-environments)
 * [Sample Code](#Sample-Code)
 * [License](#license)


## Introduction
    WarnMyChild Android sample code encapsulates APIs of the HUAWEI Scan Kit,Cloud DB,Map Kit,     Location      Kit and Ads Kit. It provides many sample programs for your reference or usage.
    The following describes packages of Android sample code.

    Geofence:       Sample code of Location Kit geofence service.
    QR Scanning:    Sample code of Scanning feature.
    Cloud Storage:  Sample code of Cloud DB Service.
    Authorization:  Sample code of Authorization service for both the apps.
    GeoLocation:    Sample code of Map Kit and Location Kit to show the position or movement child in the                        map screen.

## Supported Environments
	Android SDK Version >= 19 and JDK version >= 1.8 is recommended.


## Configuration
Before running the app, you need to:
1. If you do not have a HUAWEI Developer account, you need to register an account and pass identity verification.
2. Use your account to sign in to AppGallery Connect, create an app, and set Package type to APK (Android app).
3. Enable the API permission for below kits from Project Setting > Manage APIs and enable the API permission.
        Auth Service        
        Cloud Storage
        Cloud Hosting        
        Map Kit
        Location Kit    
             
4. Since Cloud DB is in Beta state, before using this we need download a form and send it agconnect@huawei.com. For details of mail to be send, please follow the below link
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-introduction#h1-1611710493353 
 
   Before using the Cloud DB service, you need to enable it.
   ------------------------------------------------------------
   1. Log in to AppGallery Connect and click My projects.
   2. Select a project from the project list and click an app for which you need to enable the Cloud DB            service.
   3. In the navigation bar, choose Build > Cloud DB.
   4. Click Enable now to enable the Cloud DB service
   5. (Optional) If you have not selected a data storage location, set it first. For details, see Setting a        Data Storage Location.
   6. After the service is initialized, the Cloud DB service is enabled successfully.

      After enabled, go to Cloud DB, Select Cloud DB Zones Tab, Click on Add, Give the name for the Database       to be created.
      Click on Object Types Tab, Select the Version, Click on Add, Give the details of Name, Field, Indexes       and Permission, to create the table.
      Now click on Data Entries tab, here you can download the template of the table or import or export       file or clear the table etc.

   7. After downloading the template of the table, one can use this file as a normal java class file in the       project for data operation.

5. Download the agconnect-services.json file from AppGallery Connect and replace place it in the application-level root directory.
Before compiling the APK, please make sure that the project includes the agconnect-services.json file, otherwise a compilation error will occur.


## Sample Code
  See details: [HUAWEI Scan Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/service-introduction-0000001050041994)

               [HUAWEI Ads Kit Introduction](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/publisher-service-introduction-0000001070671805)

	       [HUAWEI Cloud DB Service Introduction](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-introduction)

               [HUAWEI Account Kit]
(https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050048870)

               [HUAWEI Map Kit]
(https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/android-sdk-brief-introduction-0000001061991343-V5)

               [HUAWEI Location Kit]
(https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050706106)

##  License
  WarnMyChild sample is licensed under the: [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

