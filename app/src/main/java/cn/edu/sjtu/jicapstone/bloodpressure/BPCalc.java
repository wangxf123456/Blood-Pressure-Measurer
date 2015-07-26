package cn.edu.sjtu.jicapstone.bloodpressure;

import java.util.Arrays;

import math.jwave.exceptions.JWaveException;
import math.jwave.transforms.BasicTransform;
import math.jwave.transforms.FastWaveletTransform;
import math.jwave.transforms.wavelets.Wavelet;
import math.jwave.transforms.wavelets.haar.Haar1Orthogonal;

public class BPCalc {
    private BasicTransform transform;
    private Wavelet wavelet;
    private float sbp = -1;
    private float dbp = -1;

    public BPCalc() {
        wavelet = new Haar1Orthogonal();
        transform = new FastWaveletTransform(wavelet);
    }

    public float getSbp() {
        return sbp;
    }

    public float getDbp() {
        return dbp;
    }

    public void calculate(int[] rdata, float sampleRateHz) {
        // First append last data point until the number
        // of elements is 2^n		int dataLength = rdata.length;
        int dataLength = rdata.length;
        System.out.printf("%d points sent at %f rate\n", rdata.length, sampleRateHz);
        System.out.println("Data: " + Arrays.toString(rdata));
        int length2n = 2;
        for (int i = 0; i < 64; i++) {
            if ((2 << i) > dataLength) {
                length2n = (2 << i);
                break;
            }
        }
        double[] data = new double[length2n];
        for (int i = 0; i < rdata.length; i++) {
            data[i] = (double)rdata[i];
        }
        for (int i = rdata.length; i < length2n; i++) {
            data[i] = (double)rdata[rdata.length - 1];
        }

        // Do DWT
        double[] arrHilbert;
        try {
            arrHilbert = transform.forward(data);
        } catch (JWaveException e) {
            sbp = -1;
            dbp = -1;
            return;
        }

        // System.out.println(Arrays.toString(arrHilbert));

        // determine heart rate corresponded span
        int lb = 0; // lb is also the number of points in range.
        int ub = 0;
        int k = 0; // span of each point
        for (int i = 0; i < 64; i++) {
            lb = 1 << i;
            ub = 1 << (i+1);
            if (lb <= ((double)length2n)/sampleRateHz &&
                    ub > ((double)length2n)/sampleRateHz) {
                k = length2n / lb;
                ub -= 1;
                break;
            }
        }
        double maxAmp = 0;
        double[] hilDat = null;

        double leastR2 = 1;

        System.out.println("lb: " + lb);

        for (int lbl = lb / 2; lbl <= lb * 2; lbl *= 2) {
            // Calculate noise level and chop off trailing zeros.
            int kl = length2n / lbl;
            int n = dataLength / kl ;
            double[] hilDatLocal = new double[n];
            System.arraycopy(arrHilbert, lbl, hilDatLocal, 0, n);
            // System.out.println("Hilbert: " + Arrays.toString(hilDatLocal));
            double[] pArr = new double[hilDatLocal.length];
            for (int i = 0; i < hilDatLocal.length; i++) {
                pArr[i] = rdata[i*kl];
            }

            LRResult result = LinearRegression(pArr, hilDatLocal);

            double[] signalArr = pArr;
            for (int i = 0; i < hilDatLocal.length; i++) {
                signalArr[i] = hilDatLocal[i] - (pArr[i] * result.b0 + result.b1);
            }

            // System.out.println("signal: " + Arrays.toString(signalArr));
            // Determine max amplitude. Max amplitude is determined by
            // average deviation of amplitude from noise level for
            // up and down.

            int maxAmpIndex = 0;
            double maxAmpLocal = 0;
            for (int i = 0; i < hilDatLocal.length - 1; i++) {
                if (Math.abs(signalArr[i]) +
                        Math.abs(signalArr[i+1]) > maxAmpLocal) {
                    maxAmpLocal = Math.abs(signalArr[i]) +
                            Math.abs(signalArr[i+1]);
                    maxAmpIndex = i;
                }
            }
            System.out.println("max at "+maxAmpIndex);
            maxAmpLocal /= 2.0;

            if (Math.abs(result.r2) < leastR2 ) {
                maxAmp = maxAmpLocal;
                leastR2 = Math.abs(result.r2);
                hilDat = signalArr;
                k = kl;
            }
            System.out.printf("lbl: %d, r2: %f\n", lbl, Math.abs(result.r2));
        }

        System.out.println("Best Hilbert " + Arrays.toString(hilDat));

        // Calculate sbp, which is defined as the first point which
        // has itself and a consecutive point both larger than SBP_FACTOR
        for (int i = 1; i < hilDat.length - 1; i++) {
            if (Math.abs(hilDat[i]) > Parameters.SBP_FACTOR * maxAmp
                    && Math.abs(hilDat[i + 1]) > Parameters.SBP_FACTOR * maxAmp) {
                System.out.println("sbp at " + i);
                if (i == 0) {
                    // Case when highest pressure is not high enough.
                    sbp = -3.0f;
                }
                else {
                    double frac = (Parameters.SBP_FACTOR * maxAmp - Math.abs(hilDat[i-1])) /
                            (Math.abs(hilDat[i]) - Math.abs(hilDat[i-1]));
                    if (frac < 0 || frac > 1) {
                        frac = 0.5d;
                    }
                    sbp = rdata[(int)((i - 1 + frac) * k)];
                }
                break;
            }
        }

        // Calculate dbp, which is defined as the last point which
        // has itself and a previous point both larger than DBP_FACTOR
        for (int i = hilDat.length - 1 ; i > 1; i--) {
            if (Math.abs(hilDat[i]) > Parameters.DBP_FACTOR * maxAmp
                    && Math.abs(hilDat[i - 1]) > Parameters.DBP_FACTOR * maxAmp) {
                System.out.println("dbp at " + i);
                if (i == hilDat.length - 1) {
                    // Case when lowest pressure is not low enough.
                    dbp = -3.0f;
                }
                else {
                    double frac = (Parameters.DBP_FACTOR * maxAmp - Math.abs(hilDat[i-1])) /
                            (Math.abs(hilDat[i])- Math.abs(hilDat[i-1]));
                    if (frac < 0 || frac > 1) {
                        frac = 0.5d;
                    }
                    dbp = rdata[(int)((i + frac) * k)];
                }
                break;
            }
        }
    }


    private static class LRResult {
        double b0;
        double b1;
        double r2;
        public LRResult(double b0, double b1, double r2) {
            this.b0 = b0;
            this.b1 = b1;
            this.r2 = r2;
        }
    }

    private static LRResult LinearRegression(double[] x, double[] y) {
        double sumx = 0;
        double sumy = 0;
        int n = x.length;
        for (int i = 0; i < n; i++) {
            sumx += x[i];
            sumy += y[i];
        }

        double xbar = sumx / n;
        double ybar = sumy / n;

        double xxbar = 0;
        double xybar = 0;
        double yybar = 0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
            yybar += (y[i] - xbar) * (y[i] - ybar);
        }

        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;

        double ssr = 0.0;      // regression sum of squares
        for (int i = 0; i < n; i++) {
            double fit = beta1*x[i] + beta0;
            ssr += (fit - ybar) * (fit - ybar);
        }
        double R2 = ssr / yybar;
        return new LRResult(beta1, beta0, R2);
    }
}
