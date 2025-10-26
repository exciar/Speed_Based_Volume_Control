package com.speedvolumecontrol;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int SETTINGS_PERMISSION_REQUEST_CODE = 1002;
    
    private LocationManager locationManager;
    private AudioManager audioManager;
    private Handler handler;
    private Runnable volumeUpdateRunnable;
    
    private TextView speedTextView;
    private TextView volumeTextView;
    private TextView minVolumeValue;
    private TextView maxVolumeValue;
    private TextView modeDescription;
    private TextView gpsStatusTextView;
    private RadioGroup modeRadioGroup;
    private RadioButton carModeRadio;
    private RadioButton bikeModeRadio;
    private RadioButton joggerModeRadio;
    private RadioButton walkingModeRadio;
    private SeekBar maxVolumeSeekBar;
    private SeekBar minVolumeSeekBar;
    private Button startStopButton;
    
    private boolean isRunning = false;
    private float currentSpeed = 0.0f;
    private int minVolume = 1;
    private int maxVolume = 40;
    private int baseVolume = 20; // Volume when stationary
    private int currentMode = 0; // 0=Car, 1=Bike, 2=Jogger, 3=Walking
    
    // Speed thresholds for different modes (km/h)
    private static final float CAR_MIN_SPEED_THRESHOLD = 5.0f;
    private static final float CAR_MAX_SPEED_THRESHOLD = 120.0f;
    private static final float BIKE_MIN_SPEED_THRESHOLD = 3.0f;
    private static final float BIKE_MAX_SPEED_THRESHOLD = 50.0f;
    private static final float JOGGER_MIN_SPEED_THRESHOLD = 2.0f;
    private static final float JOGGER_MAX_SPEED_THRESHOLD = 20.0f;
    private static final float WALKING_MIN_SPEED_THRESHOLD = 1.0f;
    private static final float WALKING_MAX_SPEED_THRESHOLD = 8.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        initializeServices();
        setupListeners();
        requestPermissions();
    }
    
    private void initializeViews() {
        speedTextView = findViewById(R.id.speedTextView);
        volumeTextView = findViewById(R.id.volumeTextView);
        minVolumeValue = findViewById(R.id.minVolumeValue);
        maxVolumeValue = findViewById(R.id.maxVolumeValue);
        modeDescription = findViewById(R.id.modeDescription);
        gpsStatusTextView = findViewById(R.id.gpsStatusTextView);
        modeRadioGroup = findViewById(R.id.modeRadioGroup);
        carModeRadio = findViewById(R.id.carModeRadio);
        bikeModeRadio = findViewById(R.id.bikeModeRadio);
        joggerModeRadio = findViewById(R.id.joggerModeRadio);
        walkingModeRadio = findViewById(R.id.walkingModeRadio);
        maxVolumeSeekBar = findViewById(R.id.maxVolumeSeekBar);
        minVolumeSeekBar = findViewById(R.id.minVolumeSeekBar);
        startStopButton = findViewById(R.id.startStopButton);
        
        // Set initial values
        maxVolumeSeekBar.setProgress(maxVolume);
        minVolumeSeekBar.setProgress(minVolume);
        updateVolumeDisplay();
        updateSeekBarValues();
        updateModeDescription();
        updateGpsStatus();
        
        // Set initial volume to minimum volume
        setSystemVolume(minVolume);
    }
    
    private void initializeServices() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        handler = new Handler(Looper.getMainLooper());
    }
    
    private void setupListeners() {
        startStopButton.setOnClickListener(v -> toggleSpeedVolumeControl());
        
        // Mode selection listener
        modeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.carModeRadio) {
                currentMode = 0; // Car mode
            } else if (checkedId == R.id.bikeModeRadio) {
                currentMode = 1; // Bike mode
            } else if (checkedId == R.id.joggerModeRadio) {
                currentMode = 2; // Jogger mode
            } else if (checkedId == R.id.walkingModeRadio) {
                currentMode = 3; // Walking mode
            }
            updateModeDescription();
        });
        
        maxVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    maxVolume = progress;
                    if (maxVolume <= minVolume) {
                        minVolume = Math.max(1, maxVolume - 1);
                        minVolumeSeekBar.setProgress(minVolume);
                    }
                    updateVolumeDisplay();
                    updateSeekBarValues();
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        minVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    minVolume = progress;
                    if (minVolume >= maxVolume) {
                        maxVolume = Math.min(35, minVolume + 1);
                        maxVolumeSeekBar.setProgress(maxVolume);
                    }
                    updateVolumeDisplay();
                    updateSeekBarValues();
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkSettingsPermission();
        }
    }
    
    private void checkSettingsPermission() {
        if (!android.provider.Settings.System.canWrite(this)) {
            new AlertDialog.Builder(this)
                .setTitle("Settings Permission Required")
                .setMessage("This app needs permission to modify system settings to control volume. Please grant permission in the next screen.")
                .setPositiveButton("Grant Permission", (dialog, which) -> {
                    android.content.Intent intent = new android.content.Intent(
                        android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, SETTINGS_PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Permission required for volume control", Toast.LENGTH_LONG).show();
                })
                .show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkSettingsPermission();
            } else {
                Toast.makeText(this, "Location permission is required for speed detection", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_PERMISSION_REQUEST_CODE) {
            if (android.provider.Settings.System.canWrite(this)) {
                Toast.makeText(this, "Settings permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Settings permission denied. Volume control may not work properly.", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void toggleSpeedVolumeControl() {
        if (!isRunning) {
            startSpeedVolumeControl();
        } else {
            stopSpeedVolumeControl();
        }
    }
    
    private void startSpeedVolumeControl() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check GPS connection before starting
        checkGpsConnection();
        updateGpsStatus();
        
        try {
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
                isRunning = true;
                startStopButton.setText("Stop");
                startStopButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light, null));
                
                // Start volume update loop
                startVolumeUpdateLoop();
                
                Toast.makeText(this, "Speed-based volume control started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location service not available", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error starting location service: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopSpeedVolumeControl() {
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
        } catch (Exception e) {
            // Ignore errors when stopping
        }
        
        isRunning = false;
        startStopButton.setText("Start");
        startStopButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, null));
        
        // Stop volume update loop
        if (volumeUpdateRunnable != null) {
            handler.removeCallbacks(volumeUpdateRunnable);
        }
        
        // Reset to minimum volume
        setSystemVolume(minVolume);
        updateVolumeDisplay();
        
        Toast.makeText(this, "Speed-based volume control stopped", Toast.LENGTH_SHORT).show();
    }
    
    private void startVolumeUpdateLoop() {
        volumeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    updateVolumeBasedOnSpeed();
                    updateGpsStatus(); // Check GPS status periodically
                    handler.postDelayed(this, 500); // Update every 500ms
                }
            }
        };
        handler.post(volumeUpdateRunnable);
    }
    
    private void updateVolumeBasedOnSpeed() {
        int targetVolume = calculateVolumeFromSpeed(currentSpeed);
        setSystemVolume(targetVolume);
        updateVolumeDisplay();
    }
    
    private int calculateVolumeFromSpeed(float speed) {
        float minThreshold, maxThreshold;
        
        switch (currentMode) {
            case 0: // Car mode
                minThreshold = CAR_MIN_SPEED_THRESHOLD;
                maxThreshold = CAR_MAX_SPEED_THRESHOLD;
                break;
            case 1: // Bike mode
                minThreshold = BIKE_MIN_SPEED_THRESHOLD;
                maxThreshold = BIKE_MAX_SPEED_THRESHOLD;
                break;
            case 2: // Jogger mode
                minThreshold = JOGGER_MIN_SPEED_THRESHOLD;
                maxThreshold = JOGGER_MAX_SPEED_THRESHOLD;
                break;
            case 3: // Walking mode
                minThreshold = WALKING_MIN_SPEED_THRESHOLD;
                maxThreshold = WALKING_MAX_SPEED_THRESHOLD;
                break;
            default:
                minThreshold = CAR_MIN_SPEED_THRESHOLD;
                maxThreshold = CAR_MAX_SPEED_THRESHOLD;
                break;
        }
        
        if (speed < minThreshold) {
            return minVolume; // Stationary or very slow
        }
        
        if (speed >= maxThreshold) {
            return maxVolume; // Maximum speed
        }
        
        // Linear interpolation between min volume and max volume based on speed
        float speedRatio = (speed - minThreshold) / (maxThreshold - minThreshold);
        int volumeRange = maxVolume - minVolume;
        return minVolume + Math.round(speedRatio * volumeRange);
    }
    
    private void setSystemVolume(int volume) {
        try {
            if (android.provider.Settings.System.canWrite(this) && audioManager != null) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            }
        } catch (Exception e) {
            // Ignore volume setting errors
        }
    }
    
    private void updateVolumeDisplay() {
        try {
            if (audioManager != null) {
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                volumeTextView.setText("Current Volume: " + currentVolume + "%");
            }
        } catch (Exception e) {
            volumeTextView.setText("Current Volume: Unknown");
        }
    }
    
    private void updateSeekBarValues() {
        minVolumeValue.setText(minVolume + "%");
        maxVolumeValue.setText(maxVolume + "%");
    }
    
    private void updateModeDescription() {
        switch (currentMode) {
            case 0: // Car mode
                modeDescription.setText("Car Mode: 5-120 km/h speed range");
                break;
            case 1: // Bike mode
                modeDescription.setText("Bike Mode: 3-50 km/h speed range");
                break;
            case 2: // Jogger mode
                modeDescription.setText("Jogger Mode: 2-20 km/h speed range");
                break;
            case 3: // Walking mode
                modeDescription.setText("Walking Mode: 1-8 km/h speed range");
                break;
            default:
                modeDescription.setText("Car Mode: 5-120 km/h speed range");
                break;
        }
    }
    
    private void updateGpsStatus() {
        if (locationManager == null) {
            gpsStatusTextView.setText("GPS: Service unavailable");
            gpsStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light, null));
            return;
        }
        
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        
        if (gpsEnabled) {
            gpsStatusTextView.setText("GPS: Connected");
            gpsStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_light, null));
        } else if (networkEnabled) {
            gpsStatusTextView.setText("GPS: Using Network");
            gpsStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_light, null));
        } else {
            gpsStatusTextView.setText("GPS: Disconnected");
            gpsStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_light, null));
        }
    }
    
    private void checkGpsConnection() {
        if (locationManager != null) {
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            
            if (!gpsEnabled && !networkEnabled) {
                Toast.makeText(this, "GPS is disabled. Please enable location services.", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location.hasSpeed()) {
            currentSpeed = location.getSpeed() * 3.6f; // Convert m/s to km/h
            runOnUiThread(() -> {
                speedTextView.setText(String.format("Speed: %.1f km/h", currentSpeed));
            });
        }
    }
    
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        updateGpsStatus();
        Toast.makeText(this, "Location provider enabled: " + provider, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        updateGpsStatus();
        Toast.makeText(this, "Location provider disabled: " + provider, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRunning) {
            stopSpeedVolumeControl();
        }
    }
}
