/* Tile Template Multi-Device Manager
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
 *     Date              Who           Description
 *    ===========       ===========   =====================================================
 *    
*/

static String version()	{  return '0.0.3'  }


definition (
	name: 			"Tile Template Multi-Device Manager", 
	namespace: 		"thebearmay", 
	author: 		"Jean P. May, Jr.",
	description: 	"Use a template file to generate an HTML element for multiple named devices.",
	category: 		"Utility",
	importUrl:"https://raw.githubusercontent.com/thebearmay/hubitat/main/apps/xxxx.groovy",
    installOnOpen:  true,
	oauth: 			false,
    iconUrl:        "",
    iconX2Url:      ""
) 

preferences {
    page name: "mainPage"
    page name: "previewTemplate"
}

void installed() {
//	log.trace "installed()"
    state?.isInstalled = true
    initialize()
}

void updated(){
//	log.trace "updated()"
    if(!state?.isInstalled) { state?.isInstalled = true }
	if(debugEnable) runIn(1800,logsOff)
}

void initialize(){
}

void logsOff(){
     app.updateSetting("debugEnable",[value:"false",type:"bool"])
}

def mainPage(){
    dynamicPage (name: "mainPage", title: "", install: true, uninstall: true) {
      	if (app.getInstallationState() == 'COMPLETE') {   
	    	section("Main") {
                state.saveReq = false
                input "templateName", "string", title: "<b>Template to Process</b>", required: false, width:5, submitOnUpdate:true
                if(templateName != null) {
                    devList = templateScan()
                    paragraph "The following devices are required for this template: $devList"
                }
                input "qryDevice", "capability.*", title: "Devices of Interest:", multiple: true, required: true, submitOnChange: true
                if(qryDevice) {
                    unsubscribe()
                    qryDevice.each{
                        subscribe(it, "altHtml", [filterEvents:true])
                    }
                    href "previewTemplate", title: "Template Preview", required: false
                }
                input "clearSettings", "button", title: "Clear previous settings"
                if(state?.clearAll == true) {
                    unsubscribe()
                    settings.each {
                        if(it.key != 'isInstalled') {
                            app.removeSetting("${it.key}")
                        }
                    }
                    state.clearAll = false
                }
                if(!this.getChildDevice("ttdm${app.id}"))
                    addChildDevice("thebearmay","Generic HTML Device","ttdm${app.id}", [name: "HTML Tile Device${app.id}", isComponent: true, label:"HTML Tile Device${app.id}"])                
             }
             section("Change Application Name", hideable: true, hidden: true){
               input "nameOverride", "text", title: "New Name for Application", multiple: false, required: false, submitOnChange: true, defaultValue: app.getLabel()
               if(nameOverride != app.getLabel) app.updateLabel(nameOverride)
             }  
	    } else {
		    section("") {
			    paragraph title: "Click Done", "Please click Done to install app before continuing"
		    }
	    }
    }
}

def previewTemplate(){
    dynamicPage (name: "previewTemplate", title: "Template Preview", install: false, uninstall: false) {
	  section(""){
          html = altHtml()
          paragraph "${html}"      
      }
    }
}

List templateScan() {
    String fContents = readFile("$templateName")
    List fRecs=fContents.split("\n")
    List devList =[]
    fRecs.each {
        int vCount = it.count("<%")
        if(vCount > 0){
            recSplit = it.split("<%")
            recSplit.each {
                if(it.indexOf("%>") > -1){
                    if(it.indexOf(":") > -1){ //format of <%devId:attribute%>
                        devList.add(it.substring(0,it.indexOf(":")).toLong())
                    }
                }
            }
        }
    }
    
    return devList.unique()
}

String altHtml(evt = "") {
    //log.debug "altHtml $evt.properties"
    String fContents = readFile("$templateName")
    List fRecs=fContents.split("\n")
    String html = ""
    fRecs.each {
        int vCount = it.count("<%")
        if(vCount > 0){
            recSplit = it.split("<%")
            recSplit.each {
                if(it.indexOf("%>") == -1)
                    html+= it
                else {
                    if(it.indexOf(":") > -1){ //format of <%devId:attribute%>
                        devId = it.substring(0,it.indexOf(":")).toLong()
                        qryDevice.each{
                            if(it.deviceId == devId) dev=it
                        }
                        vName = it.substring(it.indexOf(":")+1,it.indexOf('%>'))
                        //log.debug "$devId $vName"
                    } else
                        vName = it.substring(0,it.indexOf('%>'))

                    if(vName == "@date")
                        aVal = new Date()
                    else if (vName == "@version")
                        aVal = version()
                    else if (vName == "@name")// requires a format of <%devId:attribute%>
                        aVal = dev.properties.displayName
                    else {
                        aVal = dev.currentValue("$vName",true)
                        String attrUnit = dev.currentState("vName")?.unit
                        if (attrUnit != null) aVal+=" $attrUnit"
                    }
                    html+= aVal
                    if(it.indexOf("%>")+2 != it.length()) {
                        html+=it.substring(it.indexOf("%>")+2)
                    }
                }                 
            }
        }
        else html += it
    }
    if (!evt) return html
        
    chd = getChildDevice("ttdm${app.id}")
    chd.sendEvent(name:"html1", value:html)
    return null
}

void refreshSlot(sNum) {
    altHtml([refresh:true])
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

def appButtonHandler(btn) {
    switch(btn) {
        case "clearSettings":
            state.clearAll = true
            break
        case "saveTemplate":
            if(saveAs == null) break
            state.saveReq = true
            break
        default: 
            log.error "Undefined button $btn pushed"
            break
    }
}

void intialize() {

}