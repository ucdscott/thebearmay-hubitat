 /*
 * Hub Info
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
 *    Date        Who            What
 *    ----        ---            ----
 *    2020-12-07  thebearmay     Original version 0.1.0
 *    2021-01-30  thebearmay     Add full hub object properties
 *    2021-01-31  thebearmay     Code cleanup, release ready
 *    2021-01-31  thebearmay     Putting a config delay in at initialize to make sure version data is accurate
 *    2021-02-16  thebearmay     Add text date for restart
 *    2021-03-04  thebearmay     Added CPU and Temperature polling 
 *    2021-03-05  thebearmay     Merged CSteele additions and added the degree symbol and scale to the temperature attribute 
 *    2021-03-05  thebearmay     Merged addtions from LGKhan: Added new formatted uptime attr, also added an html attr that stores a bunch of the useful 
 *                                    info in table format so you can use on any dashboard
 *    2021-03-06  thebearmay     Merged security login from BPTWorld (from dman2306 rebooter app)
 *    2021-03-06  thebearmay     Change numeric attributes to type number
 *    2021-03-08  thebearmay     Incorporate CSteele async changes along with some code cleanup and adding tags to the html to allow CSS overrides
 *    2021-03-09  thebearmay     Code tightening as suggested by CSteele, remove state variables, etc.
 *    2021-03-11  thebearmay     Add Sensor capability for Node-Red/MakerAPI 
 *    2021-03-11  thebearmay     Security not set right at initialize, remove state.attrString if it exists (now a local variable)
 *    2021-03-19  thebearmay     Add attributes for JVM Total, Free, and Free %
 *                               Add JVM info to HTML
 *                               Fix for exceeded 1024 attr limit
 *    2021-03-20  thebearmay     Firmware 2.2.6.xxx support, CPU 5min Load
 *    2021-03-23  thebearmay     Add DB Size
 *    2021-03-24  thebearmay     Calculate CPU % from load 
 *    2021-03-28  thebearmay     jvmWork.eachline error on reboot 
 *    2021-03-30  thebearmay     Index out of bounds on reboot
 *    2021-03-31  thebearmay      jvm to HTML null error (first run)
 *    2021-04-13  thebearmay     pull in suggested additions from lgkhan - external IP and combining some HTML table elements
 *    2021-04-14  thebearmay     add units to the HTML
 *    2021-04-20  thebearmay     provide a smooth transition from 1.8.x to 1.9.x
 *    2021-04-26  thebearmay     break out polls as separate preference options
 *    2021-04-27  thebearmay     replace the homegrown JSON parser, with groovy's JsonSluper
 *    2021-04-29  thebearmay     merge pull request from nh.schottfam, clean up/add type declarations, optimize code and add local variables
 *    2021-05-03  thebearmay     add nonPolling zigbee channel attribute, i.e. set at hub startup
 *    2021-05-04  thebearmay     release 2.2.7.x changes (v2.2.0 - v2.2.2)
 *    2021-05-06  thebearmay     code cleanup from 2.2.2, now 2.2.3
 *    2021-05-09  thebearmay     return NA when zigbee channel not valid
 *    2021-05-25  thebearmay     use upTime to recalculate system start when initialize called manually
 *    2021-05-25  thebearmay     upTime display lagging by 1 poll
 *    2021-06-11  thebearmay     add units to the jvm and memory attributes
 *    2021-06-12  thebearmay     put a space between unit and values
 *    2021-06-14  thebearmay     add Max State/Event days, required trimming of the html attribute
 *    2021-06-15  thebearmay     add ZWave Version attribute
 *                               2.4.1 temporary version to stop overflow on reboot
 *    2021-06-16  thebearmay     2.4.2 overflow trap/retry
 *                               2.4.3 firmware0Version and subVersion is the radio firmware. target 1 version and subVersion is the SDK
 *                               2.4.4/5 restrict Zwave Version query to C7
 *    2021-06-17  thebearmay     2.4.8-10 - add MAC address and hub model, code cleanup, better compatibility check, zwaveVersion check override
 *    2021-06-17  thebearmay     freeMemPollEnabled was combined with the JVM/CPU polling when creating the HTML
 *    2021-06-19  thebearmay     fix the issue where on a driver update, if configure isn't a hubModel and macAddr weren't updated
 *    2021-06-29  thebearmay     2.2.8.x removes JVM data -> v2.5.0
 *    2021-06-30  thebearmay     clear the JVM attributes if >=2.2.8.0, merge pull request from nh.schottfam (stronger typing)
 *    2021-07-01  thebearmay     allow Warn level logging to be suppressed
 *    2021-07-02  thebearmay	    fix missing formatAttrib call
 *    2021-07-15  thebearmay     attribute clear fix
 *    2021-07-22  thebearmay     prep work for deleteCurrentState() with JVM attributes
 *                               use the getHubVersion() call for >=2.2.8.141 
 *    2021-07-23  thebearmay     add remUnused preference to remove all attributes that are not being polled 
 *    2021-08-03  thebearmay     put back in repoll on invalid zigbee channel
 *    2021-08-14  thebearmay     add html update from HIA
 *    2021-08-19  thebearmay     zwaveSDKVersion not in HTML
 *    2021-08-23  thebearmay     simplify unit retrieval
 *    2021-09-16  thebearmay     add localIP check into the polling cycle instead of one time check
 *    2021-09-29  thebearmay     suppress temperature event if negative
 *    2021-10-21  thebearmay     force a read against the database instead of cache when building html
 *    2021-11-02  thebearmay     add hubUpdateStatus
 *    2021-11-05  thebearmay     add hubUpdateVersion
 *    2021-11-09  thebearmay     add NTP Information
 *    2021-11-24  thebearmay     remove the hub update response attribute - release notes push it past the 1024 size limit.
 *    2021-12-01  thebearmay     add additional subnets information
 *    2021-12-07  thebearmay     allow data attribute to be suppressed if zigbee data is null, remove getMacAddress() as it has been retired from the API
 *    2021-12-08  thebearmay     fix zigbee channel bug
 *    2021-12-27  thebearmay     169.254.x.x reboot option
 *    2022-01-17  thebearmay     allow reboot to be called without Hub Monitor parameter
 *    2022-01-21  thebearmay     add Mode and HSM Status as a pollable attribute
 *    2022-03-03  thebearmay     look at attribute size each poll and enforce 1024 limit
 *    2022-03-09  thebearmay     fix lastUpdated not always updated
 *    2022-03-17  thebearmay     add zigbeeStatus
 *    2022-03-18  thebearmay     add zwaveStatus
 *    2022-03-23  thebearmay     code cleanup
 *    2022-03-27  thebearmay     fix zwaveStatus with hub security
 *    2022-03-28  thebearmay     add a try..catch around the zwaveStatus
 *    2022-05-17  thebearmay     enforce 1 decimal place for temperature
 *    2022-05-20  thebearmay     remove a check/force remove for hubUpdateResp
 *    2022-06-10  thebearmay     add hubAlerts, change source for zwaveStatus
 *    2022-06-20  thebearmay     trap login error
 *    2022-06-24  thebearmay     add hubMesh data
 *    2022-06-30  thebearmay     add shutdown command
 *    2022-08-11  thebearmay     add attribute update logging
 *    2022-08-15  thebearmay     add zigbeeStatus2 from the hub2 data
 *    2022-08-19  thebearmay     allow for a user defined HTML attribute using a file template
 *    2022-08-24  thebearmay     switch all HTML attribute processing to the template
 *    2022-09-18  thebearmay     add a security in use attribute
 *    2022-09-29  thebearmay     handle null or 'null' html template
 *	  2022-10-20  thebearmay	 add sunrise sunset
 *    2022-10-21  thebearmay     add format option for lastUpdated
 *    2022-10-25  thebearmay     handle a 408 in fileExists() 
 *    2022-10-26  thebearmay     fix a typo
 *    2022-10-28  thebearmay     add a couple of additional dateTime formats, add traps for null sdf selection
 *    2022-11-18  thebearmay     add an attribute to display next poll time, add checkPolling method instead of forcing a poll at startup
 *    2022-11-22  thebearmay     catch an error on checking poll times
 *	  2022-11-22  thebearmay	 correct stack overflow
 *    2022-11-23  thebearmay     change host for publicIP
 *    2022-11-25  thebearmay     log.warn instead of log.warning
 *    2022-12-09  thebearmay     fix timing issue with Next Poll Time
 *    2022-12-23  thebearmay     use the loopback address for shutdown and reboot
 *    2022-12-29  thebearmay     more hub2 data with HEv2.3.4.126
 *    2023-01-03  thebearmay     minor cosmetic fixes
*/
import java.text.SimpleDateFormat
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Field

