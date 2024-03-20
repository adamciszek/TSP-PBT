package TSP;

import java.util.ArrayList;
import java.util.List;

public class TSP {
	// Distance lookup table
	public static Integer[][] distances = { { 0, 129, 119, 43, 98, 98, 86, 52, 85, 44 },
			{ 129, 0, 88, 149, 152, 57, 55, 141, 93, 86 },
			{ 119, 88, 0, 97, 72, 72, 42, 72, 35, 92 },
			{ 43, 149, 97, 0, 54, 119, 107, 28, 64, 60 },
			{ 98, 152, 72, 54, 0, 138, 85, 39, 48, 90 },
			{ 98, 57, 72, 119, 138, 0, 35, 111, 77, 56 },
			{ 86, 55, 42, 107, 85, 35, 0, 80, 37, 44 },
			{ 52, 141, 72, 28, 39, 111, 80, 0, 38, 52 },
			{ 85, 93, 35, 64, 48, 77, 37, 38, 0, 47 },
			{ 44, 86, 92, 60, 90, 56, 44, 52, 47, 0 }, };

	// Generic variables
	// Populate a list with the cities
	private static List<City> cities;

	// Brute force (BF) variables
	private static List<Route> BFRoutePerms = new ArrayList<Route>();
	private static Integer BFcheapestCost = Integer.MAX_VALUE;
	private static Route BFcheapestRoute;

	// Branch and bound (BaB) variables
	private static List<Route> BaBRoutePerms = new ArrayList<Route>();
	private static Integer BaBcheapestCost = Integer.MAX_VALUE;
	private static Route BaBcheapestRoute;

	/**
	 * Main function
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Used to calculate average execution times
		long time1 = 0;
		long time2 = 0;
		long time3 = 0;
		// Used to determine number of times the three algorithms should run
		int numIterations = 1;

		// Only individual algorithms should be run during profiling
		for (int i = 0; i < numIterations; i++) {
			long time = System.currentTimeMillis();
			// Run brute force
			bruteForce();
			System.out.println("\tTime:" + (System.currentTimeMillis() - time) + "ms");
			time1 += System.currentTimeMillis() - time;

			time = System.currentTimeMillis();
			// Run nearest neighbour
			nearestNeighbour();
			System.out.println("\tTime:" + (System.currentTimeMillis() - time) + "ms");
			time2 += System.currentTimeMillis() - time;

			time = System.currentTimeMillis();
			// Run branch and bound
			branchAndBound();
			System.out.println("\tTime:" + (System.currentTimeMillis() - time) + "ms");
			time3 += System.currentTimeMillis() - time;
		}

		// Output average time for functions
		System.out.println("\n\tBF:" + time1 / numIterations + "ms");
		System.out.println("\tNN:" + time2 / numIterations + "ms");
		System.out.println("\tBB:" + time3 / numIterations + "ms");
		// Output rough memory usage (profiler is more accurate)
		System.out.println(
				"KB: " + (int) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024);
	}

	/************************************************************************************************************/
	/**
	 * Calculate the shortest route using the brute force algorithm
	 */
	public static void bruteForce() {
		System.out.println("bruteForce:");
		// Setup city list
		resetLists();

		// Remove stoke from permutations as always start and end
		List<Integer> cityNums = new ArrayList<Integer>();
		for (int i = 0; i < 9; i++) {
			cityNums.add(i);
		}

		// Calculate
		permute(new Route(), cityNums, true);
		// Output the number of permutations generated
		System.out.println("\tComplete Permutations: " + BFRoutePerms.size());
		findShortestPermutation(BFRoutePerms);
	}

	/************************************************************************************************************/

	/**
	 * Calculates shortest route using nearest neighbour algorithm
	 */
	public static void nearestNeighbour() {
		System.out.println("nearestNeighbour:");
		// Setup city list
		resetLists();

		Integer routeCost = 0;

		// New route with start as Stoke
		Route nearestRoute = new Route(cities.get(9));

		while (nearestRoute.getRoute().size() != cities.size()) {

			City neighbourCity = null;
			int neighbourDistance = Integer.MAX_VALUE;

			for (int i = 0; i < 9; i++) {
				// If closer and not self and not visited
				if (distances[nearestRoute.getCurrentCity().getID()][i] < neighbourDistance
						&& distances[nearestRoute.getCurrentCity().getID()][i] != 0
						&& cities.get(i).isVisited() == false) {

					// Update closest neighbour
					neighbourCity = cities.get(i);
					neighbourDistance = distances[nearestRoute.getCurrentCity().getID()][i];
				}
			}

			if (neighbourCity != null) {
				// Update current location, add to route, set current as visited
				nearestRoute.getRoute().add(neighbourCity);
				nearestRoute.setCurrentCity(neighbourCity);
				neighbourCity.setVisited(true);

				// Add distance
				routeCost += neighbourDistance;
			}
		}

		// Add cost to return to Stoke
		routeCost += distances[nearestRoute.getStartCity().getID()][nearestRoute.getCurrentCity().getID()];

		// Add stoke to route end
		nearestRoute.getRoute().add(cities.get(9));

		System.out.println("\t" + nearestRoute.toString() + "\n\tCost: " + routeCost);
	}

