package pl.akac.rxjava2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Cancellable;

public class RxSensorManager {

  private static final String LOG_TAG = RxSensorManager.class.getName();

  private final SensorManager sensorManager;

  public RxSensorManager(SensorManager sensorManager) {
    this.sensorManager = sensorManager;
  }

  public Flowable<SensorEvent> observeSensorChanged(final Sensor sensor, final int samplingPeriodUs,
                                                    BackpressureStrategy backpressureMode) {
    return Flowable.create(new FlowableOnSubscribe<SensorEvent>() {
      @Override
      public void subscribe(final FlowableEmitter<SensorEvent> emitter) throws Exception {
        final SensorEventListener sensorListener = new SensorEventListener() {
          @Override
          public void onSensorChanged(SensorEvent sensorEvent) {
            emitter.onNext(sensorEvent);
          }

          @Override
          public void onAccuracyChanged(Sensor originSensor, int i) {
            // ignored for this example
          }
        };
        // (1) - unregistering listener when unsubscribed
        emitter.setCancellable(new Cancellable() {
          @Override
          public void cancel() throws Exception {
            Log.d(LOG_TAG, "Un-subscribing from SensorManager, calling unregisterListener().");
            sensorManager.unregisterListener(sensorListener, sensor);

          }
        });
        sensorManager.registerListener(sensorListener, sensor, samplingPeriodUs);

      }
    }, backpressureMode);
  }

}
