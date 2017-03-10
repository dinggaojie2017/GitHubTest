package com.fontlose.relayctrl;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class UiProcess {


    HandleMsg hUiMsg = null;
    Context mct = null;
    DataProcess dataProcess = null;


    private ImageView imgState, bnTemp, bnHum, bnLight;
    private EditText etIp, etPort;
    private Button bnConnect;
    private ToggleButton turnChange;
    private ContentLoadingProgressBar progress;


    public UiProcess(LinearLayout mainLayout, Context context, HandleMsg hMsg, DataProcess dProcess) {
        super();

        hUiMsg = hMsg;
        mct = context;
        dataProcess = dProcess;
        bnTemp = (ImageView) mainLayout.findViewById(R.id.iBTemp);
        bnHum = (ImageView) mainLayout.findViewById(R.id.iBHum);
        bnLight = (ImageView) mainLayout.findViewById(R.id.iBLight);
        etIp = (EditText) mainLayout.findViewById(R.id.etIp);
        etPort = (EditText) mainLayout.findViewById(R.id.etPort);

        loadIpPort();

        imgState = (ImageView) mainLayout.findViewById(R.id.imgState);
        bnConnect = (Button) mainLayout.findViewById(R.id.bnConnect);
        turnChange = (ToggleButton) mainLayout.findViewById(R.id.turn_change);
        progress = (ContentLoadingProgressBar) mainLayout.findViewById(R.id.progress);

        bnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // snd.play(hitOkSfx, (float)0.5, (float)0.5, 0, 0, 1);
                // TODO Auto-generated method stub
                if (isEditEnable()) {

                    String sip = etIp.getText().toString().trim();
                    String sport = etPort.getText().toString().trim();

                    Pattern pa = Pattern.compile("^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
                    Matcher ma = pa.matcher(sip);
                    if (ma.matches() == false) {
                        showMessage(mct.getString(R.string.msg6));
                        return;
                    }

                    int port = 0;
                    try {
                        port = Integer.parseInt(sport);
                    } catch (Exception e) {
                        showMessage(mct.getString(R.string.msg7));
                        return;
                    }
                    dataProcess.startConn(sip, port, handler);

                    progress.show();
                    imgState.setVisibility(View.GONE);
                } else {
                    stopConn();
                }
            }
        });

        turnChange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String s = "LighAdd\r\n";
                    try {
                        dataProcess.sendCmd(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String s = "LighSub\r\n";
                    try {
                        dataProcess.sendCmd(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 开启关闭反馈
     */
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
                case 0:
                    showMessage(mct.getString(R.string.msg1));
                    break;

                case 1:
                    showMessage(mct.getString(R.string.msg2));
                    imgState.setImageResource(R.drawable.true1);
                    hUiMsg.stateCheck(1);
                    setEditEnable(false);
                    break;

                case 2:
                    hUiMsg.stateCheck(0);
                    showMessage(mct.getString(R.string.msg3));
                    imgState.setImageResource(R.drawable.false0);
                    setEditEnable(true);
                    break;
            }
            progress.hide();
            imgState.setVisibility(View.VISIBLE);
			super.handleMessage(msg);
		};
	};

    /**
     * 显示Toast
     * @param msg
     */
    public void showMessage(String msg) {
        Toast.makeText(mct, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 断开连接
     */
    public void stopConn() {
        progress.show();
        imgState.setVisibility(View.GONE);
        dataProcess.stopConn(handler);
    }

    public void setEditEnable(boolean en) {
        etIp.setEnabled(en);
        etPort.setEnabled(en);
        if (en == false) {
            etIp.setInputType(InputType.TYPE_NULL);
            etPort.setInputType(InputType.TYPE_NULL);
        } else {
            etIp.setInputType(InputType.TYPE_CLASS_TEXT);
            etPort.setInputType(InputType.TYPE_CLASS_TEXT);
        }
    }

    public boolean isEditEnable() {
        return etIp.isEnabled();
    }

    public void saveIpPort() {
        SharedPreferences uiState = mct.getSharedPreferences("system", mct.MODE_PRIVATE);
        Editor et = uiState.edit();
        et.putString("ip", etIp.getText().toString());
        et.putString("port", etPort.getText().toString());
        et.commit();
    }

    public void loadIpPort() {
        SharedPreferences uiState = mct.getSharedPreferences("system", mct.MODE_PRIVATE);
        etIp.setText(uiState.getString("ip", "192.168.4.1"));
        etPort.setText(uiState.getString("port", "8080"));
    }


}
