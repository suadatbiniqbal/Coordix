package com.suadat.coordix;

import android.Manifest;
import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.os.Vibrator;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.CompoundButton;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.*;
import androidx.asynclayoutinflater.*;
import androidx.coordinatorlayout.*;
import androidx.core.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.*;
import androidx.customview.*;
import androidx.documentfile.*;
import androidx.drawerlayout.*;
import androidx.fragment.*;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.interpolator.*;
import androidx.legacy.coreui.*;
import androidx.legacy.coreutils.*;
import androidx.lifecycle.*;
import androidx.lifecycle.livedata.*;
import androidx.lifecycle.livedata.core.*;
import androidx.lifecycle.viewmodel.*;
import androidx.loader.*;
import androidx.localbroadcastmanager.*;
import androidx.print.*;
import androidx.slidingpanelayout.*;
import androidx.swiperefreshlayout.*;
import androidx.versionedparcelable.*;
import androidx.viewpager.*;
import com.google.android.gms.base.*;
import com.google.android.gms.common.*;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.*;
import com.google.android.material.*;
import com.google.android.material.color.MaterialColors;
import com.suadat.coordix.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import com.google.android.material.button.MaterialButton;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Criteria;
import com.google.android.material.textfield.TextInputLayout;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.net.Uri;

public class MainActivity extends AppCompatActivity {
	
	private Timer _timer = new Timer();
	
	private MainBinding binding;
	private CheckBox autoTp;
	private CheckBox fixPoint;
	private CheckBox usrCoord;
	private EditText etDelay;
	private EditText etLat;
	private EditText etLng;
	private MaterialButton startBtn;
	private FusedLocationProviderClient fusedClient;
	private String log = "";
	private String v_lat = "";
	private String v_lng = "";
	private String geoUrl = "";
	private boolean antiDetect = false;
	private double lastLat = 0.0;
	private double lastLng = 0.0;
	private boolean isLocked = false;
	private boolean isStoped = false;
	
