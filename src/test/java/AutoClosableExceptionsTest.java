import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoClosableExceptionsTest {

    public static class AutoClosableResource implements AutoCloseable {

        private final String name;
        private final boolean throwExceptionOnClose;

        public AutoClosableResource(String name, boolean throwExceptionOnClose) {
            this.name = name;
            this.throwExceptionOnClose = throwExceptionOnClose;
        }

        public void doOp(boolean throwException) throws Exception {
            System.out.println("Resource " + this.name + " doing operation");
            if (throwException) {
                throw new Exception("Error when calling doOp() on resource " + this.name);
            }
        }

        @Override
        public void close() throws Exception {
            System.out.println("Resource " + this.name + " close() called");
            if (this.throwExceptionOnClose) {
                throw new Exception("Error when trying to close resource " + this.name);
            }
        }
    }

    @Test
    void tryAutoClosableWithOneSuppressed() {
        try {
            tryWithResourcesSingleResource();
        } catch (Exception e) {
            assertEquals("Error when calling doOp() on resource One", e.getMessage());
            assertEquals(1, e.getSuppressed().length);
            assertEquals("Error when trying to close resource One", e.getSuppressed()[0].getMessage());
        }
    }

    public static void tryWithResourcesSingleResource() throws Exception {
        try (AutoClosableResource resourceOne = new AutoClosableResource("One", true)) {
            resourceOne.doOp(true);
        }
    }

    @Test
    void tryAutoClosableWithUnsuppressed() {
        try {
            tryWithResourcesTwoResources();
        } catch (Exception e) {
            assertEquals("Error when calling doOp() on resource One", e.getMessage());
            assertEquals(2, e.getSuppressed().length);
            assertEquals("Error when trying to close resource Two", e.getSuppressed()[0].getMessage());
            assertEquals("Error when trying to close resource One", e.getSuppressed()[1].getMessage());
        }
    }

    public static void tryWithResourcesTwoResources() throws Exception {
        try (AutoClosableResource resourceOne = new AutoClosableResource("One", true);
             AutoClosableResource resourceTwo = new AutoClosableResource("Two", true)
        ) {
            resourceOne.doOp(true);
            resourceTwo.doOp(false);
        }
    }

    @Test
    void tryAutoClosableWithResourcesThreeResources() {
        try {
            tryWithResourcesThreeResources();
        } catch (Exception e) {
            assertEquals("Error when trying to close resource Three", e.getMessage());
            assertEquals(2, e.getSuppressed().length);
            assertEquals("Error when trying to close resource Two", e.getSuppressed()[0].getMessage());
            assertEquals("Error when trying to close resource One", e.getSuppressed()[1].getMessage());
        }
    }

    public static void tryWithResourcesThreeResources() throws Exception {
        AutoClosableResource resourceOne = new AutoClosableResource("One", true);
        AutoClosableResource resourceTwo = new AutoClosableResource("Two", true);
        AutoClosableResource resourceThree = new AutoClosableResource("Three", true);
        try (resourceOne; resourceTwo; resourceThree) {
            resourceOne.doOp(false);
            resourceTwo.doOp(false);
        }
    }

    @Test
    void tryAutoClosableWithFinally() {
        try {
            tryWithResourcesFinally();
        } catch (Exception e) {
            assertEquals("Finally", e.getMessage());
            assertEquals(0, e.getSuppressed().length);
        }
    }

    public static void tryWithResourcesFinally() throws Exception {
        AutoClosableResource resourceOne = new AutoClosableResource("One", true);
        try (resourceOne) {
            resourceOne.doOp(false);
        } finally {
            throw new Exception("Finally");
        }
    }
}
