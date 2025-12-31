import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BranchPrediction {
	// input varibles
	public String predictorType;
	public int m1;
	public int m2;
	public int k;
	public int n;
	public int b;
	public String traceFile;
	// trace file variables
	public String[] nAddress;
	public String[] mAddress;
	public String[] bAddress;
	// global variables
	public String[] actual;
	public int mispredictions = 0;
	public int numPredictions;
	private static final int SATURATION_MAX = 7;
	private static final int SATURATION_MIN = 0;
	private static final int SATURATION_THRESHOLD = 4;
	private static final char TAKEN = 't';
	private static final char NOT_TAKEN = 'n';
	// smith variables
	public int counter;
	public int counterMax;
	public int counterThreshold;
	// bimodal variables
	public int[] bimodalAddress;
	public char bimodalPrediction;
	public int[] bimodalTable;
	// gshare variables
	public int[] gshareAddress;
	public char gsharePrediction;
	public int[] gshareTable;
	// hybrid variables
	public int[] chooserTable;
	public boolean bimodalTaken = true;
	public boolean gshareTaken = true;
	public int[] hybridAddress;
	public char[] historyRegister;

	// Function for the main program logic based on input arguments
	public BranchPrediction(String[] args) {
		predictorType = String.valueOf(args[0]);
		// Switch case to initialize and process based on predictor type
		switch (predictorType.toLowerCase()) {
			case "smith":
				b = Integer.valueOf(args[1]);
				traceFile = String.valueOf(args[2]);
				rTraceFile();
				smithCounterPredictor(b);
				break;
			case "bimodal":
				m2 = Integer.valueOf(args[1]);
				traceFile = String.valueOf(args[2]);
				bimodalTable = initializeIntArray((int) Math.pow(2, m2), 4);
				rTraceFile();
				bimodalAddress = new int[bAddress.length];
				for (int i = 0; i < bimodalAddress.length; i++) 
					bimodalBranchPredictor(i);
				break;
			case "gshare":
				m1 = Integer.valueOf(args[1]);
				n = Integer.valueOf(args[2]);
				traceFile = String.valueOf(args[3]);
				historyRegister = initializeCharArray(n, '0');
				gshareTable = initializeIntArray((int) Math.pow(2, m1), 4);
				rTraceFile();
				gshareAddress = new int[bAddress.length];
				nAddress = new String[bAddress.length];
				mAddress = new String[bAddress.length];
				for (int i = 0; i < bAddress.length; i++) 
					gshareBranchPredictor(i);
				break;
			case "hybrid":
				k = Integer.valueOf(args[1]);
				m1 = Integer.valueOf(args[2]);
				n = Integer.valueOf(args[3]);
				m2 = Integer.valueOf(args[4]);
				traceFile = String.valueOf(args[5]);
				rTraceFile();
				bimodalAddress = new int[bAddress.length];
				gshareAddress = new int[bAddress.length];
				hybridAddress = new int[bAddress.length];
				nAddress = new String[bAddress.length];
				mAddress = new String[bAddress.length];
				hybridBranchPredictor();
				break;
			default:
				System.out.println("Invalid predictor type: " + predictorType);
				return;
		}
		printResults();
	}

	// Function implementing Smith predictor logic
	public void smithCounterPredictor(int b) {
		counterMax = (int) Math.pow(2, b) - 1;
		counterThreshold = (int) Math.pow(2, b - 1);
		counter = counterThreshold;
		// Loop to process each branch instruction
		for (int i = 0; i < bAddress.length; i++) {
			char prediction = (counter >= counterThreshold) ? TAKEN : NOT_TAKEN;
			if (!actual[i].equalsIgnoreCase(String.valueOf(prediction))) {
				mispredictions++;
			}
			// If statements to update the counter based on actual outcome
			if (actual[i].equalsIgnoreCase("t") && counter < counterMax) {
				counter++;
			} else if (actual[i].equalsIgnoreCase("n") && counter > 0) {
				counter--;
			}
		}
	}

	// Function implementing Bimodal predictor logic for a specific address
	public void bimodalBranchPredictor(int i) {
		String value = String.format("%24s", Integer.toBinaryString(Integer.parseInt(bAddress[i], 16))).replaceAll(" ", "0");
		int stringLength = value.length();
		bimodalAddress[i] = Integer.parseInt(value.substring(stringLength - m2, stringLength), 2);
		int index = bimodalAddress[i];
		String currentPrediction = actual[i];
		if (predictorType.equalsIgnoreCase("hybrid")) {
			if (bimodalTable[index] < SATURATION_THRESHOLD && bimodalTable[index] >= SATURATION_MIN)
				bimodalPrediction = NOT_TAKEN;
			else if (bimodalTable[index] >= SATURATION_THRESHOLD && bimodalTable[index] <= SATURATION_MAX)
				bimodalPrediction = TAKEN;
		}
		switch (currentPrediction.toLowerCase()) {
			case "t": // Case for taken branch
				if (bimodalTaken) {
					for (int j=n-1; j>=0; j--) {
						if (j>0)
							historyRegister[j] = historyRegister[j-1];
						else
							historyRegister[j] = '1';
					}
					if (bimodalTable[index] < SATURATION_THRESHOLD && bimodalTable[index] >= SATURATION_MIN)
						mispredictions++;
					if (bimodalTable[index] < SATURATION_MAX)
						bimodalTable[index]++;
				}
				break;
			case "n": // Case for not taken branch
				if (bimodalTaken) {
					for (int j=n-1; j>=0; j--) {
						if (j > 0)
							historyRegister[j] = historyRegister[j-1];
						else
							historyRegister[j] = '0';
					}
					if (bimodalTable[index] >= SATURATION_THRESHOLD && bimodalTable[index] <= SATURATION_MAX)
						mispredictions++;
					if (bimodalTable[index] > SATURATION_MIN)
						bimodalTable[index]--;
				}
				break;
			default: // Default case for unexpected values
				System.out.println("Unknown branch operation: " + currentPrediction);
				break;
		}	
	}
	
	// Function implementing GShare predictor logic for a specific address
	public void gshareBranchPredictor(int i) {
		String value = String.format("%24s", Integer.toBinaryString(Integer.parseInt(bAddress[i], 16))).replaceAll(" ", "0");
		int stringLength = value.length();
		mAddress[i] = value.substring(stringLength - m1, stringLength - n);
		nAddress[i] = value.substring(stringLength - n, stringLength);
		char[] nValue = nAddress[i].toCharArray();
		StringBuilder temp = new StringBuilder();
		for (int j=0; j<historyRegister.length; j++)
			temp.append(Integer.valueOf(nValue[j]) ^ Integer.valueOf(historyRegister[j]));
		nAddress[i] = temp.toString();
		mAddress[i] = mAddress[i] + nAddress[i];
		gshareAddress[i] = Integer.parseInt(mAddress[i], 2);
		int index = gshareAddress[i];
		String currentPrediction = actual[i];
		if (predictorType.equalsIgnoreCase("hybrid")) {
			if (gshareTable[index] < SATURATION_THRESHOLD && gshareTable[index] >= SATURATION_MIN)
				gsharePrediction = NOT_TAKEN;
			else if (gshareTable[index] >= SATURATION_THRESHOLD && gshareTable[index] <= SATURATION_MAX)
				gsharePrediction = TAKEN;
		}
		switch (currentPrediction.toLowerCase()) {
			case "t": // Case for taken branch
				if (gshareTaken) {
					for (int j=n-1; j>=0; j--) {
						if (j>0)
							historyRegister[j] = historyRegister[j-1];
						else
							historyRegister[j] = '1';
					}
					if (gshareTable[index] < SATURATION_THRESHOLD && gshareTable[index] >= SATURATION_MIN)
						mispredictions++;
					if (gshareTable[index] < SATURATION_MAX)
						gshareTable[index]++;
				}
				break;
			case "n": // Case for not taken branch
				if (gshareTaken) {
					for (int j=n-1; j>=0; j--) {
						if (j>0)
							historyRegister[j] = historyRegister[j-1];
						else
							historyRegister[j] = '0';
					}
					if (gshareTable[index] >= SATURATION_THRESHOLD && gshareTable[index] <= SATURATION_MAX)
						mispredictions++;
					if (gshareTable[index] > SATURATION_MIN)
						gshareTable[index]--;
				}
				break;
			default: // Default case for unexpected values
				System.out.println("Unknown branch operation: " + currentPrediction);
				break;
		}
	}

	// Function implementing logic for the hybrid branch predictor
	public void hybridBranchPredictor() {
		chooserTable = initializeIntArray((int) Math.pow(2, k), 1);
		historyRegister = initializeCharArray(n, '0');
		gshareTable = initializeIntArray((int) Math.pow(2, m1), 4);
		bimodalTable = initializeIntArray((int) Math.pow(2, m2), 4);

		for (int i = 0; i < bAddress.length; i++) {
			String value = String.format("%22s", Integer.toBinaryString(Integer.parseInt(bAddress[i], 16))).replaceAll(" ", "0");
			int stringLength = value.length();
			bimodalAddress[i] = Integer.parseInt(value.substring(stringLength - k, stringLength), 2);
			int index = bimodalAddress[i];
			String currentPrediction = actual[i];
			if (chooserTable[index]>=2) {
				gshareTaken = true;
				bimodalTaken = false;
				gshareBranchPredictor(i);
				bimodalBranchPredictor(i);
			} else if (chooserTable[index]<2) {
				gshareTaken = false;
				bimodalTaken = true;
				gshareBranchPredictor(i);
				bimodalBranchPredictor(i);
			}
			if (currentPrediction.equalsIgnoreCase(String.valueOf(bimodalPrediction)) && !currentPrediction.equalsIgnoreCase(String.valueOf(gsharePrediction))) {
				if (chooserTable[index]<=3 && chooserTable[index]>0)
					chooserTable[index]--;
			}
			if (!currentPrediction.equalsIgnoreCase(String.valueOf(bimodalPrediction)) &&  currentPrediction.equalsIgnoreCase(String.valueOf(gsharePrediction))) {
				if (chooserTable[index]<3 && chooserTable[index]>=0)
					chooserTable[index]++;
			}
		}
	}

	// Function to read the trace file and initialize relevant data structures
	public void rTraceFile() {
		String value;
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("./" + traceFile))) {
			// Initialize variables
			List<String> addressList = new ArrayList<>();
			List<String> actualList = new ArrayList<>();
			String line;
			// Read and process each line
			while ((line = reader.readLine()) != null) {
				value = String.format("%24s", Integer.toBinaryString(Integer.parseInt(line.split(" ")[0], 16))).replaceAll(" ", "0");
				addressList.add(Integer.toString(Integer.parseInt(value.substring(0, value.length() - 2), 2), 16));
				actualList.add(line.split(" ")[1]);
			}
			// Convert lists to arrays
			bAddress = addressList.toArray(new String[0]);
			actual = actualList.toArray(new String[0]);
		} catch (IOException e) {
			System.out.println(e);
		}

		numPredictions = bAddress.length;
	}

	// Creates new int array
	private int[] initializeIntArray(int size, int defaultValue) {
		int[] array = new int[size];
		Arrays.fill(array, defaultValue);
		return array;
	}
	
	// Creates new char array
	private char[] initializeCharArray(int size, char defaultValue) {
		char[] array = new char[size];
		Arrays.fill(array, defaultValue);
		return array;
	}
	
	// Function to display the results after numPredictions
	public void printResults() {
		String mispredictionRate = String.format("%.2f", ((double) mispredictions / numPredictions) * 100);
		System.out.print("OUTPUT\r\n" + 
						 "number of predictions:         " + numPredictions + "\r\n" + 
						 "number of mispredictions:      " + mispredictions + "\r\n" + 
						 "misprediction rate:            " + mispredictionRate + "%\n");
		// Switch case to display final contents based on predictor type
		switch (predictorType.toLowerCase()) {
			case "smith":
				System.out.println("FINAL COUNTER CONTENT:         " + counter);
				break;
			case "bimodal":
				System.out.println("FINAL BIMODAL CONTENTS");
				for (int i=0; i<bimodalTable.length; i++)
					System.out.println(i + "    " + bimodalTable[i]);
				break;
			case "gshare":
				System.out.println("FINAL GSHARE CONTENTS");
				for (int i=0; i<gshareTable.length; i++)
					System.out.println(i + "    " + gshareTable[i]);
				break;
			case "hybrid":
				System.out.println("FINAL CHOOSER CONTENTS");
				for(int i=0;i<chooserTable.length;i++)
					System.out.println(i+"	"+chooserTable[i]);
				System.out.println("FINAL GSHARE CONTENTS");
				for (int i = 0; i < gshareTable.length; i++) 
					System.out.println(i + "    " + gshareTable[i]);
				System.out.println("FINAL BIMODAL CONTENTS");
				for (int i = 0; i < bimodalTable.length; i++) 
					System.out.println(i + "    " + bimodalTable[i]);
				break;
			default:
				System.out.println("Unknown predictor type: " + predictorType);
				break;
		}
	}
}
	
