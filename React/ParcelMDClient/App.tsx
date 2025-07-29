import React, { useState, useEffect } from 'react';
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
} from 'react-native';
import CheckBox from '@react-native-community/checkbox';
import Toast from 'react-native-toast-message';
import RNFS from 'react-native-fs';
import {
  EnableDimension,
  GetDimension,
  DisableDimension,
  GetDimensionParameters,
  SetDimensionParameters,
} from 'react-native-zebra-mobile-dimensioning';

const { ZebraMobileDimensioning } = NativeModules;
const eventEmitter = new NativeEventEmitter(ZebraMobileDimensioning);

const App: React.FC = () => {
  const [objectId, setObjectId] = useState('');
  const [saveImage, setSaveImage] = useState(false);
  const [unit, setUnit] = useState('cm');
  const [length, setLength] = useState(0);
  const [width, setWidth] = useState(0);
  const [height, setHeight] = useState(0);
  const [isDimensionEnabled, setIsDimensionEnabled] = useState(false);
  const [isInitialSetup, setIsInitialSetup] = useState(true);

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
              setSaveImage(event[ZebraMobileDimensioning.REPORT_IMAGE] || false);
              let apiUnit = event[ZebraMobileDimensioning.DIMENSIONING_UNIT] || 'cm';
              setUnit(apiUnit.toLowerCase());
              // Update dimensions with READY values
              setLength(event.READY_LENGTH || '0');
              setWidth(event.READY_WIDTH || '0');
              setHeight(event.READY_HEIGHT || '0');
              setIsInitialSetup(false); // Initial setup completed
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
              const length = event[ZebraMobileDimensioning.LENGTH] || 0;
              const width = event[ZebraMobileDimensioning.WIDTH] || 0;
              const height = event[ZebraMobileDimensioning.HEIGHT] || 0;
              setLength(length);
              setWidth(width);
              setHeight(height);

              // Handle the image data
              const cacheImagePath = event['IMAGE'];
              if (cacheImagePath) {
                const imageName = cacheImagePath.split('/').pop();
                const picturesPath = `${RNFS.PicturesDirectoryPath}/${imageName}`;
                try {
                  await RNFS.copyFile(cacheImagePath, picturesPath);
                  console.log('Image saved to Gallery:', picturesPath);
                } catch (error) {
                  console.error('Image storage failed', error);
                }
              }
            } else if (resultCode != ZebraMobileDimensioning.CANCELED) {
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
      DisableDimension({}); // Ensure dimensioning is disabled on unmount
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
    setLength(0);
    setWidth(0);
    setHeight(0);
  };

  return (
    <SafeAreaView style={styles.container}>
      {/* Toolbar */}
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
        <View style={styles.objectIdContainer}>
          <Text style={styles.objectIdLabel}>Object ID:</Text>
          <TextInput
            style={styles.objectIdInput}
            placeholder=""
            value={objectId}
            onChangeText={setObjectId}
          />
        </View>
        <View style={styles.scanButtonContainer}>
          <TouchableOpacity onPress={handleScanBarcode} style={styles.button}>
            <Text style={styles.buttonText}>Scan Barcode</Text>
          </TouchableOpacity>
        </View>
        <View style={styles.saveImageContainer}>
          <CheckBox
            value={saveImage}
            onValueChange={handleSaveImageChange}
            tintColors={{ true: '#2185D5', false: '#ccc' }}
          />
          <Text style={styles.saveImageText}>Save Image</Text>
        </View>
        <View style={styles.unitAndResetContainer}>
          <View style={styles.unitToggleContainer}>
            {/* INCH BUTTON */}
            <TouchableOpacity
              onPress={() => handleUnitChange('inch')}
              style={[
                styles.unitButton,
                unit.toLowerCase() === 'inch' ? styles.unitSelected : null
              ]}
            >
              <Text style={styles.unitText}>IN</Text>
            </TouchableOpacity>
            {/* CM BUTTON */}
            <TouchableOpacity
              onPress={() => handleUnitChange('cm')}
              style={[
                styles.unitButton,
                unit.toLowerCase() === 'cm' ? styles.unitSelected : null
              ]}
            >
              <Text style={styles.unitText}>CM</Text>
            </TouchableOpacity>
          </View>
          <View style={styles.resetContainer}>
            <TouchableOpacity onPress={handleReset}>
              <Text style={styles.resetText}>RESET</Text>
            </TouchableOpacity>
          </View>
        </View>
        <View style={styles.dimensionContainer}>
          <View style={styles.dimensionBox}>
            <Text style={styles.dimensionLabel}>Length:</Text>
            <Text style={styles.dimensionValue}>
              {length} {unit}
            </Text>
          </View>
          <View style={styles.dimensionBox}>
            <Text style={styles.dimensionLabel}>Width:</Text>
            <Text style={styles.dimensionValue}>
              {width} {unit}
            </Text>
          </View>
          <View style={styles.dimensionBox}>
            <Text style={styles.dimensionLabel}>Height:</Text>
            <Text style={styles.dimensionValue}>
              {height} {unit}
            </Text>
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
  scanButtonContainer: {
    width: '50%',
    alignSelf: 'center',
    marginVertical: 5,
  },
  dimButtonContainer: {
    width: '90%',
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
    color: '#000000', // Black text color
    fontSize: 16,
    fontWeight: 'bold',
  },
  saveImageContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    margin: 10,
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
    marginRight: 50,
  },
  dimensionContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    margin: 10,
  },
  dimensionBox: {
    backgroundColor: '#444444',
    width: 100,
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
  dimensionValue: {
    color: '#ffffff',
    fontSize: 16,
  },
});

export default App;
