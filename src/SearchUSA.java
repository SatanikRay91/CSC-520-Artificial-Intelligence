import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

public class SearchUSA {

	static HashSet<String> setOfUniqueCities;
	static HashMap<String, Integer> citiesToIDMapping;
	static HashMap<String,ArrayList<Double>> cityIDToHeuristicsMapping;
	static int [][] adjacencyMatrixForPathCostDetermination;
	static int numOfCity;
	static int cameFromParent[];
	static int pathCostFromSource[];
	static float heuristicCostToDestination[];
	static int totalPathCost;
	//Creates an unique set of cities from Roads Info data 
	public static HashSet<String> CreateSetOfUniqueCities(String roadInfoString) {
		StringTokenizer st = new StringTokenizer(roadInfoString,".");
		HashSet<String> setOfUniqueCities = new HashSet<String>();
		while(st.hasMoreTokens()) {
			String originalString = st.nextToken().trim();
			//System.out.println(originalString);
			String[] splitStringOnFirstParanthesis = originalString.split("\\(");
			String[] splitStringOnSecondParanthesis = splitStringOnFirstParanthesis[1].split("\\)");
			String[] splitStringOnComma = splitStringOnSecondParanthesis[0].split("\\,");
			setOfUniqueCities.add(splitStringOnComma[0].trim());
			setOfUniqueCities.add(splitStringOnComma[1].trim());
		}
		return setOfUniqueCities;
	}
	
	//Creates a HashMap table from unique set of cities to store city name as key and an unique Integer ID as its value
	public static HashMap<String, Integer> CreateCitiesToIDMapping(HashSet<String> setOfUniqueCities){
		HashMap<String, Integer> citiesToIDMapping = new HashMap<String, Integer>();
		for (int i = 0; i < setOfUniqueCities.size(); i++) {
			citiesToIDMapping.put(setOfUniqueCities.toArray()[i].toString(), i);
		}
		return citiesToIDMapping;
	}
	
	//Creates a HashMap table from city heuristics data to store city name as key and its latitude & longitude as its value
	public static HashMap<String,ArrayList<Double>> CreateCityIDToHeuristicsMapping(String cityHeuristicsInfo){
		StringTokenizer st = new StringTokenizer(cityHeuristicsInfo,"\\)");
		HashMap<String,ArrayList<Double>> cityIDToHeuristicsMapping = new HashMap<String,ArrayList<Double>>();
		while(st.hasMoreTokens()) {
			ArrayList<Double> coordinatesInfo = new ArrayList<Double>();	
			String originalString = st.nextToken().trim();
			String[] splitStringOnFirstParanthesis = originalString.split("\\(");
			String[] splitStringOnComma = splitStringOnFirstParanthesis[1].split("\\,");			
			String cityName = splitStringOnComma[0].trim();
			coordinatesInfo.add(Double.parseDouble(splitStringOnComma[1].trim()));
			coordinatesInfo.add(Double.parseDouble(splitStringOnComma[2].trim()));
			cityIDToHeuristicsMapping.put(cityName, coordinatesInfo);
		}
		return cityIDToHeuristicsMapping;
	}
	
	//Creates a NxN (N being number of unique cities) matrix to store path cost between cities
	public static int[][] CreateAdjacencyMatrixForPathCostDetermination(HashMap<String, Integer> citiesToIDMapping, String roadInfoString){
		int [][] adjacencyMatrixForPathCostDetermination = new int[citiesToIDMapping.size()][citiesToIDMapping.size()];
		StringTokenizer st = new StringTokenizer(roadInfoString,".");
		while(st.hasMoreTokens()) {
			String originalString = st.nextToken().trim();
			String[] splitStringOnFirstParanthesis = originalString.split("\\(");
			String[] splitStringOnSecondParanthesis = splitStringOnFirstParanthesis[1].split("\\)");
			String[] splitStringOnComma = splitStringOnSecondParanthesis[0].split("\\,");
			String sourceCityName = splitStringOnComma[0].trim();
			String destinationCityName = splitStringOnComma[1].trim();
			int costOfPathBetweenCities = Integer.parseInt(splitStringOnComma[2].trim());
			int destinationId=0,sourceId=0;
			for(java.util.Map.Entry<String, Integer> mapEntry:citiesToIDMapping.entrySet()){					
				if(mapEntry.getKey().equals(sourceCityName)) {
					sourceId = mapEntry.getValue();
				}
				if(mapEntry.getKey().equals(destinationCityName)) {
					destinationId = mapEntry.getValue();
				}
			}
			adjacencyMatrixForPathCostDetermination[destinationId][sourceId] = adjacencyMatrixForPathCostDetermination[sourceId][destinationId] = costOfPathBetweenCities;
		}
		return adjacencyMatrixForPathCostDetermination;
	}
	
