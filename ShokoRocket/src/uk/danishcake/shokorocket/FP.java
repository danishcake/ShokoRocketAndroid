package uk.danishcake.shokorocket;

/**
*
* 16:16 fixed point math routines, for IAppli/CLDC platform.
* A fixed point number is a 32 bit int containing 16 bits of integer and 16 bits of fraction.
*<p>
* (C) 2001 Beartronics
* Author: Henry Minsky (hqm@alum.mit.edu)
*<p>
* Licensed under terms "Artistic License"<br>
* <a href="http://www.opensource.org/licenses/artistic-license.html">http://www.opensource.org/licenses/artistic-license.html</a><br>
*
*<p>
* Numerical algorithms based on
* http://www.cs.clemson.edu/html_docs/SUNWspro/common-tools/numerical_comp_guide/ncg_examples.doc.html
* <p>
* Trig routines based on numerical algorithms described in
* http://www.magic-software.com/MgcNumerics.html
*
* http://www.dattalo.com/technical/theory/logs.html
*
* @version $Id: FP.java,v 1.6 2001/04/05 07:40:17 hqm Exp $
*/

public class FP {

     public static int toInt(int x) {
          return x >> 16;
     }

     public static int fromInt(int x) {
          return x << 16;
     }
     
     public static int fromFloat(float x) {
          return (int)(x * (1 << 16));
     }
     
     public static int fromDouble(double x) {
          return (int)(x * (1 << 16));
     }
     
     public static double toDouble(int x) {
          return (double)x / (1 << 16);
     }
     
     public static float toFloat(int x) {
          return (float)x / (1 << 16);
     }

     
     /** Multiply two fixed-point numbers */
     public static int mul(int x, int y) {
          long z = (long) x * (long) y;
          return ((int) (z >> 16));
     }

     /** Divides two fixed-point numbers */
     public static int div(int x, int y) {
          long z = (((long) x) << 32);
          return (int) ((z / y) >> 16);
     }

     /** Compute square-root of a 16:16 fixed point number */
     public static int sqrt(int n) {
          int s = (n + 65536) >> 1;
          for (int i = 0; i < 8; i++) {
               s = (s + div(n, s)) >> 1;
          }
          return s;
     }

     /** Round to nearest fixed point integer */
     public static int round(int n) {
          if (n > 0) {
               if ((n & 0x8000) != 0) {
                    return (((n + 0x10000) >> 16) << 16);
               } else {
                    return (((n) >> 16) << 16);
               }
          } else {
               int k;
               n = -n;
               if ((n & 0x8000) != 0) {
                    k = (((n + 0x10000) >> 16) << 16);
               } else {
                    k = (((n) >> 16) << 16);
               }
               return -k;
          }
     }

     public static final int PI = 205887;
     public static final int PI_OVER_2 = PI / 2;
     public static final int E = 178145;
     public static final int HALF = 2 << 15;
     
     
     static final int SK1 = 498;
     static final int SK2 = 10882;

     /**
      * Computes SIN(f), f is a fixed point number in radians. 0 <= f <= 2PI
      */
     public static int sin(int f) {
          // If in range -pi/4 to pi/4: nothing needs to be done.
          // otherwise, we need to get f into that range and account for
          // sign change.

          int sign = 1;
          if ((f > PI_OVER_2) && (f <= PI)) {
               f = PI - f;
          } else if ((f > PI) && (f <= (PI + PI_OVER_2))) {
               f = f - PI;
               sign = -1;
          } else if (f > (PI + PI_OVER_2)) {
               f = (PI << 1) - f;
               sign = -1;
          }

          int sqr = mul(f, f);
          int result = SK1;
          result = mul(result, sqr);
          result -= SK2;
          result = mul(result, sqr);
          result += (1 << 16);
          result = mul(result, f);
          return sign * result;
     }

     static final int CK1 = 2328;
     static final int CK2 = 32551;

