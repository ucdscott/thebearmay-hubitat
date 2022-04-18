/*
 * Daikin Single Thermostat 
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
 *    18Apr2022    thebearmay    Initial Creation
*/
import java.text.SimpleDateFormat
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field

@SuppressWarnings('unused')
static String version() {return "0.0.1"}

metadata {
    definition (
        name: "Daikin Single Thermostat", 
        namespace: "thebearmay", 
        author: "Jean P. May, Jr.",
        importUrl:"https://raw.githubusercontent.com/thebearmay/hubitat/main/daikin/daikinSingleTherm.groovy"
    ) {
        capability "Actuator"
        capability "Configuration"
        capability "Initialize"

        capability "Thermostat"
        capability "ThermostatCoolingSetpoint"
        capability "ThermostatFanMode"
        capability "ThermostatHeatingSetpoint"
        capability "ThermostatMode"
        capability "ThermostatOperatingState"
        capability "ThermostatSetpoint"
        
        attribute "equipmentStatus","number" //???HE - thermostatOperatingState ENUM ["heating", "pending cool", "pending heat", "vent economizer", "idle", "cooling", "fan only"]        
        attribute "fan","number"
        attribute "fanCirculateSpeed","number"
        attribute "setpointDelta","number"
        attribute "setpointMinimum","number"
        attribute "setpointMaximum","number"
        attribute "tempOutdoor","number"
        attribute "humidity", "number"
        attribute "humidOutdoor", "number"
        attribute "locationMap", "string"
        
        command "refresh"

    }   
}

preferences {
    input("serverPath", "text", title:"Daikin Server Path:", required: true, submitOnChange:true, defaultValue: "https://api.daikinskyport.com")
    input("daiEmail", "text", title:"Email registered with Daiken:", required: true, submitOnChange:true)
    input("daiPwd", "password", title:"Daikin Password:", required: true, submitOnChange: true)
	input("useFahrenheit", "bool", title: "Use Fahrenheit", defaultValue:false)
    input("pollRate", "number", title: "Thermostat Polling Rate (minutes)\nZero for no polling:", defaultValue:5)

    input("debugEnabled", "bool", title: "Enable debug logging?")
}

@SuppressWarnings('unused')
def installed() {
    log.trace "${device.displayName} v${version()} installed()"
    initialize()
}

def initialize(){

}

@SuppressWarnings('unused')
def updated(){
    if(debugEnabled) {
        log.debug "updated()"
        runIn(1800,"logsOff")
    } else 
        unschedule("logsOff")

    if(pollRate == null)
        device.updateSetting("pollRate",[value:5,type:"number"])
    if(pollRate > 0){
        runIn(pollRate*60,"refresh")
    } else
        unschedule("refresh")

}

@SuppressWarnings('unused')
def configure() {
    if(debugEnabled) log.debug "configure()"
    getLocations()
    getInitialAttributes()
}

void updateAttr(String aKey, aValue, String aUnit = ""){
    sendEvent(name:aKey, value:aValue, unit:aUnit)
}

String getAuth() {
    if(serverPath == null)
        device.updateSetting("serverPath",[value:"https://api.daikinskyport.com",type:"string"])
    
    if(debugEnabled) log.debug "getAuth $daiEmail:$daiPwd"
    
    String bodyText = JsonOutput.toJson([email:daiEmail, password:daiPwd])
    
    Map requestParams =
	[
        uri:  "$serverPath/users/auth/login",
        headers: [
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        ], 
        body: "$bodyText"
	]

    authKey=""
    if(debugEnabled) log.debug "$requestParams"
    
    httpPost(requestParams) { resp ->
        if(debugEnabled) log.debug "$resp.properties"
        jsonData = (HashMap) resp.data
        authKey = jsonData.accessToken
    }
    return authKey       

}
    
HashMap getDeviceList(){
    HashMap devMap = sendGet("/devices")
    return devMap
}

HashMap getDevDetail(id) {
    HashMap devDetail = sendGet("/deviceData/$id")
    return devDetail
}

void getLocations() {
    HashMap locData = sendGet("/locations")
    updateAttr("locationMap","$locData")
}

HashMap sendGet(command){
    authToken = getAuth()
    if(debugEnabled) log.debug "sendGet cmd: $command authToken:$authToken "
    Map requestParams =
	[
        uri:  "$serverPath$command",
        headers: [
            'Accept': 'application/json',
            "Authorization" : "Bearer $authToken"
        ]
	]

    if(debugEnabled) log.debug "get parameters $requestParams"
    httpGet(requestParams) { resp ->
        if(debugEnabled) log.debug "$resp.properties"
        jsonData = (HashMap) resp.data
    }
    if(debugEnabled) log.debug "get JSON $jsonData"
    return jsonData
                            
}

void getInitialAttributes(){
    if(debugEnabled) log.debug "Configuring Intial Attributes"
    HashMap devMap = getDeviceList()
    if(debugEnabled) log.debug "Dev List $devMap"
	
	if(debugEnabled) log.debug "${devMap.properties}"
	if(debugEnabled) log.debug "get device properties ${devMap.id}"
	devDetail = getDevDetail("$devMap.id")
	
    device.updateDataValue("daiID", "${devMap.id}")
    device.updateDataValue("daiName", "${devMap.name}")
    device.updateDataValue("firmware", "${devMap.firmwareVersion}")

	updateThermostat(location.temperatureScale)
}