@SuppressWarnings('unused')
static String version() {return "3.0.0"}


metadata {
    definition (
        name: "Hub Information v3", 
        namespace: "thebearmay", 
        author: "Jean P. May, Jr.",
        importUrl:"https://raw.githubusercontent.com/thebearmay/hubitat/main/hubInfo.groovy"
    ) {
        capability "Actuator"
        capability "Configuration"
        capability "Initialize"
        capability "Refresh"
        capability "Sensor"
        capability "TemperatureMeasurement"
        
        attribute "latitude", "string"
        attribute "longitude", "string"
        attribute "hubVersion", "string"
        attribute "id", "string"
        attribute "name", "string"
        attribute "data", "string"
        attribute "zigbeeId", "string"
        attribute "zigbeeEui", "string"
        attribute "hardwareID", "string"
        attribute "type", "string"
        attribute "localIP", "string"
        attribute "localSrvPortTCP", "string"
        attribute "uptime", "number"
        attribute "lastUpdated", "string"
        attribute "lastPoll", "string"
        attribute "lastHubRestart", "string"
        attribute "firmwareVersionString", "string"
        attribute "timeZone", "string"
        attribute "temperatureScale", "string"
        attribute "zipCode", "string"
        attribute "locationName", "string"
        attribute "locationId", "string"
        attribute "lastHubRestartFormatted", "string"
        attribute "freeMemory", "number"
        attribute "temperatureF", "string"
        attribute "temperatureC", "string"
        attribute "formattedUptime", "string"
        attribute "html", "string"
        
        attribute "cpu5Min", "number"
        attribute "cpuPct", "number"
        attribute "dbSize", "number"
        attribute "publicIP", "string"
        attribute "zigbeeChannel","string"
        attribute "maxEvtDays", "number"
        attribute "maxStateDays", "number"
        attribute "zwaveVersion", "string"
        attribute "zwaveSDKVersion", "string"        
        //attribute "zwaveData", "string"
        attribute "hubModel", "string"
        attribute "hubUpdateStatus", "string"
        attribute "hubUpdateVersion", "string"
        attribute "currentMode", "string"
        attribute "currentHsmMode", "string"
        attribute "ntpServer", "string"
        attribute "ipSubnetsAllowed", "string"
        attribute "zigbeeStatus", "string"
        attribute "zigbeeStatus2", "string"
        attribute "zigbeeStack", "string"
        attribute "zwaveStatus", "string"
        attribute "hubAlerts", "string"
        attribute "hubMeshData", "string"
        attribute "hubMeshCount", "number"
        attribute "securityInUse", "string"
		attribute "sunrise", "string"
		attribute "sunset", "string"
        attribute "nextPoll", "string"
        //HE v2.3.4.126
        attribute "connectType", "string" //Ethernet, WiFi, Dual
        attribute "dnsServers", "string"
        attribute "staticIPJson", "string"
        attribute "lanIPAddr", "string"
        attribute "wirelessIP", "string"
        attribute "wifiNetwork", "string"
        

        command "hiaUpdate", ["string"]
        command "reboot"
        command "shutdown"
        command "updateCheck"
    }   
}
preferences {
    input("quickref","href", title:"$ttStyleStr<a href='https://htmlpreview.github.io/?https://github.com/thebearmay/hubitat/blob/main/hubInfoQuickRef.html' target='_blank'>Quick Reference v${version()}</a>")
    input("debugEnable", "bool", title: "Enable debug logging?", width:4)
    input("warnSuppress", "bool", title: "Suppress Warn Level Logging", width:4)

	prefList.each { l1 ->
        l1.each{
		    pMap = (HashMap) it.value
            input ("${it.key}", "enum", title: "<div class='tTip'>${pMap.desc}<span class='tTipText'>${pMap.attributeList}</span></div>", options:pollList, submitOnChange:true, width:4, defaultValue:"0")
        }
	}
    
    input("attribEnable", "bool", title: "Enable HTML Attribute Creation?", defaultValue: false, required: false, submitOnChange: true, width:4)
    input("alternateHtml", "string", title: "Template file for HTML attribute", submitOnChange: true, defaultValue: "hubInfoTemplate.res", width:4)
//	input("remUnused", "bool", title: "Remove unused attributes (Requires HE >= 2.2.8.141", defaultValue: false, submitOnChange: true, width:4)
    input("attrLogging", "bool", title: "Log all attribute changes", defaultValue: false, submitOnChange: true, width:4)
    input("allowReboot","bool", title: "Allow Hub to be shutdown or rebooted", defaultValue: false, submitOnChange: true, width:4)
    input("security", "bool", title: "Hub Security Enabled", defaultValue: false, submitOnChange: true, width:4)
    if (security) { 
        input("username", "string", title: "Hub Security Username", required: false, width:4)
        input("password", "password", title: "Hub Security Password", required: false, width:4)
    }
    input("sunSdfPref", "enum", title: "Date/Time Format for Sunrise/Sunset", options:sdfList, defaultValue:"HH:mm:ss", width:4)
    input("updSdfPref", "enum", title: "Date/Time Format for Last Updated", options:sdfList, defaultValue:"Milliseconds", width:4)
    input("upTimeSep", "string", title: "Separator for Formatted Uptime", defaultValue: ",", width:4)
	input("pollRate1", "number", title: "Poll Rate 1 in minutes", defaultValue:0, submitOnChange: true, width:4, constraints:[[NUMBER]]) 
	input("pollRate2", "number", title: "Poll Rate 2 in minutes", defaultValue:0, submitOnChange: true, width:4) 
	input("pollRate3", "number", title: "Poll Rate 3 in minutes", defaultValue:0, submitOnChange: true, width:4) 
    input("pollRate4", "number", title: "Poll Rate 4 in <b style='background-color:red'>&nbsp;hours&nbsp;</b>", defaultValue:0, submitOnChange: true, width:4) 
}
@SuppressWarnings('unused')
void installed() {
    log.trace "installed()"
    xferFile("https://raw.githubusercontent.com/thebearmay/hubitat/main/hubInfoTemplate.res","hubInfoTemplate.res")
    initialize()
    configure()
}

