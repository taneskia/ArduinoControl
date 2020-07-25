#include "FirebaseESP8266.h" // Firebase Client lib
#include <ESP8266WiFi.h>
#include <NTPClient.h>
#include <ezTime.h>
#include <cstdlib>
#include <ctime>
#include <string>
#include <WiFiManager.h>

#define FIREBASE_HOST "HOST_URL" 
#define FIREBASE_AUTH "HOST_SECRET" 
#define TRIGGER_PIN 0

//Define FirebaseESP8266 data object
FirebaseData firebaseData;

FirebaseJson json;

void printResult(FirebaseData& data);
void onDemandAp();

Timezone currentDateTime;

unsigned long previousMillis = 0;
const long interval = 1000 * 30;

void setup()
{

  WiFi.mode(WIFI_STA); // explicitly set mode, esp defaults to STA+AP


  // put your setup code here, to run once:
  Serial.begin(115200);

  // WiFi.mode(WiFi_STA); // it is a good practice to make sure your code sets wifi mode how you want it.

  //WiFiManager, Local intialization. Once its business is done, there is no need to keep it around
  WiFiManager wm;

  //reset settings - wipe credentials for testing
  //wm.resetSettings();

  // Automatically connect using saved credentials,
  // if connection fails, it starts an access point with the specified name ( "AutoConnectAP"),
  // if empty will auto generate SSID, if password is blank it will be anonymous AP (wm.autoConnect())
  // then goes into a blocking loop awaiting configuration and will return success result

  bool res;
  // res = wm.autoConnect(); // auto generated AP name from chipid
  // res = wm.autoConnect("AutoConnectAP"); // anonymous ap
  res = wm.autoConnect("AutoConnectAP", "password"); // password protected ap

  if (!res) {
    Serial.println("Failed to connect");
    // ESP.restart();
  }
  else {
    //if you get here you have connected to the WiFi    
    Serial.println("connected...yeey :)");
  }


  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);

  currentDateTime.setLocation(F("mk"));
  waitForSync();

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);

  //Set the size of WiFi rx/tx buffers in the case where we want to work with large data.
  firebaseData.setBSSLBufferSize(1024, 1024);

  //Set the size of HTTP response buffers in the case where we want to work with large data.
  firebaseData.setResponseSize(1024);

  //Set database read timeout to 0.5 minute (max 15 minutes)
  Firebase.setReadTimeout(firebaseData, 1000 * 30);
  //tiny, small, medium, large and unlimited.
  //Size and its write timeout e.g. tiny (1s), small (10s), medium (30s) and large (60s).
  Firebase.setwriteSizeLimit(firebaseData, "tiny");

}

void loop() {
  onDemandAP();
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;

    String currentDateTimeStr = currentDateTime.dateTime(ISO8601).substring(0, 19) + "Z";
    String currentDateTimeStrForStateEcho = currentDateTimeStr;

    String startDateTimeStr;
    if (Firebase.getString(firebaseData, "/StartDateTime"))
    {
      startDateTimeStr = firebaseData.stringData();
    }
    else
    {
      Serial.println("FAILED start");
      Serial.println("REASON: " + firebaseData.errorReason());
      Serial.println("------------------------------------");
      Serial.println();
    }

    String endDateTimeStr;
    if (Firebase.getString(firebaseData, "/EndDateTime"))
    {
      endDateTimeStr = firebaseData.stringData();
    }
    else
    {
      Serial.println("FAILED end");
      Serial.println("REASON: " + firebaseData.errorReason());
      Serial.println("------------------------------------");
      Serial.println();
    }

    int manualControlEnabled = -1;
    if (Firebase.getInt(firebaseData, "/ManualControl/Enabled"))
    {
      manualControlEnabled = firebaseData.intData();
    }
    else
    {
      Serial.println("FAILED manualenabled");
      Serial.println("REASON: " + firebaseData.errorReason());
      Serial.println("------------------------------------");
      Serial.println();
    }

    int manualControlValue = -1;
    if (Firebase.getInt(firebaseData, "/ManualControl/Value"))
    {
      manualControlValue = firebaseData.intData();
    }
    else
    {
      Serial.println("FAILED manualval");
      Serial.println("REASON: " + firebaseData.errorReason());
      Serial.println("------------------------------------");
      Serial.println();
    }

    int daily = -1;
    if (Firebase.getInt(firebaseData, "/Daily"))
    {
      daily = firebaseData.intData();
    }
    else
    {
      Serial.println("FAILED manualval");
      Serial.println("REASON: " + firebaseData.errorReason());
      Serial.println("------------------------------------");
      Serial.println();
    }

    if (startDateTimeStr != NULL && endDateTimeStr != NULL && manualControlEnabled != -1 && manualControlValue != -1 && daily != -1) {
      Serial.println("=============================================");
      Serial.print("ManualControl Enabled: ");
      Serial.println(manualControlEnabled == 0 ? "OFF" : "ON");
      Serial.print("ManualControl Value: ");
      Serial.println(manualControlValue == 0 ? "OFF" : "ON");
      Serial.print("Daily: ");
      Serial.println(daily == 0 ? "OFF" : "ON");
      Serial.print("Current DateTime: ");
      Serial.println(currentDateTimeStr);
      Serial.print("Start DateTime: ");
      Serial.println(startDateTimeStr);
      Serial.print("End DateTime: ");
      Serial.println(endDateTimeStr);

      bool state_echo;

      if (manualControlEnabled) {
        if (manualControlValue) {
          digitalWrite(LED_BUILTIN, LOW);
          Serial.println("ON");
          state_echo = true;
        }
        else {
          digitalWrite(LED_BUILTIN, HIGH);
          Serial.println("OFF");
          state_echo = false;
        }
      }
      else {
        if (daily) {
          currentDateTimeStr = currentDateTimeStr.substring(11, 20);
          startDateTimeStr = startDateTimeStr.substring(11, 20);
          endDateTimeStr = endDateTimeStr.substring(11, 20);
          Serial.println("DAILY:");
          Serial.print("Current Time: ");
          Serial.println(currentDateTimeStr);
          Serial.print("Start Time: ");
          Serial.println(startDateTimeStr);
          Serial.print("End Time: ");
          Serial.println(endDateTimeStr);
        }
        if (strcmp(currentDateTimeStr.c_str(), startDateTimeStr.c_str()) >= 0 && strcmp(currentDateTimeStr.c_str(), endDateTimeStr.c_str()) <= 0) {
          digitalWrite(LED_BUILTIN, LOW);
          Serial.println("ON");
          state_echo = true;
        }
        else {
          digitalWrite(LED_BUILTIN, HIGH);
          Serial.println("OFF");
          state_echo = false;
        }
      }

      String state = state_echo == true ? "ON " + currentDateTimeStrForStateEcho : "OFF " + currentDateTimeStrForStateEcho;

      if (Firebase.setString(firebaseData, "/ArduinoStateEcho", state))
      {
        Serial.println("ArduinoStateEcho " + state);
      }
      else
      {
        Serial.println("FAILED ArduinoStateEcho");
      }

      Serial.println("=============================================");
    }

  }
}