	//Extracts city ID based on name from the key->value HashMap   
	public static int ExtractIDFromEdgesToIDMapping(String cityName){
		int cityId = 0;
		for(java.util.Map.Entry<String, Integer> mapEntry:citiesToIDMapping.entrySet())
		{
			if(mapEntry.getKey().equals(cityName)) {
				cityId = mapEntry.getValue();
			}
		}
		return cityId;
	}
	
	//Extracts city name based on ID from the key->value HashMap  
	public static String ExtractEdgeFromEdgesToIDMapping(Integer cityID){
		String edgeName="";
		for(java.util.Map.Entry<String, Integer> mapEntry:citiesToIDMapping.entrySet())
		{
			if(mapEntry.getValue() == cityID)
				edgeName = mapEntry.getKey();
		}
		return edgeName;
	}
	
	//Returns coordinates for a city based on its unique city ID
	public static ArrayList<Double> ExtractHeuristicsFromcityIDToHeuristicsMapping(Integer cityID){
		ArrayList<Double> coordinatesInfo = new ArrayList<Double>();
		String cityName = ExtractEdgeFromEdgesToIDMapping(cityID);
		for(java.util.Map.Entry<String,ArrayList<Double>> mapEntry:cityIDToHeuristicsMapping.entrySet())
		{
			if(mapEntry.getKey().equals(cityName))
			{
				coordinatesInfo = mapEntry.getValue();
				break;
			}
		}
		return coordinatesInfo;
	}
	
	//Returns heuristic cost between cities
	public static double GenerateHeuristicCostBetweenCities(int currentNodeId, int destinationNodeId) {
		ArrayList<Double> currentNodeHeuristics = ExtractHeuristicsFromcityIDToHeuristicsMapping(currentNodeId);
		ArrayList<Double> destinationNodeHeuristics = ExtractHeuristicsFromcityIDToHeuristicsMapping(destinationNodeId);
		double pi = 3.141593;
		double lat1=currentNodeHeuristics.get(0);
		double long1=currentNodeHeuristics.get(1);

		double lat2=destinationNodeHeuristics.get(0);
		double long2=destinationNodeHeuristics.get(1);

		double heuristicCost = Math.sqrt( Math.pow((69.5*(lat1-lat2)),2) + Math.pow((69.5*Math.cos((lat1+lat2)*pi/360)*(long1-long2)),2));
		
		return heuristicCost;
	}
	
	//Generate path from source to node
	public static ArrayList<String> GeneratePath(int destinationId){
		ArrayList<String> pathList = new ArrayList<String>();
		int currentNode = destinationId;
		while(cameFromParent[currentNode]!=-1) {
			pathList.add(ExtractEdgeFromEdgesToIDMapping(currentNode));
			currentNode = cameFromParent[currentNode];
		}
		pathList.add(ExtractEdgeFromEdgesToIDMapping(currentNode));
		System.out.print("The list of nodes expanded in the solution path is: ");
		for(int i=pathList.size()-1;i>=0;i--) {
			if (i != 0) {
				System.out.print(pathList.get(i)+",");
			}
			else {
				System.out.println(pathList.get(i));
			}
		}
		System.out.println("The number of nodes in the solution path is: "+pathList.size());
		return pathList;
	}
	