void initialize() {
    restartCheck()
    updated()
    if(security) cookie = getCookie()
    freeMemoryReq(cookie)
    baseData()
}

void configure() {
    baseData()
    updated()
}

void updated(){
    if(debugEnable) log.debug "updated"
	unschedule()
	state.poll1 = []
	state.poll2 = []
	state.poll3 = []
    state.poll4 = []
	prefList.each{ l1 ->
        l1.each{
            if(settings["${it.key}"] != null && settings["${it.key}"] != "0") {
                pMap = (HashMap) it.value
                if(debugEnable) log.debug "poll${settings["${it.key}"]} ${pMap.method}" 
                state["poll${settings["${it.key}"]}"].add("${pMap.method}")
            }
        }
    }    
    
	if(pollRate1 > 0)
		runIn(pollRate1*60, "poll1")
	if(pollRate2 > 0)
		runIn(pollRate2*60, "poll2")
	if(pollRate3 > 0)
		runIn(pollRate3*60, "poll3")		
    if(pollRate4 > 0)
		runIn(pollRate4*60*60, "poll4")	
}

void refresh(){
    baseData()
    poll1()
    poll2()
    poll3()
    poll4()
}

void poll1(){
    if(security) cookie = getCookie()
	state.poll1.each{
		this."$it"(cookie)
	}
	if(pollRate1 > 0)
		runIn(pollRate1*60, "poll1")
    everyPoll("poll1")
}

void poll2(){
    if(security) cookie = getCookie()
	state.poll2.each{
		this."$it"(cookie)
	}
	if(pollRate2 > 0)
		runIn(pollRate2*60, "poll2")
    everyPoll("poll2")
}

void poll3(){
    if(security) cookie = getCookie()
	state.poll3.each{
		this."$it"(cookie)
	}
	if(pollRate3*60 > 0)
		runIn(pollRate3, "poll3")
    everyPoll("poll3")
}

void poll4(){
    if(security) cookie = getCookie()
	state.poll1.each{
		this."$it"(cookie)
	}
	if(pollRate4 > 0)
		runIn(pollRate4*60*60, "poll4")
    everyPoll("poll4")
}

void baseData(dummy=null){
    String model = getHubVersion() // requires >=2.2.8.141
    updateAttr("hubModel", model)
    
    List locProp = ["latitude", "longitude", "timeZone", "zipCode", "temperatureScale"]
    locProp.each{
        if(it != "timeZone")
            updateAttr(it, location["${it}"])
        else {
            tzWork=location["timeZone"].toString().substring(location["timeZone"].toString().indexOf("TimeZone")+8)
            tzMap= (Map) evaluate(tzWork.replace("=",":\"").replace(",","\",").replace("]]","\"]"))
            updateAttr("timeZone",JsonOutput.toJson(tzMap))
        }
    }
    
    def myHub = location.hub
    List hubProp = ["id","name","zigbeeId","zigbeeEui","hardwareID","type","localIP","localSrvPortTCP","firmwareVersionString","uptime"]
    hubProp.each {
        updateAttr(it, myHub["${it}"])
    }

    if(location.hub.properties.data.zigbeeChannel != null)
        updateAttr("zigbeeChannel",location.hub.properties.data.zigbeeChannel)
    else
        updateAttr("zigbeeChannel","Not Available")
                   
    if(location.hub.properties.data.zigbeeChannel != null)
        updateAttr("zigbeeStatus", "enabled")
    else
        updateAttr("zigbeeStatus", "disabled")
    
    updateAttr("locationName", location.name)
    updateAttr("locationId", location.id)

    everyPoll("baseData")
}

