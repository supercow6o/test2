/**
 *  Motion - door - switch for bathroom
 *
 *  Copyright 2016 Mr Super Cow
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Motion - door - switch for bathroom",
    namespace: "supercow",
    author: "Mr Super Cow",
    description: "app to do the following\r\n-after set time of day, when light switch turned on, with doors open, turn on lights at a dimm level and leave on till X minutes after motion stops or light switched turned off\r\n-after set time of day, when light switch turned on, with doors closed, turn on lights at full brightness and leave on till X minutes after motion stops by \r\n-with light on, dimm lights if door opened\r\n-with light on, go full bright if door closed",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select a door, switch, motion sensor, and lights...") {
		input "themotion", "capability.motionSensor", required: true, title: "Which Motion Sensor?"
		input "theswitch", "capability.switch", required: true, title: "which switch?"
        input "thedoor", "capability.contactSensor", required: true, title: "which door?" 
        input "dimmlevel", "number", required: true, title: "Dimm to what?"
		input "waittime", "number", required: true, title: "wait how long?"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    log.debug "initialize: ${settings}"
    subscribe(thedoor, "contact", contacthandler)
    subscribe(theswitch, "switch", switchhandler)
    subscribe(themotion, "motion.inactive", motionhandler)
}
// need to verify the light is a light and has setlevel


// TODO: implement event handlers
def motionhandler(evt){
	log.debug "entered into motionhandler"
    runIn(60 * waittime, checkmotion)
}

def checkmotion() {

	log.debug "checking if motion has happened in ${waittime} minutes"
    
    def motionstate = themotion.currentState("motion")
	if (motionstate.value == "inactive") {
    	def elapsed = now() - motionstate.date.time
    	def threshold = (1000 * 60 * waittime) -2000
        if (elapsed >= threshold) {
        	log.debug "turning off light due to inactivity"
        	theswitch.off()
        }
        else
        	log.debug "have not been inactive long enough to turn off light"
        
    
    }
    else
    	log.debug "motion is active, not turning off light"


}

def switchhandler(evt) {
	def doorstate = thedoor.currentState("contact")	
   log.debug "${doorstate.value}"
   
    if (evt.value == "on") {
    	 runIn( (60 * waittime) + 20, checkmotion)
        if (doorstate.value == "open")
        	{theswitch.setLevel(dimmlevel)
            log.debug "turning on at dimmlevel : ${dimmlevel}"
            }
        else 
        {theswitch.setLevel(99)
        log.debug "turning on at full bright"
        }
        }
    else
    	log.debug "light is being turned off.  nothing to do."
}

def contacthandler(evt) {
	log.debug "entering contact event handler : ${evt.value}"
    def switchstate = theswitch.currentState("switch")
    if (switchstate.value == "on") {
    	log.debug "switch is on, so changing level based on door state"
       	if (evt.value == "open") {
    	theswitch.setLevel(dimmlevel)
       	}
    	else if (evt.value == "closed") {
    	theswitch.setLevel(100)
       	} else
    	log.debug "invalid evt: ${evt.value}"
	} else
    log.debug "switch is off, nothing to do"
}