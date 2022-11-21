/*
 * Air Things Device
 * 
 *
 *
 *  Licensed Virtual the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date         Who           What
 *    ----         ---           ----
 *    16Oct2022    thebearmay    add capability CarbonDioxideMeasureMent,RelativeHumidityMeasurement
 *    21Nov2022    thebearmay    make the tile template selection an ENUM
 *                               add absHumidity
*/
import java.text.SimpleDateFormat
import groovy.json.JsonSlurper
#include thebearmay.localFileMethods
#include thebearmay.templateProcessing

@SuppressWarnings('unused')
static String version() {return "0.0.11"}

metadata {
    definition (
        name: "Air Things Device", 
        namespace: "thebearmay", 
        author: "Jean P. May, Jr.",
        importUrl:"https://raw.githubusercontent.com/thebearmay/hubitat/main/airthings/airthingsDevice.groovy"
    ) {
        capability "Actuator"
        capability "Initialize"
        capability "Battery"
        capability "CarbonDioxideMeasurement"
        capability "RelativeHumidityMeasurement"
        capability "TemperatureMeasurement"
 
        attribute "radonShortTermAvg", "number"
        attribute "humidity", "number"
        attribute "pressure", "number"
        attribute "co2", "number"
        attribute "voc", "number"
        attribute "temperature", "number"
        attribute "battery", "number"
        attribute "pm1", "number"
        attribute "pm10", "number"
        attribute "pm11", "number"
        attribute "pm12", "number"
        attribute "pm13", "number"
        attribute "pm14", "number"
        attribute "pm15", "number"
        attribute "pm16", "number"
        attribute "pm17", "number"
        attribute "pm18", "number"
        attribute "pm19", "number"
        attribute "pm2", "number"
        attribute "pm20", "number"
        attribute "pm21", "number"
        attribute "pm22", "number"
        attribute "pm23", "number"
        attribute "pm24", "number"
        attribute "pm25", "number"
        attribute "pm26", "number"
        attribute "pm27", "number"
        attribute "pm28", "number"
        attribute "pm29", "number"
//        attribute "valuesAsOf", "string"
        attribute "absHumidity", "number"
        attribute "html", "string"
        
        command "refresh"                                  
    }   
}

preferences {
    input("debugEnabled", "bool", title: "Enable debug logging?", defaultValue:false)
    input("useFahrenheit", "bool", title: "Use Fahrenheit", defaultValue:false)
    input("usePicoC", "bool", title: "Use pCi/L for Radon", defaultValue:false)
    input("pollRate", "number", title: "Sensor Polling Rate (minutes)\nZero for no polling:", defaultValue:0)
    input("security", "bool", title: "Enable if using Hub Security", defaultValue: false, submitOnChange:true)
    if(security){
        input("username","string", title:"Hub Security Username")
        input("password","string", title:"Hub Security Password")
    }
    //input("tileTemplate", "string", title:"Template for generating HTML for dashboard tile")
    fileList = []
    fileList = listFiles()

    input("tileTemplate", "enum", title:"Template for generating HTML for dashboard tile", options:fileList, defaultValue:"--No Selection--", submitOnChange:true)
}

@SuppressWarnings('unused')
def installed() {
    log.trace "${device.displayName} v${version()} installed()"
    initialize()
}

def initialize(){
   updated()    
}

@SuppressWarnings('unused')
def updated(){
    if(debugEnabled) {
        log.debug "updated()"
        runIn(1800,logsOff)
    }
    if(pollRate == null)
        device.updateSetting("pollRate",[value:0,type:"number"])
    if(pollRate > 0){
        runIn(pollRate*60,"refresh")
    } else
        unschedule("refresh")
    refresh()
}

@SuppressWarnings('unused')
def configure() {
    if(debugEnabled) log.debug "configure()"

}

void updateAttr(String aKey, aValue, String aUnit = ""){
    sendEvent(name:aKey, value:aValue, unit:aUnit)
}

void refresh() {
    parent.updateChild(device.data.deviceId)
    if(pollRate > 0)
        runIn(pollRate*60,"refresh")
}

void dataRefresh(retData){
    retData.data.each{
            unit=""
        switch (it.key){
            case("temp"):
                unit="°C"
                if(useFahrenheit){ 
                    it.value = celsiusToFahrenheit(it.value) 
                    unit = "°F"
                }
                updateAttr("temperature", it.value, unit)
                break
            case("radonShortTermAvg"):
                if(usePicoC){
                    it.value = (it.value/37).toFloat().round(1)
                    unit="pCi/L"
                }else
                    unit="Bq/m<sup>3</sup>"
                break
            case("humidity"):
                unit="%"
                break
            case("co2"):
                unit="ppm"
                updateAttr("carbonDioxide", it.value, unit) //required for capability CarbonDioxideMeasurement, co2 retained for backward compatibility
                break
            case("pressure"):
                unit="mBar"
                break
            case("voc"):
                unit="ppb"
                break
            case("battery"):
                unit="%"
                break
            case("rssi"):
                unit="dBm"
                break
            default:
                unit=""
                break
        }
        if((it.key != "temp" && unit != null) || it.key.startsWith('pm')) //unit will be null for any values not tracked
            updateAttr(it.key, it.value, unit) 
    }
    calcAbsHumidity()
    if(tileTemplate && tileTemplate != "No selection" && tileTemplate != "--No Selection--"){
        tileHtml = genHtml(tileTemplate)
        updateAttr("html","$tileHtml")
    }
 
}

void calcAbsHumidity() {
    if(useFahrenheit)
        deviceTempInCelsius = fahrenheitToCelsius(device.currentValue("temperature").toFloat())
    else
        deviceTempInCelsius = device.currentValue("temperature").toFloat()
        
    BigDecimal absHumidity = (6.112 * Math.exp((17.67 * deviceTempInCelsius) / (deviceTempInCelsius + 243.5)) * device.currentValue("humidity").toFloat() * 2.1674)
    updateAttr("absHumidity", absHumidity, "g/kg")
}

@SuppressWarnings('unused')
List<String> listFiles(){
    if(security) cookie = getCookie()
    if(debugEnabled) log.debug "Getting list of files"
    uri = "http://${location.hub.localIP}:8080/hub/fileManager/json";
    def params = [
        uri: uri,
        headers: [
				"Cookie": cookie
            ]        
    ]
    try {
        fileList = []
        httpGet(params) { resp ->
            if (resp != null){
                if(logEnable) log.debug "Found the files"
                def json = resp.data
                for (rec in json.files) {
                    fileList << rec.name
                }
            } else {
                //
            }
            fileList.add("--No Selection--")
        }
        if(debugEnabled) log.debug fileList.sort()
        return fileList.sort()
    } catch (e) {
        log.error e
    }
}

@SuppressWarnings('unused')
void logsOff(){
     device.updateSetting("debugEnabled",[value:"false",type:"bool"])
}