package cn.codesniper.zxing.self.core;

import java.io.Serializable;

/**
 *  解析二维码的类型
 */

public enum  ParsedQrCodeType implements Serializable{


    //1.商家入驻
    MERCHANT_IN,

    //2.激活商家二维码
    ACTIVE_MERCHANT_QRCODE;


}