	/************************************************************************************************************/

	/**
	 * Calculates the shortest route using branch and bound algorithm
	 */
	public static void branchAndBound() {
		System.out.println("branchAndBound:");
		// Setup city list
		resetLists();

		// Remove halifax from permutations as always start and end
		List<Integer> cityNums = new ArrayList<Integer>();
		for (int i = 0; i < distances.length; i++) {
			cityNums.add(i);
		}

		// Calculate
		permute(new Route(), cityNums, false);
		// Output the number of complete permutations generated NOTE: This is also the
		// number of times the optimal route improved
		System.out.println("\tComplete Permutations: " + BaBRoutePerms.size());
		System.out.println("\t" + BaBcheapestRoute.toString() + "\n\tCost: " + getRouteCost(BaBcheapestRoute));
	}

	/************************************************************************************************************/

	/**
	 * Resets lists to initial state to allow multiple runs of algorithms
	 */
	private static void resetLists() {
		BFRoutePerms = new ArrayList<Route>();
		BaBRoutePerms = new ArrayList<Route>();

		cities = new ArrayList<City>();

		// Populate City list
		cities.add(new City("Vancouver", 0, false));
		cities.add(new City("Edmonton", 1, false));
		cities.add(new City("Calgary", 2, false));
		cities.add(new City("Winnipeg", 3, false));
		cities.add(new City("Hamilton", 4, false));
		cities.add(new City("Toronto", 5, false));
		cities.add(new City("Kingston", 6, false));
		cities.add(new City("Ottawa", 7, false));
		cities.add(new City("Montreal", 8, false));
		cities.add(new City("Halifax", 9, false));
	}

	/**
	 * Generates all permutations in lexicographic order
	 * 
	 * @param r
	 * @param notVisited
	 */
	private static void permute(Route r, List<Integer> notVisited, boolean isBruteForce) {
		if (!notVisited.isEmpty()) {

			for (int i = 0; i < notVisited.size(); i++) {
				// Pointer to first city in list
				int temp = notVisited.remove(0);

				Route newRoute = new Route();
				// Lazy copy
				for (City c1 : r.getRoute()) {
					newRoute.getRoute().add(c1);
				}

				// Add the first city from notVisited to the route
				newRoute.getRoute().add(cities.get(temp));

				if (isBruteForce) {
					// Recursive call
					permute(newRoute, notVisited, true);
				} else {
					// If a complete route has not yet been created keep permuting
					if (BaBRoutePerms.isEmpty()) {
						// Recursive call
						permute(newRoute, notVisited, false);
					} else if (getRouteCost(newRoute) < BaBcheapestCost) {
						// Current route cost is less than the best so far so keep permuting
						permute(newRoute, notVisited, false);
					}
				}
				// Add first city back into notVisited list
				notVisited.add(temp);
			}
		} else {
			// Route is complete
			if (isBruteForce) {
				BFRoutePerms.add(r);
			} else {
				// Add stoke to start and end of route
				r.getRoute().add(0, cities.get(9));
				r.getRoute().add(cities.get(9));

				BaBRoutePerms.add(r);

				// If shorter than best so far, update best cost
				if (getRouteCost(r) < BaBcheapestCost) {
					BaBcheapestRoute = r;
					BaBcheapestCost = getRouteCost(r);
				}
			}
		}
	}

	/**
	 * Gets the cost of all the routes in the list and outputs the cheapest
	 * 
	 * @param routeList
	 */
	private static void findShortestPermutation(List<Route> routeList) {
		// Loop through all the permutations
		for (Route r : routeList) {
			// Only used by brute force so add stoke to start and end of route
			appendStoke(r);

			if (getRouteCost(r) < BFcheapestCost) {
				BFcheapestCost = getRouteCost(r);
				BFcheapestRoute = r;
			}
		}

		System.out.println("\t" + BFcheapestRoute.toString() + "\n\tCost: " + BFcheapestCost);
	}

	/**
	 * Adds stoke to start and finish of route
	 * 
	 * @param r
	 *            route
	 */
	private static void appendStoke(Route r) {
		r.getRoute().add(0, cities.get(9));
		r.getRoute().add(cities.get(9));
	}

	/**
	 * Gets the cost of traveling between the cities in the route
	 * 
	 * @param r
	 * @return tempCost
	 */
	public static Integer getRouteCost(Route r) {
		Integer tempCost = 0;
		// Add route costs
		for (int i = 0; i < r.getRoute().size() - 1; i++) {
			tempCost += distances[r.getRoute().get(i).getID()][r.getRoute().get(i + 1).getID()];
		}
		return tempCost;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Integer[] row : distances) {
			for (Integer distance : row) {
				sb.append(distance).append("\t");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	public List<Route> getBFRoutePerms() {
		return BFRoutePerms;
	}

	public List<Route> getBaBRoutePerms() {
		return BaBRoutePerms;
	}

	public Route getBaBcheapestRoute() {
		return BaBcheapestRoute;
	}

	public Integer getBaBcheapestCost() { return BaBcheapestCost; }

	public List<City> getCities() {
		return cities;
	}
}
