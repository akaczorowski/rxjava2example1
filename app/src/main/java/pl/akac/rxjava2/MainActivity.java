package pl.akac.rxjava2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;

import io.reactivex.BackpressureStrategy;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

  private static final String LOG_TAG = MainActivity.class.getName();

  private Spinner bpModes;

  private SensorManager sensorManager;
  private RxSensorManager rxSensorManager;
  private Sensor accelerometer;
  private Disposable rxSensorSubscriptionAsync;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    bpModes = (Spinner) findViewById(pl.akac.rxjava2.R.id.bpModes);
    bpModes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, BackpressureStrategy.values()));

    setupSensorListener();
  }

  @Override
  protected void onPause() {
    super.onPause();
    // TODO: Unregister existing listeners/subscriptions
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    unregisterSensorRxAsync();
  }

  public void registerSensorRxAsync(View view) {
    Log.d(LOG_TAG, "registerSensorRxAsync()");
    if (rxSensorSubscriptionAsync == null || rxSensorSubscriptionAsync.isDisposed()) {
      rxSensorSubscriptionAsync = rxSensorManager.observeSensorChanged(accelerometer, SensorManager.SENSOR_DELAY_FASTEST,
                                                                       (BackpressureStrategy) bpModes.getSelectedItem())
                                                 .observeOn(Schedulers.computation())
                                                 .subscribe(sensorChangedOnNext, sensorChangedOnError);
    } else {
      Toast.makeText(this, "Already started!", Toast.LENGTH_SHORT).show();
      return;
    }
  }

  public void unregisterSensorRxAsync(View view) {
    unregisterSensorRxAsync();
  }

  private void unregisterSensorRxAsync() {
      Log.d(LOG_TAG, "unregisterSensorRxAsync()");
    if (rxSensorSubscriptionAsync != null && !rxSensorSubscriptionAsync.isDisposed()) {
      rxSensorSubscriptionAsync.dispose();
    }
  }

  private void setupSensorListener() {
    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    rxSensorManager = new RxSensorManager(sensorManager);
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
  }

  private final Consumer<SensorEvent> sensorChangedOnNext = new Consumer<SensorEvent>() {
    @Override
    public void accept(SensorEvent sensorEvent) throws Exception {
      try {
        // simulate a semi-expensive operation
        Thread.sleep(10);
      } catch (InterruptedException ignore) {
      }
      Log.d(LOG_TAG, "sensorEvent OnNext - timestamp=" + sensorEvent.timestamp + ", sensorEvent.values=" + Arrays
          .toString(sensorEvent.values));
    }
  };

  private final Consumer<Throwable> sensorChangedOnError = new Consumer<Throwable>() {
    @Override
    public void accept(Throwable throwable) throws Exception {
      Log.e(LOG_TAG, "sensorChangedOnError", throwable);
    }
  };

}