	//Greedy Best-First search (Informed search)
	public static void GreedyBestFirstSearch(String sourceCity, String destinationCity) {
		ArrayList<Integer> frontier = new ArrayList<Integer>();
		ArrayList<Integer> explored = new ArrayList<Integer>();
		totalPathCost = 0;
		for(int i=0;i<pathCostFromSource.length;i++) {
			pathCostFromSource[i]=Integer.MAX_VALUE;
		}
		int sourceId = ExtractIDFromEdgesToIDMapping(sourceCity);
		int destinationId = ExtractIDFromEdgesToIDMapping(destinationCity);
		frontier.add(sourceId);
		cameFromParent[sourceId] = -1;
		heuristicCostToDestination[sourceId] = (float) 0.0;
		pathCostFromSource[sourceId] = 0;
		while(!frontier.isEmpty()) {
			float minCost=Float.MAX_VALUE;
			int nextIndex=-1;
			for (int i = 0; i < frontier.size();i++) {
				if (heuristicCostToDestination[frontier.get(i)] < minCost) {
					minCost = (float) (heuristicCostToDestination[frontier.get(i)]);
					nextIndex = i;
				}
			}
			int highestPriorityNode = frontier.get(nextIndex);
			frontier.remove(nextIndex);
			if (highestPriorityNode == destinationId) {
				totalPathCost = pathCostFromSource[highestPriorityNode];
				//Printing a comma separated list of expanded nodes (the closed list)
				System.out.print("The list of nodes expanded is: ");
				for (int i = 0; i < explored.size(); i++) {
					if (i < explored.size() - 1) {
						System.out.print(ExtractEdgeFromEdgesToIDMapping(explored.get(i))+",");
					}
					else {
						System.out.println(ExtractEdgeFromEdgesToIDMapping(explored.get(i)));
					}
				}
				
				//Printing the number of nodes expanded
				System.out.println("The number of nodes expanded is: "+explored.size());
				//Function to generate solution path
				GeneratePath(highestPriorityNode);
				//Printing the total distance from source to destination in the solution path
				System.out.println("Total distance from source to destination in the solution path = "+totalPathCost);
				return;
			}
			else
			{
				explored.add(highestPriorityNode);
				int parentNode = highestPriorityNode;
				for (int i = 0; i < adjacencyMatrixForPathCostDetermination.length; i++) {
					if (adjacencyMatrixForPathCostDetermination[parentNode][i] != 0) {
						int newCostOfSFromSource = adjacencyMatrixForPathCostDetermination[parentNode][i] + pathCostFromSource[parentNode];
						heuristicCostToDestination[i] = (float) GenerateHeuristicCostBetweenCities(i, destinationId);
						
						if((!frontier.contains(i)) && (!explored.contains(i))) {
							frontier.add(i);
						}
						if (frontier.contains(i) && newCostOfSFromSource < pathCostFromSource[i]) {							
							pathCostFromSource[i] = newCostOfSFromSource;
							cameFromParent[i] = parentNode;
						}
					}
				}
			}
		}
	}

		//Dynamic Programming (Uninformed search)
		public static void DynamicProgramming(String sourceCity, String destinationCity) {
		ArrayList<Integer> frontier = new ArrayList<Integer>();
		ArrayList<Integer> explored = new ArrayList<Integer>();
		totalPathCost = 0;

		for(int i=0;i<pathCostFromSource.length;i++) {
			pathCostFromSource[i]=Integer.MAX_VALUE;
		}
		int sourceId = ExtractIDFromEdgesToIDMapping(sourceCity);
		int destinationId = ExtractIDFromEdgesToIDMapping(destinationCity);
		frontier.add(sourceId);
		cameFromParent[sourceId] = -1;
		heuristicCostToDestination[sourceId] = (float) 0.0;
		pathCostFromSource[sourceId] = 0;
		while(!frontier.isEmpty()) {
			int minCost=Integer.MAX_VALUE;
			int nextIndex=-1;
			for (int i = 0; i < frontier.size();i++) {
				if (pathCostFromSource[frontier.get(i)] < minCost) {
					minCost = (int) (pathCostFromSource[frontier.get(i)]);
					nextIndex = i;
				}
			}
			int highestPriorityNode = frontier.get(nextIndex);
//			System.out.println(ExtractEdgeFromEdgesToIDMapping(highestPriorityNode));
			frontier.remove(nextIndex);
			if (highestPriorityNode == destinationId) {
				totalPathCost = pathCostFromSource[highestPriorityNode];
				//Printing a comma separated list of expanded nodes (the closed list)
				System.out.print("The list of nodes expanded is: ");
				for (int i = 0; i < explored.size(); i++) {
					if (i < explored.size() - 1) {
						System.out.print(ExtractEdgeFromEdgesToIDMapping(explored.get(i))+",");
					}
					else {
						System.out.println(ExtractEdgeFromEdgesToIDMapping(explored.get(i)));
					}
				}
				
				//Printing the number of nodes expanded
				System.out.println("The number of nodes expanded is: "+explored.size());
				//Function to generate solution path
				GeneratePath(highestPriorityNode);
				//Printing the total distance from source to destination in the solution path
				System.out.println("Total distance from source to destination in the solution path = "+totalPathCost);
				return;
			}
			else
			{
				explored.add(highestPriorityNode);
				int parentNode = highestPriorityNode;
				for (int i = 0; i < adjacencyMatrixForPathCostDetermination.length; i++) {
					if (adjacencyMatrixForPathCostDetermination[parentNode][i] != 0) {
						int newCostOfSFromSource = adjacencyMatrixForPathCostDetermination[parentNode][i] + pathCostFromSource[parentNode];
						if((!frontier.contains(i)) && (!explored.contains(i))) {
							frontier.add(i);
						}
						if (frontier.contains(i) && newCostOfSFromSource < pathCostFromSource[i]) {							
							pathCostFromSource[i] = newCostOfSFromSource;
							cameFromParent[i] = parentNode;
						}
					}
				}
			}
		}
	}
	