     /**
      * Computes cos(f), f is a fixed point number in radians. 0 <= f <= PI/2
      */
     public static int cos(int f) {

          int sign = 1;
          if ((f > PI_OVER_2) && (f <= PI)) {
               f = PI - f;
               sign = -1;
          } else if ((f > PI_OVER_2) && (f <= (PI + PI_OVER_2))) {
               f = f - PI;
               sign = -1;
          } else if (f > (PI + PI_OVER_2)) {
               f = (PI << 1) - f;
          }

          int sqr = mul(f, f);
          int result = CK1;
          result = mul(result, sqr);
          result -= CK2;
          result = mul(result, sqr);
          result += (1 << 16);
          return result * sign;
     }

     /**
      * Computes tan(f), f is a fixed point number in radians. 0 <= f <= PI/4
      */

     static final int TK1 = 13323;
     static final int TK2 = 20810;
     
     public static int tan(int f) {
          int sqr = mul(f, f);
          int result = TK1;
          result = mul(result, sqr);
          result += TK2;
          result = mul(result, sqr);
          result += (1 << 16);
          result = mul(result, f);
          return result;
     }

     /**
      * Computes atan(f), f is a fixed point number |f| <= 1
      * <p>
      * For the inverse tangent calls, all approximations are valid for |t| <= 1.
      * To compute ATAN(t) for t > 1, use ATAN(t) = PI/2 - ATAN(1/t). For t < -1,
      * use ATAN(t) = -PI/2 - ATAN(1/t).
      */
     public static int atan(int f) {
          int sqr = mul(f, f);
          int result = 1365;
          result = mul(result, sqr);
          result -= 5579;
          result = mul(result, sqr);
          result += 11805;
          result = mul(result, sqr);
          result -= 21646;
          result = mul(result, sqr);
          result += 65527;
          result = mul(result, f);
          return result;
     }

     static final int AS1 = -1228;
     static final int AS2 = 4866;
     static final int AS3 = 13901;
     static final int AS4 = 102939;

     /**
      * Compute asin(f), 0 <= f <= 1
      */

     public static int asin(int f) {
          int fRoot = sqrt((1 << 16) - f);
          int result = AS1;
          result = mul(result, f);
          result += AS2;
          result = mul(result, f);
          result -= AS3;
          result = mul(result, f);
          result += AS4;
          result = PI_OVER_2 - (mul(fRoot, result));
          return result;
     }

     /**
      * Compute acos(f), 0 <= f <= 1
      */
     public static int acos(int f) {
          int fRoot = sqrt((1 << 16) - f);
          int result = AS1;
          result = mul(result, f);
          result += AS2;
          result = mul(result, f);
          result -= AS3;
          result = mul(result, f);
          result += AS4;
          result = mul(fRoot, result);
          return result;
     }

     /**
      * Exponential
      * /** Logarithms:
      *
      * (2) Knuth, Donald E., "The Art of Computer Programming Vol 1",
      * Addison-Wesley Publishing Company, ISBN 0-201-03822-6 ( this comes from
      * Knuth (2), section 1.2.3, exercise 25).
      *
      * http://www.dattalo.com/technical/theory/logs.html
      *
      */

     /**
      * This table is created using base of e.
      *
      * (defun fixedpoint (z) (round (* z (lsh 1 16))))
      *
      * (loop for k from 0 to 16 do (setq z (log (+ 1 (expt 2.0 (- (+ k 1))))))
      * (insert (format "%d\n" (fixedpoint z))))
      */
     static int log2arr[] = { 26573, 14624, 7719, 3973, 2017, 1016, 510, 256,
               128, 64, 32, 16, 8, 4, 2, 1, 0, 0, 0 };

     static int lnscale[] = { 0, 45426, 90852, 136278, 181704, 227130, 272557,
               317983, 363409, 408835, 454261, 499687, 545113, 590539, 635965,
               681391, 726817 };

     public static int ln(int x) {
          // prescale so x is between 1 and 2
          int shift = 0;

          while (x > 1 << 17) {
               shift++;
               x >>= 1;
          }

          int g = 0;
          int d = HALF;
          for (int i = 1; i < 16; i++) {
               if (x > ((1 << 16) + d)) {
                    x = div(x, ((1 << 16) + d));
                    g += log2arr[i - 1]; // log2arr[i-1] = log2(1+d);
               }
               d >>= 1;
          }
          return g + lnscale[shift];
     }   
} 