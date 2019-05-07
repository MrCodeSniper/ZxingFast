package cn.google.zxing.self.manager;

/**
 * Created by mac on 2018/10/1.
 */

public class Complex {
    private double m;// 实部   
    private double n;// 虚部   
     public Complex(double m, double n) {
         this.m = m;
         this.n = n;
     }

     // add   
     public Complex add(Complex c) {
         return new Complex(m + c.m, n + c.n);
     }

     // minus   
     public Complex minus(Complex c) {
         return new Complex(m - c.m, n - c.n);
     }

    // multiply   
   public Complex multiply(Complex c) {
         return new Complex(m * c.m - n * c.n, m * c.n + n * c.m);
     }
     // divide   
    public Complex divide(Complex c) {
        double d = Math.sqrt(c.m * c.m) + Math.sqrt(c.n * c.n);
        return new Complex((m * c.m + n * c.n) / d, Math.round((m * c.n - n * c.m) / d));
     }

    public double getM() {
         return m;
     }
    public double getN() {
         return n;
     }

    public String toString() {
        String rtr_str = "";
        if (n > 0) rtr_str = "(" + m + "+" + n + "i" + ")";
        if (n == 0)rtr_str = "(" + m + ")";
        if (n < 0)rtr_str = "(" + m + n + "i" + ")";
        return rtr_str;
     }



}
