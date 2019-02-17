/*
================================================================================================================
Project - Probabilistic Search

Class for Probabilistic Search Assignment; 
Run to begin Probabilistic Search. First, a random map of Flat, Hilly, Forested, and Cave cells are populated
with the frequencies of 0.2, 0.3, 0.3, 0.2 respectively. A target cell is then randomly assigned, and the
probability of each cell containing the target is calculated, assuming the target is equally likely to be in
any cell. The searcher then proceeds to search cells in order of most likely to contain the target to least
likely.

Legend:
L - Flat Land
H - Hilly
F - Forested
C - Maze of Caves
================================================================================================================
*/

package app;

import java.util.Scanner;

public class ProbabilisticSearch {

	static CellDetails[][] landscape; //Data Structure containing the grid of land
	static double[][] findingTargetProb;
	static int dimension = 50; //length and width of the grid
	static int rowTarget, colTarget; //row and column in which the target is located
	static int maximumSearchTime = dimension*dimension*100; //upperbound for maximum number of searches
	static int currentSearch; //current search iteration
	static int[] currentLocation;
	static int distanceTraveled;
	static int numTrials = 500;
	static int landMoves = 0, landCount = 0, hillMoves = 0, hillCount = 0, forestMoves = 0, forestCount = 0, caveMoves = 0, caveCount = 0, landDist = 0, hillDist = 0, forestDist = 0, caveDist = 0;
	static double landProb = 0.2, hillProb = 0.3, forestProb = 0.3, caveProb = 0.2;
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		System.out.println("Enter 1 for Stationary Target\nEnter 2 for Moving Target\nEnter 3 for Simulation\nEnter 4 for Current Location Search\nEnter any other number to quit.");
		int option = in.nextInt();
		int option2 = 0;
		if (!(option == 1 || option == 2 || option == 3 || option == 4)){
			System.exit(0);
		}
		if (option == 1 || option == 2) {
			System.out.println("Enter 1 for Rule 1, Enter 2 for Rule 2, Enter any other number to quit.");
			option2 = in.nextInt();
		}
		