void updateThermostat() {

    modeStr=["off","heat","cool","auto","emergency heat"]
    circStr=["auto","on","circulate"]
	id = device.properties.data["daiID"]
	if(debugEnabled) log.debug "Using ID:$id"
	
    devDetail = getDevDetail("$id")
    degUnit = "°C"
    if(useFahrenheit) {
        devDetail.tempDeltaMin = celsiusToFahrenheit(devDetail.tempDeltaMin.toFloat()).toFloat().round(0)
        devDetail.tempSPMin = celsiusToFahrenheit(devDetail.tempSPMin.toFloat()).toFloat().round(0)
        devDetail.hspHome = celsiusToFahrenheit(devDetail.hspHome.toFloat()).toFloat().round(0)
        devDetail.cspHome = celsiusToFahrenheit(devDetail.cspHome.toFloat()).toFloat().round(0)
        devDetail.tempSPMax = celsiusToFahrenheit(devDetail.tempSPMax.toFloat()).toFloat().round(0)
        devDetail.tempIndoor = celsiusToFahrenheit(devDetail.tempIndoor.toFloat()).toFloat().round(0)
        devDetail.tempOutdoor = celsiusToFahrenheit(devDetail.tempOutdoor.toFloat()).toFloat().round(0)
        degUnit = "°F"
    }
    updateAttr("thermostatMode",modeStr[devDetail.mode.toInteger()])
    updateAttr("fan",devDetail.fan)
    updateAttr("thermostatFanMode",circStr[devDetail.fanCirculate.toInteger()])
    updateAttr("fanCirculateSpeed",devDetail.fanCirculateSpeed)
    updateAttr("setpointDelta",devDetail.tempDeltaMin,degUnit)
    updateAttr("setpointMinimum",devDetail.tempSPMin,degUnit)
    updateAttr("heatingSetpoint",devDetail.hspHome,degUnit)
    updateAttr("coolingSetpoint",devDetail.cspHome,degUnit)
    if(devDetail.mode == 1) 
        updateAttr("thermostatSetpoint",devDetail.hspSched,degUnit)
    else if(devDetail.mode == 2)
        updateAttr("thermostatSetpoint",devDetail.cspSched,degUnit)
    updateAttr("setpointMaximum",devDetail.tempSPMax,degUnit)
    updateAttr("temperature",devDetail.tempIndoor,degUnit)
    updateAttr("tempOutdoor",devDetail.tempOutdoor,degUnit)
    updateAttr("humidity",devDetail.humIndoor,"%")
    updateAttr("humidOutdoor",devDetail.humOutdoor,"%") 
}

void sendPut(command, bodyMap){
    def bodyText = JsonOutput.toJson(bodyMap)
	Map requestParams =
	[
        uri:  "$serverPath$command",
        requestContentType: 'application/json',
		contentType: 'application/json',
        headers: [
            'Accept': 'application/json',
            "Authorization" : "Bearer $authToken"
        ],
        body: "$bodyText"
	]

    
    if(debugEnabled) log.debug "$requestParams"
    httpPut(requestParams) {resp ->
    }
}

/*****************************
 * Begin Thermostat Methods **
 ****************************/ 
 
void refresh() {
	updateThermostat()
    if(pollRate > 0)
        runIn(pollRate*60,"refresh")
}

void auto(){
	sendPut("/deviceData/${device.properties.data["daiID"]}",[mode:3])
    updateAttr("thermostatMode","auto")
}

void cool(){
	sendPut("/deviceData/${device.properties.data["daiID"]}",[mode:2])
    updateAttr("thermostatMode","cool")
}

void emergencyHeat(){
    sendPut("/deviceData/${device.properties.data["daiID"]}",[mode:4])
    updateAttr("thermostatMode","emergency heat")
}

void fanAuto(){
    sendPut("/deviceData/${device.properties.data["daiID"]}",[fanCirculate:0])
    updateAttr("thermostatFanMode","auto")
}

void fanCirculate(){
    sendPut("/deviceData/${device.properties.data["daiID"]}",[fanCirculate:2])
    updateAttr("thermostatFanMode","circulate")
}

void fanOn(){
    sendPut("/deviceData/${device.properties.data["daiID"]}",[fanCirculate:1])
    updateAttr("thermostatFanMode","on")
}

void heat(){
    sendPut("/deviceData/${device.properties.data["daiID"]}",[mode:1])
    updateAttr("thermostatMode","heat")
}

void off(){
    sendPut("/deviceData/${device.properties.data["daiID"]}",[mode:0])
    updateAttr("thermostatMode","emergency heat")
}

void setCoolingSetpoint(temp){
    if(useFahrenheit) {
        temp = fahrenheitToCelsius(temp).toFloat().round(1)
        cOrF = "°F"
    } else cOrF = "°C"
    sendPut("/deviceData/${device.properties.data["daiID"]}",[cspHome:temp])
    updateAttr("thermostatSetPoint",temp,cOrF)
    updateAttr("coolingSetPoint",temp,cOrF)                   
}

void setHeatingSetpoint(temp){
    if(useFahrenheit) {
        temp = fahrenheitToCelsius(temp).toFloat().round(1)
        cOrF = "°F"
    } else cOrF = "°C"
    sendPut("/deviceData/${device.properties.data["daiID"]}",[hspHome:temp])
    updateAttr("thermostatSetPoint",temp,cOrF)
    updateAttr("heatingSetPoint",temp,cOrF)   
}

void setThermostatFanMode(fanmode){
    if(fanmode=="on") 
       fanOn()
    else if (fanmode == "circulate")
       fanCirculate()
    else
       fanAuto()    
}

void setThermostatMode(tmode){
    if(tmode == "auto")
        auto()
    else if(tmode == "heat")
        heat()
    else if(tmode == "cool")
        cool()
    else
        emergencyHeat()
}
/***************************
 * End Thermostat Methods **
 **************************/

@SuppressWarnings('unused')
void logsOff(){
     device.updateSetting("debugEnabled",[value:"false",type:"bool"])
}
