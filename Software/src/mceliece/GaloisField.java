package mceliece;

public class GaloisField {

	// field size = 2 ^ extDegree
	public int extDegree;
	public gfPoly gf_irredPoly;
	public int size;

	public GaloisField() {
		// basic field of order 2 is default
		extDegree = 1;
	}

	public GaloisField(int extenDegree) {
		extDegree = extenDegree;
		gf_irredPoly = gfPoly.getIrredPoly(extenDegree);
	}

	public gfPoly gf_add(gfPoly polyOne, gfPoly polyTwo) {
		return new gfPoly(polyOne.coeffs ^ polyTwo.coeffs);
	}

	@SuppressWarnings("static-access")
	public gfPoly gf_mult(gfPoly polyOne, gfPoly polyTwo) {
		int resultCoeffs = 0;

		// iterate over each coefficient of the second polynomial
		for (int i = 0; i <= polyTwo.degree; i++) {
			// if current coefficient of polyTwo is 1
			if ((polyTwo.coeffs >> i & 1) == 1) {
				//add polyOne shifted by current coefficient position
				resultCoeffs = resultCoeffs ^ (polyOne.coeffs << (i));
			}
		}

		gfPoly result = new gfPoly(resultCoeffs);

		// reduce modulo irreducible polynomial if necessary
		result = result.getRemainder(result, gf_irredPoly);

		return result;
	}

	/* generates supportSize elements of current galois field given the irreducible
	 * polynomial that defines the field
	 */
	public gfPoly[] getSupport(Poly irredPoly, int supportSize) {     
		int suppSize = 0;

		// first, generate whole support if needed
		gfPoly tempSupp[] = new gfPoly[(int) Math.pow(2, extDegree)];

		for (int i = 0; i < tempSupp.length; i++) {
			gfPoly test = new gfPoly(i);
			tempSupp[i] = test;

			if (!Poly.eval(irredPoly, test, gf_irredPoly).equals(gfPoly.gfZero)) {
				tempSupp[suppSize] = test;
				suppSize++;        
			}
			else {
				System.out.println(test.toString() + " is a root of " + irredPoly.toString());
			}
		}

		// create a vector of supportSize randomly chosen locations in full support
		// to pull the elements from 
		if (supportSize < (int) (Math.pow(2, extDegree))) {
			int[] locations = new int[supportSize];

			for (int i = 0; i < locations.length; i++)
				locations[i] = supportSize;

			int addTo, numAdded;

			for (numAdded = 0; numAdded <= supportSize - 1; numAdded++) {
				do {
					addTo = (int)(Math.random() * suppSize);
				} while(McEliece.isInArray(locations, addTo));

				locations[numAdded] = addTo;
			}

			// return only chosen number of elements 
			gfPoly[] support = new gfPoly[supportSize];

			for (int j = 0; j < supportSize; j++)
				support[j] = tempSupp[locations[j]];

			return support;
		} else {
			// if support size = field size, return whole field
			return tempSupp;
		}
	}

	public void printSupport(gfPoly[] support) {
		for(int i = 0; i < support.length; i++)
			System.out.println(i + ": " + support[i].toString());
	}
}