void everyPoll(whichPoll=null){
    updateAttr("currentMode", location.properties.currentMode)
    updateAttr("currentHsmMode", location.hsmStatus)
    
    SimpleDateFormat sdfIn = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
    sunrise = sdfIn.parse(location.sunrise.toString())
    sunset = sdfIn.parse(location.sunset.toString())
    
	if(sunSdfPref == null) device.updateSetting("sunSdfPref",[value:"HH:mm:ss",type:"enum"])
    if(sunSdfPref != "Milliseconds") {
        SimpleDateFormat sdf = new SimpleDateFormat(sunSdfPref)
        updateAttr("sunrise", sdf.format(sunrise))
	    updateAttr("sunset", sdf.format(sunset))
    } else {
        updateAttr("sunrise", sunrise.getTime())
	    updateAttr("sunset", sunset.getTime())  
    }
    updateAttr("localIP",location.hub.localIP)

    if(updSdfPref == null) device.updateSetting("updSdfPref",[value:"Milliseconds",type:"string"])
    if(updSdfPref == "Milliseconds" || updSdfPref == null) 
        updateAttr("lastUpdated", new Date().getTime())
    else {
        SimpleDateFormat sdf = new SimpleDateFormat(updSdfPref)
        updateAttr("lastUpdated", sdf.format(new Date().getTime()))
    }
    if(whichPoll != null)
        updateAttr("lastPoll", whichPoll)
    
    formatUptime()
      
    if (attribEnable) createHtml()
    
}

void updateAttr(String aKey, aValue, String aUnit = ""){
    aValue = aValue.toString()
/*    if(aValue.length() > 1024) {
        log.error "Attribute value for $aKey exceeds 1024, current size = ${aValue.length()}, truncating to 1024..."
        aValue = aValue.substring(0,1023)
    }*/
    sendEvent(name:aKey, value:aValue, unit:aUnit)
    if(attrLogging) log.info "$aKey : $aValue$aUnit"
}

void cpuTemperatureReq(cookie){
    params = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub/advanced/internalTempCelsius",
        headers: ["Cookie": cookie]
    ]
    if (debugEnabled) log.debug params
    asynchttpGet("getCpuTemperature", params)    
}

void getCpuTemperature(resp, data) {
    try {
        if(resp.getStatus() == 200 || resp.getStatus() == 207) {
            Double tempWork = new Double(resp.data.toString())
            if(tempWork > 0) {
                if(debugEnable) log.debug tempWork
                if (location.temperatureScale == "F")
                    updateAttr("temperature",String.format("%.1f", celsiusToFahrenheit(tempWork)),"°F")
                else
                    updateAttr("temperature",String.format("%.1f",tempWork),"°C")

                updateAttr("temperatureF",String.format("%.1f",celsiusToFahrenheit(tempWork))+ " °F")
                updateAttr("temperatureC",String.format("%.1f",tempWork)+ " °C")
            }
        }
    } catch(ignored) {
        def respStatus = resp.getStatus()
        if (!warnSuppress) log.warn "getTemp httpResp = $respStatus but returned invalid data, will retry next cycle"
    } 
}


void freeMemoryReq(cookie){
    params = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub/advanced/freeOSMemory",
        headers: ["Cookie": cookie]
    ]
    if (debugEnable) log.debug params
        asynchttpGet("getFreeMemory", params)    
}

@SuppressWarnings('unused')
void getFreeMemory(resp, data) {
    try {
        if(resp.getStatus() == 200 || resp.getStatus() == 207) {
            Integer memWork = new Integer(resp.data.toString())
            if(debugEnable) log.debug memWork
            updateAttr("freeMemory",memWork, "KB")
        }
    } catch(ignored) {
        def respStatus = resp.getStatus()
        if (!warnSuppress) log.warn "getFreeMem httpResp = $respStatus but returned invalid data, will retry next cycle"    
    }
}

void cpuLoadReq(cookie){
    params = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub/advanced/freeOSMemoryLast",
        headers: ["Cookie": cookie]
    ]
    if (debugEnable) log.debug params
    asynchttpGet("getCpuLoad", params)    
}

void getCpuLoad(resp, data) {
    String loadWork
    List<String> loadRec = []
    try {
        if(resp.getStatus() == 200 || resp.getStatus() == 207) {
            loadWork = resp.data.toString()
        }
    } catch(ignored) {
        def respStatus = resp.getStatus()
        if (!warnSuppress) log.warn "getCpuLoad httpResp = $respStatus but returned invalid data, will retry next cycle"    
    }
    if (loadWork) {
        Integer lineCount = 0
        loadWork.eachLine{
            lineCount++
        }
        Integer lineCount2 = 0
        loadWork.eachLine{
            lineCount2++
            if(lineCount==lineCount2)
                workSplit = it.split(",")
        }
        if(workSplit.size() > 1){
            Double cpuWork=workSplit[2].toDouble()
            updateAttr("cpu5Min",cpuWork.round(2))
            cpuWork = (cpuWork/4.0D)*100.0D //Load / #Cores - if cores change will need adjusted to reflect
            updateAttr("cpuPct",cpuWork.round(2),"%")            
        }
    }
}
void dbSizeReq(cookie){
    params = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub/advanced/databaseSize",
        headers: ["Cookie": cookie]
    ]

    if (debugEnable) log.debug params
    asynchttpGet("getDbSize", params)    
}

@SuppressWarnings('unused')
void getDbSize(resp, data) {
    try {
        if(resp.getStatus() == 200 || resp.getStatus() == 207) {
            Integer dbWork = new Integer(resp.data.toString())
            if(debugEnable) log.debug dbWork
            updateAttr("dbSize",dbWork,"MB")
        }
    } catch(ignored) {
        def respStatus = resp.getStatus()
        if (!warnSuppress) log.warn "getDb httpResp = $respStatus but returned invalid data, will retry next cycle"
    } 
}

void publicIpReq(cookie){
    params = [
        uri: "https://api.ipify.org?format=json",
        headers: [            
            Accept: "application/json"
        ]
    ]
    
    if(debugEnable)log.debug params
    asynchttpGet("getPublicIp", params)
}

@SuppressWarnings('unused')
void getPublicIp(resp, data){
    try{
        if (resp.getStatus() == 200){
            if (debugEnable) log.debug resp.data
            def jSlurp = new JsonSlurper()
            Map ipData = (Map)jSlurp.parseText((String)resp.data)
            updateAttr("publicIP",ipData.ip)
        } else {
            if (!warnSuppress) log.warn "Status ${resp.getStatus()} while fetching Public IP"
        } 
    } catch (Exception ex){
        if (!warnSuppress) log.warn ex
    }
}

