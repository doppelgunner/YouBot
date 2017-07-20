import com.doppelgunner.youbot.Order;
import com.doppelgunner.youbot.Util;
import com.doppelgunner.youbot.model.VideoGroup;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.ObjectParser;
import com.google.api.services.youtube.model.SearchResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
/**
 * Created by robertoguazon on 16/07/2017.
 */
public class YouBot_TestB {

    @Test
    public void orderByEnumTest() {
        //relevance
        assertTrue(Order.RELEVANCE.value().equals("relevance"));
        assertTrue(Order.RELEVANCE.isSame("relevance"));
        assertTrue(Order.RELEVANCE.isSame(Order.RELEVANCE));

        //date
        assertTrue(Order.DATE.value().equals("date"));
        assertTrue(Order.DATE.isSame("date"));
        assertTrue(Order.DATE.isSame(Order.DATE));

        //rating
        assertTrue(Order.RATING.value().equals("rating"));
        assertTrue(Order.RATING.isSame("rating"));
        assertTrue(Order.RATING.isSame(Order.RATING));

        //alphabetical
        assertTrue(Order.ALPHABETICAL.value().equals("title"));
        assertTrue(Order.ALPHABETICAL.isSame("title"));
        assertTrue(Order.ALPHABETICAL.isSame(Order.ALPHABETICAL));

        //view count
        assertTrue(Order.VIEW_COUNT.value().equals("viewcount"));
        assertTrue(Order.VIEW_COUNT.isSame("viewcount"));
        assertTrue(Order.VIEW_COUNT.isSame(Order.VIEW_COUNT));

        assertFalse(Order.ALPHABETICAL.isSame(Order.DATE));
        assertFalse(Order.ALPHABETICAL.isSame(Order.RATING));
        assertFalse(Order.ALPHABETICAL.isSame(Order.RELEVANCE));
        assertFalse(Order.ALPHABETICAL.isSame(Order.VIEW_COUNT));

        assertTrue(Order.all.size() > 0);

        assertTrue(Order.ALPHABETICAL.sameName(Order.ALPHABETICAL.toString()));
    }

    @Test
    public void tryParse() {
        assertTrue(Util.tryParseInt("2"));
        assertFalse(Util.tryParseInt("ab"));
        assertFalse(Util.tryParseInt("2.5"));

        assertTrue(Util.tryParseLong("2"));
        assertFalse(Util.tryParseLong("ab"));
        assertFalse(Util.tryParseLong("2.5"));
        assertTrue(Util.tryParseLong("2"));
    }

    @Test
    public void rangeInt() {
        assertTrue(Util.range(1,2,1));
        assertTrue(Util.range(1,10,10));
        assertTrue(Util.range(1,10,5));
        assertTrue(Util.range(2,1,1));
        assertTrue(Util.range(10,1,10));
        assertTrue(Util.range(10,1,5));

        assertFalse(Util.range(1,10,0));
        assertFalse(Util.range(1,10,11));
        assertFalse(Util.range(10,1,0));
        assertFalse(Util.range(10,1,11));
    }

    @Test
    public void avoidDuplicates() {
        ObservableList<Person> group = FXCollections.observableArrayList();
        Person p1 = new Person("john",1);
        Person p2 = new Person("john",1);
        group.addAll(p1,p2);
        group = Util.removeDuplicates(group);
        assertTrue(group.size() == 1);
    }

    @Test
    public void serialize() {
        Person p = new Person("dummy",99);
        ObjectMapper mapper = new ObjectMapper();
        Person o = null;
        Util.toJSON(p,"data/save/test.test.json");
        o = Util.fromJSON(Person.class,"data/save/test.test.json");
        assertTrue(o.equals(p));

        ObservableList list = FXCollections.observableArrayList();
        list.add(o);
        list.add(p);
        Util.toJSON(list.toArray(),"data/save/test2.test.json");
        Person[] nl = Util.fromJSON(Person[].class,"data/save/test2.test.json");
        for (int i = 0; i < nl.length; i++) {
            assertTrue(nl[i].equals(list.get(i)));
        }
    }
}
