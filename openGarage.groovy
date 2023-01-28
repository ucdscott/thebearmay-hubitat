/*
 * Open Garage
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
 * 2023-01--4	thebearmay	Initial release
 * 2023-01-16	ucdscott	Added txtEnable logging, changed debugEnable preference to Hubitat de facto standard logEnable, updated Change History
 * 2023-01-27	ucdscott	Switched to inline logging, added check for vehicle status 'unknow', and changed attribute setting logic
 *
 */

static String version()	{  return '0.0.2'  }

metadata {
    definition (
		name: "OpenGarage", 
		namespace: "thebearmay", 
		author: "Jean P. May, Jr.",
	        importUrl:"https://raw.githubusercontent.com/thebearmay/hubitat/main/openGarage.groovy"
	) {
        capability "Actuator"
        capability "Polling"
        capability "GarageDoorControl"
        
        attribute "door", "string"
        attribute "distance", "number"
        attribute "vehStatus", "string"
        attribute "rssi","number"
        
        //command "close"
        command "toggleDoor"
        command "rebootDevice"
    }   
}

preferences {
    	input("devIP", "string", title: "IP of the OpenGarage Device", width:4)
    	input("devPwd", "password", title: "Device Password", width:4)
    	input("pollRate","number", title: "Polling interval in seconds (0 to disable)", width:4)
	input (name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: false)
	input("logEnable", "bool", title: "Enable debug logging", defaultValue: false)
}

def installed() {
    log.trace "OpenGarage v${version()} installed()"
    device.updateSetting("devIP",[value:"192.168.4.1",type:"string"])
    device.updateSetting("devPwd",[value:"opendoor",type:"password"]) 
    device.updateSetting("pollRate",[value:0,type:"number"]) 
}

def updated(){
    log.trace "updated()"
    if(logEnable) 
        runIn(1800,"logsOff")
    else 
        unschedule("logEnable")
    if(pollRate > 0)
        runIn(pollRate, "poll")
    else
        unschedule("poll")
    
}

// HTTP GET to open relay
void open() {
    if(txtEnable) log.info "Opening ${device.displayName}..."
    httpGet(
        [
            uri: "http://$devIP",
            path: "/cc?dkey=$devPwd&open=1",
            headers: [            
                   Accept: "application/json"
            ]
        ]
    ) { resp -> }
    runIn(10,"poll")
  
}

// HTTP GET to close relay. Ignored is door is already closed.
void close() {
    if(txtEnable) log.info "Closing ${device.displayName}..."
    httpGet(
        [
            uri: "http://$devIP",
            path: "/cc?dkey=$devPwd&close=1",
            headers: [            
                   Accept: "application/json"
            ]
        ]
    ) { resp -> }
    runIn(10,"poll")
  
}

// HTTP GET to toggle relay
void toggleDoor() {
    if(logEnable) log.debug "Toggling ${device.displayName}..."
    httpGet(
        [
            uri: "http://$devIP",
            path: "/cc?dkey=$devPwd&click=1",
            headers: [            
                   Accept: "application/json"
            ]
        ]
    ) { resp -> }
    runIn(10,"poll")
  
}

void rebootDevice() {
    if(logEnable) log.debug "Rebooting ${device.displayName}..."
    httpGet(
        [
            uri: "http://$devIP",
            path: "/cc?dkey=$devPwd&reboot=1",
            headers: [            
                   Accept: "application/json"
            ]
        ]
    ) { resp -> }
    runIn(30,"poll")
  
}

void updateAttr(String aKey, aValue, String aUnit = ""){
    aValue = aValue.toString()
    sendEvent(name:aKey, value:aValue, unit:aUnit)
}

void poll(){
    if(logEnable) log.debug "Polling ${device.displayName}..."
    httpGet(
        [
            uri: "http://$devIP",
            path: "/jc",
            headers: [            
                   Accept: "application/json"
            ]
        ]
    ) { resp ->
        try{
            if (resp.getStatus() == 200){
                if (logEnable) 
                    log.debug resp.data
                try {
                    processJc((Map)resp.data)
                } catch (e) {
                    processJc(respToMap(resp.data.toString()))
                }
            }
        }catch (ex) {
            log.error "$ex"
        }
    }
    
    if(pollRate > 0)
        runIn(pollRate, "poll")
    else
        unschedule("poll")
}

HashMap respToMap(String rData){
    if(logEnable) log.debug rData
    rList = rData.substring(1,rData.size()-1).split(",")
    if(logEnable) log.debug rList
    rMap = [:]
    rList.each{
        rMap["${it.substring(0,it.indexOf(":")).trim()}"] = it.substring(it.indexOf(":")+1)
    }
    if(logEnable) log.debug rMap
    
    return rMap
}

void processJc(dMap){
   updateAttr("distance", dMap.dist, "cm")
   updateAttr("rssi", dMap.rssi, "dBm")
	
   if(dMap.door.toInteger() == 1) {
	if (device.currentValue("door") != "open"){
		if(txtEnable) log.info "${device.displayName} has opened"
		updateAttr("door","open")
	}
   } else{ // door == 0
        if(device.currentValue("door") != "closed") {
		if(txtEnable) log.info "${device.displayName} has closed"
		updateAttr("door", "closed")
	}
    }
	    
    if(dMap.vehicle.toInteger() == 1){
	// If vehStatus has changed, log and update attribute
	if (device.currentValue("vehStatus") != "arrived"){
		if(txtEnable) log.info "${device.displayName} vehicle arrived"
		updateAttr("vehStatus", "arrived")
	}
    } else if(dMap.vehicle.toInteger() == 2){
    	if(txtEnable) log.info "${device.displayName} vehicle unknown"
	updateAttr("vehStatus", "unknown")
    } else { // vehicle == 0
	// If vehStatus has changed, log and update attribute	
	if(device.currentValue("vehStatus") != "departed"){
		if(txtEnable) log.info "${device.displayName} vehicle departed"
		updateAttr("vehStatus", "departed")
	}
    }
}

void logsOff(){
     device.updateSetting("logEnable",[value:"false",type:"bool"])
}