void evtStateDaysReq(cookie){
    //Max State Days
    params = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub/advanced/maxDeviceStateAgeDays",
        headers: ["Cookie": cookie]           
    ]
    
    if(debugEnable)log.debug params
    asynchttpGet("getStateDays", params)
     
     //Max Event Days
    params = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub/advanced/maxEventAgeDays",
        headers: ["Cookie": cookie]           
    ]
    
    if(debugEnable)log.debug params
    asynchttpGet("getEvtDays", params)
}

@SuppressWarnings('unused')
void getEvtDays(resp, data) {
    try {
        if(resp.getStatus() == 200 || resp.getStatus() == 207) {
            Integer evtDays = new Integer(resp.data.toString())
            if(debugEnable) log.debug "Max Event Days $evtDays"

            updateAttr("maxEvtDays",evtDays)
        }
    } catch(ignored) {
        def respStatus = resp.getStatus()
        if (!warnSuppress) log.warn "getEvtDays httpResp = $respStatus but returned invalid data, will retry next cycle"
    } 
}

@SuppressWarnings('unused')
void getStateDays(resp, data) {
    try {
        if(resp.getStatus() == 200 || resp.getStatus() == 207) {
            Integer stateDays = new Integer(resp.data.toString())
            if(debugEnable) log.debug "Max State Days $stateDays"

            updateAttr("maxStateDays",stateDays)
        }
    } catch(ignored) {
        def respStatus = resp.getStatus()
        if (!warnSuppress) log.warn "getStateDays httpResp = $respStatus but returned invalid data, will retry next cycle"
    } 
}

void zwaveVersionReq(cookie){
    if(!isCompatible(7)) {
        if(!warnSuppress) log.warn "ZWave Version information not available for this hub"
        return
    }
    param = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub/zwaveVersion",
        headers: ["Cookie": cookie]
    ]
    if (debugEnable) log.debug param
    asynchttpGet("getZwaveVersion", param)
}

@SuppressWarnings('unused')
void getZwaveVersion(resp, data) {
    try {
        if(resp.getStatus() == 200 || resp.getStatus() == 207) {
            String zwaveData = resp.data.toString()
            if(debugEnable) log.debug resp.data.toString()
            if(zwaveData.length() < 1024){
                //updateAttr("zwaveData",zwaveData)
                parseZwave(zwaveData)
            }
            else if (!warnSuppress) log.warn "Invalid data returned for Zwave, length = ${zwaveData.length()} will retry"
        }
    } catch(ignored) {
        if (!warnSuppress) log.warn "getZwave Parsing Error"    
    } 
}

@SuppressWarnings('unused')
void parseZwave(String zString){
    Integer start = zString.indexOf('(')
    Integer end = zString.length()
    String wrkStr
    
    if(start == -1 || end < 1 || zString.indexOf("starting up") > 0 ){ //empty or invalid string - possibly non-C7
        //updateAttr("zwaveData",null)
        log.error "Invalid ZWave Data returned"
    }else {
        wrkStr = zString.substring(start,end)
        wrkStr = wrkStr.replace("(","[")
        wrkStr = wrkStr.replace(")","]")

        HashMap zMap = (HashMap)evaluate(wrkStr)
        
        updateAttr("zwaveSDKVersion","${((List)zMap.targetVersions)[0].version}.${((List)zMap.targetVersions)[0].subVersion}")
        updateAttr("zwaveVersion","${zMap?.firmware0Version}.${zMap?.firmware0SubVersion}")
    }
}

void ntpServerReq(cookie){
    params = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub/advanced/ntpServer",
        headers: ["Cookie": cookie]           
    ]
    
    if(debugEnable)log.debug params
    asynchttpGet("getNtpServer", params)    
}

@SuppressWarnings('unused')
void getNtpServer(resp, data) {
    try {
        if (resp.status == 200) {
            ntpServer = resp.data.toString()
            if(ntpServer == "No value set") ntpServer = "Hub Default(Google)"
            updateAttr("ntpServer", ntpServer)
        } else {
            if(!warnSuppress) log.warn "NTP server check returned status: ${resp.status}"
        }
    }catch (ignore) {
    }
}

void ipSubnetsReq(cookie){
    params = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub/allowSubnets",
        headers: ["Cookie": cookie]           
    ]
    
    if(debugEnable)log.debug params
    asynchttpGet("getSubnets", params)
}

@SuppressWarnings('unused')
void getSubnets(resp, data) {
    try {
        if (resp.status == 200) {
            subNets = resp.data.toString()
            if(subNets == "Not set") subNets = "Hub Default"
            updateAttr("ipSubnetsAllowed", subNets)
        } else {
            if(!warnSuppress) log.warn "Subnet check returned status: ${resp.status}"
        }
    }catch (ignore) {
    }
}

void hubMeshReq(cookie){
    params =  [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub2/hubMeshJson",
        headers: ["Cookie": cookie]           
    ]
    
    if(debugEnable)log.debug params
    asynchttpGet("getHubMesh", params)
}

@SuppressWarnings('unused')
void getHubMesh(resp, data){
    try{
        if (resp.getStatus() == 200){
            if (debugEnable) log.info resp.data
            def jSlurp = new JsonSlurper()
            Map h2Data = (Map)jSlurp.parseText((String)resp.data)
            i=0
            subMap2=[:]
            jStr="["
            h2Data.hubList.each{
                if(i>0) jStr+=","
                jStr+="{\"hubName\":\"$it.name\","
                jStr+="\"active\":\"$it.active\","
                jStr+="\"offline\":\"$it.offline\","
                jStr+="\"ipAddress\":\"$it.ipAddress\","
                jStr+="\"meshProtocol\":\"$h2Data.hubMeshProtocol\"}"          
                i++
            }
            jStr+="]"
            updateAttr("hubMeshData", jStr)
            updateAttr("hubMeshCount",i)

        } else {
            if (!warnSuppress) log.warn "Status ${resp.getStatus()} on H2 request"
        } 
    } catch (Exception ex){
        if (!warnSuppress) log.warn ex
    }
}

