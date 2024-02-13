package TSPTest;

import TSP.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.*;
import org.junit.jupiter.api.Test;


class TSPProperties {

    // TESTING City.java ===================
    @Property
    @Report(Reporting.GENERATED)
    void cityProperties(@ForAll @NotEmpty String name, @ForAll @IntRange(min=0, max=100) int ID, @ForAll boolean visited) {
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
    void toStringMethodProducesValidOutput(@ForAll @NotEmpty String name, @ForAll @IntRange(min=0, max=100) int ID, @ForAll boolean visited) {
        City city = new City(name, ID, visited);
        String expectedOutput = "City{name=" + name + ", ID=" + ID + ", visited=" + visited + '}';
        Assertions.assertThat(city.toString()).isEqualTo(expectedOutput);
    }

    // TESTING Route.java



    // TESTING TSP.java
}
