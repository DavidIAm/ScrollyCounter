package com.skylosian.scrollycounter;

import org.junit.Before;
import org.junit.Test;

import java.lang.Long;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

/**
 * Created by dihnen on 4/23/16.
 */
public class ScrollPositionTest {

    ScrollPosition underTest;

    @Before
    public void setup() {
        underTest = new ScrollPosition(Long.valueOf(1111));
    }

    @Test
    public void scrollingShouldIncreasePosition() {
        Long before = underTest.getPosition();

        underTest = underTest.scroll(Long.valueOf(10));

        assertThat(before, lessThan(underTest.getPosition()));
    }

    @Test
    public void scrollingUpShouldDecreasePosition() {
        Long before = underTest.getPosition();

        underTest = underTest.scroll(Long.valueOf(-45));

        assertThat(underTest.getPosition(), lessThan(before));
    }

    @Test
    public void pageNumberShouldBeOneThousandthRoundDown() {
        underTest = new ScrollPosition(Long.valueOf(999));
        assertThat(underTest.pageNumber(), equalTo(0L));
        underTest = new ScrollPosition(Long.valueOf(1001));
        assertThat(underTest.pageNumber(), equalTo(1L));
    }

    @Test
    public void positionCanGoBelowZero() {
        underTest = new ScrollPosition(0L);

        underTest = underTest.scroll(Long.valueOf(-5));

        assertThat(underTest.getPosition(), lessThan(0L));
    }
    @Test
    public void pageNumberCannotGoBelowZero() {
        underTest = new ScrollPosition(0L);

        underTest = underTest.scroll(Long.valueOf(-5));

        assertThat(underTest.pageNumber(), greaterThan(0L));
    }

    @Test
    public void BitWidthForZeroIsZero() {
        underTest = new ScrollPosition(1L);

        assertThat(underTest.bitwidth(), equalTo(0));
    }

    @Test
    public void BitWidthForOneIsTwo() {
        underTest = new ScrollPosition(1L);
    }

    @Test
    public void shouldAddOneThousandForNextPage() {
        underTest = new ScrollPosition(1L);

        assertThat(underTest.nextPage().getPosition(), equalTo(1L +ScrollPosition.PAGETICKS));
    }

    @Test
    public void shouldHaveBitWidthOfZeroForZero() {
        underTest = new ScrollPosition(1L);

        assertThat(underTest.bitwidth(), equalTo(0));
    }

    @Test
    public void shouldHaveBitWidthOfOneForOne() {
        underTest = new ScrollPosition(1L).nextPage();

        assertThat(underTest.bitwidth(), equalTo(1));
    }

    @Test
    public void shouldHaveBitWidthOfTwoForTwoAndThree() {
        underTest = new ScrollPosition(1L).nextPage().nextPage();
        assertThat(underTest.bitwidth(), equalTo(2));
        underTest = underTest.nextPage();
        assertThat(underTest.bitwidth(), equalTo(2));

    }

    @Test
    public void shouldHaveBitWidthOfThreeForFour() {
        underTest = new ScrollPosition(1L).nextPage().nextPage().nextPage().nextPage();
        assertThat(underTest.bitwidth(), equalTo(3));
    }

    @Test
    public void shouldHaveBitsetZeroWhenOne() {
        underTest = new ScrollPosition(1L).nextPage();
        assertThat(underTest.bitSet(0), equalTo(true));
    }

    @Test
    public void shouldHaveBitsetOneWhenTwo() {
        underTest = new ScrollPosition(1L).nextPage().nextPage();
        assertThat(underTest.bitSet(1), equalTo(true));
    }

    @Test
    public void shouldUseHighPageNumberForNegativeNumber() {
        underTest = new ScrollPosition(-1L);
        assertThat(underTest.pageNumber(), greaterThan((long)Math.pow(2, 48)-10000) );
    }
    @Test
    public void excessivelyHighPageNumberIsZero() {
        underTest = new ScrollPosition(-1L).nextPage();
        assertThat(underTest.pageNumber(), equalTo(0L));
    }
    @Test
    public void prevPageOfZeroIsHigh() {
        underTest = new ScrollPosition(1L).prevPage();
        assertThat(underTest.pageNumber(), greaterThan((long)Math.pow(2, 48)-1000));
    }
    @Test
    public void nextBitChangesOfMaxIsTrue() {
        underTest = new ScrollPosition(-1L);
        assertThat(underTest.nextBitChanges(6), equalTo(true));
    }
    @Test
    public void maxBitSet() {
        underTest = new ScrollPosition(-1L);
        assertThat(underTest.bitSet(6), equalTo(true));
    }
    @Test
    public void percentageOfNegativeIsPercentage() {
        underTest = new ScrollPosition(-500L);
        assertThat(underTest.pageFraction(), equalTo(0.50F));
    }    @Test
    public void percentageOfNegativePageIsPercentage() {
        underTest = new ScrollPosition(-1500L);
        assertThat(underTest.pageFraction(), equalTo(0.50F));
    }
}