void extNetworkReq(cookie){
    if(location.hub.firmwareVersionString < "2.3.4.126"){
        if(!warnSuppress) log.warn "Extend Network Data not available for HE v${location.hub.firmwareVersionString}"
        return
    }
        
    params = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub2/networkConfiguration",
        headers: ["Cookie": cookie]           
    ]
    
    if(debugEnable)log.debug params
    asynchttpGet("getExtNetwork", params)
}

void hub2DataReq(cookie) {
    params = [
        uri    : "http://127.0.0.1:8080",
        path   : "/hub2/hubData",
        headers: ["Cookie": cookie]                   
    ]
    
        if(debugEnable)log.debug params
        asynchttpGet("getHub2Data", params)
}

@SuppressWarnings('unused')
void getHub2Data(resp, data){
    try{
        if (resp.getStatus() == 200){
            if (debugEnable) log.debug resp.data
            try{
				def jSlurp = new JsonSlurper()
			    h2Data = (Map)jSlurp.parseText((String)resp.data)
            } catch (eIgnore) {
                if (debugEnable) log.debug "H2: $h2Data <br> ${resp.data}"
                return
            }
            
            hubAlerts = []
            h2Data.alerts.each{
                if(it.value == true){
                    if("$it.key".indexOf('Database') > -1)
                        hubAlerts.add("hubDatabaseSize")
                    else if("$it.key".indexOf('Load') > -1)
                        hubAlerts.add("hubLoad")
                    else if("$it.key" != "runAlerts")
                        hubAlerts.add(it.key)
                }
            }
            updateAttr("hubAlerts",hubAlerts)
            if(h2Data?.baseModel == null) {
                if (debugEnable) log.debug "baseModel is missing from h2Data, ${device.currentValue('hubModel')} ${device.currentValue('firmwareVersionString')}<br>$h2Data"
                return
            }
            if(h2Data.baseModel.zwaveStatus == "false") 
                updateAttr("zwaveStatus","enabled")
            else
                updateAttr("zwaveStatus","disabled")
            if(h2Data.baseModel.zigbeeStatus == "false"){
                updateAttr("zigbeeStatus2", "enabled")
                if (device.currentValue("zigbeeStatus", true) != "enabled") log.warn "Zigbee Status has opposing values - may have crashed."
            } else {
                updateAttr("zigbeeStatus2", "disabled")
                if (device.currentValue("zigbeeStatus", true) != "disabled") log.warn "Zigbee Status has opposing values - may have crashed."
            }
            if(debugEnable) log.debug "securityInUse"
            updateAttr("securityInUse", h2Data.baseModel.userLoggedIn)
            if(debugEnable) log.debug "h2 security check"
            if((!security || password == null || username == null) && h2Data.baseModel.userLoggedin == true){
                log.error "Hub using Security but credentials not supplied"
                device.updateSetting("security",[value:"true",type:"bool"])
            }
        } else {
            if (!warnSuppress) log.warn "Status ${resp.getStatus()} on H2 request"
        } 
    } catch (Exception ex){
        if (!warnSuppress) log.warn ex
    }
}

@SuppressWarnings('unused')
void getExtNetwork(resp, data){
    try{
        if (resp.getStatus() == 200){
            if (debugEnable) log.info resp.data
            def jSlurp = new JsonSlurper()
            Map h2Data = (Map)jSlurp.parseText((String)resp.data)
            if(!h2Data.usingStaticIP)
                updateAttr("staticIPJson", "[]")
            else {
                jMap = [staticIP:"${h2Data.staticIP}", staticGateway:"${h2Data.staticGateway}", staticSubnetMask:"${h2Data.staticSubnetMask}",staticNameServers:"${h2Data.staticNameServers}"]
                updateAttr("staticIPJson",JsonOutput.toJson(jMap))
            }
            if(h2Data.hasEthernet && h2Data.hasWiFi ){
                updateAttr("connectType","Dual")
                updateAttr("lanIPAddr", h2Data.lanAddr)    
            } else if(h2Data.hasEthernet){
                updateAttr("connectType","Ethernet")
                updateAttr("lanIPAddr", h2Data.lanAddr)
            } else if(h2Data.hasWiFi)
                updateAttr("connectType","WiFi")
            if(h2Data.hasWiFi){
                updateAttr("wifiNetwork", h2Data.wifiNetwork)
                updateAttr("wirelessIP",h2Data.wlanAddr)
            } else {
                updateAttr("wifiNetwork", "None")
                updateAttr("wirelessIP", "None")
            }
            updateAttr("dnsServers", h2Data.dnsServers)
            updateAttr("lanIPAddr", h2Data.lanAddr)                           
        }
    }catch (ex) {
        if (!warnSuppress) log.warn ex
    }
}

void updateCheck(){
    if(security) cookie = getCookie()
    updateCheckReq(cookie)
}

void updateCheckReq(cookie){
    params = [
        uri: "http://${location.hub.localIP}:8080",
        path:"/hub/cloud/checkForUpdate",
        timeout: 10,
        headers:["Cookie": cookie]
    ]
    asynchttpGet("getUpdateCheck", params)
}

@SuppressWarnings('unused')
void getUpdateCheck(resp, data) {
    try {
        if (resp.status == 200) {
            def jSlurp = new JsonSlurper()
            Map resMap = (Map)jSlurp.parseText((String)resp.data)
            if(resMap.status == "NO_UPDATE_AVAILABLE")
                updateAttr("hubUpdateStatus","Current")
            else
                updateAttr("hubUpdateStatus","Update Available")
            if(resMap.version)
                updateAttr("hubUpdateVersion",resMap.version)
            else updateAttr("hubUpdateVersion",location.hub.firmwareVersionString)
        }
    } catch(ignore) {
       updateAttr("hubUpdateStatus","Status Not Available")
    }

}

void zigbeeStackReq(cookie){
    params = [
        uri: "http://127.0.0.1:8080",
        path:"/hub/currentZigbeeStack",
        headers:["Cookie": cookie]
    ]
        asynchttpGet("getZigbeeStack",params) 
}

void getZigbeeStack(resp, data) {
    try {
        if(resp.data.toString().indexOf('standard') > -1)
            updateAttr("zigbeeStack","standard")
        else
            updateAttr("zigbeeStack","new")      
    } catch(ignore) { }
}
                     

