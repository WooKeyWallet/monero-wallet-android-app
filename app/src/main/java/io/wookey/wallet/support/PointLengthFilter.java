package io.wookey.wallet.support;

import android.text.InputFilter;
import android.text.Spanned;

public class PointLengthFilter implements InputFilter {

    private final int mMax;

    public PointLengthFilter(int max) {
        mMax = max;
    }

    /**
     * @param source 将要插入的字符串，来自键盘输入、粘贴
     * @param start  source的起始位置，为0（暂时没有发现其它值的情况）
     * @param end    source的长度
     * @param dest   EditText中已经存在的字符串
     * @param dstart 插入点的位置
     * @param dend   插入点的结束位置，一般情况下等于dstart；如果选中一段字符串（这段字符串将会被替换），dstart的值就插入点的结束位置
     * @return
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        String sc = source.toString();
        int sourceIndex = sc.indexOf(".");
        int destIndex = dest.toString().indexOf(".");

        int destLen = dest.length() - (destIndex + 1);
        int sourceLen = end - (sourceIndex + 1);

        if (destIndex < 0) {
            if (sourceIndex < 0) {
                return null; // keep original
            } else {
                if (dstart == 0 && sourceIndex == 0) {
                    return "";
                }
                destLen = destLen - dend - 1;
            }
        } else {
            if (sourceIndex < 0) {
                if (destIndex < dstart) {
                    destLen = destLen - (dend - dstart);
                } else {
                    return null; // keep original
                }
            } else {
                return dest.subSequence(dstart, dend);
            }
        }

        int keep = mMax - destLen;
        if (keep <= 0) {
            return "";
        } else if (keep >= sourceLen) {
            return null; // keep original
        } else {
            keep += start + Math.max(sourceIndex, 0);
            if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                --keep;
                if (keep == start) {
                    return "";
                }
            }
            return source.subSequence(start, keep);
        }
    }

    /**
     * @return the maximum length enforced by this input filter
     */
    public int getMax() {
        return mMax;
    }
}
