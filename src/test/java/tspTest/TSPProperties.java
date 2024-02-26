package TSPTest;

import net.jqwik.api.*;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.StringLength;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.*;
import org.junit.jupiter.api.Test;

import TSP.City;
import TSP.Route;
import TSP.TSP;

import java.util.ArrayList;
import java.util.List;

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
    void testTSPWithOnePath(@ForAll("onePathMatrix") double[][] distances) {

        // tsp instance
        TSP newTSP = new TSP();

        newTSP.distances = distances; // assigning our generated distance table to the instance

        newTSP.branchAndBound();

        Route sol = newTSP.getBaBcheapestRoute(); // getting the 'cheapest route' which is the solution

        List<Route> routes = newTSP.getBaBRoutePerms(); //getting all possible permutations

        // need to fix this so that i am getting only feasible permutations, rather then all permutations possible (there is only one feasible path, but can be displayed in 10 different ways due to there being 10 different cities)

        for (Route route : routes) {
            Assertions.assertThat(route).isEqualTo(sol);
        }


    }

    @Property
    @Report(Reporting.GENERATED)
    void testNumCities(@ForAll("onePathMatrix") double[][] distances) {

        // tsp instance
        TSP newTSP = new TSP();

        newTSP.distances = distances; // assigning our generated distance table to the instance

        newTSP.branchAndBound();

        //test that the number of cities in the path is exactly n

        int actualNumOfCities = 10;

        int NumOfCities = newTSP.getBaBcheapestRoute().getRoute().size()-1; // must subtract 1 since solution set contains first city twice

        Assertions.assertThat(NumOfCities).isEqualTo(actualNumOfCities);

    }

    @Property
    @Report(Reporting.GENERATED)
    void testUniqueCities(@ForAll("onePathMatrix") double[][] distances) {

        // tsp instance
        TSP newTSP = new TSP();
        // assigning our generated distance table to the instance
        newTSP.distances = distances;

        //test that each city is only visited once

        newTSP.branchAndBound();

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

    @Provide
    Arbitrary<double[][]> onePathMatrix() {
        // need to fix this so that it is generating all possible unique matrixes such that only one path exists
        return Arbitraries.create(() -> {
            int size = 10;
            double[][] matrix = new double[size][size];

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    matrix[i][j] = 0;
                }
            }
            int start = Arbitraries.integers().between(0, size - 1).sample();

            for (int i = 0; i < size; i++) {
                int nextCity = (start + i) % size;
                matrix[nextCity][((nextCity + 1) % size)] = 1;
            }

            return matrix;
        });
    }
}