@SuppressWarnings('unused')
boolean isCompatible(Integer minLevel) { //check to see if the hub version meets the minimum requirement
    String model = getHubVersion()
    String[] tokens = model.split('-')
    String revision = tokens.last()
    return (Integer.parseInt(revision) >= minLevel)
}

@SuppressWarnings('unused')
String getCookie(){
    try{
  	  httpPost(
		[
		uri: "http://127.0.0.1:8080",
		path: "/login",
		query: [ loginRedirect: "/" ],
		body: [
			username: username,
			password: password,
			submit: "Login"
			]
		]
	  ) { resp -> 
		cookie = ((List)((String)resp?.headers?.'Set-Cookie')?.split(';'))?.getAt(0) 
        if(debugEnable)
            log.debug "$cookie"
	  }
    } catch (e){
        cookie = ""
    }
    return "$cookie"
}

@SuppressWarnings('unused')
Boolean xferFile(fileIn, fileOut) {
    fileBuffer = (String) readExtFile(fileIn)
    retStat = writeFile(fileOut, fileBuffer)
    if(logResponses) log.info "File xFer Status: $retStat"
    return retStat
}

@SuppressWarnings('unused')
String readExtFile(fName){
    if(security) cookie = getCookie()    
    def params = [
        uri: fName,
        contentType: "text/html",
        textParser: true,
        headers: [
				"Cookie": cookie
            ]        
    ]

    try {
        httpGet(params) { resp ->
            if(resp!= null) {
               int i = 0
               String delim = ""
               i = resp.data.read() 
               while (i != -1){
                   char c = (char) i
                   delim+=c
                   i = resp.data.read() 
               } 
               if(debugEnable) log.info "Read External File result: delim"
               return delim
            }
            else {
                log.error "Read External - Null Response"
            }
        }
    } catch (exception) {
        log.error "Read Ext Error: ${exception.message}"
        return null;
    }
}

@SuppressWarnings('unused')
Boolean fileExists(fName){
    if(fName == null) return false
    uri = "http://${location.hub.localIP}:8080/local/${fName}";

     def params = [
        uri: uri          
    ]

    try {
        httpGet(params) { resp ->
            if (resp != null){
                if(debugEnable) log.debug "File Exist: true"
                return true;
            } else {
                if(debugEnable) log.debug "File Exist: true"
                return false
            }
        }
    } catch (exception){
        if (exception.message == "status code: 404, reason phrase: Not Found"){
            if(debugEnable) log.debug "File Exist: false"
        } else if (resp.getStatus() != 408) {
            log.error "Find file $fName :: Connection Exception: ${exception.message}"
        }
        return false
    }
}

@SuppressWarnings('unused')
void hiaUpdate(htmlStr) {
	updateAttr("html",htmlStr)
}

void createHtml(){
    if(alternateHtml == null || fileExists("$alternateHtml") == false){
        xferFile("https://raw.githubusercontent.com/thebearmay/hubitat/main/hubInfoTemplate.res","hubInfoTemplate.res")
        device.updateSetting("alternateHtml",[value:"hubInfoTemplate.res", type:"string"])
    }
    String fContents = readFile("$alternateHtml")
    if(fContents == 'null' || fContents == null) {
        xferFile("https://raw.githubusercontent.com/thebearmay/hubitat/main/hubInfoTemplate.res","hubInfoTemplate.res")
        device.updateSetting("alternateHtml",[value:"hubInfoTemplate.res", type:"string"]) 
        fContents = readFile("$alternateHtml")
    }
    List fRecs=fContents.split("\n")
    String html = ""
    fRecs.each {
        int vCount = it.count("<%")
        if(debugEnable) log.debug "variables found: $vCount"
        if(vCount > 0){
            recSplit = it.split("<%")
            if(debugEnable) log.debug "$recSplit"
            recSplit.each {
                if(it.indexOf("%>") == -1)
                    html+= it
                else {
                    vName = it.substring(0,it.indexOf('%>'))
                    if(debugEnable) log.debug "${it.indexOf("5>")}<br>$it<br>${it.substring(0,it.indexOf("%>"))}"
                    if(vName == "date()" || vName == "@date")
                        aVal = new Date()
                    else if (vName == "@version")
                        aVal = version()
                    else {
                        aVal = device.currentValue("$vName",true)
                        String attrUnit = getUnitFromState("$vName")
                        if (attrUnit != null) aVal+=" $attrUnit"
                    }
                    html+= aVal
                    if(it.indexOf("%>")+2 != it.length()) {
                        if(debugEnable) log.debug "${it.substring(it.indexOf("%>")+2)}"
                        html+=it.substring(it.indexOf("%>")+2)
                    }
                }                 
            }
        }
        else html += it
    }
    if (debugEnable) log.debug html
    updateAttr("html", html)
}

@SuppressWarnings('unused')
String readFile(fName){
    if(security) cookie = getCookie()
    uri = "http://${location.hub.localIP}:8080/local/${fName}"

    def params = [
        uri: uri,
        contentType: "text/html",
        textParser: true,
        headers: [
				"Cookie": cookie,
                "Accept": "application/octet-stream"
            ]
    ]

    try {
        httpGet(params) { resp ->
            if(resp!= null) {       
               int i = 0
               String delim = ""
               i = resp.data.read() 
               while (i != -1){
                   char c = (char) i
                   delim+=c
                   i = resp.data.read() 
               }
               if(debugEnabled) log.info "File Read Data: $delim"
               return delim
            }
            else {
                log.error "Null Response"
            }
        }
    } catch (exception) {
        log.error "Read Error: ${exception.message}"
        return null;
    }
}
@SuppressWarnings('unused')
Boolean writeFile(String fName, String fData) {
    now = new Date()
    String encodedString = "thebearmay$now".bytes.encodeBase64().toString(); 
    
try {
		def params = [
			uri: 'http://127.0.0.1:8080',
			path: '/hub/fileManager/upload',
			query: [
				'folder': '/'
			],
			headers: [
				'Content-Type': "multipart/form-data; boundary=$encodedString;text/html; charset=utf-8"
			],
            body: """--${encodedString}
Content-Disposition: form-data; name="uploadFile"; filename="${fName}"
Content-Type: text/plain

${fData}

--${encodedString}
Content-Disposition: form-data; name="folder"


--${encodedString}--""",
			timeout: 300,
			ignoreSSLIssues: true
		]
		httpPost(params) { resp ->
		}
		return true
	}
	catch (e) {
		log.error "Error writing file $fName: ${e}"
	}
	return false
}


