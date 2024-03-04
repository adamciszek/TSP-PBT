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

public class TSPProperties {

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

    // TESTING City.java ============================================================================

    @Property
    @Report(Reporting.GENERATED)
    void cityProperties(@ForAll @CharRange(from='A', to='Z') @StringLength(min=1,max = 2) String name, @ForAll @IntRange(max=2) int ID, @ForAll boolean visited) {
        City city = new City(name, ID, visited);

        // verify the properties set by the constructor
        Assertions.assertThat(city.getName()).isEqualTo(name);
        Assertions.assertThat(city.getID()).isEqualTo(ID);
        Assertions.assertThat(city.isVisited()).isEqualTo(visited);

        String newName = "NewCity";
        int newID = -10;
        boolean newVisited = !visited;

        city.setName(newName);
        city.setID(newID);
        city.setVisited(newVisited);

        // verify the properties changed
        Assertions.assertThat(city.getName()).isEqualTo(newName);
        Assertions.assertThat(city.getID()).isEqualTo(newID);
        Assertions.assertThat(city.isVisited()).isEqualTo(newVisited);
    }

    @Property
    @Report(Reporting.GENERATED)
    void toStringMethodProducesValidOutput(@ForAll @CharRange(from='A', to='Z') @StringLength(min=1,max = 2) String name, @ForAll @IntRange(max=2) int ID, @ForAll boolean visited) {
        City city = new City(name, ID, visited);
        String expectedOutput = "City{name=" + name + ", ID=" + ID + ", visited=" + visited + '}';
        Assertions.assertThat(city.toString()).isEqualTo(expectedOutput);
    }

    // TESTING Route.java ===============================================================================================
    @Property
    @Report(Reporting.GENERATED)
    void routePropertiesWithStartCity(@ForAll("validCities") City startCity) {
        Route route = new Route(startCity);

        Assertions.assertThat(route.getStartCity()).isEqualTo(startCity);
        Assertions.assertThat(route.getCurrentCity()).isEqualTo(startCity);
        Assertions.assertThat(route.getRoute()).containsExactly(startCity);
    }

    @Property
    @Report(Reporting.GENERATED)
    void setCurrentCityShouldChangeCurrentCity(@ForAll("validCities") City startCity,
                                               @ForAll("validCities") City newCity) {
        Route route = new Route(startCity);
        route.setCurrentCity(newCity);

        Assertions.assertThat(route.getCurrentCity()).isEqualTo(newCity);
    }

    @Property
    @Report(Reporting.GENERATED)
    void setRouteShouldChangeRouteList(@ForAll("validCities") City startCity,
                                       @ForAll("cityList") List<City> newRoute) {
        Route route = new Route(startCity);
        route.setRoute(newRoute);

        Assertions.assertThat(route.getRoute()).isEqualTo(newRoute);
    }

    // custom generator for valid cities
    @Provide
    Arbitrary<City> validCities() {
        return Arbitraries.of(VANCOUVER, EDMONTON, CALGARY, WINNIPEG, HAMILTON,
                TORONTO, KINGSTON, OTTAWA, MONTREAL, HALIFAX);
    }
    @Provide
    Arbitrary<List<City>> cityList() {
        return validCities().list().ofMinSize(0).ofMaxSize(10);
    }

    // TESTING TSP.java ===============================================================================================

    @Property
    @Report(Reporting.GENERATED)
    void testTSPWithOnePath(@ForAll("onePathMatrixGenerator") Integer[][] distances) {

        // tsp instance
        TSP newTSP = new TSP();

        TSP.distances = distances; // assigning our generated distance table to the instance
        TSP.branchAndBound();

        System.out.println(newTSP);

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

        System.out.println(newTSP);

        Assertions.assertThat(( TSP.getRouteCost(newTSP.getBaBcheapestRoute()))).isEqualTo(10);

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
            int weight = distances[r.getRoute().get(i).getID()][r.getRoute().get(i + 1).getID()];

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
            int weight = distances[r.getRoute().get(i).getID()][r.getRoute().get(i + 1).getID()];

            if ((weight > maxWeight) && (weight != 0)) {
                maxWeight = weight;
            }
        }

