# InAppConsent_Triangle_android
The Triangle app is an example of how to integrate the Evidon App Notice SDK. It includes these features:

1. Manages three different types of third party SDK that provide serveices within the app:
  1. __Evidon:__ This SDK informs the end user about tracking  in the app and provides them the ability to manage how they are tracked by this app. This SDK is configured as an Essential tracker.
  2. __AdMob:__ This SDK represents a class of SDKs that can be disabled on the fly and do not require an app restart. This SDK is configured as an Advertising tracker.
  3. __Crashlytics:__ This SDK represents a class of SDKs that can NOT be disabled on the fly and require an app restart. This SDK is configured as an Analytics tracker.
