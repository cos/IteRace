package asyncsubjects;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class AndroidTest extends Activity {

	int raceOnMe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AsyncTask<Void, Void, Void> async = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... arg0) {
				raceOnMe = 9;
				return null;
			}
		};
		async.execute();
		raceOnMe = 11;
	}
}