	//A* search (Informed search)
	public static void AstarSearch(String sourceCity, String destinationCity) {
		ArrayList<Integer> frontier = new ArrayList<Integer>();
		ArrayList<Integer> explored = new ArrayList<Integer>();
		totalPathCost = 0;
		for(int i=0;i<pathCostFromSource.length;i++) {
			pathCostFromSource[i]=Integer.MAX_VALUE;
		}
		int sourceId = ExtractIDFromEdgesToIDMapping(sourceCity);
		int destinationId = ExtractIDFromEdgesToIDMapping(destinationCity);
		frontier.add(sourceId);
		cameFromParent[sourceId] = -1;
		heuristicCostToDestination[sourceId] = (float) 0.0;
		pathCostFromSource[sourceId] = 0;
		while(!frontier.isEmpty()) {
			float minCost=Float.MAX_VALUE;
			int nextIndex=-1;
			for (int i = 0; i < frontier.size();i++) {
				if ((heuristicCostToDestination[frontier.get(i)]) < minCost) {
					minCost = (float) (heuristicCostToDestination[frontier.get(i)]);
					nextIndex = i;
				}
			}
			int highestPriorityNode = frontier.get(nextIndex);
			frontier.remove(nextIndex);
			
			if (highestPriorityNode == destinationId) {
				totalPathCost = pathCostFromSource[highestPriorityNode];
				//Printing a comma separated list of expanded nodes (the closed list)
				System.out.print("The list of nodes expanded is: ");
				for (int i = 0; i < explored.size(); i++) {
					if (i < explored.size() - 1) {
						System.out.print(ExtractEdgeFromEdgesToIDMapping(explored.get(i))+",");
					}
					else {
						System.out.println(ExtractEdgeFromEdgesToIDMapping(explored.get(i)));
					}
				}
				
				//Printing the number of nodes expanded
				System.out.println("The number of nodes expanded is: "+explored.size());
				//Function to generate solution path
				GeneratePath(highestPriorityNode);
				//Printing the total distance from source to destination in the solution path
				System.out.println("Total distance from source to destination in the solution path = "+totalPathCost);
				return;
			}
			else
			{
				explored.add(highestPriorityNode);
				
				int parentNode = highestPriorityNode;
				for (int i = 0; i < adjacencyMatrixForPathCostDetermination.length; i++) {
					if (adjacencyMatrixForPathCostDetermination[parentNode][i] != 0) {
						
						int newCostOfSFromSource = adjacencyMatrixForPathCostDetermination[parentNode][i] + pathCostFromSource[parentNode];
						heuristicCostToDestination[i] = (float) GenerateHeuristicCostBetweenCities(i, destinationId)+newCostOfSFromSource;
						if((!frontier.contains(i)) && (!explored.contains(i))) {
							frontier.add(i);
						}
						if (frontier.contains(i) && newCostOfSFromSource < pathCostFromSource[i]) {	
							pathCostFromSource[i] = newCostOfSFromSource;
							heuristicCostToDestination[i] = (float) GenerateHeuristicCostBetweenCities(i, destinationId) + newCostOfSFromSource;
							cameFromParent[i] = parentNode;
						}
					}
				}
			}
		}
	}

