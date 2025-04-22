const INTENT_ACTION_ENABLE_DIMENSION = "com.zebra.dimensioning.ENABLE_DIMENSION";
const INTENT_ACTION_DISABLE_DIMENSION = "com.zebra.dimensioning.DISABLE_DIMENSION";
const INTENT_ACTION_GET_DIMENSION = "com.zebra.parceldimensioning.GET_DIMENSION";
const INTENT_ACTION_GET_DIMENSION_PARAMETER = "com.zebra.parceldimensioning.GET_DIMENSION_PARAMETER";
const INTENT_ACTION_SET_DIMENSION_PARAMETER = "com.zebra.parceldimensioning.SET_DIMENSION_PARAMETER";

var enableToken = false;
var token = "";

function generateToken() {
  if (enableToken) {
	token = EB.Accessmgr.acquireToken("delegation-zebra-com.zebra.mobiledimensioning-Enable");
	if (!token)
	  alert("Token is empty or null");
  }
}

function sendIntentAPI(action, callback, extras) {
  console.log(sendIntentAPI, action);
  extras = extras || {};
  extras["APPLICATION_PACKAGE"] = EB.Application.appName;
  if (enableToken)
	extras["TOKEN"] = token;
  var pendingintent= {
	"name": 'CALLBACK_RESPONSE',
	"requestCode": 1000,
	"flag": EB.Intent.FLAG_MUTABLE
  };
  var params = {
	intentType: EB.Intent.BROADCAST,
	appName: 'com.zebra.dimensioning',
	action: action,
	data: extras,
	pendingintent: pendingintent
  };
  if (action == INTENT_ACTION_ENABLE_DIMENSION)
	params.intentType = EB.Intent.START_SERVICE;
  EB.Intent.send(params, callback);
}

function enableDimension() {
  sendIntentAPI(INTENT_ACTION_ENABLE_DIMENSION, () => {}, {MODULE: "parcel"});
}

function disableDimension() {
  sendIntentAPI(INTENT_ACTION_DISABLE_DIMENSION, () => {});
}

function getDimensionCallback(param) {
  console.log('actionName', param.actionName);
  console.log('data', JSON.stringify(param));
  //alert(JSON.stringify(param));
  document.getElementById('lengthValue').textContent = param.LENGTH + " " + param.DIMENSIONING_UNIT;
  document.getElementById('widthValue').textContent = param.WIDTH + " " + param.DIMENSIONING_UNIT;
  document.getElementById('heightValue').textContent = param.HEIGHT + " " + param.DIMENSIONING_UNIT;
  document.getElementById('lengthStatus').textContent = param.LENGTH_STATUS;
  document.getElementById('widthStatus').textContent = param.WIDTH_STATUS;
  document.getElementById('heightStatus').textContent = param.HEIGHT_STATUS;
  document.getElementById('parcelid').value = "";
  document.getElementById('parcelid').focus();
}

function startDimensioning() {
  const parcelId = document.getElementById("parcelid").value;
  sendIntentAPI(INTENT_ACTION_GET_DIMENSION, getDimensionCallback, {PARCEL_ID: parcelId});
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function loadEvent() {
  await sleep(200);
  generateToken();
  enableDimension();
  document.getElementById('parcelid').focus();
}

window.addEventListener('load', loadEvent);
window.addEventListener("visibilitychange", () => {
  if (document.visibilityState === "visible") {
	enableDimension();
  } else {
	disableDimension();
  }
});
document.getElementById('startDimensioning').addEventListener('click', startDimensioning);
