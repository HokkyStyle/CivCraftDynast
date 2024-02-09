
package com.dynast.civcraft.util;

import java.text.DecimalFormat;

public class DecimalHelper {

	
	public static String formatPercentage(double d) {
		DecimalFormat df = new DecimalFormat();
		return df.format(d*100)+"%";
	}
	
}
