package cn.google.zxing.self.callback;

import android.graphics.Bitmap;

import cn.google.zxing.Result;
import cn.google.zxing.client.result.ParsedResult;

/**
 * Created by hupei on 2016/7/1.
 */
public interface OnScannerCompletionListener {
    /**
     * 扫描成功后将调用
     * <pre>
     *     ParsedResultType type = parsedResult.getType();
     *     switch (type) {
     *         case ADDRESSBOOK:
     *             AddressBookParsedResult addressResult = (AddressBookParsedResult) parsedResult;
     *         break;
     *         case URI:
     *              URIParsedResult uriParsedResult = (URIParsedResult) parsedResult;
     *         break;
     *     }
     * </pre>
     *
     * @param rawResult    扫描结果
     * @param parsedResult 抽象类，结果转换成目标类型
     * @param barcode      位图
     */
    void onScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode);
}