		if (option == 3){ //Simulate 500 grids for each Rule, calculate average number of searches
			int rule1Count = 0;
			int rule2Count = 0;
			int moveRule1Count = 0;
			int moveRule2Count = 0;
			int locationCount = 0;
			int distanceCount = 0;
			
			landMoves = 0; landCount = 0; hillMoves = 0; hillCount = 0; forestMoves = 0; forestCount = 0; caveMoves = 0; caveCount = 0;
			for (int i = 0; i<numTrials; i++) {
				System.out.println("Rule 1, iteration: " + (i+1));
				landscape = new CellDetails[dimension][dimension];
				char type = populateLandscape();
				currentSearch = 1;
				
				while(currentSearch < maximumSearchTime) { //continues to search until target is found or 10000 cells searched
					int[] XY = pickNext();
					if(chkLandscape(XY[0],XY[1])) {
						break;
					} else {
						reCalcProb(XY[0], XY[1]);
					}
					++currentSearch;
				}
				
				if(type == 'L') {
					++landCount;
					landMoves += currentSearch;
				} else if(type == 'H') {
					++hillCount;
					hillMoves += currentSearch;
				} else if(type == 'F') {
					++forestCount;
					forestMoves += currentSearch;
				} else {
					++caveCount;
					caveMoves += currentSearch;
				}
				
				//System.out.println("Search Steps: " + currentSearch);
				rule1Count = rule1Count + currentSearch;
			}
			System.out.println("\nStationary Target Rule 1 Average: \nLand : " + (double)landMoves/landCount+" Hill : " 
			+ (double)hillMoves/hillCount+" Forest : " + (double)forestMoves/forestCount+" Cave : " + (double)caveMoves/caveCount);
			System.out.println("Weighted Average for Rule 1 is : "
			+(((double)landMoves/landCount)*landProb+((double)hillMoves/hillCount)*hillProb+((double)forestMoves/forestCount)*forestProb+((double)caveMoves/caveCount)*caveProb));
			
			
			currentSearch = 1;
			landMoves = 0; landCount = 0; hillMoves = 0; hillCount = 0; forestMoves = 0; forestCount = 0; caveMoves = 0; caveCount = 0;
			for (int i = 0; i< numTrials; i++) {
				System.out.println("Rule 2, iteration: " + (i+1));
				landscape = new CellDetails[dimension][dimension];
				char type = populateLandscape();
				currentSearch = 1;
				findingTargetProb = new double[dimension][dimension];
				
				while(currentSearch < maximumSearchTime) { //continues to search until target is found or 10000 cells searched
					int[] XY = pickNextRule2();
					if(chkLandscape(XY[0],XY[1])) {
						break;
					} else {
						reCalcProb(XY[0], XY[1]);
					}
					++currentSearch;
				}
				
				if(type == 'L') {
					++landCount;
					landMoves += currentSearch;
				} else if(type == 'H') {
					++hillCount;
					hillMoves += currentSearch;
				} else if(type == 'F') {
					++forestCount;
					forestMoves += currentSearch;
				} else {
					++caveCount;
					caveMoves += currentSearch;
				}
				//System.out.println("Search Steps: " + currentSearch);
				rule2Count = rule2Count + currentSearch;
			}
			System.out.println("\nStationary Target Rule 2 Average: \nLand : " + (double)landMoves/landCount+" Hill : " 
					+ (double)hillMoves/hillCount+" Forest : " + (double)forestMoves/forestCount+" Cave : " + (double)caveMoves/caveCount);
			System.out.println("Weighted Average for Rule 2 is : "
					+(((double)landMoves/landCount)*landProb+((double)hillMoves/hillCount)*hillProb+((double)forestMoves/forestCount)*forestProb+((double)caveMoves/caveCount)*caveProb));
			
			
			landMoves = 0; landCount = 0; hillMoves = 0; hillCount = 0; forestMoves = 0; forestCount = 0; caveMoves = 0; caveCount = 0; landDist = 0; hillDist = 0; forestDist = 0; caveDist = 0;
			for (int i = 0; i<numTrials; i++) {
				System.out.println("Location Based Action, iteration: " + (i+1));
				landscape = new CellDetails[dimension][dimension];
				char type = populateLandscape();
				currentSearch = 1;
				currentLocation = new int[]{0, 0};
				distanceTraveled = 0;
				
				while(currentSearch < maximumSearchTime) { //continues to search until target is found or 10000 cells searched
					int[] XY;
					XY = pickNextCurrentLocation();
					
					if(chkLandscape(XY[0],XY[1])) {
						break;
					} else {
						reCalcProb(XY[0], XY[1]);
					}
					++currentSearch;
				}
				
				if(type == 'L') {
					++landCount;
					landMoves += currentSearch;
					landDist += distanceTraveled;
				} else if(type == 'H') {
					++hillCount;
					hillMoves += currentSearch;
					hillDist += distanceTraveled;
				} else if(type == 'F') {
					++forestCount;
					forestMoves += currentSearch;
					forestDist += distanceTraveled;
				} else {
					++caveCount;
					caveMoves += currentSearch;
					caveDist += distanceTraveled;
				}
				
				//System.out.println("Search Steps + Distance Traveled = " + currentSearch + " + " + distanceTraveled + " = " + (currentSearch+distanceTraveled));
				locationCount = locationCount + currentSearch;
				distanceCount = distanceCount + distanceTraveled;
			}
			System.out.println("\nStationary Target Location Based Action Average for Number of Moves+Dist Travelled : \nLand : " + (double)(landMoves+landDist)/landCount+" Hill : " 
					+ (double)(hillMoves+hillDist)/hillCount+" Forest : " + (double)(forestMoves+forestDist)/forestCount+" Cave : " + (double)(caveMoves+caveDist)/caveCount);
			System.out.println("Weighted Average for Location Based Action for Number of Moves+Dist Travelled  is : "
					+(((double)(landMoves+landDist)/landCount)*landProb+((double)(hillMoves+hillDist)/hillCount)*hillProb+((double)(forestMoves+forestDist)/forestCount)*forestProb+((double)(caveMoves+caveDist)/caveCount)*caveProb));
			System.out.println("\nStationary Target Location Based Action Average for Number of Moves : \nLand : " + (double)landMoves/landCount+" Hill : " 
					+ (double)hillMoves/hillCount+" Forest : " + (double)forestMoves/forestCount+" Cave : " + (double)caveMoves/caveCount);
			System.out.println("Weighted Average for Location Based Action for Number of Moves is : "
					+(((double)landMoves/landCount)*landProb+((double)hillMoves/hillCount)*hillProb+((double)forestMoves/forestCount)*forestProb+((double)caveMoves/caveCount)*caveProb));
			
			for (int i = 0; i<numTrials; i++) {
				System.out.println("Moving Target Rule 1, iteration: " + (i+1));
				landscape = new CellDetails[dimension][dimension];
				populateLandscape();
				currentSearch = 1;
				findingTargetProb = new double[dimension][dimension];
				
				while(currentSearch < maximumSearchTime) { //continues to search until target is found or 10000 cells searched
					int[] XY = pickNext();
					if(chkLandscape(XY[0],XY[1])) {
						break;
					} else {
						pickMovingNext("type1");
					}
					++currentSearch;
				}
				
				//System.out.println("Search Steps: " + currentSearch);
				moveRule1Count = moveRule1Count + currentSearch;
			}
			
			for (int i = 0; i<numTrials; i++) {
				System.out.println("Moving Target Rule 2, iteration: " + (i+1));
				landscape = new CellDetails[dimension][dimension];
				populateLandscape();
				currentSearch = 1;
				findingTargetProb = new double[dimension][dimension];
				
				while(currentSearch < maximumSearchTime) { //continues to search until target is found or 10000 cells searched
					int[] XY = pickNextRule2();
					if(chkLandscape(XY[0],XY[1])) {
						break;
					} else {
						pickMovingNext("type2");
					}
					++currentSearch;
				}
				
				//System.out.println("Search Steps: " + currentSearch);
				moveRule2Count = moveRule2Count + currentSearch;
			}
			
			System.out.println("Stationary Target Rule 1 Average: " + (double)rule1Count/numTrials);
			System.out.println("Stationary Target Rule 2 Average: " + (double)rule2Count/numTrials);
			System.out.println("Location Based Search Average: " + (double)locationCount/numTrials + " + " + (double)distanceCount/numTrials + " = "  +  ((double)locationCount/numTrials + (double)distanceCount/numTrials));
			System.out.println("Moving Target Rule 1 Average: " + (double)moveRule1Count/numTrials);
			System.out.println("Moving Target Rule 2 Average: " + (double)moveRule2Count/numTrials);
			System.exit(0);
			
		} 
		
