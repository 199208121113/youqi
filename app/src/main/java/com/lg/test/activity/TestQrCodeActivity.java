package com.lg.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lg.base.core.BaseActivity;
import com.lg.qrcode.sample.CaptureActivity;
import com.lg.test.R;

import roboguice.inject.InjectView;

/**
 * Created by liguo on 2015/11/3.
 */
public class TestQrCodeActivity extends BaseActivity implements View.OnClickListener {

    @InjectView(R.id.act_qr_code_create)
    TextView tvCreate;

    @InjectView(R.id.act_qr_code_scan)
    TextView tvScan;

    @InjectView(R.id.act_qr_code_scan_result)
    TextView tvScanResult;

    private final static int SCANNIN_GREQUEST_CODE = 1;

    public static Intent createIntent(Context ctx){
        Intent it = new Intent(ctx,TestQrCodeActivity.class);
        return it;
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_qr_code;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvCreate.setOnClickListener(this);
        tvScan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == tvCreate){

        }else if(v == tvScan){
            startActivityForResult(CaptureActivity.createIntent(this), SCANNIN_GREQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    tvScanResult.setText(bundle.getString("result"));
                    //mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
                }
                break;
        }
    }

}
