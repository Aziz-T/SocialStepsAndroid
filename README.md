# Social Steps

Social Steps is a social fitness application that allows users to track their daily step counts and burned calories, and share them with friends. The app is designed to promote a healthy lifestyle and facilitate interaction among users.

## Features

- **Step Count and Calorie Tracking:** Users can record their daily step count and burned calorie amount.

- **Social Interaction:** View, like, and comment on the daily activities of friends. Create groups to work towards common goals and challenge each other.

- **Messaging:** Send private or group messages to friends or group members. Provide support with motivational messages.

- **Huawei Kits Integration:** Our app utilizes Huawei developer kits to offer special features. This provides Huawei users with exclusive advantages and integrations.


## Huawei Kits

### Push Kit
Enables the delivery of real-time notifications to keep users engaged and informed about their friends' activities.

### Health Kit
Allows access to health and fitness data, enhancing the accuracy of step counts and calorie tracking.

### Cloud DB
Facilitates secure and scalable cloud-based storage for user data, ensuring seamless synchronization across devices.

### Cloud Functions
Enables the creation and execution of serverless functions, enhancing the application's backend capabilities.

### Auth Service
Provides secure user authentication, ensuring that user data is protected and only accessible to authorized individuals.

### Account Kit
Simplifies the login process, allowing users to log in easily with their Huawei accounts, enhancing the overall user experience.



## ❓ Let's Try the App - What You Will Need

- A computer that can run Android Studio 😊.
- An android device with Hms Core installed and OS version Android 6 or higher (minSdk is API 23)
- Fork this repo by clicking top right and clone it to your computer.
- You need to AGConnect-Services.json file to run this project properly. Follow the below steps to
  obtain it.
    - [Create Huawei Developer Account if you haven't got an account](https://developer.huawei.com/consumer/en/doc/start/10104)
    - Go to the app-level build.gradle file and change the application id of the project. (You don't need to change the package name in the whole repo.)
    - [Create a SignIn Certificate with key0 alias, ExploreLandmarks password and named as ExploreLandmarks.jks](https://medium.com/@corruptedkernel/android-creating-a-signing-certificate-keystore-and-signing-your-apk-fa67fdd27cf)
      . Actually this password and alias are not mandatory but if you want to sign with different
      alias or password please don't forget to change needed information in build.gradle (app:level)
      to with new information. Replace this file with SocialSteps.jks which already exist on the
      app directory.
    - [Generate SHA-256 Fingerprint via Keytool](https://medium.com/@corruptedkernel/android-generating-fingerprint-from-a-keystore-jks-file-b624bacd90fd)
      or running the signingReport task (View => Tool Windows => Gradle => Tasks => Android =>
      signingReport)
    - Login to [Huawei Developer Console](https://developer.huawei.com/consumer/en/console) if you
      didn't log in.
    - [Create a new  app with the new applicationId in AppGallery and integrate the repo with it](https://medium.com/huawei-developers/android-integrating-your-apps-with-huawei-hms-core-1f1e2a090e98) (
      Creating an app, adding SHA-256 to AppGallery Connect, Downloading AgConnectServices.json file)
    - Before downloading agconnect-services.json choose a Data Processing Location on Project Settings
      -> General Information (I recommended Germany because I use it and the search feature is worked
      properly with it.).
    - Before downloading and moving agconnect-services.json file to the "app" folder,
      please [make enable necessary services](https://developer.huawei.com/consumer/en/doc/distribution/app/agc-help-enabling-service-0000001146598793)
      . For this project you have to set enable Auth Service, Account Kit, ML Kit, Search Kit, Keyring
      Service, Analytics Kit.
- You need to client id and client secret to obtain an access token from Account Kit via OAuth-based
  authentication. [Get this information](https://developer.huawei.com/consumer/en/doc/distribution/app/agc-help-appinfo-0000001100014694)
  under Project Settings => General Information => App information => OAuth 2.0 client ID section
  and add them into app level build.gradle file.
- Solution of some issues you might be faced
    - Error Code 907135000 : Please move agconnect-services.json file under to app directory.
    - Error Code 6003 : [Enable App Signing by choosing "Let AppGallery Connect create and manage app signature for me"](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-appsigning-newapp-0000001052418290)


## Screenshots

![Screenshot 1]()
*Caption for Screenshot 1*

![Screenshot 2]()
*Caption for Screenshot 2*
