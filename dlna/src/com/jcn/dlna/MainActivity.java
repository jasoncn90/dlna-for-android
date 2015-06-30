package com.jcn.dlna;

import java.util.logging.Logger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jcn.dlna.new_sdk.DlnaService;
import com.jcn.dlna.new_sdk.device.DeviceManager.OnSearchDmrDeviceListener;
import com.jcn.dlna.new_sdk.device.DmrDevice;
import com.jcn.dlna.new_sdk.device.DmrDeviceManager;

public class MainActivity extends AppCompatActivity {

	private static final Logger log = Logger.getLogger(MainActivity.class
			.getName());
	private Button btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		DlnaService.getInstance().bind(this);
		btn = (Button) findViewById(R.id.btn);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DmrDeviceManager dm = new DmrDeviceManager();
				dm.searchDmrDevices(new OnSearchDmrDeviceListener() {

					@Override
					public void onDeviceRemove(DmrDevice device) {
						log.info("onDeviceRemove " + device.getName());
					}

					@Override
					public void onDeviceAdd(DmrDevice device) {
						log.info("onDeviceAdd " + device.getName());
					}
				});
			}
		});
	}

	@Override
	protected void onDestroy() {
		DlnaService.getInstance().unbind(this);
		super.onDestroy();
	}
}
