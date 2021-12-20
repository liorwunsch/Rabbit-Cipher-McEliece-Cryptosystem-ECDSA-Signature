package mceliece;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class McEliece {

	// extension degree of finite field
	public int extDegree;

	// number of errors Goppa code is able to correct
	public int numErrors;

	// support of finite field - either whole field or subset of field
	public gfPoly[] support;

	// irreducible polynomial of degree numErrors to define Goppa code
	public Poly irredPoly;

	public Scanner scan = new Scanner(System.in);

	// underlying Galois Field
	public GaloisField gF;

	// parity check matrix of Goppa code
	public int[][] parityCheck;

	// permutation matrix - part of public key
	public int[][] permutation;

	// scrambling matrix (nonsingular) - part of public key
	public int[][] scramble;

	// generator matrix of Goppa code - calculated from parity checj matrix
	public int[][] genMatrix;

	// parity check matrix before expanding into binary matrix
	public Poly[][] polyParity;

	// error vector used during encryption
	public int[] errorVec;

	// public key (scramble * genMatrix * permutation)
	public int[][] pubKey;

	/**
	 * Creates the public key
	 * @param numErrors maximum number of errors Goppa code is able to correct
	 * @param extDegree extension degree of the finite field
	 * @param supportSize support size (can be at most the size of the finite field)
	 * @return scrambling matrix * generator matrix * permutation matrix
	 **/
	public int[][] createPublicKey(int numErrors, int extDegree, int supportSize) {
		int[][] generatorMatrix = createGeneratorMatrix(numErrors, extDegree, supportSize);
		genMatrix = generatorMatrix;

		int[][] permMatrix = createPermMatrix(generatorMatrix[0].length);
		permutation = permMatrix;

		// initialization for random matrix generation function
		int rows = generatorMatrix.length;
		int[][] mA = new int[rows][rows];	
		int[][] mT = new int[rows][rows];

		int[][] randomMatrix = getRandMatrix(mA, mT, rows);		
		scramble = randomMatrix;

		int[][] publicKey =  multiply(generatorMatrix, permMatrix);
		publicKey = multiply(randomMatrix, publicKey);

		return publicKey;
	}

	/**
	 * Tests equality of integer vectors
	 * @param one One vector to compare
	 * @param other Other vector to compare
	 * @return Whether or not the vectors are the same
	 */
	public static Boolean vecEquals(int[] one, int[] other) {
		if(one.length != other.length)
			return false;

		for (int i = 0; i < other.length; i++) {
			if(one[i] != other[i])
				return false;
		}
		return true;
	}

	/** 
	 * Encrypts a given message
	 * @param message a message to encrypt (must be of length supportSize -  extDegree*numErrors)
	 * @param numErrors the number of errors the Goppa code should be able to correct
	 * @param extDegree the extension degree that defines the size of the finite field
	 * @param suppSize  the support size
	 * @return: the encrypted message (message * publicKey + random error)
	 **/
	public int[] encrypt(int[] message, int numErrors, int extDegree, int suppSize) {
		// create public key
		int[][] publicKey = createPublicKey(extDegree, numErrors, suppSize);
		pubKey = publicKey;

		// temporary message stored as matrix instead of vector for multiplication later
		int[][] modMessage = new int[1][message.length];
		modMessage[0] = message;

		// multiply message by public key
		int[][] tempEnc = multiply(modMessage, publicKey);

		int[] error = getRandomErrorVector(numErrors, tempEnc[0].length);
		errorVec = error;

		// add error vector to message * publicKey
		int[] encrypted = tempEnc[0];
		for (int i = 0; i < encrypted.length; i++)
			encrypted[i] ^= error[i];

		// System.out.println("Encrypted:");
		// print(encrypted);

		return encrypted;
	}

	/*****************************Lior Added*************************************/
	public List<int[]> encrypt(int[] message, int numErrors, int extDegree, int suppSize, int[][] o_public_key) {
		// temporary message stored as matrix instead of vector for multiplication later
		int[][] modMessage = new int[1][message.length];
		modMessage[0] = message;

		// multiply message by public key
		int[][] tempEnc = multiply(modMessage, o_public_key);

		int[] error = getRandomErrorVector(numErrors, tempEnc[0].length);
		errorVec = error;

		// add error vector to message * publicKey
		int[] encrypted = tempEnc[0];
		for (int i = 0; i < encrypted.length; i++)
			encrypted[i] ^= error[i];

		// System.out.println("Encrypted:");
		// print(encrypted);

		return Arrays.asList(encrypted, errorVec);
	}

	/**
	 * Prints an integer vector in brackets with commas between entries
	 * @param vec Integer vector to print
	 */
	public static void print(int[] vec)	{
		System.out.print("[");

		for (int i = 0; i < vec.length; i++) {
			System.out.print(vec[i]);

			if (i != vec.length - 1)
				System.out.print(", ");
		}

		System.out.print("] \n");
	}

	/**
	 * Prints an vector of finite field elements in brackets with commas between entries
	 * @param vec gfPoly vector to print
	 */
	public static void print(gfPoly[] vec) {
		System.out.print("[");

		for (int i = 0; i < vec.length; i++) {
			System.out.print(vec[i].toString());

			if(i != vec.length - 1)
				System.out.print(", ");
		}

		System.out.print("] \n");
	}

	/**
	 * Creates the generator matrix of the Goppa code given parameters 
	 * @param extDegree extension degree of the finite field
	 * @param numErrors the number of errors the Goppa code should be able to correct
	 * @param supportSize the support size (number of elements from the finite field used)
	 * @return Generator matrix of a Goppa code with the specified parameters
	 **/
	public int[][] createGeneratorMatrix(int extDegree, int numErrors, int supportSize) {
		// get a parity check matrix in systematic form (if the
		// generated matrix is not in systematic form, generate one
		// based on a new Goppa code
		int[][] parityCheckMatrix;

		do {
			parityCheckMatrix = getParityCheck(extDegree, numErrors, supportSize);
		} while (!isSystematic(parityCheckMatrix, parityCheckMatrix[0].length - parityCheckMatrix.length));
		parityCheck = parityCheckMatrix;

		// use parity check matrix to create generator matrix 
		int[][] generatorMatrix = new int[parityCheckMatrix[0].length - parityCheckMatrix.length][support.length];

		for (int idRow = 0; idRow < generatorMatrix.length; idRow++) { 
			for (int idCol = 0; idCol < generatorMatrix.length; idCol++) {
				if(idRow == idCol)
					generatorMatrix[idRow][idCol] = 1;
			}
		}

		for (int copyingCol = 0; copyingCol < parityCheckMatrix[0].length - parityCheckMatrix.length; copyingCol++)
			for(int copyingRow = 0; copyingRow < parityCheckMatrix.length; copyingRow++)
				generatorMatrix[copyingCol][copyingRow + generatorMatrix.length] = parityCheckMatrix[copyingRow][copyingCol];

		return generatorMatrix;
	}

	/**
	 * Creates a random length x length permutation matrix
	 * @param length Length of input messages
	 * @return Random permutation matrix of dimensions length x length
	 */
	public static int[][] createPermMatrix(int length) {
		int[][] permMatrix = new int[length][length];

		// identity matrix rows that permMatrix rows will be swapped with
		int[] swappingWith = new int[length];

		int adding;
		for (adding = 0; adding < length; adding++)
			swappingWith[adding] = length;

		int[][] idMatrix = createIDMatrix(length);

		int toSwap, index, swapping;

		for (index = 0; index < length; index++) {
			do {
				toSwap = (int) (Math.random() * length);
			} while (isInArray(swappingWith, toSwap));

			swappingWith[index] = toSwap;
		}

		for (swapping = 0; swapping < length; swapping++)
			permMatrix[swapping] = idMatrix[swappingWith[swapping]];

		return permMatrix;
	}

	/**
	 * Generates a random vector of specified length and Hamming weight
	 * @param weight Desired Hamming weight of vector
	 * @param length Desired length of vector
	 * @return Randomly generated vector of given weight and length
	 */
	public static int[] getRandomErrorVector(int weight, int length) {
		// error vector
		int[] vector = new int[length];

		// locations where 1s are placed
		int[] locations = new int[weight];

		for (int i = 0; i < weight; i++)
			locations[i] = length;

		int addTo, numAdded;

		for (numAdded = 0; numAdded < weight; numAdded++) {
			do {
				addTo = (int) (Math.random() * length);
			} while (isInArray(locations, addTo));

			locations[numAdded] = addTo;
			vector[addTo] = 1;
		}

		return vector;
	}

	/**
	 * Checks whether or not an integer is in a given integer array
	 * @param array The array to look in 
	 * @param testVal The value to check for
	 * @return Boolean value of whether or not testVal is in array 
	 */
	public static boolean isInArray(int[] array, int testVal) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == testVal)
				return true;
		}
		return false;
	}

	/**
	 * Creates identity matrix of size length x length
	 * @param length Number of rows/columns of identity matrix
	 * @return Identity matrix of given size
	 */
	public static int[][] createIDMatrix(int length) {
		int index;
		int[][] idMatrix = new int[length][length];

		for(index = 0; index < length; index++)
			idMatrix[index][index] = 1;

		return idMatrix;
	}

	/**
	 * Prints 2D integer array
	 * @param array Array to print
	 */
	public static void print(int[][] array) {
		int rowIndex, columnIndex;

		for (rowIndex = 0; rowIndex < array.length; rowIndex++)	{
			System.out.print("[");

			for (columnIndex = 0; columnIndex < array[0].length; columnIndex++)	{
				System.out.print(array[rowIndex][columnIndex]);

				if (columnIndex != (array[0].length - 1))
					System.out.print(", ");
			}

			System.out.print("]");
			System.out.println();
		}
	}

	/**
	 * Prints 2D polynomial array (where entries are polynomials over finite fields)
	 * @param array Array to print
	 */
	public static void print(Poly[][] array) {
		int rowIndex, columnIndex;

		for (rowIndex = 0; rowIndex < array.length; rowIndex++) {
			for (columnIndex = 0; columnIndex < array[0].length; columnIndex++)
				System.out.print(array[rowIndex][columnIndex].toString() + " " );

			System.out.println();
		}
	}

	/**
	 * Multiplies two integer matrices 
	 * <p>
	 * Note: matrix1 columns must equal matrix2 rows
	 * @param matrix1 First matrix to multiply
	 * @param matrix2 Second matrix to multiply
	 * @return Matrix product if applicable, null if relevant dimensions of input matrices do not match
	 */
	public static int[][] multiply(int[][] matrix1, int[][] matrix2) {
		// System.out.println(matrix1[0].length);
		// System.out.println(matrix2.length);
		if (matrix1[0].length != matrix2.length)
			return null;

		int[][] result = new int[matrix1.length][matrix2[0].length];

		int matrix1Row, matrix2Column, iterIndex;

		for (matrix1Row = 0; matrix1Row < matrix1.length; matrix1Row++) {
			for (matrix2Column = 0; matrix2Column < matrix2[0].length; matrix2Column++)	{
				result[matrix1Row][matrix2Column] = 0;

				for (iterIndex = 0; iterIndex < matrix2.length;	iterIndex++)
					result[matrix1Row][matrix2Column] += (matrix1[matrix1Row][iterIndex] * matrix2[iterIndex][matrix2Column]) ;

				result[matrix1Row][matrix2Column] = result[matrix1Row][matrix2Column] % 2;
			}
		}

		return result;
	}

	/**
	 * Generates a random square matrix of size dim x dim
	 * @param mA Placeholder matrix of size dim x dim
	 * @param mT Placeholder matrix of size dim x dim
	 * @param dim Desired size of returned matrix 
	 * @return Randomly generated square matrix of size dim x dim
	 */
	public static int[][] getRandMatrix(int[][] mA, int[][] mT, int dim) {
		ArrayList<int[][]> matrices = genMatrix(mA, mT, dim);
		return multiply(matrices.get(0), matrices.get(1));
	}

	/**
	 * Helper mathod for getRandMatrix 
	 * @param mA Placeholder matrix of size dim x dim
	 * @param mT Placeholder matrix of size dim x dim
	 * @param dim Size of matrix at current iteration 
	 * @return Randomly generated square matrix of size dim x dim
	 */
	public static ArrayList<int[][]> genMatrix(int[][] mA, int[][] mT, int dim) {
		if (dim == 1) {
			mA[0][0] = 1;
			mT[0][0] = 1;
		} else if (dim > 1)	{
			int[] vector = new int[dim];
			int vecIndex, firstNonzero;

			do {
				for (vecIndex = 0; vecIndex < vector.length; vecIndex++)
					vector[vecIndex] = (int)(Math.random() * 2);
			} while (isZeroVector(vector));

			vecIndex = 0;
			firstNonzero = vector.length;

			while (firstNonzero == vector.length) {
				if (vector[vecIndex] != 0)
					firstNonzero = vecIndex;

				vecIndex++;
			}

			int[] firstRow = new int[dim];
			firstRow[firstNonzero] = 1;

			mA[0] = firstRow;
			mT[firstNonzero] = vector;

			int copyingRow, copyingColumn;
			int newRow = 0;
			int newColumn = 0;

			// create the minors
			int[][] newMatrixA = new int[mA.length - 1][mA[0].length - 1];
			int[][] newMatrixT = new int[mT.length - 1][mT[0].length  - 1];

			newRow = 0;
			newColumn = 0;
			copyingRow = 0;
			copyingColumn = 0;

			ArrayList<int[][]> returned = genMatrix(newMatrixA, newMatrixT, dim - 1); 
			newMatrixA = returned.get(0);
			for (newRow = 1; newRow < mA.length; newRow++) {
				for (newColumn = 0, copyingColumn = 0; newColumn < mA[0].length; newColumn++) {
					if (newColumn != firstNonzero) {
						mA[newRow][newColumn]= newMatrixA[copyingRow][copyingColumn];
						copyingColumn++;
					}
				}
				copyingRow++;
			}

			newMatrixT = returned.get(1);
			copyingRow = copyingColumn = 0;
			for (newRow = 0; newRow < mT.length; newRow++) {
				if (newRow != firstNonzero) {
					for (newColumn = 0, copyingColumn = 0; newColumn < mT[0].length; newColumn++) {
						if (newColumn != firstNonzero) {
							mT[newRow][newColumn] = newMatrixT[copyingRow][copyingColumn];
							copyingColumn++;
						}
					}
					copyingRow++;
				}
			}
		}

		ArrayList<int[][]> arrays = new ArrayList<int[][]>();
		arrays.add(mA);
		arrays.add(mT);
		return arrays;
	}

	/**
	 * Tests whether or not vector is all zeroes
	 * @param vector Vector to check
	 * @return Boolean value indicating whether or not vector is all zeroes
	 */
	public static boolean isZeroVector(int[] vector) {
		int index;
		for (index = 0; index < vector.length; index++)	{
			if (vector[index] != 0)
				return false;
		}
		return true;
	}

	public static int[][] invert(int[][] matrix) {
		// System.out.println("inverting:");
		// print(matrix);

		@SuppressWarnings("unused")
		int[][] orig = matrix;
		int[][] newMatrix = new int[matrix.length][2 * matrix.length];

		for (int addOne = 0; addOne < matrix.length; addOne++)
			newMatrix[addOne][matrix.length + addOne] = 1;

		int copyingRow, copyingColumn;

		for (copyingRow = 0; copyingRow < matrix.length; copyingRow++)
			for (copyingColumn = 0; copyingColumn < matrix.length; copyingColumn++)
				newMatrix[copyingRow][copyingColumn] = matrix[copyingRow][copyingColumn];

		matrix = newMatrix;
		// System.out.println("row reducing:");
		// print(matrix);  

		int searchRow;

		for (int j = 0; j < matrix.length; j++)	{
			searchRow = j;

			/*
			 * boolean found = false;
			 * while(found == false && k < matrix.length) {
			 * 		if(matrix[k][j] == 1) {
			 * 			searchRow = k;
			 * 			found = true;
			 * 		}
			 * 		k++;
			 * }
			 */

			for (int k = searchRow; k < matrix.length; k++)	{
				if (matrix[k][j] == 1)
					searchRow = k;
			}

			if (matrix[searchRow][j] == 1) {
				int[] temp = matrix[searchRow];
				matrix[searchRow] = matrix[j];
				matrix[j] = temp;

				/* for (int u = j + 1; u < matrix.length; u++) {
				 * 		for (int matrCol = 0; matrCol < matrix[0].length; matrCol++) {
				 * 			System.out.println("multiplying " + matrix[u][j] + " which is at " + u + ", " + j + " by entry at " + j + ", " + matrCol + " and assigning to " + u + ", " + matrCol);
				 * 			matrix[u][matrCol] ^= (matrix[u][j] * matrix[j][matrCol]);
				 * 		}
				 * }
				 * 
				 * System.out.println("now matrix is:");
				 * print(matrix);
				 */
			}

			int clearingCol = j;
			for (int clearingRow = clearingCol + 1; clearingRow < matrix[0].length/2; clearingRow++) {
				if (matrix[clearingRow][clearingCol] == 1)
					matrix[clearingRow] = add(matrix[clearingRow], matrix[clearingCol]);
			}
		}

		// System.out.println("after clearing below");
		// print(matrix);

		int pivotCol = matrix[0].length / 2 - 1;
		int pivotRow = matrix.length - 1;

		int currentRow, currentCol;

		for (currentCol = pivotCol; currentCol >= 0; currentCol--) {
			for (currentRow = matrix.length - 2; currentRow >= 0; currentRow--) {
				if (matrix[currentRow][currentCol] == 1 && (currentRow != pivotRow)) {
					for (int addCol = 0; addCol < matrix[0].length; addCol++)
						matrix[currentRow][addCol] ^= matrix[pivotRow][addCol];
				}	
				pivotCol--;
			}
			pivotRow--;
		}

		int[][] matrixInverse = new int[matrix.length][matrix[0].length / 2];
		for (int inverseRow = 0; inverseRow < matrix.length; inverseRow++) 
			for (int inverseColumn = matrix.length; inverseColumn < 2 * matrix.length; inverseColumn++)
				matrixInverse[inverseRow][inverseColumn - matrix.length] = matrix[inverseRow][inverseColumn];

		matrix = matrixInverse;
		return matrix;
	}

	/**
	 * Creates parity check matrix for Goppa code with given parameters
	 * @param numErrors the number of errors the Goppa code should be able to correct
	 * @param extDegree the extension degree that defines the size of the finite field
	 * @param suppSize  the support size
	 * @return Parity check matrix of Goppa code
	 */
	public int[][] getParityCheck(int extenDegree, int numErrors_, int suppSize) {
		gF = new GaloisField(extenDegree);
		extDegree = extenDegree;
		numErrors = numErrors_;
		irredPoly = Poly.getIrredPoly(numErrors, gF.gf_irredPoly);
		support = gF.getSupport(irredPoly, suppSize);

		// System.out.println("support:");
		// print(support);

		Poly[][] mat1 = new Poly[irredPoly.degree][irredPoly.degree];
		Poly[][] mat2 = new Poly[irredPoly.degree][support.length];
		Poly[][] mat3 = new Poly[support.length][support.length];

		// matrix 1 is a lower triangular matrix with the coefficients of the irredPoly
		for (int i = 0; i < mat1.length; i++) {
			for (int j = 0; j <= i; j++) {
				if (j == i) { 
					// mat1[i][j] = new Polynomial(1);
					mat1[i][j] = Poly.onePoly;
				} else {
					// mat1[i][j] = irredPoly.coefficients[irredPoly.degree - i];
					// mat1[i][j] = new Polynomial(irredPoly.coefficients >> (irredPoly.degree - (i -j)) & 1);
					mat1[i][j] = new Poly(new gfPoly[] {irredPoly.coefficients[irredPoly.degree - (i - j)]}) ;
				}
			}

			for (int j = i + 1; j < mat1[0].length; j++) {
				// mat1[i][j] = new Polynomial(0);
				mat1[i][j] = Poly.zeroPoly;
			}
		}

		// matrix2 has the powers of each element of the support
		for (int i = 0; i < mat2.length; i++)
			for (int j = 0; j < mat2[0].length; j++)
				mat2[i][j] = new Poly(gfPoly.toPower(support[j], gF.gf_irredPoly, i));

		// matrix3 has inverses of g(support) on diagonal 
		for (int i = 0; i < mat3.length; i++) {
			for (int j = 0; j < mat3[0].length; j++) {
				if (i == j)	{
					gfPoly entry = Poly.eval(irredPoly, support[i], gF.gf_irredPoly);
					// entry = Polynomial.getRemainder(entry, gF.gf_irredPoly);

					mat3[i][j] = new Poly(gfPoly.getModularInverse(entry, gF.gf_irredPoly));
				} else {
					// mat3[i][j] = new Polynomial(0);
					mat3[i][j] = Poly.zeroPoly;
				}
			}
		}

		// multiply matrices and reduce each polynomial entry mod irredPoly
		// Poly[][] polyMatr = reduce(multiply(multiply(mat1, mat2), mat3), irredPoly);
		Poly[][] polyMatr = multiply(multiply(mat1, mat2), mat3);
		polyParity = polyMatr;

		int pcRow = 0;
		int pcCol = 0;

		int[][] parityCheckMatrix = new int[irredPoly.degree * extDegree][support.length];
		for (int i = 0; i < polyMatr.length; i++) {
			for (int j = 0; j < polyMatr[0].length; j++) {
				// pcRow = irredPoly.degree*i?
				pcRow = gF.gf_irredPoly.degree*i;
				pcCol = j;
				for (int k = 0; k < gF.gf_irredPoly.degree; k++) {
					if (k <= polyMatr[i][j].coefficients[0].degree)
						parityCheckMatrix[pcRow][pcCol] = (polyMatr[i][j].coefficients[0].coeffs >> k) & 1;
					else
						parityCheckMatrix[pcRow][pcCol] = 0;
					pcRow++;
				}
			}
		}

		parityCheckMatrix = rowReduce(parityCheckMatrix, parityCheckMatrix[0].length - parityCheckMatrix.length);
		return parityCheckMatrix;
	}

	/**
	 * Multiplies two matrices whose entries are polynomials over finite fields
	 * @param matrix1 First matrix to multiply
	 * @param matrix2 Second matrix to multiply
	 * @return Matrix product with entries reducted over finite field
	 */
	public Poly[][] multiply(Poly[][] matrix1, Poly[][] matrix2) {
		if (matrix1[0].length != matrix2.length)
			return null;

		Poly[][] result = new Poly[matrix1.length][matrix2[0].length];

		int matrix1Row, matrix2Column, iterIndex;

		for (matrix1Row = 0; matrix1Row < matrix1.length; matrix1Row++)	{
			for (matrix2Column = 0; matrix2Column < matrix2[0].length; matrix2Column++)	{
				// result[matrix1Row][matrix2Column] = new Polynomial(0);
				result[matrix1Row][matrix2Column] = Poly.zeroPoly;

				for (iterIndex = 0; iterIndex < matrix2.length; iterIndex++)
					result[matrix1Row][matrix2Column] = Poly.add(result[matrix1Row][matrix2Column],	Poly.multiply(matrix1[matrix1Row][iterIndex], matrix2[iterIndex][matrix2Column], gF.gf_irredPoly));
			}
		}
		return result;
	}

	/**
	 * Reduce matrix of polynomials over a finite field modulo an irreducible polynomial over
	 * the finite field
	 * @param matr Matrix to reduce 
	 * @param irredPoly Polynomial modulus 
	 * @return Matrix with entries reduces modulo irredPoly
	 */
	public Poly[][] reduce(Poly[][] matr, Poly irredPoly) {
		for (int i = 0; i < matr.length; i++)
			for (int j = 0; j < matr[0].length; j++)
				matr[i][j] = Poly.getRemainder(matr[i][j], irredPoly, gF.gf_irredPoly);
		return matr;
	}

	/**
	 * Row reduces the given matrix from the starting column to the end
	 * @param matrix Matrix to reduce
	 * @param startingCol Column to start at 
	 * @return Matrix with rows in rref starting at startingCol
	 */
	public static int[][] rowReduce(int[][] matrix, int startingCol) {
		int searchRow;

		for (int j = startingCol; j < matrix[0].length; j++) {        
			// find a 1 in column j
			searchRow = j - startingCol; // ???

			for (int k = j - startingCol; k < matrix.length; k++) {
				if (matrix[k][j] == 1)
					searchRow = k;
			}

			// if we found one, swap it so it's in the right position 
			if (matrix[searchRow][j] == 1) {
				int[] temp = matrix[searchRow];
				matrix[searchRow] = matrix[j - startingCol];
				matrix[j - startingCol] = temp;

				// zero everything below it?          
				for (int i = j - startingCol + 1; i < matrix.length; i++) {
					if (matrix[i][j] == 1) {
						for (int add = 0; add < matrix[0].length; add++)
							matrix[i][add] ^= matrix[j - startingCol][add];
					}
				}
			}
		}


		/*
	     for (currentCol = pivotCol; currentCol >= 0; currentCol--) {
	        for (currentRow = matrix.length - 2; currentRow >= 0; currentRow--) {
	           if (matrix[currentRow][currentCol] == 1 && (currentRow != pivotRow)) {
	              for (int addCol = 0; addCol < matrix[0].length; addCol++)
	                 matrix[currentRow][addCol] ^= matrix[pivotRow][addCol];
	           }
	           pivotCol--;
	        }
	        pivotRow--;
	     }
		 */

		// reduced row echelon form
		for (int matrCol = startingCol; matrCol < matrix[0].length; matrCol++) {
			for (int matrRow = 0; matrRow < matrCol - startingCol; matrRow++) {
				// if upper entries are 1's, add the proper row to make them zero
				if (matrix[matrRow][matrCol] == 1) {
					// add row matrCol to matrRow??
					for (int i = 0; i < matrix[0].length; i++)
						matrix[matrRow][i] ^= matrix[matrCol - startingCol][i];
				}
			}
		}

		return matrix;
	}

	/* 
	 * DECRYPTION STUFF 
	 */

	/**
	 * Calculated error locator polynomial for debugging purposes
	 * @param errorVector Error vector 
	 * @return Polynomial with roots corresponding to locations of 1s in errorVector
	 */
	public Poly getErrorLoc(int[] errorVector) {
		Poly errorLoc = new Poly(Poly.onePoly, 0);
		Poly factor = new Poly();

		@SuppressWarnings("unused")
		int weight = 0;

		for (int i = 0; i < errorVector.length; i++) {
			if (errorVector[i] == 1) {
				weight++;
				factor = Poly.add(Poly.xPoly, new Poly(support[i]));
				errorLoc = Poly.multiply(errorLoc, factor, gF.gf_irredPoly);
			}
		}
		return errorLoc;
	}

	/**
	 * Gets transpose of given matrix
	 * @param matrix Matrix to transpose
	 * @return Transpose of matrix
	 */
	public static int[][] getTranspose(int[][] matrix) {
		int[][] transpose = new int[matrix[0].length][matrix.length];
		for(int col = 0; col < matrix[0].length; col++)
			for(int row = 0; row < matrix.length; row++)
				transpose[col][row] = matrix[row][col];
		return transpose;
	}

	/**
	 * Calculates syndrome polynomial of a code word given the parity check matrix
	 * @param parityCheck Parity check matrix of a Goppa code
	 * @param codeWord Encrypted message to calculate syndrome of
	 * @return Syndrome polynomial of given code word
	 */
	@SuppressWarnings("static-access")
	public Poly getSyndrome(int[][] parityCheck, int[] codeWord) {
		Poly syndrome = new Poly();
		Poly newFact;

		// if code word vector is 1, then add the factor	  
		for (int parityCol = 0; parityCol < polyParity[0].length; parityCol++) {
			if (codeWord[parityCol] == 1) {
				for (int parityRow = 0; parityRow < polyParity.length; parityRow++)	{
					newFact = Poly.toPower(Poly.xPoly, gF.gf_irredPoly, polyParity.length - parityRow - 1);
					newFact = Poly.multiply(newFact, polyParity[parityRow][parityCol], gF.gf_irredPoly);
					syndrome = syndrome.add(syndrome, newFact);
				}
			}
		}
		return syndrome;
	}

	/**
	 * Decodes a code word using the Patterson decoding algorithm
	 * @param codeWord Code word to decode
	 * @param parityCheck Parity check matrix of Goppa code 
	 * @return Original message that was encrypted into the given code word
	 */
	@SuppressWarnings({ "unused" })
	public int[] pattersonDecode(int[] codeWord, int[][] parityCheck) {
		// Multiply ciphertext by inverse of permutation matrix
		int[][] permInverse = invert(permutation);

		/*
		 * int[][] codeTemp = new int[codeWord.length][1];
		 * for (int copyIndex = 0; copyIndex < codeWord.length; copyIndex++)
		 * 		codeTemp[copyIndex][0] = codeWord[copyIndex];
		 */

		int[][] codeTemp = new int[1][codeWord.length];
		codeTemp[0] = codeWord;

		// multiply by permInverse or perm?
		codeTemp = multiply(codeTemp, permInverse);

		/* 
		 * for (int k = 0; k < codeWord.length; k++)
		 * 		codeWord[k] = codeTemp[k][0];
		 */

		codeWord = codeTemp[0];

		int[] decoded = new int[genMatrix.length];

		// Multiply ciphertext by parity check matrix to get syndrome polynomial
		Poly syndrome = getSyndrome(parityCheck, codeWord);
		Poly altSyndrome = altSyndrome(codeWord);

		// Invert syndrome polynomial mod goppa polynomial
		Poly synInverse = Poly.getModularInverse(syndrome, irredPoly, gF.gf_irredPoly).get(1);

		// add f(x) = x to inverse
		// Polynomial add = new Polynomial(synInverse.coefficients + 2);
		Poly add = Poly.add(synInverse, Poly.xPoly);

		// calculate sqrt(x + syndrome inverse)
		Poly sqrt = Poly.calcSqrt(add, irredPoly, gF.gf_irredPoly.degree, gF.gf_irredPoly);

		// calculate a and b s.t. (b * sqrt(x + synd)) = (a) mod (goppa poly)
		// where deg(a) <= floor(goppa deg / 2) and deg(b) <= floor((goppa deg - 1) / 2)
		Poly[] polys = Poly.partialGCDNew(sqrt, irredPoly, gF.gf_irredPoly);
		Poly polyA = polys[0];
		Poly polyB = polys[1];
		Poly test = Poly.multiply(polyB, sqrt, gF.gf_irredPoly);

		Poly errorLoc = Poly.multiply(polyA, polyA, gF.gf_irredPoly);
		polyB = Poly.multiply(polyB, polyB, gF.gf_irredPoly);
		polyB = Poly.multiply(Poly.xPoly, polyB, gF.gf_irredPoly);
		// errorLoc.coefficients ^= polyB.coefficients;
		errorLoc = Poly.add(errorLoc, polyB);
		Poly.getDegree(errorLoc);
		// System.out.println("ErrorLoc: " + errorLoc.toString());

		Poly trace = getTracePolynomial();

		List<gfPoly> roots = runBerl(errorLoc, 0, trace);

		int[] decryptedError = new int[codeWord.length];
		// System.out.print("1s of decrypted error: ");
		for (int i = 0; i < support.length; i++) {
			for (int k = 0; k < roots.size(); k++) {
				if (support[i].equals(roots.get(k))) {
					decryptedError[i] = 1;
					// System.out.print(i + ", ");
				}
			}
		}
		// System.out.println();
		// System.out.print("permErr 1s: " );

		int[][] permMatErr = new int[1][errorVec.length];
		for (int cop = 0; cop < errorVec.length; cop++)
			permMatErr[0][cop] = errorVec[cop];

		permMatErr = multiply(permMatErr, invert(permutation));
		/*
		for (int i = 0; i < errorVec.length; i++) {
			if (permMatErr[0][i] == 1)
				System.out.print(i + ", ");
		}
		System.out.println();
		 */

		int[][] errorMatrix = new int[decryptedError.length][1];
		for (int loc = 0; loc < decryptedError.length; loc++)
			errorMatrix[loc][0] = decryptedError[loc];

		// errorMatrix = multiply(invert(McEliece.permutation), errorMatrix);
		for (int eLoc = 0; eLoc < codeWord.length; eLoc++)
			decryptedError[eLoc] = errorMatrix[eLoc][0];

		codeWord = add(codeWord, decryptedError);
		// System.out.println("Code word:");
		// print(codeWord);

		/*
		 * int [][] codeWordMatrix = new int[1][codeWord.length];
		 * for(int convert = 0; convert < codeWord.length; convert++)
		 * 		codeWordMatrix[0][convert] = codeWord[convert];
		 */

		int messageLength = support.length - extDegree*numErrors;
		int[] recMessage = new int[messageLength];
		for (int msgIndex = 0; msgIndex < messageLength; msgIndex++)
			recMessage[msgIndex] = codeWord[msgIndex];

		// System.out.println("Getting first " + messageLength + " bits ");
		// print(recMessage);

		int[][] codeWordMatrix = new int[1][messageLength];
		for (int matIndex = 0; matIndex<messageLength; matIndex++)
			codeWordMatrix[0][matIndex] = recMessage[matIndex];

		/*
		System.out.println("getting right inverse of:");
		print(genMatrix);
		System.out.println("\n\n about to get inverse of gen");	   
		int[][] rightInverseGen = multiply(getTranspose(genMatrix), invert(multiply(genMatrix, getTranspose(genMatrix))));
		System.out.println("Inverse?");
		print(multiply(genMatrix, rightInverseGen));

		int[][] otherElt = multiply(genMatrix, getTranspose(genMatrix));
		codeWordMatrix = multiply(codeWordMatrix, rightInverseGen);
		 */

		/*
		if (!isSystematic(multiply(scramble, invert(scramble)), 0))
			System.out.println("not inverse!");
		 */

		codeWordMatrix = multiply(codeWordMatrix, invert(scramble));

		for (int k = 0; k < codeWordMatrix[0].length; k++)
			decoded[k] = codeWordMatrix[0][k];

		// System.out.println("Decrypted: ");
		// print(decoded);

		return decoded;
	}

	/*****************************Lior Added*************************************/
	public int[] pattersonDecode(List<int[]> encrypted_cipher_key) {
		int[] codeWord = encrypted_cipher_key.get(0);
		errorVec = encrypted_cipher_key.get(1);
		return pattersonDecode(codeWord, parityCheck);
	}

	/**
	 * XORs two vectors 
	 * @param vec1 First vector to add
	 * @param vec2 Second vector to add
	 * @return Result of vec1 XOR vec2
	 */
	private static int[] add(int[] vec1, int[] vec2) {
		if (vec1.length == vec2.length) {
			int[] result = new int[vec1.length];
			for (int loc = 0; loc < vec1.length; loc++)
				result[loc] = vec1[loc] ^ vec2[loc];
			return result;
		}
		return null;
	}

	/**
	 * Calculates roots of the error locator polynomial using the Berlekamp Trace Algorithm
	 * @param errorLoc Error Locator Polynomaial
	 * @param basisLoc Current location in polynomial basis 
	 * @param trace Trace polynomial
	 * @return Roots of error locator polynomial (and so locations of 1s in the error vector)
	 */
	public List<gfPoly> runBerl(Poly errorLoc, int basisLoc, Poly trace)	{
		if (errorLoc.degree <= 1) {
			List<gfPoly> roots = Poly.getRoot(errorLoc, gF.gf_irredPoly);
			return roots;
		}

		// Polynomial basis_i = new Polynomial(1L << basisLoc);
		gfPoly basis_i = gfPoly.toPower(new gfPoly("10", 1), gF.gf_irredPoly, basisLoc);
		// Polynomial basis_iplus = new Polynomial(basis_i.coefficients ^ 1);
		Poly evaluated = Poly.eval(trace, Poly.multiply(Poly.xPoly, basis_i, gF.gf_irredPoly), gF.gf_irredPoly);

		Poly sig_0 = Poly.gcd(errorLoc, evaluated, gF.gf_irredPoly);
		Poly eval2 = Poly.add(evaluated, Poly.onePoly);

		Poly sig_1 = Poly.gcd(errorLoc, eval2, gF.gf_irredPoly);

		List<gfPoly> result1 = runBerl(sig_0, basisLoc + 1, trace);
		List<gfPoly> result2 = runBerl(sig_1, basisLoc + 1, trace);
		for (int i = 0; i < result2.size(); i++)
			result1.add(result2.get(i));

		return result1;

	}

	/*
	public static List<Polynomial> berlTrace(Polynomial toFactor, Polynomial galoisMod) {
	   int r = 1;
	   Polynomial h = new Polynomial();
	   Polynomial trace = getTracePolynomial();

	   List<Polynomial> factors = new ArrayList<Polynomial>();
	   factors.add(toFactor);

	   for (int j = 0; j < extDegree; j++) {
	      if (r != toFactor.degree)
	         h = Polynomial.eval(trace, inner, mod)
	   }
	}
	 */

	/**
	 * Calculates the trace polynomial for the Berlekamp Trace Polynomial
	 * @return Trace polynomial
	 */
	public Poly getTracePolynomial() {
		Poly trace = new Poly((int) Math.pow(2, extDegree - 1));
		for (int i = 1; i <= trace.degree; i *= 2)
			trace.coefficients[i] = gfPoly.gfOne;
		return trace;
	}

	/**
	 * Checks whether or not the given matrix is in systematic form from startingCol onwards
	 * @param matr Matrix to check
	 * @param startingCol Column to start at
	 * @return Boolean value representing whether or not given matrix is systematic
	 */
	public static boolean isSystematic(int[][] matr, int startingCol) {
		for (int i = 0; i < matr.length; i++) {
			for (int j = startingCol; j < matr[0].length; j++) {
				if (i == j - startingCol) {
					if (matr[i][j] != 1)
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * Method used for debugging - checks whether or not polynomial inversion is working properly
	 * @param suppLoc Location of support element to check 
	 * @return Inverse of support polynomial at given location
	 */
	@SuppressWarnings("static-access")
	public Poly getSuppInvTest(int suppLoc) {
		Poly inv = Poly.zeroPoly;
		Poly fact = new Poly();

		for (int parityLoc = 0; parityLoc < polyParity.length; parityLoc++) {
			fact = Poly.toPower(Poly.xPoly, gF.gf_irredPoly, polyParity.length - parityLoc - 1);
			fact = Poly.multiply(fact, polyParity[parityLoc][suppLoc], gF.gf_irredPoly);
			inv = Poly.add(inv, fact);
		}

		Poly poly = Poly.xPoly;
		Poly supp = new Poly(support[suppLoc]);
		poly = poly.add(poly, supp);
		return inv;
	}

	/**
	 * Method used for debugging - calculates the syndrome a different way than getSyndrome
	 * Used to ensure syndrome calculations are correct
	 * @param encoded Code word to calculate syndrome of 
	 * @return Syndrome polynomial of encoded
	 */
	public Poly altSyndrome(int[] encoded) {
		Poly factor;
		Poly syndrome = new Poly(Poly.zeroPoly, 0);

		for (int i = 0; i < encoded.length; i++) {
			if (encoded[i] == 1) {
				factor = Poly.getModularInverse(Poly.add(Poly.xPoly, new Poly(support[i])), irredPoly, gF.gf_irredPoly).get(1);
				syndrome = Poly.add(syndrome,  factor);
			}
		}
		return syndrome;
	}
}