        Assertions.assertThat((TSP.getRouteCost(newTSP.getBaBcheapestRoute()))).isLessThan(maxWeight);
    }


//GENERATORS------------------------------------------------------------
    @Provide
    public Arbitrary<Integer[][]> onePathMatrixGenerator() {
        int size = 10;

        Arbitrary<Integer[]> zeroArrayArb = Arbitraries.integers().between(Integer.MAX_VALUE, Integer.MAX_VALUE).array(Integer[].class).ofSize(size);

        Arbitrary<Integer[][]> matrixArb = zeroArrayArb.array(Integer[][].class)
                .ofSize(size)
                .map(m -> {
                    List<Integer> indices = IntStream.range(0, size).boxed().collect(Collectors.toList());

                    // Shuffle indices for random 0s placement
                    Collections.shuffle(indices);
                    for (int i = 0; i < size; i++) {
                        m[i][indices.get(i)] = 0;
                    }

                    List<Integer> indicesForOnes = new ArrayList<>(indices);
                    // Shuffle indices again for random 1s placement, independent of 0s
                    Collections.shuffle(indicesForOnes);

                    for (int i = 0; i < size; i++) {
                        if (m[i][indicesForOnes.get(i)] != 0) {
                            m[i][indicesForOnes.get(i)] = 1;
                        }
                    }
                    return m;
                });

        return matrixArb;
    }


    @Provide
    public Arbitrary<Integer[][]> matrixGenerator() {
        Arbitrary<Integer> numArb = Arbitraries.integers().between(1, Integer.MAX_VALUE);
        int size = 10;

        Arbitrary<Integer[]> intArrayArb = numArb.array(Integer[].class).ofSize(size);
        Arbitrary<Integer[][]> intMatrixArb =
                intArrayArb.array(Integer[][].class)
                        .ofSize(size)
                        .map(m -> {    // Ensure there is a 0 in each row and no two zeros are in the same column

                            // Create a list of column indices and shuffle it
                            List<Integer> columns = IntStream.range(0, m[0].length)
                                    .boxed()
                                    .collect(Collectors.toList());
                            Collections.shuffle(columns);

                            for (int i = 0; i < m.length; i++) {
                                m[i][columns.get(i)] = 0; // Place a 0 in a randomly chosen (unique per row) column
                            }
                            return m;
                        });

        return intMatrixArb;
    }

    @Provide
    public Arbitrary<Integer[][]> matrixCostTen() {
        Arbitrary<Integer> numArb = Arbitraries.integers().between(1, Integer.MAX_VALUE);
        int size = 10;

        Arbitrary<Integer[]> intArrayArb = numArb.array(Integer[].class).ofSize(size);
        Arbitrary<Integer[][]> intMatrixArb =
                intArrayArb.array(Integer[][].class)
                        .ofSize(size)
                        .map(m -> {    // Ensure there is a 0 in each row and no two zeros are in the same column

                            // Create a list of column indices and shuffle it
                            List<Integer> columns = IntStream.range(0, m[0].length)
                                    .boxed()
                                    .collect(Collectors.toList());
                            Collections.shuffle(columns);

                            for (int i = 0; i < m.length; i++) {
                                m[i][columns.get(i)] = 0; // Place a 0 in a randomly chosen (unique per row) column
                            }

                            List<Integer> indicesForOnes = new ArrayList<>(columns);
                            // Shuffle indices again for random 1s placement, independent of 0s
                            Collections.shuffle(indicesForOnes);

                            for (int i = 0; i < size; i++) {
                                if (m[i][indicesForOnes.get(i)] != 0) {
                                    m[i][indicesForOnes.get(i)] = 1;
                                }
                            }
                            return m;
                        });
        return intMatrixArb;
    }


}