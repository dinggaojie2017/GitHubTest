package com.fontlose.relayctrl;


import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.io.IOException;


public class RelayCtrlActivity extends AppCompatActivity {
    AlertDialog aDailog;
    DataProcess dataProcess;
    UiProcess uiProcess;

    //状态
    private TextView state;
    // QMainMenu mainDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.ipset));

        state = (TextView) findViewById(R.id.state);

        dataProcess = new DataProcess(hMsg, this);
        uiProcess = new UiProcess((LinearLayout) findViewById(R.id.mainLay), this, hMsg, dataProcess);
        createDialog();
    }

    public void tempUp(View view) {
        String s = "TempAdd\r\n";
        try {
            dataProcess.sendCmd(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tempDown(View view) {
        String s = "TempSub\r\n";
        try {
            dataProcess.sendCmd(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void humUp(View view) {
        String s = "HumtAdd\r\n";
        try {
            dataProcess.sendCmd(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void humDown(View view) {
        String s = "HumtSub\r\n";
        try {
            dataProcess.sendCmd(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取信息回调
     */
    HandleMsg hMsg = new HandleMsg() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            if (dataProcess == null)
                return;
            if (msg.what == dataProcess.RELAYCHK) {
                if (ckeck) {
                    state.setText("状态信息↓\n" + "当前温度:" + dataProcess.getsData()[0] + "°C\n" +
                            "当前湿度:" + dataProcess.getsData()[1] + "%\n" + "当前亮度:" + dataProcess.getsData()[2] + "Lm");
                    this.sendEmptyMessageDelayed(dataProcess.RELAYCHK, 2000);
                }
            } else if (msg.what == dataProcess.CLOSETCP) {
                uiProcess.stopConn();
            } else if (msg.what == dataProcess.APPQUIT) {
                aDailog.show();
            }
        }
    };


    public void createDialog() {
        aDailog = new AlertDialog.Builder(RelayCtrlActivity.this)
                .setTitle(RelayCtrlActivity.this.getString(R.string.tileQuit))
                .setNegativeButton(RelayCtrlActivity.this.getString(R.string.lbCancle), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        //uiProcess.soundPlay();
                        aDailog.dismiss();
                    }
                })
                .setPositiveButton(RelayCtrlActivity.this.getString(R.string.lbQuit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        //uiProcess.soundPlay();
                        uiProcess.saveIpPort();
                        Intent home = new Intent(Intent.ACTION_MAIN);
                        home.addCategory(Intent.CATEGORY_HOME);
                        RelayCtrlActivity.this.startActivity(home);
                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                        System.exit(0);

                    }
                })
                .create();


        aDailog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if ((event.ACTION_DOWN == event.getAction()) && (event.getRepeatCount() == 0)) {
                    if (keyCode == event.KEYCODE_BACK) {
                        Intent home = new Intent(Intent.ACTION_MAIN);
                        home.addCategory(Intent.CATEGORY_HOME);
                        RelayCtrlActivity.this.startActivity(home);
                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        System.exit(0);
                    }
                    return true;
                }
                return false;
            }
        });
    }
 /*
    class onMenuItemClick implements OnItemClickListener
    {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			mainDialog.close();			
			if(arg2==0)
			{//编辑服务
				uiProcess.createConfigWindow.showAtLocation((LinearLayout)findViewById(R.id.mainLay),Gravity.CENTER_VERTICAL, 0, 0); 
			} 
			else if(arg2==1)
			{	
				//AppConnect.getInstance(mct).showOffers(mct);
				mainDialog.close(); 
				createaboutWindow(); 
			}
			
		}
    }
*/

    /**
     * 菜单点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itabout:
                new AlertDialog.Builder(RelayCtrlActivity.this)
                        .setView(R.layout.about)
                        .setPositiveButton(RelayCtrlActivity.this.getString(R.string.lbOK), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                aDailog.dismiss();
                            }
                        }).setCancelable(true).show();

                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if ((event.ACTION_DOWN == event.getAction()) && (event.getRepeatCount() == 0)) {
            if (keyCode == event.KEYCODE_BACK) {
                hMsg.sendEmptyMessageDelayed(DataProcess.APPQUIT, 2);
            } else if (keyCode == event.KEYCODE_MENU) {
                //hMsg.sendEmptyMessageDelayed(DataProcess.MAINMENU, 2);
                return true;
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}