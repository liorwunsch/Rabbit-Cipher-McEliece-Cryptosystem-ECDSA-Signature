package mceliece;

public class PolynomialIntegers {

	public int coefficients;
	public int degree;

	public PolynomialIntegers() {
		coefficients = 0;
		degree = 0;
	}

	public PolynomialIntegers(int coeffs) {
		coefficients = coeffs;
		degree = (int) (Math.log10(coeffs) + 1) - 1;
	}

	public int[] getSupport(int extDegree) {
		int[] support = new int[(int) Math.pow(2,  extDegree)];
		for (int i = 0; i < (int) Math.pow(2, extDegree); i++) { }
		return support;
	}

	/*
    public static Polynomial getPrimitivePolynomial(int extDegree) {
    	// Polynomial test = new Polynomial(extDegree);
      	Polynomial test = Polynomial.getIrredPoly(extDegree);

      	int numTried = extDegree + 1;
      	boolean isPrimitive;
      	boolean primitiveFound = false;
      	do {
        	test = Polynomial.getIrredPoly(extDegree);
        	isPrimitive = true;
        	numTried = extDegree + 1;
        	while (isPrimitive && numTried < Math.pow(2, extDegree) - 1) {
            	Polynomial divideInto = new Polynomial(numTried);
           		// TODO: fix this
            	// divideInto.coefficients[divideInto.degree] = 1;
            	//divideInto.coefficients[0] = 1;
            	Polynomial remainder = Polynomial.getRemainder(divideInto, test);
            	if (remainder.equals(Polynomial.zeroPolynomial)) {
					System.out.println("found that " + test.toString() + " is not primitive" + " when dividing into" + divideInto.toString());
					System.out.println("remainder here is  " + remainder.toString());
               		isPrimitive = false;
            	}
               numTried++;
         }
         if (isPrimitive)
            primitiveFound = true;
      } while (!primitiveFound);
      return test;
   }
	 */
}
