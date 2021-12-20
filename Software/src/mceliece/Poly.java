package mceliece;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Poly {

	public int degree;

	public gfPoly[] coefficients;

	public static Poly zeroPoly = new Poly(new gfPoly[] {gfPoly.gfZero});

	public static Poly onePoly = new Poly(new gfPoly[] {gfPoly.gfOne});

	public static Poly xPoly = new Poly(new gfPoly[] {gfPoly.gfZero, gfPoly.gfOne});

	public static Scanner scan = new Scanner(System.in);

	public Poly() {
		coefficients = new gfPoly[]{gfPoly.gfZero};
		degree = 0;
	}

	public Poly(gfPoly poly) {
		coefficients = new gfPoly[1];
		coefficients[0] = poly;
		degree = 0;
	}

	public Poly(int n) {
		coefficients = new gfPoly[n + 1];
		for (int i = 0; i < coefficients.length; i++)
			coefficients[i] = new gfPoly(0);
		degree = n;
	}

	public Poly(gfPoly[] coeffs) {
		coefficients = coeffs;
		degree = coeffs.length - 1;
	}

	public Poly(Poly copied, int shift) {
		if (shift > 0) {
			degree = copied.degree + shift;
			gfPoly[] newCoeffs = new gfPoly[degree + 1];

			int i;
			for (i = 0; i <= copied.degree; i++)
				newCoeffs[degree - i] = copied.coefficients[copied.degree - i];
			for (i = copied.degree + 1; i <= degree; i++)
				newCoeffs[degree - i] = new gfPoly(0);

			if (copied.degree == 0)
				newCoeffs[0] = new gfPoly(0);
			coefficients = newCoeffs;
		} else {
			degree = copied.degree;
			coefficients = copied.coefficients;
		}
	}

	/*
    public static Poly getRandPoly(int deg) {
       int[] randCoeffs = McEliece.getRandomErrorVector((int) (Math.random() * deg), deg + 1);
       randCoeffs[randCoeffs.length - 1] = 1;
       gfPoly[] gfCoeffs = new gfPoly[deg + 1];
       for (int i = 0; i <= deg; i++) {
          if (randCoeffs[i] == 1)
             gfCoeffs[i] = gfPoly.gfOne;
          else
             gfCoeffs[i] = gfPoly.gfZero;
       }
       return new Poly(gfCoeffs);
    }
	 */

	public static Poly getRandPoly(int deg, int fieldExp) {
		gfPoly[] coeffs = new gfPoly[deg + 1];
		int gfPolyDeg;
		for (int coeffLoc = 0; coeffLoc < deg; coeffLoc++) {
			gfPolyDeg = (int) (Math.random() * fieldExp);
			coeffs[coeffLoc] = gfPoly.getRandPoly(gfPolyDeg);
		}
		coeffs[deg] = gfPoly.gfOne;
		return new Poly(coeffs);
	}

	/*
    public static long decToBinary(long n) {
       String temp = Long.toBinaryString(n);
       return Long.parseLong(temp);
    }
	 */

	@SuppressWarnings("static-access")
	public static Poly multiply(Poly poly, gfPoly gf, gfPoly irred) {
		Poly result = new Poly(poly.degree);

		for (int i = 0; i < result.coefficients.length; i++)       
			result.coefficients[i] = gf.gf_multiply(gf, poly.coefficients[i], irred);

		getDegree(result);
		return result;
	}

	public static Poly getRemainder(Poly numerator, Poly denominator, gfPoly irred) {  
		Poly num = new Poly(numerator.coefficients);
		Poly denom = new Poly(denominator.coefficients);

		if (!denom.equals(zeroPoly)) {              
			if (denom.equals(onePoly) || num.equals(denom) || num.equals(zeroPoly))
				return zeroPoly;

			if (num.degree >= denom.degree) { 
				// gfPoly factor = gfPoly.getQuotient(num.coefficients[num.degree], denom.coefficients[denom.degree]);
				gfPoly factor =	gfPoly.getModularInverse(denominator.coefficients[denominator.degree], irred);
				factor = gfPoly.gf_multiply(factor, num.coefficients[num.degree], irred);

				Poly shiftedDenominator = Poly.toPower(xPoly, irred , num.degree - denom.degree);
				shiftedDenominator = multiply(shiftedDenominator, factor, irred);
				shiftedDenominator = multiply(denominator, shiftedDenominator, irred);
				num = add(num, shiftedDenominator);
				getDegree(num);

				/*
             	for (int i = 0; i < num.coefficients.length; i++)
                	num.coefficients[i] = gfPoly.gf_add(num.coefficients[i], newDenom.coefficients[i]);
                // num.coefficients = num.coefficients.xor(add.coefficients);
                getDegree(num);
				 */

				return getRemainder(num, denom, irred);
			} else {
				return num;
			}
		}	
		return zeroPoly;
	}

	public static Poly multiply(Poly polyOne, Poly polyTwo, gfPoly irredPoly) {
		gfPoly[] resultCoeffs = new gfPoly[polyOne.degree + polyTwo.degree + 1];

		for (int resultLoc = 0; resultLoc < resultCoeffs.length; resultLoc++)
			resultCoeffs[resultLoc] = new gfPoly(0);

		for (int i = 0; i <= polyOne.degree; i++)
			for (int j = 0; j <= polyTwo.degree; j++)
				resultCoeffs[i+j] = gfPoly.gf_add(resultCoeffs[i+j], gfPoly.gf_multiply(polyOne.coefficients[i], polyTwo.coefficients[j], irredPoly));

		return new Poly(resultCoeffs);
	}

	public static Poly multiply(Poly polyOne, Poly polyTwo, gfPoly irredPoly, Poly pMod) {
		gfPoly[] resultCoeffs = new gfPoly[polyOne.degree + polyTwo.degree + 1];

		for (int resultLoc = 0; resultLoc < resultCoeffs.length; resultLoc++)
			resultCoeffs[resultLoc] = new gfPoly(0);

		for (int i = 0; i <= polyOne.degree; i++)
			for (int j = 0; j <= polyTwo.degree; j++)
				resultCoeffs[i+j] = gfPoly.gf_add(resultCoeffs[i+j], gfPoly.gf_multiply(polyOne.coefficients[i], polyTwo.coefficients[j], irredPoly));

		Poly returnPoly = new Poly(resultCoeffs);
		returnPoly = getRemainder(returnPoly, pMod, irredPoly);
		return returnPoly;
	}

	@SuppressWarnings("static-access")
	public static Poly getIrredPoly(int deg, gfPoly fieldPoly) {
		Poly irredPoly;
		do {
			irredPoly = Poly.getRandPoly(deg, fieldPoly.degree);
		} while (!irredPoly.isIrreducible(irredPoly, fieldPoly, fieldPoly.degree));
		return irredPoly;
	}

	public boolean isIrreducible(gfPoly fieldPoly) { 
		// Polynomial factor = new Polynomial(10, 1);
		gfPoly[] squareCoeffs = new gfPoly[] {gfPoly.gfZero, gfPoly.gfOne};
		Poly square = new Poly(squareCoeffs);

		/*
		// i <= this.degree/2
		for(int i = 0; i < this.degree; i++) {
			square = multiply(square, square);
			square = getRemainder(square, this);
		}
		if(square.coefficients.equals(new BigInteger("2")))
			return true;
		return false;
		 */

		for (int i = 0; i < this.degree / 2; i++) {
			square = multiply(square, square, fieldPoly);
			square = getRemainder(square, this, fieldPoly);
			Poly tempSquare = xPoly;
			if (!gcd(this, tempSquare, fieldPoly).equals(onePoly))
				return false;
		}
		return true;
	}

	@SuppressWarnings("unused")
	public static Boolean isIrreducible(Poly test, gfPoly gf_irred, int extDegree) {
		// h_0 = x
		Poly polyH = xPoly;

		// get prime divisors of test degree
		List<Integer> primeDivisors = getPrimeDivisors(test.degree);  

		List<Integer> degrees = new ArrayList<Integer>();

		// for each prime divisor
		for (int primeDivLoc = 0; primeDivLoc < primeDivisors.size(); primeDivLoc++) {
			degrees.add(test.degree / primeDivisors.get(primeDivLoc));
			// n_j = test.degree / prime divisor
		}

		// sort these n_js
		int fieldSize = (int) Math.pow(2, extDegree);

		// for i = 1 to #prime divisors
		for (int i = 0; i < primeDivisors.size(); i++) {
			polyH = xPoly;
			for (int j = 0; j < gf_irred.degree * degrees.get(i); j++) {
				polyH = toPower(polyH, gf_irred, 2);
				polyH = getRemainder(polyH, test, gf_irred);
			}

			// calculate gcd of f, h_i-x
			Poly gcdPolys = gcd(test, add(polyH, xPoly), gf_irred);

			// if gcd is not 1, f is reducible, stop
			if (!gcdPolys.equals(onePoly))
				return false;
		}

		// g = h_k^(n-n_k-x) mod f
		Poly polyG = xPoly;

		for (int i = 0; i < test.degree * extDegree; i++) {
			polyG = toPower(polyG, gf_irred, 2);
			polyG = getRemainder(polyG, test, gf_irred);
		}

		polyG = add(polyG, xPoly);
		polyG = getRemainder(polyG, test, gf_irred);
		getDegree(polyG);

		// if g=0, f is irreducible, otherwise is isn't
		if (polyG.equals(zeroPoly))
			return true;
		return false;
	}

	public static Poly getQuotient(Poly numerator, Poly denominator, gfPoly fieldPoly) {
		if (numerator.degree < denominator.degree)
			return zeroPoly;

		Poly quotient = new Poly(numerator.degree - denominator.degree);

		Poly shiftedDenominator;
		while (numerator.degree >= denominator.degree && !numerator.equals(zeroPoly)) {
			quotient.coefficients[numerator.degree - denominator.degree] = gfPoly.getModularInverse(denominator.coefficients[denominator.degree], fieldPoly);
			quotient.coefficients[numerator.degree - denominator.degree] = gfPoly.gf_multiply(quotient.coefficients[numerator.degree - denominator.degree], numerator.coefficients[numerator.degree], fieldPoly);

			shiftedDenominator = new Poly(denominator, numerator.degree - denominator.degree);
			shiftedDenominator = multiply(shiftedDenominator, quotient.coefficients[numerator.degree - denominator.degree], fieldPoly);
			numerator = add(numerator, shiftedDenominator);
			getDegree(numerator);
		}

		/*
		if(numerator.degree == denominator.degree)
			quotient.coefficients[0] = gfPoly.gf_add(quotient.coefficients[0], gfPoly.gfOne);
		 */

		return quotient;
	}

	/*
    public static Poly gcd(Poly polyOne, Poly polyTwo, gfPoly irredPoly) {
       Poly pOne = polyOne;
       Poly pTwo = polyTwo;
       Poly remainder = getRemainder(pOne, pTwo, irredPoly);
       while (!remainder.equals(zeroPoly)) {  
          pOne = pTwo;
          pTwo = remainder;
          getDegree(pTwo);
          remainder = getRemainder(pOne, pTwo, irredPoly);
       }
       return pTwo;
    }
	 */

	public static Poly gcd(Poly polyOne, Poly polyTwo, gfPoly irredPoly) {
		if (polyTwo.equals(zeroPoly))
			return polyOne;
		if (polyOne.equals(zeroPoly))
			return polyTwo;
		return getModularInverse(polyOne, polyTwo, irredPoly).get(0);
	}

	public static void getDegree(Poly poly) {
		int deg = poly.coefficients.length - 1;
		Boolean degFound = false;
		while (!degFound && deg > 0) {
			if (poly.coefficients[deg].equals(gfPoly.gfZero))
				deg--;
			else
				degFound = true;
		}
		poly.degree = deg;
		gfPoly[] newCoeffs = new gfPoly[deg + 1];
		for (int copyIndex = 0; copyIndex <= deg; copyIndex++)
			newCoeffs[copyIndex] = poly.coefficients[copyIndex];
		poly.coefficients = newCoeffs;
	}

	public String toString() {
		getDegree(this);
		String polynomial = "";
		if (this.degree == 0 && coefficients[0].equals(gfPoly.gfZero)) {
			polynomial = "0";
		} else {
			for (int printing = 0; printing <= degree; printing++) {
				gfPoly coefficient = coefficients[printing];

				if (!coefficient.equals(gfPoly.gfZero))	{
					polynomial += "(";
					polynomial += coefficient.toString();
					polynomial += ")";

					if (printing != 0)
						polynomial += "x^" + printing;
					if (printing != degree)
						polynomial += " + ";
				}
			}
		}
		return polynomial;
	}

	public boolean equals(Poly pTwo) {
		if (this.degree != pTwo.degree)
			return false;

		for (int i = 0; i < this.coefficients.length; i++) {
			if (!this.coefficients[i].equals(pTwo.coefficients[i]))
				return false;
		}
		return true;
	}

	// gets result of polyOne - polyTwo
	public static Poly add(Poly polyOne, Poly polyTwo) {
		gfPoly[] coeffs = new gfPoly[Math.max(polyOne.degree, polyTwo.degree) + 1];
		Poly largerPoly, otherPoly;
		if (polyOne.degree > polyTwo.degree) {
			largerPoly = polyOne;
			otherPoly = polyTwo;
		} else {
			largerPoly = polyTwo;
			otherPoly = polyOne;
		}

		for (int addIndex = 0; addIndex <= otherPoly.degree; addIndex++)
			coeffs[addIndex] = gfPoly.gf_add(largerPoly.coefficients[addIndex],	otherPoly.coefficients[addIndex]);

		for (int addIndex = otherPoly.degree + 1; addIndex <= largerPoly.degree; addIndex++)
			coeffs[addIndex] = largerPoly.coefficients[addIndex];

		return new Poly(coeffs);
	}

	public static Poly toPower(Poly poly, gfPoly irredPoly, int power) {
		Poly result = onePoly;
		for (int i = 0; i < power; i++) {
			result = multiply(result, poly, irredPoly);
			getDegree(result);
		}
		return result;
	}

	static Poly calcSqrt(Poly poly, Poly mod, int extDegree, gfPoly fieldPoly) {
		// return toPower(poly, mod, (int)Math.pow(2, extDegree * mod.degree - 1));
		Poly even = new Poly(poly.degree);
		for (int i = 0; i <= poly.degree; i += 2)
			even.coefficients[i] = poly.coefficients[i];

		getDegree(even);
		Poly odd = new Poly(poly.degree);
		for (int i = 1; i <= poly.degree; i += 2)
			odd.coefficients[i - 1] = poly.coefficients[i];

		getDegree(odd);
		Poly sqrtX = new Poly(new gfPoly[] {gfPoly.gfZero, gfPoly.gfOne});
		for (int i = 0; i < extDegree*mod.degree - 1; i++) {
			sqrtX = toPower(sqrtX, fieldPoly, 2);
			sqrtX = getRemainder(sqrtX, mod, fieldPoly);
		}

		Poly sqrtEven = new Poly(poly.degree / 2);
		for (int i = 0; i <= even.degree; i += 2) {
			sqrtEven.coefficients[(int)(i/2)] = even.coefficients[i];
			// sqrtEven.coefficients ^= even.coefficients >> (i/2) & (1 << i/2);
		}

		for (int i = 0; i <= sqrtEven.degree; i++)
			sqrtEven.coefficients[i] = gfPoly.calcSqrt(sqrtEven.coefficients[i], fieldPoly, extDegree);

		getDegree(sqrtEven);
		// odd.coefficients = odd.coefficients >> 1;

		Poly sqrtOdd = new Poly(poly.degree / 2);
		for (int i = 0; i <= odd.degree; i += 2)
			sqrtOdd.coefficients[(int)(i/2)] = odd.coefficients[i];

		for (int i = 0; i <= sqrtOdd.degree; i++)
			sqrtOdd.coefficients[i] = gfPoly.calcSqrt(sqrtOdd.coefficients[i], fieldPoly, extDegree);

		getDegree(sqrtOdd);
		Poly squareRoot = multiply(sqrtX, sqrtOdd, fieldPoly);
		squareRoot = add(sqrtEven, squareRoot);

		return getRemainder(squareRoot, mod, fieldPoly);
	}

	public static gfPoly eval(Poly outer, gfPoly inner, gfPoly mod) {
		gfPoly result = new gfPoly(0);
		gfPoly multPoly;

		for (int i = 0; i <= outer.degree; i++) {
			multPoly = gfPoly.toPower(inner, mod, i);
			multPoly = gfPoly.gf_multiply(multPoly, outer.coefficients[i], mod);
			result = gfPoly.gf_add(result, multPoly);
		}

		return result;
	}

	public static Poly eval(Poly outer, Poly inner, gfPoly mod) {
		Poly result = new Poly();
		Poly multPoly;

		for (int i = 0; i <= outer.degree; i++) {
			multPoly = toPower(inner, mod, i);
			multPoly = multiply(multPoly, outer.coefficients[i], mod);
			result = Poly.add(result, multPoly);
		}

		return result;
	}

	public static List<Poly> getModularInverse(Poly poly, Poly mod, gfPoly fieldPoly) {
		// return getModularInverseHelper(poly, mod, fieldPoly).get(1);
		List<Poly> result = getModularInverseHelper(poly, mod, fieldPoly);   
		gfPoly factor = result.get(0).coefficients[result.get(0).degree];
		factor = gfPoly.getModularInverse(factor, fieldPoly);
		for (int i = 0; i < result.size(); i++)
			result.set(i, multiply(result.get(i), factor, fieldPoly));
		return result;
	}

	@SuppressWarnings("unused")
	public static List<Poly> getModularInverseHelper(Poly pOne, Poly pTwo, gfPoly fieldPoly) {
		Poly poly = new Poly(pOne.coefficients);
		Poly mod = new Poly (pTwo.coefficients);
		getDegree(poly);
		getDegree(mod);

		List<Poly> result = new ArrayList<Poly>();

		if (mod.equals(zeroPoly)) {
			result.add(poly);
			result.add(onePoly);
			result.add(zeroPoly);
		} else {
			result = getModularInverseHelper(mod, getRemainder(poly, mod, fieldPoly), fieldPoly);

			Poly inverse = result.get(2); 
			gfPoly leadingCoeff = inverse.coefficients[inverse.degree]; 
			Poly second = getQuotient(poly, mod, fieldPoly);
			Poly rem = getRemainder(poly, mod, fieldPoly);
			second = multiply(second, result.get(2), fieldPoly);

			second = add(result.get(1), second);
			result.set(2, second);
			result.set(1, inverse);
		}
		return result;
	}

	// source: timing attack on patterson paper
	@SuppressWarnings("unused")
	public static Poly[] partialGCD(Poly sqrt, Poly mod, gfPoly fieldPoly) {
		int breakPoint = (int) (mod.degree / 2);
		// System.out.println("breakpoint: " + breakPoint);      

		Poly rem_prev = new Poly(mod, 0);
		Poly rem = new Poly(sqrt, 0);
		Poly rem_temp = new Poly();

		Poly b_prev = new Poly(zeroPoly, 0);
		// Polynomial b = new Polynomial(1, 0);
		Poly b = new Poly(onePoly, 0);
		Poly b_temp = new Poly();
		Poly quotient = new Poly();

		int i = 0;
		while (rem.degree > breakPoint) {
			i++;
			quotient = getQuotient(rem_prev, rem, fieldPoly);
			getDegree(quotient);
			rem_temp = rem;
			// System.out.println("Getting remainder of " + rem_prev.toString() + " divided by " + rem.toString());
			rem = getRemainder(rem_prev, rem, fieldPoly);
			getDegree(rem);
			// System.out.println("now rem is: " + rem.toString());
			rem_prev = rem;
			b_temp = b;
			b = add(b_prev, multiply(quotient, b, fieldPoly));
			// b.coefficients = b_prev.coefficients.xor(multiply(quotient, b).coefficients);
			getDegree(b);
			b_prev = b_temp;
		}

		Poly[] result = new Poly[2];
		result[0] = rem;
		result[1] = b;
		return result;
	}

	public static Poly[] partialGCDNew(Poly sqrt, Poly mod, gfPoly fieldPoly) {
		Poly[] polyRes = new Poly[2];
		int stoppingPoint = (int) (mod.degree / 2);
		Poly s = new Poly(onePoly, 0);
		Poly prev_s = new Poly(zeroPoly, 0);
		Poly[] sArray = new Poly[] {prev_s, s};

		Poly r = sqrt;
		Poly prev_r = mod;
		Poly[] rArray = new Poly[] {prev_r, r};

		Poly r_temp;
		Poly s_temp;
		Poly quotient;

		while (rArray[1].degree > stoppingPoint) {
			// System.out.println("now getting quotient of: ");
			// System.out.println(rArray[0].toString() + " / ");
			// System.out.println(rArray[1].toString());
			quotient = getQuotient(rArray[0], rArray[1], fieldPoly);
			r_temp = rArray[0];
			rArray[0] = rArray[1];
			// System.out.println("quotient is: " + quotient.toString());
			// System.out.println("prev_r is " + r_temp.toString());
			// scan.nextLine();
			rArray[1] = add(r_temp, multiply(quotient, rArray[1], fieldPoly));
			getDegree(rArray[1]);

			// System.out.println("rArray[1] is " + rArray[1].toString());

			s_temp = sArray[0];
			// System.out.println("prev_s is " + s_temp.toString());
			sArray[0] = sArray[1];
			sArray[1] = add(s_temp, multiply(quotient, sArray[1], fieldPoly));
			getDegree(sArray[1]);
			// System.out.println("new s is " + sArray[1].toString());
		}
		polyRes[0] = rArray[1];
		polyRes[1] = sArray[1];
		return polyRes;
	}

	public static List<gfPoly> getRoot(Poly poly, gfPoly fieldPoly) {
		List<gfPoly> roots = new ArrayList<gfPoly>();
		if (poly.degree == 1)
			roots.add(gfPoly.multiply(poly.coefficients[0], gfPoly.getModularInverse(poly.coefficients[1], fieldPoly)));
		return roots;
	}

	/*
    public static Polynomial rabinIrred(int degree) { }
	 */

	// source: https://www.topcoder.com/community/competitive-programming/tutorials/prime-numbers-factorization-and-euler-function/
	public static List<Integer> getPrimeDivisors(int n) {
		List<Integer> primeDivisors = new ArrayList<Integer>();   

		for (int i = 2; i <= Math.sqrt(n); i++)	{
			while (n % i == 0) {
				if (!primeDivisors.contains(i))
					primeDivisors.add(i);
				n /= i;
			}
		}

		if (n > 1)
			primeDivisors.add(n);

		return primeDivisors;
	}
}