	private LocationManager mockLocation;
	private LocationListener _mockLocation_location_listener;
	private TimerTask timer;
	private AlertDialog.Builder dg;
	private SharedPreferences sp;
	private Intent intent = new Intent();
	private SharedPreferences lng_sp;
	private Vibrator vibr;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = MainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);} else {
			initializeLogic();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		mockLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		dg = new AlertDialog.Builder(this);
		sp = getSharedPreferences("sp", Activity.MODE_PRIVATE);
		lng_sp = getSharedPreferences("lng", Activity.MODE_PRIVATE);
		vibr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		binding.linearAutoTp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				
			}
		});
		
		binding.autoTp.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View _view) {
				dg.setTitle("Auto-teleport");
				dg.setMessage("Moves your location to random coordinates every X interval.");
				dg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				dg.create().show();
				return true;
			}
		});
		
		binding.autoTp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					binding.usrCoord.setEnabled(false);
					binding.fixPoint.setEnabled(false);
					binding.linearUsrCoord.setAlpha((float)(0.5d));
					binding.linearFixPoint.setAlpha((float)(0.5d));
				} else {
					binding.usrCoord.setEnabled(true);
					binding.fixPoint.setEnabled(true);
					binding.linearUsrCoord.setAlpha((float)(1));
					binding.linearFixPoint.setAlpha((float)(1));
				}
			}
		});
		
		binding.usrCoord.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View _view) {
				dg.setTitle("User Coordinate");
				dg.setMessage("You can specify your own coordinates, whichever you like. \n\nFor example:\nLat: 66.61997 & Lng: 0.0000 ");
				dg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				dg.create().show();
				return true;
			}
		});
		
		binding.usrCoord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					binding.autoTp.setEnabled(false);
					binding.linearAutoTp.setAlpha((float)(0.5d));
				} else {
					binding.autoTp.setEnabled(true);
					binding.linearAutoTp.setAlpha((float)(1));
				}
			}
		});
		
		binding.fixPoint.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View _view) {
				dg.setTitle("Fix the point");
				dg.setMessage("Fixes the location you specify without constant randomization.\n\nIf you do not specify coordinates, scripted ones will be selected.");
				dg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				dg.create().show();
				return true;
			}
		});
		
		binding.fixPoint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					binding.autoTp.setEnabled(false);
					binding.linearAutoTp.setAlpha((float)(0.5d));
				} else {
					binding.autoTp.setEnabled(true);
					binding.linearAutoTp.setAlpha((float)(1));
				}
			}
		});
		
		binding.logging.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View _view) {
				dg.setTitle("Logs");
				dg.setMessage("Information about actions. In this case, movements.");
				dg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				dg.create().show();
				return true;
			}
		});
		
		binding.logging.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					binding.linearDevLogs.setVisibility(View.VISIBLE);
					binding.vscrollLogs.setVisibility(View.VISIBLE);
				} else {
					binding.linearDevLogs.setVisibility(View.GONE);
					binding.vscrollLogs.setVisibility(View.GONE);
				}
			}
		});
		
		binding.btStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (isStoped) {
					// стоп
					Intent intent = new Intent(MainActivity.this, ForegroundService.class);
					stopService(intent);
					intent.setClass(getApplicationContext(), MainActivity.class);
					startActivity(intent);
					finishAffinity();
				} else {
					if (Double.parseDouble(binding.edittextDelay.getText().toString()) < 10000) {
						dg.setMessage("You have set the delay to less than 10 seconds (10000 ms), freezing is possible if your device is not powerful enough.");
						dg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface _dialog, int _which) {
								// старт
								Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
								startService(intent);
								
								boolean auto = autoTp.isChecked();
								boolean fix = fixPoint.isChecked();
								boolean user = usrCoord.isChecked();
								
								long delay = 3000;
								try {
									delay = Long.parseLong(etDelay.getText().toString());
								} catch (Exception ignored) {}
								
								double lat = 51.5074; // Default
								double lng = -0.1278;
								
								if (user) {
									try {
										lat = Double.parseDouble(etLat.getText().toString());
										lng = Double.parseDouble(etLng.getText().toString());
									} catch (Exception ignored) {}
								}
								
								_startMocking(auto, fix, user, delay, lat, lng);
								
								binding.btStart.setText("Stop");
								isStoped = true;
							}
						});
						dg.create().show();
					} else {
						// старт
						Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
						startService(intent);
						binding.btStart.setText("Stop");
						isStoped = true;
						
						boolean auto = autoTp.isChecked();
						boolean fix = fixPoint.isChecked();
						boolean user = usrCoord.isChecked();
						
						long delay = 3000;
						try {
							delay = Long.parseLong(etDelay.getText().toString());
						} catch (Exception ignored) {}
						
						double lat = 51.5074; // Default
						double lng = -0.1278;
						
						if (user) {
							try {
								lat = Double.parseDouble(etLat.getText().toString());
								lng = Double.parseDouble(etLng.getText().toString());
							} catch (Exception ignored) {}
						}
						
						_startMocking(auto, fix, user, delay, lat, lng);
						
					}
				}
				/*⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀c⠀⠀⠀⠀r⠀⠀⠀⠀e⠀⠀⠀⠀a⠀⠀⠀⠀t⠀⠀⠀⠀e⠀⠀⠀⠀d⠀⠀⠀⠀ ⠀⠀⠀⠀b⠀⠀⠀⠀y⠀⠀⠀⠀ ⠀⠀⠀⠀ν⠀⠀⠀⠀έ⠀⠀⠀⠀ο⠀⠀⠀⠀ς⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀*/
			}
		});
	}
	
	private void initializeLogic() {
		/*Log cat is enabled*/
		_lng_pack();
		if (sp.getString("sp", "").equals("")) {
			intent.setClass(getApplicationContext(), GrantAccessActivity.class);
			startActivity(intent);
			finish();
		} else {
			_graphics();
			_logsScroll();
			
			SharedPreferences prefs = getSharedPreferences("Erebus Fake GPS", MODE_PRIVATE);
			autoTp = findViewById(R.id.auto_tp);
			fixPoint = findViewById(R.id.fix_point);
			usrCoord = findViewById(R.id.usr_coord);
			etDelay = findViewById(R.id.edittext_delay);
			etLat = findViewById(R.id.et_lat);
			etLng = findViewById(R.id.et_lng);
			startBtn = findViewById(R.id.bt_start);
			if (mockLocation.getProvider("Erebus") == null) {
				mockLocation.addTestProvider("Erebus", false, false, false, false,
				true, true, true, 2, 1);
				mockLocation.setTestProviderEnabled("Erebus", true);
			}
			fusedClient = LocationServices.getFusedLocationProviderClient(this);
			/*
JHebvYqhwu7Uwh2jHs77u2h2j2jaia8
iKssi8w
hHw
hahwb_2u7wi1j1(tsiw);
&u7qh1b2jwi;a82uj2us8q81ueHwb
LnwbUwv2uah2niws
l
l
l
HanwnnaiNBWajwjbebeiiYqhbwu6_1
*/
			/*⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀c⠀⠀⠀⠀r⠀⠀⠀⠀e⠀⠀⠀⠀a⠀⠀⠀⠀t⠀⠀⠀⠀e⠀⠀⠀⠀d⠀⠀⠀⠀ ⠀⠀⠀⠀b⠀⠀⠀⠀y⠀⠀⠀⠀ ⠀⠀⠀⠀ν⠀⠀⠀⠀έ⠀⠀⠀⠀ο⠀⠀⠀⠀ς⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀*/
			binding.map.getSettings().setJavaScriptEnabled(true);
			binding.map.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
			
			
			
			/*Map setup | Настройка карты*/
			/**/
			/*
binding.linearDevMap.setVisibility(View.GONE);
*/
			binding.linearDevMap.setVisibility(View.VISIBLE);
			
			WebView web = binding.map;
			WebSettings s = web.getSettings();
			s.setJavaScriptEnabled(true);
			s.setDomStorageEnabled(true);
			
			web.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url != null && url.startsWith("app://")) {
						try {
							String payload = url.substring("app://".length()); // "lat,lng"
							String[] parts = payload.split(",");
							if (parts.length == 2) {
								binding.etLat.setText(parts[0]);
								binding.etLng.setText(parts[1]);
								binding.usrCoord.setChecked(true);
							}
						} catch (Exception ignored) {}
						return true; // не загружать в браузер
					}
					return false;
				}
			});
			
			web.loadUrl("file:///android_asset/map.html");
			/*I hid the WebView because I decided to postpone the embedded maps feature for later.

The functionality is done, all that's left is to choose a link to beautiful and problem-free maps.


Я скрыл WebView, потому что решил отложить функцию встроенных карт на потом.

Функционал готов, осталось только выбрать ссылку на красивые и беспроблемные карты.
*/
			
			if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.M) {
				
				Intent intent = new Intent();
				String packageName =  MainActivity.this.getPackageName();
				PowerManager pm = (PowerManager) MainActivity.this.getSystemService(Context.POWER_SERVICE);
				
				if (!pm.isIgnoringBatteryOptimizations(packageName)) {
					intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
					intent.setData(Uri.parse("package:" + packageName));
					startActivity(intent);
					dg.setMessage("To work properly, permission to ignore battery optimizations is required.");
					dg.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface _dialog, int _which) {
							
						}
					});
					dg.create().show();
				} else {
					
				}
			} else {
				
			}
		}
		WebView map = findViewById(R.id.map);
		ScrollView vscroll_bg = findViewById(R.id.vscroll_bg);
		map.setOnTouchListener((v, event) -> {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
				// Запрещаем ScrollView перехватывать тач
				vscroll_bg.requestDisallowInterceptTouchEvent(true);
				break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
				// Разрешаем обратно
				vscroll_bg.requestDisallowInterceptTouchEvent(false);
				break;
			}
			return false; // WebView продолжит обрабатывать касание
		});
		dg.setTitle("Alert!");
		dg.setIcon(R.drawable.icon_location_on_round);
		dg.setMessage("Follow Me On Instagram!");
		dg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface _dialog, int _which) {
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("https://www.instagram.com/suadatbiniqbal?igsh=b2I2YnJncmNwd2R4"));
				startActivity(intent);
			}
		});
		dg.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface _dialog, int _which) {
				
			}
		});
		dg.create().show();
	}
	
	public void _graphics() {
		/* Edttext UI */
		
		binding.delay.setBoxBackgroundMode(2);
		binding.lng.setBoxBackgroundMode(2);
		binding.lat.setBoxBackgroundMode(2);
		binding.delay.setBoxStrokeColor(0xFF9C27B0);
		binding.delay.setBoxCornerRadii((float)50, (float)50, (float)50, (float)50);
		binding.delay.setBoxBackgroundColor(Color.TRANSPARENT);
		binding.lat.setBoxStrokeColor(0xFF9C27B0);
		binding.lat.setBoxCornerRadii((float)50, (float)50, (float)50, (float)50);
		binding.lat.setBoxBackgroundColor(Color.TRANSPARENT);
		binding.lng.setBoxStrokeColor(0xFF9C27B0);
		binding.lng.setBoxCornerRadii((float)50, (float)50, (float)50, (float)50);
		binding.lng.setBoxBackgroundColor(Color.TRANSPARENT);
		binding.linearDev2.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)1, (int)1, getMaterialColor(R.attr.colorSecondaryContainer), Color.TRANSPARENT));
		binding.linearDevLogs.setVisibility(View.GONE);
		binding.vscrollLogs.setVisibility(View.GONE);
		dg = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		binding.logs.setTextIsSelectable(true);
		/* Animation UI */ /* Анимация UI */
		
		View[] animatedViews = {
			findViewById(R.id.linear_title),
			findViewById(R.id.title),
			findViewById(R.id.linear_version),
			findViewById(R.id.linear_dev1),
			findViewById(R.id.linear_dev2),
			findViewById(R.id.linear_dev3),
			findViewById(R.id.linear_lat),
			findViewById(R.id.linear_lng),
			findViewById(R.id.linear_log),
			findViewById(R.id.linear_dev_button_start),
			findViewById(R.id.bt_start)
		};
		
		for (int i = 0; i < animatedViews.length; i++) {
			View v = animatedViews[i];
			if (v != null) {
				v.setAlpha(0f);
				v.setTranslationY(50f);
				v.animate()
				.alpha(1f)
				.translationY(0f)
				.setStartDelay(i * 100) // задержка волной
				.setDuration(300)
				.start();
			}
		}
		/*⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀c⠀⠀⠀⠀r⠀⠀⠀⠀e⠀⠀⠀⠀a⠀⠀⠀⠀t⠀⠀⠀⠀e⠀⠀⠀⠀d⠀⠀⠀⠀ ⠀⠀⠀⠀b⠀⠀⠀⠀y⠀⠀⠀⠀ ⠀⠀⠀⠀ν⠀⠀⠀⠀έ⠀⠀⠀⠀ο⠀⠀⠀⠀ς⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀*/
	}
	
	
	public void _startMocking(final boolean _auto, final boolean _fix, final boolean _user, final double _delay, final double _lat, final double _lng) {
		/* don't touch this if you don't know */
		/* Не трогай - если не знаешь */
		/* don't touch this if you don't know */
		/* Не трогай - если не знаешь */
		/* don't touch this if you don't know */
		/* Не трогай - если не знаешь */
		fusedClient.setMockMode(true).addOnSuccessListener(unused -> {
			long startTime = System.currentTimeMillis(); // For "GPS warm-up"
			
			// Cancel previous task if running to avoid parallel timers
			if (timer != null) {
				try { timer.cancel(); } catch (Exception ignored) {}
			}
			
			timer = new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(() -> {
						try {
							Location mock = new Location("fused");
							
							if (_auto && !_fix) {
								mock.setLatitude(-90 + Math.random() * 180);
								mock.setLongitude(-180 + Math.random() * 360);
								binding.linearUsrCoord.setEnabled(false);
								binding.linearFixPoint.setEnabled(false);
								binding.linearUsrCoord.setAlpha(0.5f);
								binding.linearFixPoint.setAlpha(0.5f);
							} else {
								mock.setLatitude(_lat);
								mock.setLongitude(_lng);
							}
							
							boolean badAccuracyApplied = false;
							
							// ANTI-DETECT START
							if (binding.antiDetect.isChecked()) {
								long now = System.currentTimeMillis();
								
								// "Warm-up" for first 2 seconds
								if (now - startTime < 2000) {
									Log.d("AntiDetect", "GPS warming up...");
									log = log + "\nEREBUS: GPS warming up...";
									if (log.length() > 8000) { log = log.substring(log.length() - 8000); }
									binding.logs.setText(log);
									return;
								}
								
								// 10% chance of "signal loss"
								if (Math.random() < 0.1) {
									Log.d("AntiDetect", "Signal lost...");
									log = log + "\nEREBUS: Signal lost...";
									if (log.length() > 8000) { log = log.substring(log.length() - 8000); }
									binding.logs.setText(log);
									return;
								}
								
								// Add pseudo-noise to coordinates
								double noiseLat = (Math.random() - 0.5) * 0.0002;
								double noiseLng = (Math.random() - 0.5) * 0.0002;
								mock.setLatitude(mock.getLatitude() + noiseLat);
								mock.setLongitude(mock.getLongitude() + noiseLng);
								
								// Sometimes make bad accuracy
								if (Math.random() < 0.05) {
									mock.setAccuracy(50.0f + (float) (Math.random() * 50));
									badAccuracyApplied = true;
									Log.d("AntiDetect", "Bad GPS accuracy");
									log = log + "\nEREBUS: Bad GPS accuracy";
									if (log.length() > 8000) { log = log.substring(log.length() - 8000); }
									binding.logs.setText(log);
								}
							}
							// ANTI-DETECT END
							
							if (!badAccuracyApplied) {
								mock.setAccuracy(1.0f);
							}
							mock.setTime(System.currentTimeMillis());
							mock.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
							
							fusedClient.setMockLocation(mock)
							.addOnSuccessListener(aVoid -> {
								Log.d("FakeGPS", "Location: " + mock.getLatitude() + ", " + mock.getLongitude());
								log = log + "\nEREBUS: Location: " + mock.getLatitude() + " | " + mock.getLongitude();
								if (log.length() > 8000) { log = log.substring(log.length() - 8000); }
								binding.logs.setText(log);
								
								// Update map in WebView only if visible
								if (binding.linearDevMap.getVisibility() == View.VISIBLE) {
									binding.map.loadUrl("javascript:updateMarker(" + mock.getLatitude() + "," + mock.getLongitude() + ")");
								}
							})
							.addOnFailureListener(e -> {
								Log.e("FakeGPS", "Failed to set mock location: " + e.getMessage());
								log = log + "\nEREBUS: Failed to set mock location: " + e.getMessage();
								if (log.length() > 8000) { log = log.substring(log.length() - 8000); }
								binding.logs.setText(log);
							});
							
						} catch (Exception e) {
							Log.e("FakeGPS", "Timer error: " + e.getMessage());
							log = log + "\nEREBUS: Timer error: " + e.getMessage();
							if (log.length() > 8000) { log = log.substring(log.length() - 8000); }
							binding.logs.setText(log);
						}
					});
				}
			};
			
			// Use shared timer to avoid multiple Timer instances
			_timer.scheduleAtFixedRate(timer, 0, (long) _delay);
		}).addOnFailureListener(e -> {
			Log.e("FakeGPS", "Failed to enable mock location mode: " + e.getMessage());
			log = log + "\nEREBUS: Failed to enable mock location mode: " + e.getMessage();
			if (log.length() > 8000) { log = log.substring(log.length() - 8000); }
			binding.logs.setText(log);
		});
		/* Nwbwb2biIj2n888jeniIwh2beuz8
bani8uwb<<g777726Gya7wh87yw
Hwb8a81yhIwjq9wusba7wheusjUwn
jBvVzvhzyqgapLpp8/_718/_7wyw/_VgaUauw8su + uwhYqi8Y7uh<8~

Lwll08a7Hai88UjajshYyH1 @ ususau7YHUYSOSI8!6Bxhuw

l
l
l
l
l
$ATtqnU67^hGgahua76tysj
*/
	}
	
	
	public void _lng_pack() {
		binding.autoTp.setText(getString(R.string.auto_teleport)); // CheckBox с id: auto_tp
		binding.usrCoord.setText(getString(R.string.user_coordinates)); // CheckBox с id: usr_coord
		binding.fixPoint.setText(getString(R.string.fix_the_point)); // CheckBox с id: fix_point
		binding.logging.setText(getString(R.string.enable_logs)); // CheckBox с id: logging
		binding.textviewDelay.setText(getString(R.string.enter_delay)); // TextView с id: textview_delay
		binding.textviewEnterCoord.setText(getString(R.string.enter_coords)); // TextView с id: textview_enter_coord
		binding.etLat.setHint(getString(R.string.lat)); // EditText с id: et_lat
		binding.etLng.setHint(getString(R.string.lng)); // EditText с id: et_lng
		binding.btStart.setText(getString(R.string.start)); // Кнопка с id: bt_start
		binding.title.setText(getString(R.string.created_by)); // TextView с id: title
		
	}
	
	
	public void _logsScroll() {
		ScrollView vscroll_logs = findViewById(R.id.vscroll_logs);
		TextView logs = findViewById(R.id.logs);
		
		logs.addTextChangedListener(new android.text.TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				vscroll_logs.post(() -> vscroll_logs.fullScroll(android.view.View.FOCUS_DOWN));
			}
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void afterTextChanged(android.text.Editable s) {}
		});
	}
	
	
	private int getMaterialColor(int resourceId) {
		return MaterialColors.getColor(this, resourceId, "getMaterialColor");
	}
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}