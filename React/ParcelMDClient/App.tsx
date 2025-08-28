import React, { useState, useEffect, useRef } from 'react';
import {
  SafeAreaView,
  View,
  Text,
  TextInput,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  NativeEventEmitter,
  NativeModules,
  Dimensions,
  ScaledSize,
} from 'react-native';
import CheckBox from '@react-native-community/checkbox';
import Toast from 'react-native-toast-message';
import { CameraRoll } from '@react-native-camera-roll/camera-roll';
import MaterialIcons from '@react-native-vector-icons/material-icons';

import {
  EnableDimension,
  GetDimension,
  DisableDimension,
  GetDimensionParameters,
  SetDimensionParameters,
} from 'react-native-zebra-mobile-dimensioning';

const { ZebraMobileDimensioning } = NativeModules;
const eventEmitter = new NativeEventEmitter(ZebraMobileDimensioning);

type DimensionStatus =
  | typeof ZebraMobileDimensioning.NO_DIM
  | typeof ZebraMobileDimensioning.ABOVE_RANGE
  | typeof ZebraMobileDimensioning.BELOW_RANGE
  | typeof ZebraMobileDimensioning.IN_RANGE;

const App: React.FC = () => {
  const [objectId, setObjectId] = useState('');
  const [saveImage, setSaveImage] = useState(false);
  const [unit, setUnit] = useState('');
  const [showUnits, setShowUnits] = useState(false);
  const [length, setLength] = useState('');
  const [width, setWidth] = useState('');
  const [height, setHeight] = useState('');
  const [isDimensionEnabled, setIsDimensionEnabled] = useState(false);
  const [isInitialSetup, setIsInitialSetup] = useState(true);
  const [isLandscape, setIsLandscape] = useState(false);
  const [lengthStatus, setLengthStatus] = useState('');
  const [widthStatus, setWidthStatus] = useState('');
  const [heightStatus, setHeightStatus] = useState('');

  const readyLength = useRef('');
  const readyWidth = useRef('');
  const readyHeight = useRef('');

  const handleOrientationChange = ({ window }: { window: ScaledSize }) => {
    setIsLandscape(window.width > window.height);
  };

  useEffect(() => {
    const subscription = Dimensions.addEventListener('change', handleOrientationChange);
    handleOrientationChange({ window: Dimensions.get('window') });
    return () => {
      subscription?.remove();
    };
  }, []);

  useEffect(() => {
    EnableDimension({ MODULE: ZebraMobileDimensioning.PARCEL_MODULE });

    const subscription = eventEmitter.addListener(
      ZebraMobileDimensioning.DIMENSIONING_EVENT,
      async (event) => {
        const action = event[ZebraMobileDimensioning.ACTION];
        const message = event[ZebraMobileDimensioning.RESULT_MESSAGE];
        const resultCode = event[ZebraMobileDimensioning.RESULT_CODE];

        console.log('event received', event);

        switch (action) {
          case ZebraMobileDimensioning.INTENT_ACTION_ENABLE_DIMENSION:
            // After enabling, call Get Parameters to update the UI
            if (resultCode === ZebraMobileDimensioning.SUCCESS) {
              setIsDimensionEnabled(true);
              GetDimensionParameters({});
            } else {
              Toast.show({
                type: 'error',
                text1: 'Enable Dimension Failed',
                text2: message,
              });
            }
            break;
          case ZebraMobileDimensioning.INTENT_ACTION_DISABLE_DIMENSION:
            if (resultCode !== ZebraMobileDimensioning.SUCCESS) {
              Toast.show({
                type: 'error',
                text1: 'Disable Dimension Failed',
                text2: message,
              });
            }
            break;
          case ZebraMobileDimensioning.INTENT_ACTION_SET_DIMENSION_PARAMETER:
            // After setting parameters, call Get Parameters again to update the ready values
            if (resultCode === ZebraMobileDimensioning.SUCCESS) {
              GetDimensionParameters({});
            } else {
              Toast.show({
                type: 'error',
                text1: 'Set Parameters Failed',
                text2: message,
              });
            }
            break;
          case ZebraMobileDimensioning.INTENT_ACTION_GET_DIMENSION_PARAMETER:
            if (resultCode === ZebraMobileDimensioning.SUCCESS) {
              setSaveImage(
                event[ZebraMobileDimensioning.REPORT_IMAGE] || false,
              );
              let apiUnit =
                event[ZebraMobileDimensioning.DIMENSIONING_UNIT] || 'cm';
              setUnit(apiUnit.toLowerCase());
              setShowUnits(
                event[ZebraMobileDimensioning.SUPPORTED_UNITS].length > 1,
              );
              // Update dimensions with READY values
              readyLength.current =
                event[ZebraMobileDimensioning.READY_LENGTH] || '0';
              readyWidth.current =
                event[ZebraMobileDimensioning.READY_WIDTH] || '0';
              readyHeight.current =
                event[ZebraMobileDimensioning.READY_HEIGHT] || '0';
              setLength(readyLength.current);
              setWidth(readyWidth.current);
              setHeight(readyHeight.current);
              setIsInitialSetup(false);
            } else {
              Toast.show({
                type: 'error',
                text1: 'Get Parameters Failed',
                text2: message,
              });
            }
            break;
          case ZebraMobileDimensioning.INTENT_ACTION_GET_DIMENSION:
            if (resultCode === ZebraMobileDimensioning.SUCCESS) {
              const length =
                event[ZebraMobileDimensioning.LENGTH] || readyLength.current;
              const width =
                event[ZebraMobileDimensioning.WIDTH] || readyWidth.current;
              const height =
                event[ZebraMobileDimensioning.HEIGHT] || readyHeight.current;
              setLength(length);
              setWidth(width);
              setHeight(height);

              setLengthStatus(event[ZebraMobileDimensioning.LENGTH_STATUS]);
              setWidthStatus(event[ZebraMobileDimensioning.WIDTH_STATUS]);
              setHeightStatus(event[ZebraMobileDimensioning.HEIGHT_STATUS]);

              const cacheImagePath = event[ZebraMobileDimensioning.IMAGE];

              if (cacheImagePath) {
                try {
                  // Save to gallery using CameraRoll
                  const savedToGallery = await CameraRoll.saveAsset(cacheImagePath);
                  console.log('Image saved to Gallery:', savedToGallery);
                } catch (error) {
                  console.error('Image storage failed', error);
                }
              }
            } else if (resultCode !== ZebraMobileDimensioning.CANCELED) {
              Toast.show({
                type: 'error',
                text1: 'Dimensioning Failed',
                text2: message,
              });
            }
            break;
          default:
            break;
        }
      }
    );

    return () => {
      DisableDimension({});
      subscription.remove();
    };
  }, []);

  useEffect(() => {
    if (!isInitialSetup) {
      const updateDimensionParameters = async () => {
        await SetDimensionParameters({
          DIMENSIONING_UNIT: unit,
          REPORT_IMAGE: saveImage,
          TIMEOUT: 15,
        });
      };
      updateDimensionParameters();
    }
  }, [saveImage, unit, isInitialSetup]); // Trigger updates when saveImage or unit changes, except during initial setup

  const handleSaveImageChange = (newValue: boolean) => {
    setSaveImage(newValue);
  };

  const handleUnitChange = (newUnit: 'cm' | 'inch') => {
    setUnit(newUnit);
  };

  const handleScanBarcode = () => {
    // Implement barcode scanning functionality
    Toast.show({
      type: 'info',
      text1: 'Scan Bar Code',
      text2: 'Functionality to be implemented...',
    });
  };

  const handleStartDimensioning = () => {
    GetDimension({ OBJECT_ID: objectId });
  };

  const handleReset = () => {
    setObjectId('');
    setLength(readyLength.current);
    setWidth(readyWidth.current);
    setHeight(readyHeight.current);
    setLengthStatus('');
    setWidthStatus('');
    setHeightStatus('');
  };

  const getDimensionStyle = (status: DimensionStatus) => {
    switch (status) {
      case ZebraMobileDimensioning.NO_DIM:
        return styles.redBackground;
      case ZebraMobileDimensioning.ABOVE_RANGE:
      case ZebraMobileDimensioning.BELOW_RANGE:
        return styles.orangeBackground;
      case ZebraMobileDimensioning.IN_RANGE:
        return styles.greenBackground;

      default:
        return styles.defaultBackground;
    }
  };

  const getDimensionIcon = (status: DimensionStatus) => {
    switch (status) {
      case ZebraMobileDimensioning.ABOVE_RANGE:
      case ZebraMobileDimensioning.BELOW_RANGE:
        return <MaterialIcons name="warning" size={12} color="#FF8000" />;
      case ZebraMobileDimensioning.IN_RANGE:
        return <MaterialIcons name="check" size={12} color="#00B400" />;
      case ZebraMobileDimensioning.NO_DIM:
        return <MaterialIcons name="warning" size={12} color="#ED1C24" />;
      default:
        return null;
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.toolbar}>
        <Text style={styles.toolbarText}>React Mobile Parcel Dimensioning</Text>
      </View>

      <ScrollView>
        <View style={styles.headerContainer}>
          <Text style={styles.headerText}>
            Welcome to the Zebra Mobile Dimensioning React Demo
          </Text>
          <Text style={styles.infoText}>
            This app is designed to capture the dimensions of the length, width,
            and height using the depth sensing hardware and camera on your
            device.
          </Text>
        </View>
        <Text style={styles.descriptionText}>
          Scan or type in your Object ID:
        </Text>
        <View style={isLandscape ? styles.landscapeObjectIdRow : styles.objectIdContainer}>
          <Text style={styles.objectIdLabel}>Object ID:</Text>
          <TextInput
            style={styles.objectIdInput}
            placeholder=""
            value={objectId}
            onChangeText={setObjectId}
          />
          {isLandscape && (
            <TouchableOpacity onPress={handleScanBarcode} style={styles.landscapeScanButton}>
              <Text style={styles.buttonText}>Scan Barcode</Text>
            </TouchableOpacity>
          )}
        </View>
        {!isLandscape && (
          <View style={styles.scanButtonContainer}>
            <TouchableOpacity onPress={handleScanBarcode} style={styles.button}>
              <Text style={styles.buttonText}>Scan Barcode</Text>
            </TouchableOpacity>
          </View>
        )}
        {!isLandscape && (
          <View style={styles.saveImageContainer}>
            <CheckBox
              value={saveImage}
              onValueChange={handleSaveImageChange}
              tintColors={{ true: '#2185D5', false: '#ccc' }}
            />
            <Text style={styles.saveImageText}>Save Image</Text>
          </View>
        )}
        <View
          style={
            isLandscape ? styles.landscapeUnitRow : styles.unitAndResetContainer
          }
        >
          {showUnits && (
            <View style={styles.unitToggleContainer}>
              <TouchableOpacity
                onPress={() => handleUnitChange('inch')}
                style={[
                  styles.unitButton,
                  unit.toLowerCase() === 'inch' ? styles.unitSelected : null,
                ]}
              >
                <Text style={styles.unitText}>IN</Text>
              </TouchableOpacity>
              <TouchableOpacity
                onPress={() => handleUnitChange('cm')}
                style={[
                  styles.unitButton,
                  unit.toLowerCase() === 'cm' ? styles.unitSelected : null,
                ]}
              >
                <Text style={styles.unitText}>CM</Text>
              </TouchableOpacity>
            </View>
          )}
          <View style={styles.resetContainer}>
            <TouchableOpacity onPress={handleReset}>
              <Text
                style={[
                  styles.resetText,
                  isLandscape ? { marginLeft: 40 } : { marginRight: 40 },
                ]}
              >
                RESET
              </Text>
            </TouchableOpacity>
          </View>
          {isLandscape && (
            <View style={styles.landscapeSaveImageContainer}>
              <CheckBox
                value={saveImage}
                onValueChange={handleSaveImageChange}
                tintColors={{ true: '#2185D5', false: '#ccc' }}
              />
              <Text style={styles.saveImageText}>Save Image</Text>
            </View>
          )}
        </View>
        <View style={styles.dimensionContainer}>
          <View style={[styles.dimensionBox, getDimensionStyle(lengthStatus)]}>
            <Text style={styles.dimensionLabel}>Length:</Text>
            <View style={styles.dimensionValueContainer}>
              <Text style={styles.dimensionValue}>
                {lengthStatus === ZebraMobileDimensioning.NO_DIM
                  ? ZebraMobileDimensioning.NO_DIM
                  : `${length} ${unit}`}
              </Text>
              {getDimensionIcon(lengthStatus)}
            </View>
          </View>
          <View style={[styles.dimensionBox, getDimensionStyle(widthStatus)]}>
            <Text style={styles.dimensionLabel}>Width:</Text>
            <View style={styles.dimensionValueContainer}>
              <Text style={styles.dimensionValue}>
                {widthStatus === ZebraMobileDimensioning.NO_DIM
                  ? ZebraMobileDimensioning.NO_DIM
                  : `${width} ${unit}`}
              </Text>
              {getDimensionIcon(widthStatus)}
            </View>
          </View>
          <View style={[styles.dimensionBox, getDimensionStyle(heightStatus)]}>
            <Text style={styles.dimensionLabel}>Height:</Text>
            <View style={styles.dimensionValueContainer}>
              <Text style={styles.dimensionValue}>
                {heightStatus === ZebraMobileDimensioning.NO_DIM
                  ? ZebraMobileDimensioning.NO_DIM
                  : `${height} ${unit}`}
              </Text>
              {getDimensionIcon(heightStatus)}
            </View>
          </View>
        </View>
        <View style={styles.dimButtonContainer}>
          <TouchableOpacity
            onPress={handleStartDimensioning}
            style={[styles.button, { opacity: isDimensionEnabled ? 1 : 0.5 }]}
            disabled={!isDimensionEnabled}
          >
            <Text style={styles.buttonText}>Start Dimensioning</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
      <Toast />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#515050',
  },
  toolbar: {
    backgroundColor: '#0c0c0cff',
    padding: 15,
    alignItems: 'center',
  },
  toolbarText: {
    color: '#ffffffff',
    fontSize: 18,
    fontWeight: 'bold',
  },
  headerContainer: {
    backgroundColor: '#686767',
    padding: 10,
    margin: 10,
  },
  headerText: {
    color: '#ffffff',
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  infoText: {
    color: '#ffffff',
    fontSize: 15,
  },
  descriptionText: {
    color: '#ffffff',
    fontSize: 10,
    fontWeight: 'bold',
    marginLeft: 20,
    marginTop: 8,
    marginRight: 20,
  },
  objectIdContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#686767',
    margin: 10,
    padding: 10,
  },
  objectIdLabel: {
    color: '#ffffff',
    fontSize: 12,
  },
  objectIdInput: {
    flex: 1,
    color: '#ffffff',
    fontSize: 12,
    marginLeft: 10,
  },
  landscapeObjectIdRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    backgroundColor: '#686767',
    alignItems: 'center',
    margin: 10,
    padding: 10,
  },
  scanButtonContainer: {
    width: '50%',
    alignSelf: 'center',
    marginVertical: 5,
  },
  landscapeScanButton: {
    flex: 0.3,
    backgroundColor: '#2196F3',
    borderRadius: 5,
    padding: 10,
    alignItems: 'center',
    marginLeft: 10,
  },
  dimButtonContainer: {
    width: '95%',
    alignSelf: 'center',
    marginVertical: 5,
  },
  button: {
    backgroundColor: '#2196F3',
    borderRadius: 5,
    padding: 10,
    alignItems: 'center',
  },
  buttonText: {
    color: '#000000',
    fontSize: 16,
    fontWeight: 'bold',
  },
  saveImageContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    margin: 10,
  },
  landscapeSaveImageContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 10,
  },
  saveImageText: {
    color: '#ffffff',
    fontSize: 15,
  },
  unitAndResetContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    margin: 15,
  },
  landscapeUnitRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    margin: 10,
    padding: 10,
  },
  unitToggleContainer: {
    flexDirection: 'row',
  },
  unitButton: {
    fontSize: 10,
    width: 25,
    height: 25,
    justifyContent: 'center',
    alignItems: 'center',
    textAlignVertical: 'center',
    backgroundColor: '#444444',
  },
  unitSelected: {
    backgroundColor: '#2185D5',
  },
  unitText: {
    color: '#fff',
    fontSize: 10,
    fontWeight: 'bold',
  },
  resetContainer: {
    flex: 1,
    alignItems: 'center',
  },
  resetText: {
    color: '#ffffff',
    fontSize: 15,
    fontWeight: 'bold',
    padding: 10,
  },
  dimensionContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    margin: 10,
  },
  dimensionBox: {
    flex: 1, // Equally spaced boxes in landscape
    backgroundColor: '#444444',
    marginHorizontal: 5,
    height: 50,
    justifyContent: 'center',
    padding: 5,
    borderWidth: 1,
    borderColor: '#ffffff',
    borderRadius: 5,
  },
  dimensionLabel: {
    color: '#ffffff',
    fontSize: 10,
  },
  dimensionValueContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  dimensionValue: {
    color: '#ffffff',
    fontSize: 16,
  },
  redBackground: {
    borderColor: '#FF0000',
    borderWidth: 2,
  },
  orangeBackground: {
    borderColor: '#FFA500',
    borderWidth: 2,
  },
  greenBackground: {
    borderColor: '#008000',
    borderWidth: 2,
  },
  defaultBackground: {
    backgroundColor: '#686767',
  },
});

export default App;
