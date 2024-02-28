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
    void testTSPWithOnePath(@ForAll("onePathMatrixList") double[][] distances) {

        // tsp instance
        TSP newTSP = new TSP();

        newTSP.distances = distances; // assigning our generated distance table to the instance

        newTSP.branchAndBound();

        Route sol = newTSP.getBaBcheapestRoute(); // getting the 'cheapest route' which is the solution

        List<Route> routes = newTSP.getBaBRoutePerms(); //getting all possible permutations

        // check if routes contain the expected solution
        Assertions.assertThat(routes).contains(sol);


    }

    @Property
    @Report(Reporting.GENERATED)
    void testNumCities(@ForAll("matrixList") double[][] distances) {
        //test that the number of cities in the path is exactly n

        // tsp instance
        TSP newTSP = new TSP();

        newTSP.distances = distances; // assigning our generated distance table to the instance


        int actualNumOfCities = 10;

        int NumOfCities = newTSP.getBaBcheapestRoute().getRoute().size()-1; // must subtract 1 since solution set contains first city twice

        Assertions.assertThat(NumOfCities).isEqualTo(actualNumOfCities);

    }

    @Property
    @Report(Reporting.GENERATED)
    void testUniqueCities(@ForAll("matrixList") double[][] distances) {
        //test that each city is only visited once

        // tsp instance
        TSP newTSP = new TSP();

        // assigning our generated distance table to the instance
        newTSP.distances = distances;

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

//GENERATORS------------------------------------------------------------
    @Provide
    public Arbitrary<double[][]> onePathMatrixList() {
        return onePathMatrixGenerator();
    }

    private Arbitrary<double[][]> onePathMatrixGenerator() {
        return Arbitraries.just(10) // matrix size of 10
                .flatMap(size -> Arbitraries.create(() -> {
                    double[][] matrix = new double[size][size];
                    // initialize matrix with Integer.MAX_VALUE
                    for (double[] row : matrix) Arrays.fill(row, Integer.MAX_VALUE);

                    // shuffle indices
                    List<Integer> shuffledIndices = IntStream.range(0, size).boxed().collect(Collectors.toList());
                    Collections.shuffle(shuffledIndices);

                    // set 0 and 1 for each row according to the shuffled indices
                    for (int i = 0; i < size; i++) {
                        matrix[i][shuffledIndices.get(i)] = 0;
                        // shift location for 1 by 1 position in the loop,
                        // % size to loop back to 0 when exceeding matrix size
                        matrix[i][shuffledIndices.get((i + 1) % size)] = 1;
                    }
                    return matrix;
                }));
    }


    @Provide
    public Arbitrary<double[][]> matrixList() {
        return singleMatrixGenerator();
    }

    private Arbitrary<double[][]> singleMatrixGenerator() {
        // create an arbitrary number in the range 1 to 100, inclusive
        Arbitrary<Double> doubles = Arbitraries.doubles().between(1.0, 100.0);

        int size = 10;
        return Arbitraries.just(size).flatMap(s -> Arbitraries.create(() -> {
            double[][] matrix = new double[s][s];

            // initialize the array with random doubles
            for (double[] row : matrix) {
                for (int j = 0; j < s; j++) {
                    row[j] = doubles.sample();
                }
            }

            List<Integer> positions = IntStream.range(0, s)
                    .boxed()
                    .collect(Collectors.toList());
            Collections.shuffle(positions);

            for (int i = 0; i < s; i++) {
                // put 0.0 in a unique column for each row.
                matrix[i][positions.get(i)] = 0.0;
            }

            return matrix;
        }));
    }



}
