package org.cug.driver;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: liulin
 * Date: 13-5-23
 * Time: 下午1:07
 * To change this template use File | Settings | File Templates.
 */
public class ShowMessageActivity extends Activity {

    private TextView startName;
    private TextView endName;
    private TextView distance;
    private TextView usertime;
    private Button submitButton;
    private Button cancelButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmessage);

        startName = (TextView) findViewById(R.id.textView_showmessage_strname);
        endName = (TextView) findViewById(R.id.textView_showmessage_endname);
        distance = (TextView) findViewById(R.id.textView_showmeaasge_distance);
        usertime = (TextView) findViewById(R.id.textView_showmeaasge_time);

        submitButton = (Button) findViewById(R.id.button_showmeaasge_submit);
        cancelButton = (Button) findViewById(R.id.button_showmeaasge_cancel);


    }
}