//
// $Id$
// $Header$
// $Log$
// Revision 1.2  2005/03/01 02:13:13  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.util.*;

// Sun class can figure out its own geocentric position
public class Sun
{
	// Compute unit vector pointing toward the sun
    static Vector3D getVector(Date date) {

		// Julian centuries is the date format used in published astronomical algorithms
		double julianCenturies = dateToJulianCenturies(date);
		
		double latitude = degToRad(calcSunDeclination(julianCenturies));
		// Longitude should be modified by Greenwich sidereal time
		double gmst = greenwichMeanSiderealTime(julianCenturies);
		// System.out.println("gmst = " + gmst);
		double rightAscension = calcSunRtAscension(julianCenturies);
		// System.out.println("right ascension = " + rightAscension);
		double longitude = degToRad(rightAscension - gmst);
		
		// Convert latitude/longitude to xyz cartesian coordinates
		double y = Math.asin(latitude);
		double latcoeff = Math.cos(y);
		double z = Math.cos(longitude) * latcoeff;
		double x = -Math.sin(longitude) * latcoeff;
		
		Vector3D answer = new Vector3D(x, y, z);
		return answer;
    }
	
    // from http://www.pcigeomatics.com/cgi-bin/pcihlp/AVHRRAD%7CDETAILS%7CANGLE+GENERATION%7CCALCULATIONS
    // Step 3.  Compute the GMST value at 0h UT. This value is in seconds.
    
    //          GMST = 24110.54841 + 8640184.812866 * Tu + 0.093104 * Tu * Tu +
    //                 (-6.2e-6) * Tu * Tu * Tu;
    
    // Step 4.  Now add the appropriate mean sidereal time interval to GMST
    //          using the fractional day value.
    
    //          GMST = GMST + FracMJD * SecPerDay * SolarSiderealDayRatio
    
    // Step 5.  Reduce the GMST value to between 0 and 86400 seconds.
    //          Then convert it to an angular measure (degrees).
    
    //          GMST = GMST / SecPerDay * 360.0 degrees
    static double greenwichMeanSiderealTime(double julianCenturies) {
		// Compute the GMST value at 0h UT. This value is in seconds.
		double gmst = 24110.54841 + 8640184.812866 * julianCenturies + 0.093104 * julianCenturies * julianCenturies + (-6.2e-6) * julianCenturies * julianCenturies * julianCenturies; // seconds
		
		// double T = (jd - 2451545.0)/36525.0;
		double julianDay = (julianCenturies * 36525.0) + 2451545.0;
		double fracDay = julianDay - (int)julianDay;
		fracDay *= 1.00273790934; // solar to sidereal
								  // System.out.println("fraction of day = " + fracDay);
		
		gmst /= 86400; // convert to revolutions
		double fracGmst = gmst - (int)gmst + 0.5 - fracDay;
		while (fracGmst < 0) fracGmst += 1.0;
		while (fracGmst > 1) fracGmst -= 1.0;
		return 360 * fracGmst;
    }
	
    static double dateToJulianCenturies(Date date) {
		
		SimpleTimeZone ut = new SimpleTimeZone(0, "GMT");
		Calendar calendar = new GregorianCalendar(ut);
		calendar.setTime(date);
		
		double day = calendar.get(Calendar.DAY_OF_MONTH);
		double month = calendar.get(Calendar.MONTH) + 1;
		double year = calendar.get(Calendar.YEAR);
		
		day += (1.0/24.0) * calendar.get(Calendar.HOUR_OF_DAY);
		day += (1.0/(24.0*60.0)) * calendar.get(Calendar.MINUTE);
		day += (1.0/(24.0*3600.0)) * calendar.get(Calendar.SECOND);
		day += (1.0/(24.0*3600000.0)) * calendar.get(Calendar.MILLISECOND);
		
		// System.out.println("" + year + " " + month + " " + day);
		
		double julianDay = calcJD(year, month, day);
		// System.out.println("Julian day = " + julianDay); // correct
		return calcTimeJulianCent(julianDay);
    }
	