	//Generate usable strings from usroads.pl file
	public static ArrayList<String> GenerateRoadAndCityStrings() throws IOException{
		File file = new File("usroads.pl"); 
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		String st; 
	    ArrayList<String> roadInfolist = new ArrayList<String>();
	    ArrayList<String> cityHeuristicsInfolist = new ArrayList<String>();
	    String roadString = "";
	    String roadInfoString = "";
	    String cityString = "";
	    String cityInfoString = "";
	    while ((st = br.readLine()) != null) {
	    	if ((st.contains("road")) && (!st.contains("%"))) {
				  try {
					  Integer.parseInt(st.substring(st.length()-3, st.length()-2));
					  roadInfolist.add(st.trim());
				  }
				  catch(NumberFormatException e){
					  
				  }
			  }
			  if ((st.contains("city")) && (!st.contains("%"))) {
				  try {
					  Integer.parseInt(st.substring(st.length()-3, st.length()-2));
					  cityHeuristicsInfolist.add(st.trim());
				  }
				  catch(NumberFormatException e){
					  
				  }
			  }
	    }
	    br.close();
	  
	    for (int  i = 0; i < roadInfolist.size(); i++) {
	    	roadString = roadString+roadInfolist.get(i).trim();
	    }
	    if(roadString.substring(roadString.length()-1, roadString.length()).equals(".")) {
	    	roadInfoString = roadString.substring(0, roadString.length()-1);
	    }
	    else {
	    	roadInfoString = roadString;
	    }
	  
	    for (int  i = 0; i < cityHeuristicsInfolist.size(); i++) {
	    	cityString = cityString+cityHeuristicsInfolist.get(i).trim();
	    }
	    if(cityString.substring(cityString.length()-1, cityString.length()).equals(".")) {
	    	cityInfoString = cityString.substring(0, cityString.length()-1);
	    }
	    else {
	    	cityInfoString = cityString;
	    }
	    ArrayList<String> roadCityList = new ArrayList<String>();
	    roadCityList.add(roadInfoString);
	    roadCityList.add(cityInfoString);  
	    return roadCityList;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		setOfUniqueCities = new HashSet<String>();
		citiesToIDMapping = new HashMap<String, Integer>();
		cityIDToHeuristicsMapping = new HashMap<String,ArrayList<Double>>();
		ArrayList<String> roadCityList = GenerateRoadAndCityStrings();
		String roadInfoString = roadCityList.get(0);
		String cityHeuristicsInfo = roadCityList.get(1);
		setOfUniqueCities = CreateSetOfUniqueCities(roadInfoString);
		citiesToIDMapping = CreateCitiesToIDMapping(setOfUniqueCities);
		cityIDToHeuristicsMapping = CreateCityIDToHeuristicsMapping(cityHeuristicsInfo);
		numOfCity = setOfUniqueCities.size();
		cameFromParent = new int[numOfCity];
		pathCostFromSource = new int[numOfCity];
		heuristicCostToDestination = new float[numOfCity];	
		adjacencyMatrixForPathCostDetermination = CreateAdjacencyMatrixForPathCostDetermination(citiesToIDMapping, roadInfoString);

		boolean flag=false;
		String searchChoice="",sourceCity="",destinationCity="";
		while(flag==false)
		{
			System.out.println("Enter your search string as : searchtype srccityname destcityname.");
			System.out.println("searchtype should be  either astar, greedy, or dynamic.");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			StringTokenizer st = new StringTokenizer(br.readLine()," ");
			
			searchChoice = st.nextToken();
			sourceCity = st.nextToken();
			destinationCity = st.nextToken();
			
			if (!searchChoice.equals("astar") && !searchChoice.equals("greedy") && !searchChoice.equals("dynamic")) {
				System.out.println("Please enter a valid search choice.");
				continue;
			}
			
			if (!setOfUniqueCities.contains(sourceCity)) {
				System.out.println("Please enter a valid srccityname choice.");
				continue;
			}
			if (!setOfUniqueCities.contains(destinationCity)) {
				System.out.println("Please enter a valid destcityname choice.");
				continue;
			}
			flag = true;
		}
		
		if (flag == true) {
			if (searchChoice.equals("astar")) {
				System.out.println("Astar Search");
				System.out.println("----------------------------");
				AstarSearch(sourceCity,destinationCity);
			}
			else if (searchChoice.equals("greedy")) {
				System.out.println("Greedy Search");
				System.out.println("----------------------------");
				GreedyBestFirstSearch(sourceCity,destinationCity);
			}
			else if (searchChoice.equals("dynamic")) {
				System.out.println("Dynamic Programming");
				System.out.println("----------------------------");
				DynamicProgramming(sourceCity,destinationCity);
			}
		}
		

		
		
	}

}
