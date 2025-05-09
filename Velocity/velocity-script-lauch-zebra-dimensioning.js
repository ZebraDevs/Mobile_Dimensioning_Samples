
Logger.debug("ZEBRA Dimensioning - Launch");

// Create intent to launch the integration app
var actionName = "com.sample.mdintegrationapp.GET_DIMENSION";

var intent = {  
	action: actionName,
    package: "com.sample.mdintegrationapp",
    class: "com.sample.mdintegrationapp.MainActivity",
};

//Launch the integration app and capture dimensions
Action.launchForResult(intent, function(intent) {

	Logger.debug("Intent = " + JSON.stringify(intent));
	if (intent.result == -1) {
		var length = 0;
		var width = 0;
		var height = 0;

		var dimObj =  JSON.parse(intent.extras[0].value);
		Logger.debug("Extra = " + JSON.stringify(dimObj));

		length = parseFloat(dimObj.length);
		width = parseFloat(dimObj.width);
		height = parseFloat(dimObj.height);

		View.toast(length, true);
		Logger.debug("ZEBRA Dimensioning - Launch - Script Success");

		// Returns volume
		var volume = length * width * height;
		Device.sendKeys(volume);
	}
});

Logger.debug("ZEBRA Dimensioning - Launch Ended");