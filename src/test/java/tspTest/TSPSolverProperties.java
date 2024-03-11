package TSPTest;

import net.jqwik.api.*;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.assertj.core.api.Assertions;

import TSP.City;
import TSP.Route;
import TSP.TSP;

import java.util.*;
import java.util.stream.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TSPSolverProperties {

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
    void testTSPWithOnePath(@ForAll("onePathMatrixGenerator") Integer[][] distances) {

        // tsp instance
        TSP newTSP = new TSP();

        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        // System.out.println(newTSP);

        Route sol = newTSP.getBaBcheapestRoute(); // getting the 'cheapest route' which is the solution

        List<Route> routes = newTSP.getBaBRoutePerms(); //getting all possible permutations

        // check if routes contain the expected solution
        Assertions.assertThat(routes).contains(sol);


    }

    @Property
    @Report(Reporting.GENERATED)
    void testNumCities(@ForAll("matrixGenerator") Integer[][] distances) {
        //test that the number of cities in the path is exactly n

        // tsp instance
        TSP newTSP = new TSP();

        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();
        // System.out.println(newTSP);


        int actualNumOfCities = 10;

        int NumOfCities = newTSP.getBaBcheapestRoute().getRoute().size()-1; // must subtract 1 since solution set contains first city twice

        Assertions.assertThat(NumOfCities).isEqualTo(actualNumOfCities);

    }

    @Property
    @Report(Reporting.GENERATED)
    void testUniqueCities(@ForAll("matrixGenerator") Integer[][] distances) {
        //test that each city is only visited once

        // tsp instance
        TSP newTSP = new TSP();

        // assigning our generated distance table to the instance
        TSP.distances = distances;

        TSP.branchAndBound();
        //System.out.println(newTSP);

        Route sol = newTSP.getBaBcheapestRoute(); // getting the 'cheapest route' which is the solution

        List<City> cities = sol.getRoute(); //all cities in the solution (contains first city twice)

        List<City> uniqueCities = new ArrayList<>();; //contains unique cities in solution

        for (City city : cities) {
            if (!uniqueCities.contains(city)) {
                uniqueCities.add(city);
            }
        }

        Assertions.assertThat(uniqueCities.size()).isEqualTo(cities.size()-1);

    }

    @Property
    @Report(Reporting.GENERATED)
    void testCostIsTen(@ForAll("matrixCostTen") Integer[][] distances){
        // tsp instance
        TSP newTSP = new TSP();

        // assigning our generated distance table to the instance
        TSP.distances = distances;
        TSP.branchAndBound();


        int routeCost = TSP.getRouteCost(newTSP.getBaBcheapestRoute());

        if (routeCost < 0){
            routeCost *= -1;
        }

        Assertions.assertThat(( routeCost)).isEqualTo(10);

    }

    @Property
    @Report(Reporting.GENERATED)
    void testCostIsGreaterThanMin(@ForAll("matrixGenerator") Integer[][] distances){
        // tsp instance
        TSP newTSP = new TSP();

        // assigning our generated distance table to the instance
        TSP.distances = distances;
        TSP.branchAndBound();

        Route r = newTSP.getBaBcheapestRoute();
        int minWeight = Integer.MAX_VALUE;

        for (int i = 0; i < r.getRoute().size() - 1; i++) {
            int weight = distances[r.getRoute().get(i).getID()][r.getRoute().get(i+1).getID()];

            if ((weight < minWeight) && (weight != 0)) {
                minWeight = weight;
            }
        }

        Assertions.assertThat((TSP.getRouteCost(newTSP.getBaBcheapestRoute()))).isGreaterThan(minWeight);

    }

    @Property
    @Report(Reporting.GENERATED)
    void testCostIsLessThanMax(@ForAll("matrixGenerator") Integer[][] distances){
        // tsp instance
        TSP newTSP = new TSP();

        // assigning our generated distance table to the instance
        TSP.distances = distances;
        TSP.branchAndBound();

        Route r = newTSP.getBaBcheapestRoute();
        int maxWeight = Integer.MIN_VALUE;

        for (int i = 0; i < r.getRoute().size() - 1; i++) {
            int weight = distances[r.getRoute().get(i).getID()][r.getRoute().get(i+1).getID()];

            if ((weight > maxWeight) && (weight != 0)) {
                maxWeight = weight;
            }
        }

        Assertions.assertThat((TSP.getRouteCost(newTSP.getBaBcheapestRoute()))).isLessThan(maxWeight * 11);
    }


//GENERATORS------------------------------------------------------------
    @Provide
    public Arbitrary<Integer[][]> onePathMatrixGenerator() {
        int size = 10;

        Arbitrary<Integer[]> zeroArrayArb = Arbitraries.integers().between(999999, Integer.MAX_VALUE/15).array(Integer[].class).ofSize(size);

        Arbitrary<Integer[][]> matrixArb = zeroArrayArb.array(Integer[][].class)
                .ofSize(size)
                .map(m -> {
                    // place 0's at diagonal
                    IntStream.range(0, size).forEach(i -> m[i][i] = 0);

                    List<Integer> indices = IntStream.range(0, size).boxed().collect(Collectors.toList());
                    // Shuffle indices  for random 1s placement
                    Collections.shuffle(indices);

                    for (int i = 0; i < size; i++) {
                        if (m[i][indices.get(i)] != 0) {
                            m[i][indices.get(i)] = 1;
                        }
                    }
                    return m;
                })
                .filter(m -> //makes sure the matrix is symmetrical
                        IntStream.range(0, m.length)
                                .allMatch(i -> IntStream.range(i, m[i].length)
                                        .allMatch(j -> m[i][j].equals(m[j][i]))))
                .filter(m ->
                        Arrays.stream(m)
                        .flatMap(Arrays::stream)
                        .filter(num -> num == 1)
                        .count() == 10); // filter for matrix with exactly 10 ones

        return matrixArb;
    }


    @Provide
    public Arbitrary<Integer[][]> matrixGenerator() {
        Arbitrary<Integer> numArb = Arbitraries.integers().between(1, Integer.MAX_VALUE/15);
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

        return intMatrixArb;
    }

    @Provide
    public Arbitrary<Integer[][]> matrixCostTen() {
        Arbitrary<Integer> numArb = Arbitraries.integers().between(999, Integer.MAX_VALUE/15);
        int size = 10;

        Arbitrary<Integer[]> intArrayArb = numArb.array(Integer[].class).ofSize(size);
        Arbitrary<Integer[][]> intMatrixArb =
                intArrayArb.array(Integer[][].class)
                        .ofSize(size)
                        .map(m -> {

                            // place 0's at diagonal
                            IntStream.range(0, size).forEach(i -> m[i][i] = 0);

                            List<Integer> indices = IntStream.range(0, size).boxed().collect(Collectors.toList());
                            // Shuffle indices  for random 1s placement
                            Collections.shuffle(indices);

                            for (int i = 0; i < size; i++) {
                                if (m[i][indices.get(i)] != 0) {
                                    m[i][indices.get(i)] = 1;
                                }
                            }
                            return m;
                        })
                        .filter(m ->
                            IntStream.range(0, m.length)
                                .allMatch(i -> IntStream.range(i, m[i].length)
                                    .allMatch(j -> m[i][j].equals(m[j][i]))))
                        .filter(m ->
                                Arrays.stream(m)
                                        .flatMap(Arrays::stream)
                                        .filter(num -> num == 1)
                                        .count() == 10); // filter for matrix with exactly 10 ones
        return intMatrixArb;
    }


}