void onDemandAP() {
  if (digitalRead(TRIGGER_PIN) == LOW) {
    WiFiManager wm;

    //reset settings - for testing
    //wifiManager.resetSettings();

    // set configportal timeout
    wm.setConfigPortalTimeout(150);

    if (!wm.startConfigPortal("OnDemandAP")) {
      Serial.println("failed to connect and hit timeout");
      delay(3000);
      //reset and try again, or maybe put it to deep sleep
      ESP.restart();
      delay(5000);
    }

    //if you get here you have connected to the WiFi
    Serial.println("connected...yeey :)");

  }
}

/*
void printResult(FirebaseData &data)
{

  if (data.dataType() == "int")
    Serial.println(data.intData());
  else if (data.dataType() == "float")
    Serial.println(data.floatData(), 5);
  else if (data.dataType() == "double")
    printf("%.9lf\n", data.doubleData());
  else if (data.dataType() == "boolean")
    Serial.println(data.boolData() == 1 ? "true" : "false");
  else if (data.dataType() == "string")
    Serial.println(data.stringData());
  else if (data.dataType() == "json")
  {
    Serial.println();
    FirebaseJson &json = data.jsonObject();
    //Print all object data
    Serial.println("Pretty printed JSON data:");
    String jsonStr;
    json.toString(jsonStr,true);
    Serial.println(jsonStr);
    Serial.println();
    Serial.println("Iterate JSON data:");
    Serial.println();
    size_t len = json.iteratorBegin();
    String key, value = "";
    int type = 0;
    for (size_t i = 0; i < len; i++)
    {
      json.iteratorGet(i, type, key, value);
      Serial.print(i);
      Serial.print(", ");
      Serial.print("Type: ");
      Serial.print(type == FirebaseJson::JSON_OBJECT ? "object" : "array");
      if (type == FirebaseJson::JSON_OBJECT)
      {
        Serial.print(", Key: ");
        Serial.print(key);
      }
      Serial.print(", Value: ");
      Serial.println(value);
    }
    json.iteratorEnd();
  }
  else if (data.dataType() == "array")
  {
    Serial.println();
    //get array data from FirebaseData using FirebaseJsonArray object
    FirebaseJsonArray &arr = data.jsonArray();
    //Print all array values
    Serial.println("Pretty printed Array:");
    String arrStr;
    arr.toString(arrStr,true);
    Serial.println(arrStr);
    Serial.println();
    Serial.println("Iterate array values:");
    Serial.println();
    for (size_t i = 0; i < arr.size(); i++)
    {
      Serial.print(i);
      Serial.print(", Value: ");

      FirebaseJsonData &jsonData = data.jsonData();
      //Get the result data from FirebaseJsonArray object
      arr.get(jsonData, i);
      if (jsonData.typeNum == FirebaseJson::JSON_BOOL)
        Serial.println(jsonData.boolValue ? "true" : "false");
      else if (jsonData.typeNum == FirebaseJson::JSON_INT)
        Serial.println(jsonData.intValue);
      else if (jsonData.typeNum == FirebaseJson::JSON_DOUBLE)
        printf("%.9lf\n", jsonData.doubleValue);
      else if (jsonData.typeNum == FirebaseJson::JSON_STRING ||
           jsonData.typeNum == FirebaseJson::JSON_NULL ||
           jsonData.typeNum == FirebaseJson::JSON_OBJECT ||
           jsonData.typeNum == FirebaseJson::JSON_ARRAY)
        Serial.println(jsonData.stringValue);
    }
  }
}
*/
