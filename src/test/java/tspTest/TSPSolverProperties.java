package TSPTest;

import net.jqwik.api.*;
import org.assertj.core.api.Assertions;

import TSP.City;
import TSP.Route;
import TSP.TSP;
import TSP.Weight;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TSPSolverProperties {
    TSP newTSP = new TSP();

    private static final City VANCOUVER = new City("Vancouver", 0, false);
    private static final City EDMONTON = new City("Edmonton", 1, false);
    private static final City CALGARY = new City("Calgary", 2, false);
    private static final City WINNIPEG = new City("Winnipeg", 3, false);
    private static final City HAMILTON = new City("Hamilton", 4, false);
    private static final City TORONTO = new City("Toronto", 5, false);
    private static final City KINGSTON = new City("Kingston", 6, false);
    private static final City OTTAWA = new City("Ottawa", 7, false);
    private static final City MONTREAL = new City("Montreal", 8, false);
    private static final City HALIFAX = new City("Halifax", 9, false);

    // TESTING TSP.java ===============================================================================================

    @Property
    @Report(Reporting.GENERATED)
    void testTSPWithOnePath(@ForAll("onePathMatrixGenerator") Weight distances) {
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        Route sol = newTSP.getBaBcheapestRoute(); // getting the 'cheapest route' which is the solution
        List<Route> routes = newTSP.getBaBRoutePerms(); //getting all possible permutations

        // check if routes contain the expected solution
        Assertions.assertThat(routes).contains(sol);
    }

    @Property
    @Report(Reporting.GENERATED)
    void testNumCities(@ForAll("matrixGenerator") Weight distances) {
        //test that the number of cities in the path is exactly n
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        int actualNumOfCities = 10;
        int numOfCities = newTSP.getBaBcheapestRoute().getRoute().size() - 1; // must subtract 1 since solution set contains first city twice

        Assertions.assertThat(numOfCities).isEqualTo(actualNumOfCities);
    }

    @Property
    @Report(Reporting.GENERATED)
    void testUniqueCities(@ForAll("matrixGenerator") Weight distances) {
        //test that each city is only visited once
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        Route sol = newTSP.getBaBcheapestRoute(); // getting the 'cheapest route' which is the solution
        List<City> cities = sol.getRoute(); //all cities in the solution (contains first city twice)
        List<City> uniqueCities = new ArrayList<>(); //contains unique cities in solution

        for (City city : cities) {
            if (!uniqueCities.contains(city)) {
                uniqueCities.add(city);
            }
        }

        Assertions.assertThat(uniqueCities.size()).isEqualTo(cities.size() - 1);
    }

    @Property
    @Report(Reporting.GENERATED)
    void testCostIsTen(@ForAll("onePathMatrixGenerator") Weight distances) {
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        int routeCost = TSP.getRouteCost(newTSP.getBaBcheapestRoute());

        if (routeCost < 0) {
            routeCost *= -1;
        }

        Assertions.assertThat(routeCost).isEqualTo(10);
    }

    @Property
    @Report(Reporting.GENERATED)
    void testCostIsGreaterThanMin(@ForAll("matrixGenerator") Weight distances) {
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        Route r = newTSP.getBaBcheapestRoute();
        int minWeight = Integer.MAX_VALUE;

        for (int i = 0; i < r.getRoute().size() - 1; i++) {
            int weight = distances.getWeight(r.getRoute().get(i).getID(), r.getRoute().get(i + 1).getID());

            if ((weight < minWeight) && (weight != 0)) {
                minWeight = weight;
            }
        }

        Assertions.assertThat(TSP.getRouteCost(newTSP.getBaBcheapestRoute())).isGreaterThan(minWeight);
    }

    @Property
    @Report(Reporting.GENERATED)
    void testCostIsLessThanMax(@ForAll("matrixGenerator") Weight distances) {
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        Route r = newTSP.getBaBcheapestRoute();
        int maxWeight = Integer.MIN_VALUE;

        for (int i = 0; i < r.getRoute().size() - 1; i++) {
            int weight = distances.getWeight(r.getRoute().get(i).getID(), r.getRoute().get(i + 1).getID());

            if ((weight > maxWeight) && (weight != 0)) {
                maxWeight = weight;
            }
        }

        Assertions.assertThat(TSP.getRouteCost(newTSP.getBaBcheapestRoute())).isLessThan(maxWeight * 11);
    }

    //OPERATIONS-----------------------------------------------------------

    /*
     1) WORKS
     if you get a route r=[c1,c2,...,cn] of weight/length w for a distance matrix m1, and you add
     e to the weight of a single edge (w/o making the weight of the edge be MAX_VALUE) to get matrix m2,
     then the length of the route that you get (same or different) is between w and (w+e).
     */
    @Property
    @Report(Reporting.GENERATED)
    void testAddingWeight(@ForAll("matrixGenerator") Weight distances, @ForAll("extraWeight") int extra, @ForAll("getPosition") int[] position) {
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        int cost1 = newTSP.getBaBcheapestCost();

        Weight newDistances = new Weight(distances.getWeight().clone());

        newDistances.addExtraToPos(extra, position);

        TSP.distances = (newDistances);
        TSP.branchAndBound();
        int cost2 = newTSP.getBaBcheapestCost();

        Assertions.assertThat(cost2).isBetween(cost1, cost1 + extra);
    }

    /*
    2) WORKS
     if you get a route r=[c1,c2,...,cn] of weight/length w for a distance matrix m1, and you subtract
     e from the weight of a single edge to get matrix m2, then the length of the route should be (w-e)
     */
    @Property
    @Report(Reporting.GENERATED)
    void testSubtractingWeight(@ForAll("matrixGenerator") Weight distances, @ForAll("getPosition") int[] position) {
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        int cost1 = newTSP.getBaBcheapestCost();

        Weight newDistances = new Weight(distances.getWeight().clone());


        Random rand = new Random();
        int extra = rand.nextInt(newDistances.getWeight(position[0], position[1])); // random number between 1 to the value at iPos, jPos

        newDistances.subtractExtraFromPos(extra, position);

        TSP.distances = (newDistances);
        TSP.branchAndBound();
        int cost2 = newTSP.getBaBcheapestCost();

        Assertions.assertThat(cost2).isBetween(cost1 - extra, cost1);
    }

    /*
    3) FIX
    if i multiple the  distance matrix m1 by 3 to become m2, then the cost of m2 should be m1*3
     */
    @Property
    @Report(Reporting.GENERATED)
    void testMultiplyingMatrix(@ForAll("matrixGenerator") Weight distances, @ForAll("multiplier") int multiplier) {
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        int cost1 = newTSP.getBaBcheapestCost();

        Weight newDistances = new Weight(distances.getWeight().clone());
        newDistances.multiplyByM(multiplier);

        TSP.distances = (newDistances);
        TSP.branchAndBound();
        int cost2 = newTSP.getBaBcheapestCost();

        Assertions.assertThat(cost2).isEqualTo(cost1 * multiplier);
    }

    /*
    4) WORKS
    changing the order of the distance matrix shouldn't change the cost of the matrix
     */
    @Property
    @Report(Reporting.GENERATED)
    void testShufflingMatrix(@ForAll("matrixGenerator") Weight distances) {
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        int cost1 = newTSP.getBaBcheapestCost();

        Weight shuffledDistances = shuffleMatrix(distances);

        TSP.distances = (shuffledDistances);
        TSP.branchAndBound();
        int cost2 = newTSP.getBaBcheapestCost();

        Assertions.assertThat(cost2).isEqualTo(cost1);
    }

    private Weight shuffleMatrix(Weight distances) {
        int size = distances.getSize();
        Integer[][] shuffledMatrix = new Integer[size][size];

        for (int i = 0; i < size; i++) {
            shuffledMatrix[i][i] = 0; //  diagonal stays 0
        }

        // shuffle the non-diagonal elements
        List<Integer> indices = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Collections.shuffle(indices);

        for (int i = 0; i < size; i++) {
            int newRow = indices.get(i);
            for (int j = 0; j < size; j++) {
                if (i != j) { // skip diagonal elements
                    int newCol = indices.get(j);
                    shuffledMatrix[newRow][newCol] = distances.getWeight(i, j);
                }
            }
        }

        return new Weight(shuffledMatrix);
    }

    /*
    5)
    adding weight e to each edge in the distance matrix should increase the cost for the cheapest travel by e*the number
    of cities
     */
    @Property
    @Report(Reporting.GENERATED)
    void testAddingWeightToAll(@ForAll("matrixGenerator") Weight distances, @ForAll("extraWeight") int extra) {
        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        int cost1 = newTSP.getBaBcheapestCost();

        Weight newDistances = new Weight(distances.getWeight().clone());
        newDistances.addExtraToAll(extra);

        TSP.distances = (newDistances);
        TSP.branchAndBound();
        int cost2 = newTSP.getBaBcheapestCost();

        Assertions.assertThat(cost2).isEqualTo(cost1 + (extra * (distances.getSize())));
    }


    //GENERATORS------------------------------------------------------------
    @Provide
    public Arbitrary<Weight> onePathMatrixGenerator() {
        int size = 10;

        Arbitrary<Integer[]> zeroArrayArb = Arbitraries.integers().between(100, 10000).array(Integer[].class).ofSize(size);

        Arbitrary<Integer[][]> matrixArb = zeroArrayArb.array(Integer[][].class)
                .ofSize(size)
                .map(m -> {
                    // place 0's at diagonal
                    IntStream.range(0, size).forEach(i -> m[i][i] = 0);

                    List<Integer> indices = IntStream.range(0, size).boxed().collect(Collectors.toList());
                    // Shuffle indices  for random 1s placement
                    Collections.shuffle(indices);

                    for (int i = 0; i < (size / 2); i++) {
                        if (m[i][indices.get(i)] != 0) {
                            m[i][indices.get(i)] = 1;
                            m[indices.get(i)][i] = 1;
                        }
                    }
                    return m;
                })
                .filter(m -> //makes sure the matrix is symmetrical
                        IntStream.range(0, m.length)
                                .allMatch(i -> IntStream.range(i, m[i].length)
                                        .allMatch(j -> m[i][j].equals(m[j][i]))));

        return matrixArb.map(Weight::new);
    }

    @Provide
    public Arbitrary<Weight> matrixGenerator() {
        Arbitrary<Integer> numArb = Arbitraries.integers().between(1, 500);
        int size = 10;

        Arbitrary<Integer[]> intArrayArb = numArb.array(Integer[].class).ofSize(size);
        Arbitrary<Integer[][]> intMatrixArb =
                intArrayArb.array(Integer[][].class)
                        .ofSize(size)
                        .map(m -> {    // Ensure there is a 0 at the diagonal

                            // place 0's at diagonal
                            IntStream.range(0, size).forEach(i -> m[i][i] = 0);

                            return m;
                        })
                        .filter(m ->
                                IntStream.range(0, m.length)
                                        .allMatch(i -> IntStream.range(i, m[i].length)
                                                .allMatch(j -> m[i][j].equals(m[j][i]))));

        return intMatrixArb.map(Weight::new);
    }


    //GENERATORS FOR OPERATIONS
    @Provide
    Arbitrary<Integer> extraWeight() {
        return Arbitraries.integers().between(1, 50);  // generates random number between 1 to 50
    }

    @Provide
    Arbitrary<Integer> multiplier() {
        return Arbitraries.integers().between(1, 5);  // generates random number between 1 to 5
    }

    @Provide
    Arbitrary<int[]> getPosition() {
        return Combinators.combine(
                        Arbitraries.integers().between(0, 9), // considering the matrix will be 10x10
                        Arbitraries.integers().between(0, 9))
                .as((i, j) -> new int[] {i, j})
                .filter(pos -> pos[0] != pos[1]); // i and j should not be same
    }


}