		landscape = new CellDetails[dimension][dimension];
		populateLandscape();
		printLandscape();
		currentSearch = 1;
		
		if (option == 1) {
			if (option2 == 1) {
				while(currentSearch < maximumSearchTime) { //continues to search until target is found or 10000 cells searched
					int[] XY = pickNext();
					System.out.println("Checking ... "+(XY[0]+1)+" - "+(XY[1]+1)+" Count: "+currentSearch + " Land Type: " + landscape[XY[0]][XY[1]].type + " Relative Probability: " + landscape[XY[0]][XY[1]].relativeProb);
					//System.out.println("Relative Prob of [0][0]: " + landscape[0][0].relativeProb);
					//if (landscape[0][0].relativeProb < 0 ) System.exit(0);
					//System.out.println("Actual Target Location :: "+(rowTarget+1)+"-"+(colTarget+1) + " Land Type: " + landscape[rowTarget][colTarget].type);
					if(chkLandscape(XY[0],XY[1])) {
						System.out.println("Target Found !!!! @ Row - "+(XY[0]+1)+" & Col - "+(XY[1]+1)+" Count: "+currentSearch);
						System.out.println("Actual Target Location :: "+(rowTarget+1)+"-"+(colTarget+1));
						System.out.println();
						break;
					} else {
						reCalcProb(XY[0], XY[1]);
					}
					++currentSearch;
				}
			} else if (option2 == 2) {
				findingTargetProb = new double[dimension][dimension];
				
				while(currentSearch < maximumSearchTime) { //continues to search until target is found or 10000 cells searched
					int[] XY = pickNextRule2();
					System.out.println("Checking ... "+(XY[0]+1)+" - "+(XY[1]+1)+" Count: "+currentSearch);
					if(chkLandscape(XY[0],XY[1])) {
						System.out.println("Target Found !!!! @ Row - "+(XY[0]+1)+" & Col - "+(XY[1]+1)+" Count: "+currentSearch);
						System.out.println("Actual Target Location :: "+(rowTarget+1)+"-"+(colTarget+1));
						System.out.println();
						break;
					} else {
						reCalcProb(XY[0], XY[1]);
					}
					++currentSearch;
				}
				
			} else {
				System.exit(0);
			}
			
		} else if (option == 2) {
			if(option2 == 1) {
				while(currentSearch < maximumSearchTime) { //continues to search until target is found or 10000 cells searched
					int[] XY;
					XY = pickMovingNext("type1");
					System.out.println("Checking ... "+(XY[0]+1)+" - "+(XY[1]+1)+" Count: "+currentSearch);
					if(chkLandscape(XY[0],XY[1])) {
						System.out.println("Target Found !!!! @ Row - "+(XY[0]+1)+" & Col - "+(XY[1]+1)+" Count: "+currentSearch);
						System.out.println("Actual Target Location :: "+(rowTarget+1)+"-"+(colTarget+1));
						System.out.println();
						break;
					}
					++currentSearch;
				}	
			} else if(option2 == 2) {
				
				findingTargetProb = new double[dimension][dimension];
				
				while(currentSearch < maximumSearchTime) { //continues to search until target is found or 10000 cells searched
					int[] XY;
					XY = pickMovingNext("type2");
					System.out.println("Checking ... "+(XY[0]+1)+" - "+(XY[1]+1)+" Count: "+currentSearch);
					if(chkLandscape(XY[0],XY[1])) {
						System.out.println("Target Found !!!! @ Row - "+(XY[0]+1)+" & Col - "+(XY[1]+1)+" Count: "+currentSearch);
						System.out.println("Actual Target Location :: "+(rowTarget+1)+"-"+(colTarget+1));
						System.out.println();
						break;
					}
					++currentSearch;
				}	
			}
		} else if (option == 4) {
			currentLocation = new int[]{0, 0};
			distanceTraveled = 0;
			
			while(currentSearch < maximumSearchTime) { //continues to search until target is found or 10000 cells searched
				int[] XY;
				XY = pickNextCurrentLocation();
				
				System.out.println("Checking ... "+(XY[0]+1)+" - "+(XY[1]+1)+" Count: "+currentSearch);
				if(chkLandscape(XY[0],XY[1])) {
					System.out.println("Target Found !!!! @ Row - "+(XY[0]+1)+" & Col - "+(XY[1]+1)+" Count: "+currentSearch);
					System.out.println("Actual Target Location :: "+(rowTarget+1)+"-"+(colTarget+1));
					System.out.println();
					break;
				} else {
					reCalcProb(XY[0], XY[1]);
				}
				++currentSearch;
			}
			
			System.out.println("Total number of searaches: " + currentSearch + " + " + distanceTraveled + " = " + (currentSearch + distanceTraveled));
			
		}
		
