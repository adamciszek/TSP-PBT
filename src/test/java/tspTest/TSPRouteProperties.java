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

public class TSPRouteProperties {

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

}