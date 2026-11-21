package dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResetDBTest {

    @Test
    public void resetDatabase() throws Exception {
        TestDB.reset();
        assertEquals(2, TestDB.queryInt("SELECT COUNT(*) FROM tblUser"));
        assertEquals(5, TestDB.queryInt("SELECT COUNT(*) FROM tblCategory"));
        assertEquals(2, TestDB.queryInt("SELECT COUNT(*) FROM tblProduct"));
        assertEquals(0, TestDB.queryInt("SELECT COUNT(*) FROM tblOrder"));
    }
}
