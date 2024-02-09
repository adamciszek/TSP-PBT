package TSPTest;

import TSP.*;
import net.jqwik.api.*;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.*;


class TSPProperties {

    // TESTING City.java ===================
    @Property
    @Report(Reporting.GENERATED)
    void cityProperties(@ForAll String name, @ForAll int ID, @ForAll boolean visited) {
        City city = new City(name, ID, visited);

        // verify the properties set by the constructor
        Assertions.assertThat(city.getName()).isEqualTo(name);
        Assertions.assertThat(city.getID()).isEqualTo(ID);
        Assertions.assertThat(city.isVisited()).isEqualTo(visited);

        String newName = "NewCityName";
        int newID = 42;
        boolean newVisited = !visited;

        city.setName(newName);
        city.setID(newID);
        city.setVisited(newVisited);

        // verify that mutator methods change the properties
        Assertions.assertThat(city.getName()).isEqualTo(newName);
        Assertions.assertThat(city.getID()).isEqualTo(newID);
        Assertions.assertThat(city.isVisited()).isEqualTo(newVisited);
    }


    @Property
    @Report(Reporting.GENERATED)
    void toStringMethodProducesValidOutput(@ForAll String name, @ForAll int ID, @ForAll boolean visited) {
        City city = new City(name, ID, visited);
        String expectedOutput = "City{name=" + name + ", ID=" + ID + ", visited=" + visited + '}';
        Assertions.assertThat(city.toString()).isEqualTo(expectedOutput);
    }
}
