package mceliece;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Polynomial {

	public int degree;

	public BigInteger coefficients;

	public static Polynomial zeroPolynomial = new Polynomial(BigInteger.ZERO);

	public static Polynomial polyX = new Polynomial(new BigInteger("2"));

	public static Polynomial polyOne = new Polynomial(BigInteger.ONE);

	public static Scanner scan = new Scanner(System.in);

	public Polynomial() {
		coefficients = BigInteger.ZERO;
		degree = 0;
	}

	public Polynomial(BigInteger coeffs) {
		coefficients = coeffs;
		if (coefficients.equals(BigInteger.ZERO))
			degree = 0;
		else
			degree = coeffs.bitLength() - 1;
	}

	public Polynomial(String coeffs, int radix) {
		coefficients = new BigInteger(coeffs, radix);
		degree = coefficients.bitLength() - 1;
	}

	public Polynomial(Polynomial copied, int shift) {
		if (shift > 0) {
			degree = copied.degree + shift;
			coefficients = copied.coefficients.shiftLeft(shift);
		} else {
			degree = copied.degree;
			coefficients = new BigInteger((copied.coefficients.toString()));
		}
	}

	public static Polynomial getRandPoly(int deg) {
		Random rnd = new Random();
		BigInteger maxCoeff = new BigInteger(deg, rnd);
		BigInteger min = new BigInteger("2");
		min = min.pow(deg);

		return new Polynomial(maxCoeff.add(min));
	}

	/*
    public static long decToBinary(long n) {
       String temp = Long.toBinaryString(n);
       return Long.parseLong(temp);
    }
	 */

	public static Polynomial getRemainder(Polynomial numerator, Polynomial denominator) {  
		Polynomial num = new Polynomial(numerator.coefficients);
		Polynomial denom = new Polynomial(denominator.coefficients);

		if (!denom.equals(zeroPolynomial)) {        
			if (denom.equals(polyOne) || num.equals(denom) || num.equals(zeroPolynomial))
				return zeroPolynomial;

			if (num.degree >= denom.degree) {
				Polynomial add = new Polynomial(denom, num.degree - denom.degree);
				num.coefficients = num.coefficients.xor(add.coefficients);
				getDegree(num);
				return getRemainder(num, denom);
			} else {
				return num;
			}
		}
		return zeroPolynomial;
	}

	/*
    public static long binaryToDec(long n) {
       String rep = Long.toString(n);
       return Long.parseLong(rep, 2);
    }
	 */

	public static Polynomial multiply(Polynomial polyOne, Polynomial polyTwo) {
		BigInteger resultCoeffs = BigInteger.ZERO;

		for (int i = 0; i <= polyTwo.degree; i++) {
			if ((polyTwo.coefficients.shiftRight(i).and(BigInteger.ONE).intValue()) == 1) {
				// resultCoeffs ^= polyOne.coefficients << i;
				resultCoeffs = resultCoeffs.xor(polyOne.coefficients.shiftLeft(i));
			}
		}
		return new Polynomial(resultCoeffs);
	} 

	public static Polynomial reduce(int galoisExp, Polynomial num, Polynomial mod) {
		while (num.degree >= galoisExp) {
			num = getRemainder(num, mod);
		}
		return num;
	}

	public static Polynomial getModularInverse(Polynomial poly, Polynomial mod) {
		return getModularInverseHelper(poly, mod).get(1);
	}

	public static List<Polynomial> getModularInverseHelper(Polynomial pOne, Polynomial pTwo) {
		Polynomial poly = new Polynomial(pOne.coefficients);
		Polynomial mod = new Polynomial (pTwo.coefficients);

		List<Polynomial> result = new ArrayList<Polynomial>();

		if (mod.equals(zeroPolynomial)) {
			result.add(poly);
			// all i changed was these two lines
			result.add(new Polynomial("1", 2));
			result.add(new Polynomial("0", 2));
		} else {
			result = getModularInverseHelper(pTwo, getRemainder(pOne, pTwo));

			Polynomial inverse = result.get(2); 
			Polynomial second = getQuotient(poly, mod);

			second = multiply(second, result.get(2));
			second = subtract(result.get(1), second);
			result.set(2, second);
			result.set(1, inverse);
		}
		return result;
	}

	public static Polynomial getIrredPoly(int deg) {
		Polynomial irredPoly;
		do {
			irredPoly = getRandPoly(deg);
			if(irredPoly.coefficients.mod(new BigInteger("2")) == BigInteger.ZERO)
				irredPoly.coefficients = irredPoly.coefficients.add(BigInteger.ONE);
		} while (!irredPoly.isIrreducible());
		return irredPoly;
	}

	public boolean isIrreducible() { 
		// Polynomial factor = new Polynomial(10, 1);
		Polynomial square = new Polynomial("10", 2);

		/*
		i <= this.degree/2
       	for(int i = 0; i < this.degree; i++) {
        	square = multiply(square, square);
        	square = getRemainder(square, this);
        }
        if(square.coefficients.equals(new BigInteger("2")))
        	return true;
        return false;
		 */

		for (int i = 0; i < this.degree / 2; i++) {
			square = multiply(square, square);
			square = getRemainder(square, this);
			Polynomial tempSquare = new Polynomial(square.coefficients.add(new BigInteger("2")));
			if (!gcd(this, tempSquare).equals(polyOne))
				return false;
		}
		return true;
	}

	public static Polynomial getQuotient(Polynomial numerator, Polynomial denominator) {
		if (numerator.degree < denominator.degree)
			return zeroPolynomial;

		Polynomial quotient = new Polynomial();
		quotient.degree = numerator.degree - denominator.degree;

		Polynomial shiftedDenominator;
		while (numerator.degree > denominator.degree) {
			// quotient.coefficients ^= 1 << numerator.degree - denominator.degree;
			quotient.coefficients = quotient.coefficients.xor(BigInteger.ONE.shiftLeft(numerator.degree - denominator.degree));
			shiftedDenominator = new Polynomial(denominator, numerator.degree - denominator.degree);
			// numerator.coefficients ^= shiftedDenominator.coefficients;
			numerator.coefficients = numerator.coefficients.xor(shiftedDenominator.coefficients);
			getDegree(numerator);
		}

		if (numerator.degree == denominator.degree)
			quotient.coefficients = quotient.coefficients.xor(BigInteger.ONE);
		return quotient;
	}

	public static Polynomial gcd(Polynomial polyOne, Polynomial polyTwo) {
		Polynomial pOne = polyOne;
		Polynomial pTwo = polyTwo;
		Polynomial remainder = getRemainder(pOne, pTwo);
		while (!remainder.equals(zeroPolynomial)) {  
			pOne = pTwo;
			pTwo = remainder;
			getDegree(pTwo);
			remainder = getRemainder(pOne, pTwo);
		}
		return pTwo;
	}

	public static void getDegree(Polynomial poly) {
		if (poly.coefficients.equals(BigInteger.ZERO))
			poly.degree = 0;
		else
			poly.degree = poly.coefficients.bitLength() - 1;
	}

	public String toString() {
		getDegree(this);
		String polynomial = "";
		if (coefficients.equals(BigInteger.ZERO)) {
			polynomial = "0";
		} else {
			for (int printing = 0; printing <= degree; printing++) {
				int coefficient = coefficients.shiftRight(printing).and(BigInteger.ONE).intValue();

				if (coefficient == 1) {
					polynomial += coefficient;
					if (printing != 0)
						polynomial += "x^" + printing;

					if (printing != degree)
						polynomial += " + ";
				}
			}
		}
		return polynomial;
	}

	public boolean equals(Polynomial pTwo) {
		return this.coefficients.equals(pTwo.coefficients);
	}

	// gets result of polyOne - polyTwo
	public static Polynomial subtract(Polynomial polyOne, Polynomial polyTwo) {
		return new Polynomial(polyOne.coefficients.xor(polyTwo.coefficients));
	}

	public static Polynomial toPower(Polynomial poly, Polynomial mod, int power) {
		Polynomial result = new Polynomial("1", 2); 
		for (int i = 0; i < power; i++) {
			result = getRemainder(multiply(poly, result),mod);
			getDegree(result);
		}
		return result;
	}

	static Polynomial calcSqrt(Polynomial poly, Polynomial mod, int extDegree) {
		// return toPower(poly, mod, (int)Math.pow(2, extDegree * mod.degree - 1));

		Polynomial even = new Polynomial(BigInteger.ZERO);
		for (int i = 0; i <= poly.degree; i += 2) {
			BigInteger toAdd = poly.coefficients.and(BigInteger.ONE.shiftLeft(i));
			even.coefficients = even.coefficients.xor(toAdd);
			// even.coefficients ^= (poly.coefficients  & (1 << i));
		}

		getDegree(even);
		// System.out.println("even is " + even.toString());
		Polynomial odd = new Polynomial("0", 2);
		for (int i = 1; i <= poly.degree; i += 2) {
			BigInteger toAdd = poly.coefficients.and(BigInteger.ONE.shiftLeft(i));
			odd.coefficients = odd.coefficients.xor(toAdd);
			// odd.coefficients ^= (poly.coefficients  & (1 << i));
		}

		getDegree(odd);
		// System.out.println("odd is " + odd.toString());
		Polynomial sqrtX = new Polynomial("10", 2);
		for (int i = 0; i < extDegree - 1; i++)
			sqrtX = toPower(sqrtX, mod, 2);

		// System.out.println("sqrt x is " + sqrtX.toString());
		Polynomial sqrtEven = new Polynomial("0", 2);
		for (int i = 0; i <= even.degree; i += 2) {
			BigInteger toAdd = even.coefficients.shiftRight(i/2);
			toAdd = toAdd.and(BigInteger.ONE.shiftLeft(i/2));
			sqrtEven.coefficients = sqrtEven.coefficients.xor(toAdd);
			// sqrtEven.coefficients ^= even.coefficients >> (i/2) & (1 << i/2);
		}

		getDegree(sqrtEven);
		odd.coefficients = odd.coefficients.shiftRight(1);

		getDegree(odd);
		// odd.coefficients = odd.coefficients >> 1;
		Polynomial sqrtOdd = new Polynomial("0", 2);
		for (int i = 0; i <= odd.degree; i += 2) {
			BigInteger toAdd = odd.coefficients.shiftRight(i/2);
			toAdd = toAdd.and(BigInteger.ONE.shiftLeft(i/2));
			sqrtOdd.coefficients = sqrtOdd.coefficients.xor(toAdd);
			// sqrtOdd.coefficients ^= odd.coefficients >> (i/2) & (1 << i/2);
		}

		getDegree(sqrtOdd);
		BigInteger sqrtCoeffs = multiply(sqrtX, sqrtOdd).coefficients;
		sqrtCoeffs = sqrtEven.coefficients.xor(sqrtCoeffs);
		Polynomial squareRoot = new Polynomial(sqrtCoeffs);
		// Polynomial squareRoot = new Polynomial(sqrtEven.coefficients ^ multiply(sqrtX, sqrtOdd).coefficients);

		return getRemainder(squareRoot, mod);
	}

	public static Polynomial eval(Polynomial outer, Polynomial inner, Polynomial mod) {
		BigInteger resultCoeffs = new BigInteger("0");

		for (int i = 0; i <= outer.degree; i++) {
			if (outer.coefficients.shiftRight(i).and(BigInteger.ONE).equals(BigInteger.ONE)) {
				// if((outer.coefficients >> i & 1) == 1)
				// resultCoeffs ^= toPower(inner, mod, i ).coefficients;
				resultCoeffs = resultCoeffs.xor(toPower(inner,mod,i).coefficients);
			}
		}

		// TODO: handle degree > irredPolyDegree here?
		return new Polynomial(resultCoeffs);
	}

	// source: timing attack on patterson paper
	@SuppressWarnings("unused")
	public static Polynomial[] partialGCD(Polynomial sqrt, Polynomial mod) {
		int breakPoint = (int) (mod.degree / 2);

		Polynomial rem_prev = new Polynomial(mod, 0);
		Polynomial rem = new Polynomial(sqrt, 0);
		Polynomial rem_temp = new Polynomial();

		Polynomial b_prev = zeroPolynomial;
		// Polynomial b = new Polynomial(1, 0);
		Polynomial b = new Polynomial(BigInteger.ONE);
		Polynomial b_temp = new Polynomial();
		Polynomial quotient = new Polynomial();

		int i = 0;
		while (rem.degree > breakPoint) {
			// System.out.println("in here");
			i++;
			quotient = getQuotient(rem_prev, rem);
			getDegree(quotient);
			rem_temp = rem;
			rem = getRemainder(rem_prev, rem);
			// System.out.println("now rem is " + rem.toString());
			getDegree(rem);
			rem_prev = rem;
			b_temp = b;
			b.coefficients = b_prev.coefficients.xor(multiply(quotient, b).coefficients);
			getDegree(b);
			b_prev = b_temp;
		}

		Polynomial[] result = new Polynomial[2];
		result[0] = rem;
		result[1] = b;
		return result;
	}

	public static List<Polynomial> getRoot(Polynomial poly) {
		List<Polynomial> roots = new ArrayList<Polynomial>();
		if (poly.degree == 1) {
			if (poly.coefficients.equals(new BigInteger("2")))
				roots.add(zeroPolynomial);
			else
				roots.add(polyOne);
		}
		return roots;
	}

	/*
    public static Polynomial rabinIrred(int degree) { }
	 */
}
