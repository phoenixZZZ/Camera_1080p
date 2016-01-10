package com.jiuan.it.ipc.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.ui.widget.CustomEditGroup;

public class AddDeviceActivity extends ActionBarActivity{

    private CustomEditGroup name,uid,user,password;

    private RadioGroup radio;

    private RadioButton radio_goolink ,radio_rtsp;

    private static String TAG = "ConnectActivity";

    private int type = 1 ;

    private Toast tipError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_dialog_show);
       /* name = (CustomEditGroup)findViewById(R.id.name);
        uid = (CustomEditGroup) findViewById(R.id.id);
        user = (CustomEditGroup) findViewById(R.id.user);
        password = (CustomEditGroup) findViewById(R.id.password);
        radio = (RadioGroup) findViewById(R.id.radio);
        radio_goolink = (RadioButton) findViewById(R.id.radio_goolink);
        radio_rtsp = (RadioButton) findViewById(R.id.radio_rtsp);
        //绑定一个匿名监听器
        radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == radio_goolink.getId()) {
                    uid.setTitle("设备ID");
                    type = 1;
                }
                if (checkedId == radio_rtsp.getId()) {
                    uid.setTitle("URL");
                    type = 3;
                }
            }
        });*/
        tipError = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            //save();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*private void save(){
        Connect bean = new Connect();
        if(TextUtils.isEmpty(uid.getValue())
           ||TextUtils.isEmpty(name.getValue())
           ||TextUtils.isEmpty(user.getValue())
           ||TextUtils.isEmpty(password.getValue())){
            tipErrorShow("数据项存在空值，不可保存");
           return;
       }
        bean.setType(type);
        bean.setuId(uid.getValue());
        bean.setName(name.getValue());
        bean.setUser(user.getValue());
        bean.setPassword(password.getValue());
        List<Connect>  connect = Config.getDevice(this);
        if(connect == null){
            connect = new ArrayList<>();
        }
        connect.add(bean);
        Gson gson = new Gson();
        Config.setDevice(this, gson.toJson(connect));
        finish();
    }*/

    /**
     * 提示错误信息
     */
    protected final Toast tipErrorShow(String value) {
        tipError.setText(value);
        tipError.show();
        return tipError;
    }

}