@SuppressWarnings('unused')
void reboot() {
    if(!allowReboot){
        log.error "Reboot was requested, but allowReboot was set to false"
        return
    }
    log.info "Hub Reboot requested"
    // start - Modified from dman2306 Rebooter app
    String cookie=(String)null
    if(security) cookie = getCookie()
	httpPost(
		[
			uri: "http://127.0.0.1:8080",
			path: "/hub/reboot",
			headers:[
				"Cookie": cookie
			]
		]
	) {		resp ->	} 
    // end - Modified from dman2306 Rebooter app
}

@SuppressWarnings('unused')
void shutdown() {
    if(!allowReboot){
        log.error "Shutdown was requested, but allowReboot/Shutdown was set to false"
        return
    }
    log.info "Hub Reboot requested"
    // start - Modified from dman2306 Rebooter app
    String cookie=(String)null
    if(security) cookie = getCookie()
	httpPost(
		[
			uri: "http://127.0.0.1:8080",
			path: "/hub/shutdown",
			headers:[
				"Cookie": cookie
			]
		]
	) {		resp ->	} 
    // end - Modified from dman2306 Rebooter app
}
                   
void formatUptime(){
    updateAttr("uptime", location.hub.uptime)
    try {
        Long ut = location.hub.uptime.toLong()
        Integer days = Math.floor(ut/(3600*24)).toInteger()
        Integer hrs = Math.floor((ut - (days * (3600*24))) /3600).toInteger()
        Integer min = Math.floor( (ut -  ((days * (3600*24)) + (hrs * 3600))) /60).toInteger()
        Integer sec = Math.floor(ut -  ((days * (3600*24)) + (hrs * 3600) + (min * 60))).toInteger()
        if(upTimeSep == null){
            device.updateSetting("upTimeSep",[value:",", type:"string"])
            upTimeSep = ","
        }
        String attrval = "${days.toString()}d$upTimeSep${hrs.toString()}h$upTimeSep${min.toString()}m$upTimeSep${sec.toString()}s"
        updateAttr("formattedUptime", attrval) 
    } catch(ignore) {
        updateAttr("formattedUptime", "")
    }
}

@SuppressWarnings('unused')
void restartCheck() {
    if(debugEnable) log.debug "$rsDate"
    Long ut = new Date().getTime().toLong() - (location.hub.uptime.toLong()*1000)
    Date upDate = new Date(ut)
    if(debugEnable) log.debug "RS: $rsDate  UT:$ut  upTime Date: $upDate   upTime: ${location.hub.uptime}"
    
    updateAttr("lastHubRestart", ut)
    SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    updateAttr("lastHubRestartFormatted",sdf.format(upDate))
}

@SuppressWarnings('unused')
String getUnitFromState(String attrName){
   	return device.currentState(attrName)?.unit
}

@SuppressWarnings('unused')
void logsOff(){
     device.updateSetting("debugEnable",[value:"false",type:"bool"])
}

@Field static String minFwVersion = "2.2.8.141"
@Field static List <String> pollList = ["0", "1", "2", "3", "4"]
@Field static prefList = [[parm01:[desc:"CPU Temperature Polling", attributeList:"temperatureF, temperatureC, temperature", method:"cpuTemperatureReq"]],
[parm02:[desc:"Free Memory Polling", attributeList:"freeMemory", method:"freeMemoryReq"]],
[parm03:[desc:"CPU Load Polling", attributeList:"cpuLoad, cpuPct", method:"cpuLoadReq"]],
[parm04:[desc:"DB Size Polling", attributeList:"dbSize", method:"dbSizeReq"]],
[parm05:[desc:"Public IP Address", attributeList:"publicIP", method:"publicIpReq"]],
[parm06:[desc:"Max Event/State Days Setting", attributeList:"maxEvtDays,maxStateDays", method:"evtStateDaysReq"]], 
[parm07:[desc:"ZWave Version", attributeList:"zwaveVersion, zwaveSDKVersion", method:"zwaveVersionReq"]],
[parm08:[desc:"Time Sync Server Address", attributeList:"ntpServer", method:"ntpServerReq"]],
[parm09:[desc:"Additional Subnets", attributeList:"ipSubnetsAllowed", method:"ipSubnetsReq"]],
[parm10:[desc:"Hub Mesh Data", attributeList:"hubMeshData, hubMeshCount", method:"hubMeshReq"]],
[parm11:[desc:"Expanded Network Data", attributeList:"connectType (Ethernet, WiFi, Dual), dnsServers, staticIPJson, lanIPAddr, wirelessIP, wifiNetwork", method:"extNetworkReq"]],
[parm12:[desc:"Check for Firmware Update",attributeList:"hubUpdateStatus, hubUpdateVersion",method:"updateCheckReq"]],
[parm13:[desc:"Zwave Status & Hub Alerts",attributeList:"hubAlerts,zwaveStatus, zigbeeStatus2, securityInUse", method:"hub2DataReq"]],
[parm14:[desc:"Base Data",attributeList:"firmwareVersionString, hardwareID, id, latitude, localIP, localSrvPortTCP, locationId, locationName, longitude, name, temperatureScale, timeZone, type, uptime, zigbeeChannel, zigbeeEui, zigbeeId, zigbeeStatus, zipCode",method:"baseData"]]]
@Field static String ttStyleStr = "<style>.tTip {display:inline-block;border-bottom: 1px dotted black;}.tTip .tTipText {display:none;border-radius: 6px;padding: 5px 0;position: absolute;z-index: 1;}.tTip:hover .tTipText {display:inline-block;background-color:yellow;color:black;}</style>"
@Field sdfList = ["yyyy-MM-dd","yyyy-MM-dd HH:mm","yyyy-MM-dd h:mma","yyyy-MM-dd HH:mm:ss","ddMMMyyyy HH:mm","ddMMMyyyy HH:mm:ss","ddMMMyyyy hh:mma", "dd/MM/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm:ss", "dd/MM/yyyy hh:mma", "MM/dd/yyyy hh:mma", "MM/dd HH:mm", "HH:mm", "H:mm","h:mma", "HH:mm:ss", "Milliseconds"]