package mceliece;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class gfPoly {

	public int degree;
	public int coeffs;
	public static gfPoly gfZero = new gfPoly(0);
	public static gfPoly gfOne = new gfPoly(1);
	public static gfPoly gfZ = new gfPoly("10", 1);

	public static Scanner scan = new Scanner(System.in);

	public gfPoly(int coeff) {
		coeffs = coeff;
		if (coeff == 0)
			degree = 0;
		else
			degree = (int) (Math.log(coeff) / Math.log(2));
	}

	public gfPoly(String coeff, int deg) {
		coeffs = Integer.parseInt(coeff, 2);
		degree = deg;
	}

	public static gfPoly getIrredPoly(int deg) {
		gfPoly irredPoly;
		do {
			irredPoly = getRandPoly(deg);

			if (irredPoly.coeffs % 2 == 0)
				irredPoly.coeffs = irredPoly.coeffs ^ 1;

		} while (!irredPoly.isIrreducible());

		return irredPoly;
	}

	public gfPoly(gfPoly poly, int shift) {
		coeffs = poly.coeffs << shift;
		degree = poly.degree + shift;
	}

	public boolean isIrreducible() { 
		// Polynomial factor = new Polynomial(10, 1);
		gfPoly square = new gfPoly("10", 2);

		/* i <= this.degree/2
      	   for(int i = 0; i < this.degree; i++) {
         	   square = multiply(square, square);
         	   square = getRemainder(square, this);
           }
      	   if (square.coefficients.equals(new BigInteger("2")))
      	   	   return true;
      	   return false;
		 */

		for (int i = 0; i < this.degree / 2; i++) {
			square = gf_multiply(square, square, this);

			gfPoly tempSquare = new gfPoly(square.coeffs ^ 2);

			if (!gcd(this, tempSquare).equals(gfOne))
				return false;
		}

		return true;
	}

	public static gfPoly getRandPoly(int deg) {
		int coeffs;

		if (deg != 0)
			coeffs = (int) (Math.random() * ((int) Math.pow(2, deg + 1) - (int) Math.pow(2, deg)) + (int) Math.pow(2, deg));
		else
			coeffs = (int) (Math.random() * 2);

		return new gfPoly(coeffs);
	}

	// gets result of polyOne - polyTwo
	public static gfPoly gf_add(gfPoly polyOne, gfPoly polyTwo)	{
		gfPoly result = new gfPoly(0);

		// result.coefficients = polyOne.coefficients ^ polyTwo.coefficients;
		result.coeffs = polyOne.coeffs ^ polyTwo.coeffs;
		getDegree(result);

		return result;
	}

	public static gfPoly gf_multiply(gfPoly pOne, gfPoly pTwo, gfPoly irredPoly) {
		int resultCoeffs = 0;
		getDegree(pOne);
		getDegree(pTwo);

		for (int i = 0; i <= pTwo.degree; i++) {
			if ((pTwo.coeffs >> i & 1) == 1) {
				// resultCoeffs ^= polyOne.coefficients << i;
				resultCoeffs = resultCoeffs ^ pOne.coeffs << i;
			}
		}

		gfPoly result = new gfPoly(resultCoeffs);
		result = getRemainder(result, irredPoly);
		return result; 
	}

	public static gfPoly multiply(gfPoly pOne, gfPoly pTwo) {
		int resultCoeffs = 0;
		getDegree(pOne);
		getDegree(pTwo);

		for (int i = 0; i <= pTwo.degree; i++) {
			if ((pTwo.coeffs >> i & 1) == 1) {
				// resultCoeffs ^= polyOne.coefficients << i;
				resultCoeffs = resultCoeffs ^ pOne.coeffs << i;
			}
		}

		gfPoly result = new gfPoly(resultCoeffs);
		return result; 
	}

	public boolean equals(gfPoly other)	{
		if (this.coeffs == other.coeffs)
			return true;
		return false;
	}

	public static gfPoly getRemainder(gfPoly numerator, gfPoly denominator) {  
		gfPoly num = new gfPoly(numerator.coeffs);
		gfPoly denom = new gfPoly(denominator.coeffs);

		if (!denom.equals(gfZero)) {        
			if (denom.equals(gfOne) || num.equals(denom) || num.equals(gfZero))
				return gfZero;

			if (num.degree >= denom.degree) {
				gfPoly add = new gfPoly(denom, num.degree - denom.degree);

				num.coeffs = num.coeffs ^ (add.coeffs);

				// getDegree(num);
				return getRemainder(num, denom);
			}
			else {
				return num;
			}
		}

		return gfZero;
	}

	public static gfPoly gcd(gfPoly polyOne, gfPoly polyTwo) {
		gfPoly pOne = polyOne;
		gfPoly pTwo = polyTwo;
		gfPoly remainder = getRemainder(pOne, pTwo);

		while (!remainder.equals(gfZero)) {
			pOne = pTwo;
			pTwo = remainder;

			//getDegree(pTwo);
			remainder = getRemainder(pOne, pTwo);
		}
		return pTwo;
	}

	public String toString() {
		String polynomial = "";

		if (this.equals(gfZero)) {
			polynomial = "0";
		} else {
			for (int printing = 0; printing <= degree; printing++) {
				int coefficient = (coeffs >> printing) & 1;

				if (coefficient == 1) {
					if (printing == 0)
						polynomial += coefficient;	
					if (printing != 0)
						polynomial += "z^" + printing;
					if (printing != degree)
						polynomial += " + ";
				}
			}
		}

		return polynomial;
	}

	public static void getDegree(gfPoly poly) {
		if (poly.coeffs == 0)
			poly.degree = 0;
		else
			poly.degree = (int) (Math.log(poly.coeffs) / Math.log(2));
	}

	public static gfPoly getQuotient(gfPoly num, gfPoly denom) {
		gfPoly numerator = new gfPoly(num, 0);
		gfPoly denominator = new gfPoly(denom, 0);

		/* 
		 * System.out.println(numerator.toString() + "/" + denominator.toString());
		 * System.out.println(numerator.degree + ", " + denominator.degree);
		 */

		getDegree(numerator);
		getDegree(denominator);

		if (numerator.degree < denominator.degree)
			return gfZero;

		gfPoly quotient = new gfPoly(0);
		quotient.degree = numerator.degree - denominator.degree;

		gfPoly shiftedDenominator;
		while (numerator.degree > denominator.degree) {
			// quotient.coefficients ^= 1 << numerator.degree - denominator.degree;
			quotient.coeffs = quotient.coeffs ^ (1 << numerator.degree - denominator.degree);
			shiftedDenominator = new gfPoly(denominator, numerator.degree - denominator.degree);
			// numerator.coefficients ^= shiftedDenominator.coefficients;
			numerator.coeffs = numerator.coeffs ^ (shiftedDenominator.coeffs);
			getDegree(numerator);
		}

		if(numerator.degree == denominator.degree && numerator.coeffs != 0)
			quotient.coeffs = quotient.coeffs ^ 1;

		return quotient;
	}

	public static gfPoly getModularInverse(gfPoly poly, gfPoly mod) {
		return getModularInverseHelper(poly, mod).get(1);
	}

	public static List<gfPoly> getModularInverseHelper(gfPoly pOne, gfPoly pTwo) {
		gfPoly poly = new gfPoly(pOne, 0);
		gfPoly mod = new gfPoly(pTwo, 0);

		List<gfPoly> result = new ArrayList<gfPoly>();

		if (mod.equals(gfZero)) {
			result.add(poly);
			result.add(gfOne);
			result.add(gfZero);
		} else {
			// System.out.println("mod isn't zero yet");
			result = getModularInverseHelper(pTwo, getRemainder(pOne, pTwo));

			gfPoly inverse = result.get(2); 
			gfPoly second = getQuotient(poly, mod);

			@SuppressWarnings("unused")
			gfPoly temp = second;
			second = multiply(second, result.get(2));

			second = gf_add(result.get(1), second);
			result.set(2, second);
			result.set(1, inverse);
		}

		return result;
	}

	public static gfPoly toPower(gfPoly poly, gfPoly mod, int power) {
		gfPoly result = gfOne;
		for (int i = 0; i < power; i++)	{
			result = getRemainder(multiply(poly, result), mod);
			getDegree(result);
		}
		return result;
	}

	public static gfPoly toPower(gfPoly poly, int power) {
		gfPoly result = gfOne;
		for (int i = 0; i < power; i++) {
			result = multiply(poly, result);
			getDegree(result);
		}
		return result;
	}

	/*
	 * public static gfPoly eval(gfPoly outer, gfPoly inner, gfPoly mod) {
	 * 		gfPoly result = new gfPoly(0);
	 * 		gfPoly multPoly;
	 * 		for (int i = 0; i <= outer.degree; i++) {
	 * 			multPoly = gfPoly.toPower(inner, mod, i);
	 * 			multPoly = gfPoly.gf_multiply(multPoly, outer.coefficients[i], mod);
	 * 			result = gfPoly.gf_add(result, multPoly);
	 * 		}
	 * 		return result;
	 * }
	 */

	public static gfPoly calcSqrt(gfPoly poly, gfPoly mod, int extDegree) {
		// return toPower(poly, mod, (int) Math.pow(2, extDegree * mod.degree - 1));
		gfPoly even = new gfPoly(0);

		for (int i = 0; i <= poly.degree; i += 2)
			even.coeffs ^= (poly.coeffs  & (1 << i));

		getDegree(even);
		gfPoly odd = new gfPoly(0);

		for (int i = 1; i <= poly.degree; i += 2)		
			odd.coeffs ^= (poly.coeffs  & (1 << i));

		getDegree(odd);
		gfPoly sqrtX = new gfPoly("10", 2);

		for (int i = 0; i < extDegree - 1; i++)
			sqrtX = toPower(sqrtX, mod, 2);

		gfPoly sqrtEven = new gfPoly(0);

		for (int i = 0; i <= even.degree; i += 2)
			sqrtEven.coeffs ^= even.coeffs >> (i/2) & (1 << i/2);

		getDegree(sqrtEven);
		odd.coeffs = odd.coeffs >> 1;

			getDegree(odd);
			// odd.coefficients = odd.coefficients >> 1;
			gfPoly sqrtOdd = new gfPoly(0);

			for (int i = 0; i <= odd.degree; i += 2)
				sqrtOdd.coeffs ^= odd.coeffs >> (i/2) & (1 << i/2);

			getDegree(sqrtOdd);
			gfPoly squareRoot = new gfPoly(sqrtEven.coeffs ^ multiply(sqrtX, sqrtOdd).coeffs);

			return getRemainder(squareRoot, mod);
	}
}
