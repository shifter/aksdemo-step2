package com.mseclab.devfest.androidkeystoredemostep2;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import com.mseclab.devfest.androidkeystoredemostep2.R;

public class MainActivity extends Activity {

	private final static String ALIAS = "DEVKEY1";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements
			View.OnClickListener {

		private Button mGenChiaviButton;
		private Button mAccChiaviButton;
		private TextView mDebugText;
		ProgressDialog progressdialog;

		private static final String TAG = "AndroidKeyStoreDemo";

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			mGenChiaviButton = (Button) rootView
					.findViewById(R.id.generate_button);
			mGenChiaviButton.setOnClickListener(this);

			mAccChiaviButton = (Button) rootView
					.findViewById(R.id.access_button);
			mAccChiaviButton.setOnClickListener(this);

			mDebugText = (TextView) rootView.findViewById(R.id.debugText);

			return rootView;
		}

		@Override
		public void onClick(View view) {

			switch (view.getId()) {
			case R.id.generate_button:
				debug("Cliccato Genera chiavi");
				generaChiavi();
				break;
			case R.id.access_button:
				debug("Accedi alle chiavi");
				accediChiavi();
				break;
			}

		}

		private KeyStore keyStore = null;
		private KeyStore.Entry entry = null;

		private void accediChiavi() {
			// TODO Auto-generated method stub
			keyStore = initKeyStore();
			entry = dammiElementoDalKeystore();
			debug("Certtificate Type: "
					+ ((KeyStore.PrivateKeyEntry) entry).getCertificate()
							.getType());
			debug(entry.toString());

		}

		// Load the key pair from the Android Key Store
		private KeyStore.Entry dammiElementoDalKeystore() {
			KeyStore.Entry entry = null;
			try {
				entry = keyStore.getEntry(ALIAS, null);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnrecoverableEntryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return entry;

		}

		private KeyStore initKeyStore() {
			KeyStore keyStore = null;
			// Init KeyStore
			try {
				keyStore = KeyStore.getInstance("AndroidKeyStore");
			} catch (KeyStoreException e) {
				debug("KeyStore Exception Error: " + e);
				return null;
			}

			try {
				keyStore.load(null);
			} catch (NoSuchAlgorithmException e) {
				debug(e.toString());
			} catch (CertificateException e) {
				debug(e.toString());
			} catch (IOException e) {
				debug(e.toString());
			}
			return keyStore;
		}

		private void generaChiavi() {
			new AsyncTask<Void, String, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					Context cx = getActivity();
					// Generate a key pair inside the AndroidKeyStore
					Calendar notBefore = Calendar.getInstance();
					Calendar notAfter = Calendar.getInstance();
					notAfter.add(1, Calendar.YEAR);

					android.security.KeyPairGeneratorSpec.Builder builder = new KeyPairGeneratorSpec.Builder(
							cx);
					builder.setAlias(ALIAS);
					String infocert = String.format("CN=%s, OU=%s", ALIAS,
							cx.getPackageName());
					builder.setSubject(new X500Principal(infocert));
					builder.setSerialNumber(BigInteger.ONE);
					builder.setStartDate(notBefore.getTime());
					builder.setEndDate(notAfter.getTime());
					KeyPairGeneratorSpec spec = builder.build();

					KeyPairGenerator kpGenerator;
					KeyPair kp = null;
					try {
						kpGenerator = KeyPairGenerator.getInstance("RSA",
								"AndroidKeyStore");
						kpGenerator.initialize(spec);
						kp = kpGenerator.generateKeyPair();

						publishProgress("Generated key pair : " + kp.toString());
						PublicKey publickey = kp.getPublic();
						PrivateKey privateKey = kp.getPrivate();
						publishProgress("Formato della chiave pubblica : "
								+ publickey.getFormat());
						publishProgress("Algoritmo utilizzato : "
								+ publickey.getAlgorithm());
						if (privateKey.getEncoded() == null)
							publishProgress("Imossibile accedere direttamente alla chiave privata :-(");

					} catch (NoSuchAlgorithmException e) {
						debug(e.toString());
					} catch (NoSuchProviderException e) {
						debug(e.toString());
					} catch (InvalidAlgorithmParameterException e) {
						debug(e.toString());
					}
					return null;
				}

				protected void onProgressUpdate(String... values) {
					debug(values[0]);
				}

				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					progressdialog.dismiss();

				}

				@Override
				protected void onPreExecute() {
					progressdialog = ProgressDialog.show(getActivity(),
							"Please wait...", "Generating keys...");
				}

			}.execute();

		}

		private void debug(String message) {
			mDebugText.append(message + "\n");
			Log.v(TAG, message);
		}
	}

}
