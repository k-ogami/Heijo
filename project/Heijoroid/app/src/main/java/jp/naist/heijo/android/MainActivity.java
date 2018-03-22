package jp.naist.heijo.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity
{

  private Spinner appSpinner = null;
  private EditText hostEditText = null;
  private EditText portEditText = null;
  private EditText ignoreEditText = null;
  private Button startButton = null;
  private TextView logTextView = null;

  private SharedPreferences dataSaver = null;

  @Override
  protected void onCreate(Bundle bundle)
  {
    super.onCreate(bundle);

    setContentView(R.layout.layout);

    appSpinner = (Spinner)findViewById(R.id.appSpinner);
    hostEditText = (EditText)findViewById(R.id.hostEditText);
    portEditText = (EditText)findViewById(R.id.portEditText);
    ignoreEditText = (EditText)findViewById(R.id.ignoreEditText);
    startButton = (Button)findViewById(R.id.startButton);
    logTextView = (TextView)findViewById(R.id.logTextView);

    // 名前は適当
    dataSaver = getSharedPreferences(ConstValue.THIS_PACKAGE_NAME, Context.MODE_PRIVATE);

    setSavedInput();
    setInstalledApps();
    setSavingTextEventListener();
    setButtonEventListener();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    setNewInstalledApps();
  }

  private void setSavedInput()
  {
    String host = dataSaver.getString(ConstValue.BUNDLE_HOST, null);
    if (host != null && host.length()!= 0) {
      hostEditText.setText(host);
    }
    String port = dataSaver.getString(ConstValue.BUNDLE_PORT, null);
    if (port != null && port.length()!= 0) {
      portEditText.setText(port);
    }
    String ignore = dataSaver.getString(ConstValue.BUNDLE_IGNORE_PACKAGE_NAMES, null);
    if (ignore != null && ignore.length()!= 0) {
      ignoreEditText.setText(ignore);
    }
  }

  private void setInstalledApps()
  {
    // インストール済みのアプリケーション一覧を取得
    List<ApplicationInfo> installedApps = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    // パッケージ名でソート
    Collections.sort(installedApps, new Comparator<ApplicationInfo>()
    {
      @Override
      public int compare(ApplicationInfo o1, ApplicationInfo o2)
      {
        return o1.packageName.compareTo(o2.packageName);
      }
    });
    // アプリケーションをスピナーに追加していく
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    adapter.add("*** Select app package name ***");
    for (ApplicationInfo app : installedApps) {
      // これ自身および起動可能でないアプリケーションは無視する
      if (app.packageName.equals(getPackageName())) continue;
      if (getPackageManager().getLaunchIntentForPackage(app.packageName) == null) continue;
      adapter.add(app.packageName);
    }
    appSpinner.setAdapter(adapter);
  }

  private void setNewInstalledApps()
  {
    String preSelected = appSpinner.getSelectedItem().toString();
    setInstalledApps();
    int position = 0;
    for (; position < appSpinner.getAdapter().getCount(); position++) {
      if (appSpinner.getAdapter().getItem(position).equals(preSelected)) break;
    }
    if (position == appSpinner.getAdapter().getCount()) position = 0;
    appSpinner.setSelection(position);
  }

  private void setSavingTextEventListener()
  {
    // キーはBundle用の文字列を使いまわす
    hostEditText.addTextChangedListener(new TextWatcher()
    {
      @Override
      public void afterTextChanged(Editable s)
      {
        dataSaver.edit().putString(ConstValue.BUNDLE_HOST, hostEditText.getText().toString()).apply();
      }
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) { }
    });
    portEditText.addTextChangedListener(new TextWatcher()
    {
      @Override
      public void afterTextChanged(Editable s)
      {
        dataSaver.edit().putString(ConstValue.BUNDLE_PORT, portEditText.getText().toString()).apply();
      }
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) { }
    });
    ignoreEditText.addTextChangedListener(new TextWatcher()
    {
      @Override
      public void afterTextChanged(Editable s)
      {
        dataSaver.edit().putString(ConstValue.BUNDLE_IGNORE_PACKAGE_NAMES, ignoreEditText.getText().toString()).apply();
      }
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) { }
    });
  }

  private void setButtonEventListener()
  {
    startButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        if (appSpinner.getSelectedItemPosition() == 0) {
          logTextView.setText("Error:App is not selected.");
          return;
        }
        String packageName = (String) appSpinner.getSelectedItem();
        Intent selectedApp = getPackageManager().getLaunchIntentForPackage(packageName);
        if (selectedApp == null) {
          logTextView.setText("Error:Selected app cannot be launched.");
           return;
         }
        // 転送するデータをここに追加する
        {
          selectedApp.putExtra(ConstValue.BUNDLE_TARGET_PACKAGE_NAME, packageName);
          selectedApp.putExtra(ConstValue.BUNDLE_HOST, hostEditText.getText().toString());
          selectedApp.putExtra(ConstValue.BUNDLE_PORT, Integer.valueOf(portEditText.getText().toString()));
          selectedApp.putExtra(ConstValue.BUNDLE_IGNORE_PACKAGE_NAMES, ignoreEditText.getText().toString());
        }
        startActivity(selectedApp);
      }
    });
  }

}