    // From http://www.srrb.noaa.gov/highlights/sunrise/program.txt
	
    //***********************************************************************/
    //***********************************************************************/
    //*									    */
    //*This section contains subroutines used in calculating solar position */
    //*							        	    */
    //***********************************************************************/
    //***********************************************************************/
	
    // Convert radian angle to degrees
	
    private static double radToDeg(double angleRad) 
{
		return (180.0 * angleRad / Math.PI);
}

//*********************************************************************/

// Convert degree angle to radians

private static double degToRad(double angleDeg) 
{
	return (Math.PI * angleDeg / 180.0);
}

//*********************************************************************/
//***********************************************************************/
//* Name:    calcDayOfYear					      	*/
//* Type:    Private Static Double						*/
//* Purpose: Finds numerical day-of-year from mn, day and lp year info  */
//* Arguments:		       					       */
//*   month: January = 1							   */
//*   day  : 1 - 31									*/
//*   lpyr : 1 if leap year, 0 if not						*/
//* Return value:										*/
//*   The numerical day of year							*/
//***********************************************************************/

private static double calcDayOfYear(double mn, double dy, boolean lpyr) 
{
	double k = (lpyr ? 1 : 2);
	double doy = Math.floor((275 * mn)/9) - k * Math.floor((mn + 9)/12) + dy -30;
	return doy;
}


//***********************************************************************/
//* Name:    calcDayOfWeek								*/
//* Type:    Private Static Double									*/
//* Purpose: Derives weekday from Julian Day					*/
//* Arguments:										*/
//*   juld : Julian Day									*/
//* Return value:										*/
//*   String containing name of weekday						*/
//***********************************************************************/

private static String calcDayOfWeek(double juld)
{
	double A = (juld + 1.5) % 7;
	String DOW = (A==0)?"Sunday":(A==1)?"Monday":(A==2)?"Tuesday":(A==3)?"Wednesday":(A==4)?"Thursday":(A==5)?"Friday":"Saturday";
	return DOW;
}


//***********************************************************************/
//* Name:    calcJD									*/
//* Type:    Private Static Double									*/
//* Purpose: Julian day from calendar day						*/
//* Arguments:										*/
//*   year : 4 digit year								*/
//*   month: January = 1								*/
//*   day  : 1 - 31									*/
//* Return value:										*/
//*   The Julian day corresponding to the date					*/
//* Note:											*/
//*   Number is returned for start of day.  Fractional days should be	*/
//*   added later.									*/
//***********************************************************************/

private static double calcJD(double year, double month, double day)
{
	if (month <= 2) {
	    year -= 1;
	    month += 12;
	}
	double A = Math.floor(year/100);
	double B = 2 - A + Math.floor(A/4);
	
	double JD = Math.floor(365.25*(year + 4716)) + Math.floor(30.6001*(month+1)) + day + B - 1524.5;
	return JD;
}


//***********************************************************************/
//* Name:    calcTimeJulianCent							*/
//* Type:    Private Static Double									*/
//* Purpose: convert Julian Day to centuries since J2000.0.			*/
//* Arguments:										*/
//*   jd : the Julian Day to convert						*/
//* Return value:										*/
//*   the T value corresponding to the Julian Day				*/
//***********************************************************************/

private static double calcTimeJulianCent(double jd)
{
	double T = (jd - 2451545.0)/36525.0;
	return T;
}


//***********************************************************************/
//* Name:    calcJDFromJulianCent							*/
//* Type:    Private Static Double									*/
//* Purpose: convert centuries since J2000.0 to Julian Day.			*/
//* Arguments:										*/
//*   t : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   the Julian Day corresponding to the t value				*/
//***********************************************************************/

private static double calcJDFromJulianCent(double julianCenturies)
{
	double JD = julianCenturies * 36525.0 + 2451545.0;
	return JD;
}


//***********************************************************************/
//* Name:    calGeomMeanLongSun							*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the Geometric Mean Longitude of the Sun		*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   the Geometric Mean Longitude of the Sun in degrees			*/
//***********************************************************************/

private static double calcGeomMeanLongSun(double julianCenturies)
{
	double L0 = 280.46646 + julianCenturies * (36000.76983 + 0.0003032 * julianCenturies);
	while(L0 > 360.0)
	{
		L0 -= 360.0;
	}
	while(L0 < 0.0)
	{
		L0 += 360.0;
	}
	return L0;		// in degrees
}


//***********************************************************************/
//* Name:    calGeomAnomalySun							*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the Geometric Mean Anomaly of the Sun		*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   the Geometric Mean Anomaly of the Sun in degrees			*/
//***********************************************************************/

private static double calcGeomMeanAnomalySun(double julianCenturies)
{
	double M = 357.52911 + julianCenturies * (35999.05029 - 0.0001537 * julianCenturies);
	return M;		// in degrees
}

//***********************************************************************/
//* Name:    calcEccentricityEarthOrbit						*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the eccentricity of earth's orbit			*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   the unitless eccentricity							*/
//***********************************************************************/


private static double calcEccentricityEarthOrbit(double julianCenturies)
{
	double e = 0.016708634 - julianCenturies * (0.000042037 + 0.0000001267 * julianCenturies);
	return e;		// unitless
}

//***********************************************************************/
//* Name:    calcSunEqOfCenter							*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the equation of center for the sun			*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   in degrees										*/
//***********************************************************************/


private static double calcSunEqOfCenter(double julianCenturies)
{
	double m = calcGeomMeanAnomalySun(julianCenturies);
	
	double mrad = degToRad(m);
	double sinm = Math.sin(mrad);
	double sin2m = Math.sin(mrad+mrad);
	double sin3m = Math.sin(mrad+mrad+mrad);
	
	double C = sinm * (1.914602 - julianCenturies * (0.004817 + 0.000014 * julianCenturies)) + sin2m * (0.019993 - 0.000101 * julianCenturies) + sin3m * 0.000289;
	return C;		// in degrees
}

//***********************************************************************/
//* Name:    calcSunTrueLong								*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the true longitude of the sun				*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   sun's true longitude in degrees						*/
//***********************************************************************/


private static double calcSunTrueLong(double julianCenturies)
{
	double l0 = calcGeomMeanLongSun(julianCenturies);
	double c = calcSunEqOfCenter(julianCenturies);
	
	double O = l0 + c;
	return O;		// in degrees
}

//***********************************************************************/
//* Name:    calcSunTrueAnomaly							*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the true anamoly of the sun				*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   sun's true anamoly in degrees							*/
//***********************************************************************/

private static double calcSunTrueAnomaly(double julianCenturies)
{
	double m = calcGeomMeanAnomalySun(julianCenturies);
	double c = calcSunEqOfCenter(julianCenturies);
	
	double v = m + c;
	return v;		// in degrees
}

//***********************************************************************/
//* Name:    calcSunRadVector								*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the distance to the sun in AU				*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   sun radius vector in AUs							*/
//***********************************************************************/

private static double calcSunRadVector(double julianCenturies)
{
	double v = calcSunTrueAnomaly(julianCenturies);
	double e = calcEccentricityEarthOrbit(julianCenturies);
	
	double R = (1.000001018 * (1 - e * e)) / (1 + e * Math.cos(degToRad(v)));
	return R;		// in AUs
}

//***********************************************************************/
//* Name:    calcSunApparentLong							*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the apparent longitude of the sun			*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   sun's apparent longitude in degrees						*/
//***********************************************************************/

private static double calcSunApparentLong(double julianCenturies)
{
	double o = calcSunTrueLong(julianCenturies);
	
	double omega = 125.04 - 1934.136 * julianCenturies;
	double lambda = o - 0.00569 - 0.00478 * Math.sin(degToRad(omega));
	return lambda;		// in degrees
}

//***********************************************************************/
//* Name:    calcMeanObliquityOfEcliptic						*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the mean obliquity of the ecliptic			*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   mean obliquity in degrees							*/
//***********************************************************************/

private static double calcMeanObliquityOfEcliptic(double julianCenturies)
{
	double seconds = 21.448 - julianCenturies*(46.8150 + julianCenturies*(0.00059 - julianCenturies*(0.001813)));
	double e0 = 23.0 + (26.0 + (seconds/60.0))/60.0;
	return e0;		// in degrees
}

//***********************************************************************/
//* Name:    calcObliquityCorrection						*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the corrected obliquity of the ecliptic		*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   corrected obliquity in degrees						*/
//***********************************************************************/

private static double calcObliquityCorrection(double julianCenturies)
{
	double e0 = calcMeanObliquityOfEcliptic(julianCenturies);
	
	double omega = 125.04 - 1934.136 * julianCenturies;
	double e = e0 + 0.00256 * Math.cos(degToRad(omega));
	return e;		// in degrees
}

//***********************************************************************/
//* Name:    calcSunRtAscension							*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the right ascension of the sun				*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   sun's right ascension in degrees						*/
//***********************************************************************/

private static double calcSunRtAscension(double julianCenturies)
{
	double e = calcObliquityCorrection(julianCenturies);
	double lambda = calcSunApparentLong(julianCenturies);
	
	double tananum = (Math.cos(degToRad(e)) * Math.sin(degToRad(lambda)));
	double tanadenom = (Math.cos(degToRad(lambda)));
	double alpha = radToDeg(Math.atan2(tananum, tanadenom));
	return alpha;		// in degrees
}

//***********************************************************************/
//* Name:    calcSunDeclination							*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the declination of the sun				*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   sun's declination in degrees							*/
//***********************************************************************/

private static double calcSunDeclination(double julianCenturies)
{
	double e = calcObliquityCorrection(julianCenturies);
	double lambda = calcSunApparentLong(julianCenturies);
	
	double sint = Math.sin(degToRad(e)) * Math.sin(degToRad(lambda));
	double theta = radToDeg(Math.asin(sint));
	return theta;		// in degrees
}

//***********************************************************************/
//* Name:    calcEquationOfTime							*/
//* Type:    Private Static Double									*/
//* Purpose: calculate the difference between true solar time and mean	*/
//*		solar time									*/
//* Arguments:										*/
//*   julianCenturies : number of Julian centuries since J2000.0				*/
//* Return value:										*/
//*   equation of time in minutes of time						*/
//***********************************************************************/

private static double calcEquationOfTime(double julianCenturies)
{
	double epsilon = calcObliquityCorrection(julianCenturies);
	double l0 = calcGeomMeanLongSun(julianCenturies);
	double e = calcEccentricityEarthOrbit(julianCenturies);
	double m = calcGeomMeanAnomalySun(julianCenturies);
	
	double y = Math.tan(degToRad(epsilon)/2.0);
	y *= y;
	
	double sin2l0 = Math.sin(2.0 * degToRad(l0));
	double sinm   = Math.sin(degToRad(m));
	double cos2l0 = Math.cos(2.0 * degToRad(l0));
	double sin4l0 = Math.sin(4.0 * degToRad(l0));
	double sin2m  = Math.sin(2.0 * degToRad(m));
	
	double Etime = y * sin2l0 - 2.0 * e * sinm + 4.0 * e * y * sinm * cos2l0
	    - 0.5 * y * y * sin4l0 - 1.25 * e * e * sin2m;
	
	return radToDeg(Etime)*4.0;	// in minutes of time
}
}
