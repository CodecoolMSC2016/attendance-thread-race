import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public final class Main {

    private static final boolean FUZZY = false;

    private static abstract class Person extends Thread implements Comparable<Person> {

        final int timeout;
        final String name;

        boolean arrived;

        Person(int timeout, String name) {
            super();
            if (FUZZY) {
                this.timeout = new Random().nextInt(timeout) + 1;
            } else {
                this.timeout = timeout;
            }
            this.name = name;
        }

        @Override
        public final void run() {
            try {
                TimeUnit.SECONDS.sleep(timeout);
                arrive();
                arrived = true;
            } catch (InterruptedException ignored) {
                ;
            }
        }

        protected abstract void arrive() throws InterruptedException;

        @Override
        public final String toString() {
            return String.format("%s{name=%s, timeout=%s, arrived=%s}", getClass().getSimpleName(), name, timeout, arrived);
        }

        @Override
        public final int compareTo(Person o) {
            int result = Integer.compare(timeout, o.timeout);
            if (result == 0) {
                return this instanceof Teacher ? -1 : 1;
            }
            return result;
        }
    }

    private static final class Teacher extends Person {

        private final List<Student> students;

        protected Teacher(int timeout, String name, List<Student> students) {
            super(timeout, name);
            this.students = students;
        }

        @Override
        public void arrive() {
            synchronized (this) {
                this.notifyAll();
            }
            students.stream()
                    .filter(s -> !s.arrived)
                    .forEach(Thread::interrupt);
        }
    }

    private static final class Student extends Person {

        private final Teacher teacher;

        protected Student(int timeout, String name, Teacher teacher) {
            super(timeout, name);
            this.teacher = teacher;
        }

        @Override
        public void arrive() throws InterruptedException {
            synchronized (teacher) {
                teacher.wait();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<Student> students = new ArrayList<>();
        Teacher teacher = new Teacher(5, "Joan", students);
        students.add(new Student(1, "Jane", teacher));
        students.add(new Student(2, "Joe", teacher));
        students.add(new Student(3, "Jack", teacher));
        students.add(new Student(4, "Jeremy", teacher));
        students.add(new Student(5, "John", teacher));
        students.add(new Student(6, "Julius", teacher));
        students.add(new Student(7, "Jenny", teacher));
        students.add(new Student(8, "Jockey", teacher));
        students.add(new Student(9, "Jamie", teacher));
        students.add(new Student(10, "Joy", teacher));

        teacher.start();
        students.stream()
                .forEach(Thread::start);
        teacher.join();
        for (Student student : students) {
            student.join();
        }

        List<Person> persons = new ArrayList<>();
        persons.add(teacher);
        persons.addAll(students);
        persons.stream()
                .sorted()
                .forEach(p -> System.out.format("%s %s%n", p.arrived ? "ARRIVED" : "KICKED OUT", p));
    }
}
