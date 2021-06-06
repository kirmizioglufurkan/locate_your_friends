package com.furkan.locateyourfriends;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class ReferralCodeActivity extends AppCompatActivity {

    private final int QR_CODE_DIMENSIONS = 500;
    Bitmap bitmap;
    private ImageView imgBack;
    private ImageView imgQRCode;
    private Button btnCopy;
    private String referralCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral_code);
        imgBack = findViewById(R.id.img_referral_back);
        imgQRCode = findViewById(R.id.img_qrCode);
        btnCopy = findViewById(R.id.btn_referral_code);
        Intent intent = getIntent();
        if (intent != null) {
            referralCode = intent.getStringExtra("referralCode");
        }

        qrCodeGenerator(referralCode);

    }

    @Override
    protected void onResume() {
        super.onResume();
        imgBack.setOnClickListener(v -> goBack());
        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invite Code", getResources().getString(R.string.user_invite_code_prefix) + " " + referralCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_invite_code_copied), Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    private void goBack() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        imgBack.startAnimation(animation);
        Intent intent = new Intent(ReferralCodeActivity.this, UserLocationMainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }


    private void qrCodeGenerator(String code) {
        try {
            bitmap = TextToImageEncode(code);
            imgQRCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    private Bitmap TextToImageEncode(String value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    value,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_DIMENSIONS, QR_CODE_DIMENSIONS, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        ContextCompat.getColor(ReferralCodeActivity.this, android.R.color.black) : ContextCompat.getColor(ReferralCodeActivity.this, R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

}