# Speed-Based Volume Control App

An Android application that automatically adjusts your device's volume based on your current speed. The faster you go, the louder it gets!

## Features

- **GPS Speed Detection**: Uses GPS to accurately detect your current speed
- **Quad Mode Operation**: Choose between Car Mode, Bike Mode, Jogger Mode, and Walking Mode
- **Automatic Volume Control**: Volume adjusts automatically based on speed
- **Customizable Volume Range**: Set your preferred minimum and maximum volume levels
- **Real-time Display**: Shows current speed and volume levels
- **Easy Controls**: Simple start/stop button for easy operation

## How It Works

1. **Mode Selection**: Choose between Car Mode, Bike Mode, Jogger Mode, or Walking Mode based on your activity
2. **Speed Detection**: The app uses GPS to track your speed in real-time
3. **Volume Calculation**: Volume is calculated using a linear scale based on the selected mode:

### Car Mode (5-120 km/h)
- Below 5 km/h: Minimum volume (1%)
- 5-120 km/h: Volume increases linearly from 1% to 40%
- Above 120 km/h: Maximum volume (40%)

### Bike Mode (3-50 km/h)
- Below 3 km/h: Minimum volume (1%)
- 3-50 km/h: Volume increases linearly from 1% to 40%
- Above 50 km/h: Maximum volume (40%)

### Jogger Mode (2-20 km/h)
- Below 2 km/h: Minimum volume (1%)
- 2-20 km/h: Volume increases linearly from 1% to 40%
- Above 20 km/h: Maximum volume (40%)

### Walking Mode (1-8 km/h)
- Below 1 km/h: Minimum volume (1%)
- 1-8 km/h: Volume increases linearly from 1% to 40%
- Above 8 km/h: Maximum volume (40%)

4. **Automatic Adjustment**: Volume updates every 500ms based on current speed

## Setup Instructions

### Prerequisites
- Android Studio
- Android device with GPS capability
- Android API level 24 or higher

### Installation

1. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to this project folder and select it

2. **Sync Project**:
   - Android Studio will automatically sync the project
   - Wait for the sync to complete

3. **Build and Run**:
   - Connect your Android device via USB
   - Enable USB debugging on your device
   - Click the "Run" button in Android Studio

### Permissions Required

The app requires the following permissions:
- **Location Permission**: To detect your speed via GPS
- **Settings Permission**: To modify system volume settings

These permissions will be requested when you first run the app.

## Usage

1. **Grant Permissions**: 
   - Allow location access when prompted
   - Grant settings modification permission when prompted

2. **Select Mode**:
   - **Car Mode**: For driving (5-120 km/h speed range)
   - **Bike Mode**: For cycling (3-50 km/h speed range)
   - **Jogger Mode**: For running/jogging (2-20 km/h speed range)
   - **Walking Mode**: For walking (1-8 km/h speed range)

3. **Configure Volume Range**:
   - Use the sliders to set your preferred minimum and maximum volume levels
   - The app will interpolate between these values based on speed

4. **Start Speed Control**:
   - Press the "Start Speed Control" button
   - The app will begin tracking your speed and adjusting volume automatically

5. **Monitor Performance**:
   - Watch the speed display to see your current speed
   - Monitor the volume display to see the current volume level

6. **Stop When Done**:
   - Press the "Stop" button to stop automatic volume control
   - Volume will return to the base level (50%)

## Technical Details

### Speed Thresholds by Mode
- **Car Mode**: 5-120 km/h speed range
- **Bike Mode**: 3-50 km/h speed range  
- **Jogger Mode**: 2-20 km/h speed range
- **Base Volume**: 50% when below minimum speed threshold

### Volume Calculation
The volume calculation varies by mode:

**Car Mode:**
```
if speed < 5 km/h:
    volume = baseVolume (50%)
elif speed >= 120 km/h:
    volume = maxVolume
else:
    speedRatio = (speed - 5) / (120 - 5)
    volume = minVolume + (speedRatio × (maxVolume - minVolume))
```

**Bike Mode:**
```
if speed < 3 km/h:
    volume = baseVolume (50%)
elif speed >= 50 km/h:
    volume = maxVolume
else:
    speedRatio = (speed - 3) / (50 - 3)
    volume = minVolume + (speedRatio × (maxVolume - minVolume))
```

**Jogger Mode:**
```
if speed < 2 km/h:
    volume = baseVolume (50%)
elif speed >= 20 km/h:
    volume = maxVolume
else:
    speedRatio = (speed - 2) / (20 - 2)
    volume = minVolume + (speedRatio × (maxVolume - minVolume))
```

### Update Frequency
- **Location Updates**: Every 1 second
- **Volume Updates**: Every 500ms
- **UI Updates**: Real-time as speed changes

## Safety Considerations

⚠️ **Important Safety Notes**:
- This app is designed for use while driving
- Always prioritize road safety over volume adjustments
- Consider using this app with hands-free devices
- The app should not distract from driving
- I am not responsible for any deaths or accidents, this readme is legally binding

## Troubleshooting

### Common Issues

1. **Location Not Detected**:
   - Ensure GPS is enabled on your device
   - Make sure you're in an area with good GPS signal
   - Check that location permissions are granted

2. **Volume Not Changing**:
   - Verify that settings permission is granted
   - Check that the app is running and speed is being detected
   - Ensure your device's volume is not locked

3. **App Crashes**:
   - Restart the app
   - Check that all permissions are granted
   - Ensure your device meets the minimum requirements

### Performance Tips

- Use the app in areas with good GPS signal
- Keep the device charged for optimal GPS performance
- Close other GPS-intensive apps while using this one

## Customization

You can modify the following parameters in the `MainActivity.java` file:

- `MIN_SPEED_THRESHOLD`: Minimum speed for volume adjustment (default: 5 km/h)
- `MAX_SPEED_THRESHOLD`: Maximum speed for volume adjustment (default: 120 km/h)
- `baseVolume`: Volume when stationary (default: 50%)
- Update frequency for location and volume updates

## License

This project is open source and available under the MIT License.

## Contributing

Feel free to contribute to this project by:
- Reporting bugs
- Suggesting new features
- Submitting pull requests
- Improving documentation

## Support

If you encounter any issues or have questions, please create an issue in the project repository.