		if(currentSearch >= maximumSearchTime) {
			System.out.println("Couldn't find the target after "+maximumSearchTime+" number of moves..");
		}
		in.close();

	}
	
	/*
	 * Function to randomly populate the Grid of land, as well as randomly assign target
	 */
	public static char populateLandscape() {
		char type;
		double probForFind;
		double relativeProb = (double)(1.0/(dimension*dimension));
		rowTarget = (int) Math.floor(Math.random() * (dimension));
		colTarget = (int) Math.floor(Math.random() * (dimension));
		//System.out.println("Target Location :: "+(rowTarget+1)+"-"+(colTarget+1));
		for(int i = 0; i < dimension; i++) {
			for(int j=0; j < dimension; j++) {
				double rand = Math.random();
				if(rand <= landProb) {
					type = 'L';	// Flat Land
					probForFind = 0.99;
				} else if(rand > landProb && rand <= landProb+hillProb) {
					type = 'H';	// Hilly Area
					probForFind = 0.9;
				} else if(rand > landProb+hillProb && rand <= landProb+hillProb+forestProb) {
					type = 'F';	// Forest
					probForFind = 0.1;
				} else {
					type = 'C';	// Caves and tunnels
					probForFind = 0.01;
				}
				
				if(rowTarget == i && colTarget == j) {
					landscape[i][j] = new CellDetails(type,probForFind,relativeProb,true);
				} else {
					landscape[i][j] = new CellDetails(type,probForFind,relativeProb,false);
				}
				
				//landscape[i][j].probBeliefOverTime[0] = landscape[i][j].relativeProb;
			}
		}
		
		//System.out.println("Count :: Flat Land - "+lCount+" Hilly - "+hCount+" Forest - "+fCount+" Caves "+cCount);
		
		return landscape[rowTarget][colTarget].type;
	}
	
	/*
	 * Function to print the Grid of Land
	 */
	public static void printLandscape() {	
		System.out.print("   ");
		for(int i = 0; i < dimension; i++) {
			if(i < 10)
				System.out.print((i+1)+"      ");
			else
				System.out.print((i+1)+"     ");
		}
		System.out.println();
		for(int i = 0; i < dimension; i++) {
			if(i < 9){
				System.out.print((i+1)+"  ");	
			} else {
				System.out.print((i+1)+" ");
			}
			
			for(int j=0; j < dimension; j++) {
				System.out.print(landscape[i][j].toString()+"  ");
			}
			System.out.println();
		}
	}
	
	/*
	 * Searches a cell, returns true if the cell contains the target AND search was successful based on search probability 
	 */
	public static boolean chkLandscape(int row, int col) {
		if(landscape[row][col].target && landscape[row][col].probForFind >= Math.random()) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * Function to find the cell with the greatest likelihood of containing the target cell
	 */
	public static int[] pickNext() {
		int[] XY = new int[2];
		double nextCell=0.0;
		for(int i=0; i<dimension; i++) {
			for(int j=0; j<dimension; j++) {
				//System.out.println(landscape[i][j].relativeProb[currentSearch-1]);
				if(nextCell < landscape[i][j].relativeProb || nextCell == 0.0) {
					nextCell = landscape[i][j].relativeProb;
					XY = new int[]{i,j};
				}
			}
		}
		return XY;
	}
	
	/*
	 * Function to find the cell with the greatest likelihood of finding the target cell after searching
	 */
	public static int[] pickNextRule2() {
		int[] XY = new int[2];
		double nextCell=0.0;
		for(int i=0; i<dimension; i++) {
			for(int j=0; j<dimension; j++) {
				double scale = landscape[i][j].probForFind;
				findingTargetProb[i][j] = scale*landscape[i][j].relativeProb;
				if(nextCell < findingTargetProb[i][j]) {
					nextCell = findingTargetProb[i][j];
					XY = new int[]{i,j};
				}
			}
		}
		return XY;
	}
	
	/*
	 * Function to find the next cell with the greatest likelihood of containing the target cell while considering distance needed to travel
	 */
	public static int[] pickNextCurrentLocation() {
		int XY[] = new int[2];
		double nextCell = 0.0;
		double distance = 0.0;
		int d = 0;
		
		//nextCell = (landscape[0][0].relativeProb - landscape[0][0].relativeProb * (1-landscape[0][0].probForFind) / (distance+1));
		for (int i=0; i<dimension; i++) {
			for (int j=0; j<dimension; j++) {
				distance = Math.abs((i-currentLocation[0])) + Math.abs((j-currentLocation[1]));
				
				if(nextCell < (landscape[i][j].relativeProb * landscape[i][j].probForFind / (distance + 1.0))) { 
				// Math.pow(landscape[i][j].probForFind, (distance)) / (distance-1.0))){
					nextCell = (landscape[i][j].relativeProb * landscape[i][j].probForFind / (distance + 1.0)); 
				//Math.pow(landscape[i][j].probForFind, (distance)) / (distance-1.0));
					XY = new int[]{i,j};
					d = (int)distance;
				}
			}
		}
		currentLocation[0] = XY[0];
		currentLocation[1] = XY[1];
		distanceTraveled = distanceTraveled + d;
		return XY;
				
	}
	
	public static int[] pickMovingNext(String type) {
		int[] XY = new int[2];
		String move = move();
		
		char type1 = move.split("-")[0].charAt(0);
		char type2 = move.split("-")[1].charAt(0);
		double extra = 0.0;
		
		for(int i=0; i<dimension; i++) {
			for(int j=0; j<dimension; j++) {
				double surroundings = 0.0;
				
				if(landscape[i][j].relativeProb != 0.0) {
					
					if(i>0 && (landscape[i-1][j].type == type1 || landscape[i-1][j].type == type2)) {
						++surroundings;
					}
					if(i<dimension-1 && (landscape[i+1][j].type == type1 || landscape[i+1][j].type == type2)) {
							++surroundings;				
					}
					if(j>0 && (landscape[i][j-1].type == type1 || landscape[i][j-1].type == type2)) {
						++surroundings;
					}
					if(j<dimension-1 && (landscape[i][j+1].type == type1 || landscape[i][j+1].type == type2)) {
						++surroundings;
					}
					
					if(surroundings>0) {
						if(i>0 && (landscape[i-1][j].type == type1 || landscape[i-1][j].type == type2)) {
							landscape[i-1][j].relativeProb += landscape[i][j].relativeProb/surroundings;
						}
						if(i<dimension-1 && (landscape[i+1][j].type == type1 || landscape[i+1][j].type == type2)) {
							landscape[i+1][j].relativeProb += landscape[i][j].relativeProb/surroundings;			
						}
						if(j>0 && (landscape[i][j-1].type == type1 || landscape[i][j-1].type == type2)) {
							landscape[i][j-1].relativeProb += landscape[i][j].relativeProb/surroundings;
						}
						if(j<dimension-1 && (landscape[i][j+1].type == type1 || landscape[i][j+1].type == type2)) {
							landscape[i][j+1].relativeProb += landscape[i][j].relativeProb/surroundings;
						}
						landscape[i][j].relativeProb = 0.0;
					} else {
						extra += landscape[i][j].relativeProb;
						landscape[i][j].relativeProb = 0.0;
					}
					
				}
			}
		}
		
		if(extra != 0.0) {
			double nonEmptyCount = 0.0;
			for(int i=0; i<dimension; i++) {
				for(int j=0; j<dimension; j++) {
					if(landscape[i][j].relativeProb != 0.0) {
						++nonEmptyCount;
					}
				}
			}
			
			for(int i=0; i<dimension; i++) {
				for(int j=0; j<dimension; j++) {
					if(landscape[i][j].relativeProb != 0.0) {
						landscape[i][j].relativeProb = landscape[i][j].relativeProb*(1.0+(extra/nonEmptyCount));
					}
				}
			}
			
		}
		
		if(type == "type1") {
			XY = pickNext();
		} else {
			XY = pickNextRule2();
		}
		
		return XY;
	}
	
	/*
	 * Function to move the target and return the next cell
	 */
	/*
	public static int[] pickMovingNext() {
		int[] XY = new int[2];
		String move = move();
		
		if(targetMove == "") {
			targetMove = move;
		} else {
			targetMove += "-"+move.split("-")[1];
		}
		String[] trackTarget = targetMove.split("-");
		int i=0;
		ArrayList<int[]> location = find(trackTarget[i]);
		while(! location.isEmpty() && ++i<trackTarget.length) {
			location = findNext(trackTarget,location,i);
		}
		
		for(int j=0; j<location.size(); j++) {
			if(j == 0) {
				XY = location.get(0);
			}
			int[] loc = new int[2];
			loc = location.get(j);
			
			if(landscape[XY[0]][XY[1]].relativeProb < landscape[loc[0]][loc[1]].relativeProb) {
				XY = loc;
			}
		}
		
		return XY;
	}
	
	public static ArrayList<int[]> find(String locate) {
		ArrayList<int[]> loc = new ArrayList<int[]>();
		for(int i=0; i<dimension; i++) {
			for(int j=0; j<dimension; j++) {
				if(landscape[i][j].type == locate.charAt(0)){
					loc.add(new int[] {i,j});
				}
			}
		}
		
		return loc;
	}
	
	public static ArrayList<int[]> findNext(String[] trackTarget, ArrayList<int[]> location, int pos) {
		ArrayList<int[]> targetLoc = new ArrayList<int[]>();
		char type = trackTarget[pos].charAt(0);
		
		for(int i=0; i<location.size(); i++) {
			int row = location.get(i)[0];
			int col = location.get(i)[1];
			
			if(row>0 && landscape[row-1][col].type == type) {
				targetLoc.add(new int[] {row-1,col});
			} 
			if(row<dimension-1 && landscape[row+1][col].type == type) {
				targetLoc.add(new int[] {row+1,col});
			} 
			if(col>0 && landscape[row][col-1].type == type) {
				targetLoc.add(new int[] {row,col-1});
			} 
			if(col<dimension-1 && landscape[row][col+1].type == type) {
				targetLoc.add(new int[] {row,col+1});
			}
		}
		
		return targetLoc;
	}*/
	
	/*
	 * Function to randomly move the target Cell & report the moves
	 */
	public static String move() {
		
		double rand = Math.random();
		int tempCol=colTarget, tempRow=rowTarget;
		
		if(rowTarget > 0 && rowTarget < dimension-1 && colTarget > 0 && colTarget < dimension-1) {
			if(rand <= 0.25) {
				tempRow = rowTarget-1;
			} else if(rand > 0.25 && rand <= 0.5) {
				tempCol = colTarget-1;
			} else if(rand > 0.5 && rand <= 0.75) {
				tempCol = colTarget+1;
			} else if(rand > 0.75 ) {
				tempRow = rowTarget+1;
			}
		} else if(rowTarget > 0 && rowTarget < dimension-1) {
			if(rand <= 0.33 && colTarget == 0) {
				tempCol = colTarget+1;
			} else if(rand <= 0.33 && colTarget == dimension-1) {
				tempCol = colTarget-1;
			} else if(rand > 0.33 && rand <= 0.66) {
				tempRow = rowTarget-1;
			} else {
				tempRow = rowTarget+1;
			}
		} else if(colTarget > 0 && colTarget < dimension-1) {
			if(rand <= 0.33 && rowTarget == 0) {
				tempRow = rowTarget+1;
			} else if(rand <= 0.33 && rowTarget == dimension-1) {
				tempRow = rowTarget-1;
			} else if(rand > 0.33 && rand <= 0.66) {
				tempCol = colTarget-1;
			} else {
				tempCol = colTarget+1;
			}
		} else {
			if(rowTarget == 0 && colTarget == 0) {
				if(rand <= 0.5) {
					tempRow = rowTarget+1;
				} else {
					tempCol = colTarget+1;
				}
			} else if(rowTarget == 0 && colTarget == dimension-1) {
				if(rand <= 0.5) {
					tempRow = rowTarget+1;
				} else {
					tempCol = colTarget-1;
				}
			} else if(rowTarget == dimension-1 && colTarget == 0) {
				if(rand <= 0.5) {
					tempRow = rowTarget-1;
				} else {
					tempCol = colTarget+1;
				}
			} else {
				if(rand <= 0.5) {
					tempRow = rowTarget-1;
				} else {
					tempCol = colTarget-1;
				}
			}
		}
		
		landscape[rowTarget][colTarget].target = false;
		char initialType = landscape[rowTarget][colTarget].type;
		rowTarget = tempRow; colTarget = tempCol;
		landscape[rowTarget][colTarget].target = true;
		char finalType = landscape[rowTarget][colTarget].type;
		//System.out.println("Moved :: "+(rowTarget+1)+"-"+(colTarget+1));
		return initialType+"-"+finalType;
	}
	
	/*
	 * Function to recalculate relative probabilities of containing the target after a search has completed 
	 */
	public static void reCalcProb(int row, int col) {
		double relProb = landscape[row][col].relativeProb;
		double probForFind = landscape[row][col].probForFind;
		double overallProb = 1-relProb;
		
		for(int i=0; i<dimension; i++) {
			for(int j=0; j<dimension; j++) {
					if(row!=i || col!=j) {
						//landscape[i][j].probBeliefOverTime[currentSearch] = landscape[i][j].relativeProb;
						//landscape[i][j].relativeProb = landscape[i][j].relativeProb*(1.0+((relProb*(1.0-probForFind)/overallProb)));
						landscape[i][j].relativeProb = landscape[i][j].relativeProb + (relProb*probForFind)*(landscape[i][j].relativeProb/overallProb);
						//given the target was not found in the searched cell
						//and the probability of finding the target if the target was in the cell,
						//evenly distributes the decrease in probability of the searched cell among the other cells 
					}
			}
		}
		//landscape[row][col].probBeliefOverTime[currentSearch] = landscape[row][col].relativeProb;
		landscape[row][col].relativeProb = relProb*(1.0-probForFind);
	}
	
	/*
	 * Class for the grid, contains cell type, probability for finding the target given the target is in the cell,
	 * relative probability of containing the target, an array containing all of the relative probabilities
	 * at time t, and finally a boolean for if the cell contains the target.
	 */
	static class CellDetails {
		char type;
		double probForFind;
		double relativeProb;
		//double[] probBeliefOverTime;
		boolean target;
		
		public CellDetails(char type, double prob, double relativeProb, boolean target) {
			this.type = type;
			this.probForFind = prob;
			this.relativeProb = relativeProb;
			//probBeliefOverTime = new double[maximumSearchTime];
			this.target = target;
		}
		
		public String toString() {
			return type+"-"+probForFind;
		}
	}
}