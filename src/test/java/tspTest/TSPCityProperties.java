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

public class TSPCityProperties {

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
}