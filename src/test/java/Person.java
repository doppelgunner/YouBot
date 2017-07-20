/**
 * Created by robertoguazon on 16/07/2017.
 */
public class Person {
    public String name;
    public int age;

    public Person() {}

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        Person p = (Person)o;
        if (this.name.equals(p.name) && this.age == p.age) return true;
        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + age;
    }
}