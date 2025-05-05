
  Logger.debug("ZEBRA Dimensioning - Launch");

  //Create the values that will be passed to the action.
  var actionName = "com.sample.mdintegrationapp.GET_DIMENSION";

  //Use either action, class and package, or both. Define extras with a name, value, and type.
  //var intent = { action: actionName, extras: [ ] };
  var intent = {  
	action: actionName,
    	package: "com.sample.mdintegrationapp",
    	class: "com.sample.mdintegrationapp.MainActivity",
 };
  //Launch the action and mark the input as handled.
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
	var volume = length * width * height;
 	Device.sendKeys(volume);
    }
  });

  Logger.debug("ZEBRA Dimensioning - Launch Ended");