package com.jcn.dlna;

import java.util.logging.Logger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.jcn.dlna.new_sdk.DlnaService;
import com.jcn.dlna.new_sdk.device.DmrDevice;
import com.jcn.dlna.new_sdk.device.DmrDeviceManager;
import com.jcn.dlna.new_sdk.device.DmrDeviceManager.OnSearchDmrDeviceListener;
import com.jcn.dlna.new_sdk.dmc.ActionController.ActionResultListener;

public class MainActivity extends AppCompatActivity {

	private static final Logger log = Logger.getLogger(MainActivity.class
			.getName());
	private Button btn;
	private ListView list;
	private ArrayAdapter<DmrDevice> deviceListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		DlnaService.getInstance().bind(this);
		btn = (Button) findViewById(R.id.btn);
		list = (ListView) findViewById(R.id.list);
		deviceListAdapter = new ArrayAdapter<DmrDevice>(this,
				android.R.layout.simple_list_item_1);
		list.setAdapter(deviceListAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				DmrDevice device = deviceListAdapter.getItem(position);
				device.setMute(true, new ActionResultListener() {

					@Override
					public void onResult(boolean success, int errorCode,
							String errorMsg) {
						log.warning("set mute result--> " + success + " "
								+ errorCode + " " + errorMsg);
					}
				});
			}
		});
		final DmrDeviceManager dm = new DmrDeviceManager();
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dm.searchDmrDevices(new OnSearchDmrDeviceListener() {

					@Override
					public void onDeviceRemove(final DmrDevice device) {
						log.info("onDeviceRemove " + device.getName());
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								deviceListAdapter.remove(device);
							}
						});
					}

					@Override
					public void onDeviceAdd(final DmrDevice device) {
						log.info("onDeviceAdd " + device.getName());
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								deviceListAdapter.add(device);
							}
						});
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
