package asyncsubjects;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class AndroidTest extends Activity {

	int raceOnMe;
	
	static class Particle {
		int x;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AsyncTask<Particle, Void, Void> async = new AsyncTask<Particle, Void, Void>() {
			@Override
			protected Void doInBackground(Particle... arg0) {
				raceOnMe = 9;
				arg0[0].x = 8;
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				
			}
		};
		Particle particle = new Particle();
		async.execute(particle);
		raceOnMe = 11;
		particle.x = 10;
	}
}