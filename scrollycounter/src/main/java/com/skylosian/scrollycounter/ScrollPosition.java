package com.skylosian.scrollycounter;

import android.graphics.Shader;

import com.google.common.math.LongMath;

import java.math.RoundingMode;

/**
 * Created by dihnen on 4/23/16.
 */
public class ScrollPosition {
    private Long mils = 0L;
    protected static Long PAGETICKS = Long.valueOf(1000);
    protected static Long MAXPAGE = (long)Math.pow(2, 48);

    public ScrollPosition(Long startPosition) {
        mils = startPosition;
    }

    public ScrollPosition scroll(float mils) {
        return scroll(Long.valueOf(Math.round(mils)));
    }

    public ScrollPosition scroll(Long mils) {
        return new ScrollPosition(this.mils + mils);
    }

    public Long getPosition() {
        return mils;
    }

    public Long pageNumber() {
        Long pageRender = mils;// < 0 ? ((MAXPAGE*PAGETICKS)+mils) : mils;
        return Double.valueOf(Math.floor((float)pageRender / (float)PAGETICKS)).longValue();
    }

    public int bitwidth() {
        if (pageNumber() == 0) return 0;
        return (int)(LongMath.log2(Long.highestOneBit(Math.abs(pageNumber())),RoundingMode.UP)+1);
    }
    public int prevBitwidth() {
        return prevPage().bitwidth();
    }

    // like a compare, returns 0 if there is no difference for the specified bit.
    // if its changing into a 0, it was one, and it returns -1
    // if its changing into a 1, it was 0, and it returns 1
    public boolean prevBitChanges(int bit) {
        if (getPosition() < 0) {
            return this.bitSet(bit) ^ this.nextPage().bitSet(bit);
        } else {
            return this.prevPage().bitSet(bit) ^ this.bitSet(bit);
        }
    }
    public boolean nextBitChanges(int bit) {
        if (getPosition() < 0) {
            return this.bitSet(bit) ^ this.prevPage().bitSet(bit);
        } else {
            return this.bitSet(bit) ^ this.nextPage().bitSet(bit);
        }
    }

    public boolean nextBitValue(int bit) {
        return 0 != (Math.abs((pageNumber() + 1)) & (1L << bit));
    }

    public boolean bitSet(int bit) {
        return 0 != (Math.abs(pageNumber()) & (1L << bit));
    }


    public boolean nextBitSet(int bit) {
        return this.nextPage().bitSet(bit);
    }

    public float pageFraction() {
        float fract = Math.abs(mils.floatValue() % 1000) / 1000;
        if (mils < 0) {
            fract = 1 - fract;
        }
        return fract;
    }

    public ScrollPosition nextPage() {
        return scroll(PAGETICKS);
    }
    public ScrollPosition prevPage() {
        return scroll(-PAGETICKS);
    }

    public Shader shaderByNegative(Shader negative, Shader notNegative) {
        if (getPosition() < 0) {
            return negative;
        }
        return notNegative;
    }

    public Shader shaderByBit(int bit, Shader set, Shader notset) {
        if (bitSet(bit)) {
            return set;
        }
        return notset;
    }

    public static Shader shaderByChange(int bit, ScrollPosition former, ScrollPosition latter, Shader upwards, Shader downwards, Shader top, Shader bottom) {
        if (former.bitSet(bit) ^ latter.bitSet(bit)) {
            return former.shaderByBit(bit, downwards, upwards);
        }
        return former.shaderByBit(bit, top, bottom);
    }
}
