package cn.codesniper.zxing.self.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hrz.qrcode.R;

/**
 * Created by mac on 2018/10/31.
 */

public class QrCodeHintDialog extends Dialog{

    private Context context;
     private String hintText;
     private String confirmText;
     private ClickListenerInterface clickListenerInterface;

    public interface ClickListenerInterface {
        public void doConfirm();
    }

    public void setClickListenerInterface(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    public QrCodeHintDialog(Context context, String hint, String check) {
        super(context);
        this.context=context;
        this.hintText=hint;
        this.confirmText=check;
    }

    public QrCodeHintDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected QrCodeHintDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.qrcode_dialog_layout, null);
        setContentView(view);
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_qrcode_hint);
        TextView tvConfirm = (TextView) view.findViewById(R.id.tv_qrcode_confirm);
        tvTitle.setText(hintText);
        tvConfirm.setText(confirmText);

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickListenerInterface!=null){
                    clickListenerInterface.doConfirm();
                }
            }
        });

//        Window dialogWindow = getWindow();
//        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
//        lp.width = (int) (d.widthPixels * 0.74);
//        lp.height= (int) (d.heightPixels * 0.3);
//        dialogWindow.setAttributes(lp);
    }
}
