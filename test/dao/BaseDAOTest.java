package dao;

import org.junit.jupiter.api.BeforeEach;


public abstract class BaseDAOTest {

    @BeforeEach
    public void resetDatabase() throws Exception {
        TestDB.reset();
    }
}
