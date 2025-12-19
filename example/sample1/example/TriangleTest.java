import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * All test cases correspond to Myers's 14 questions.
 */
public class TriangleTest {

  @Test public void testScalene1() {
    int type = Triangle.classify(2, 3, 4); // Q1
    assertEquals(3, type);
  }

  @Test public void testEquiliteral() {
    int type = Triangle.classify(1, 1, 1); // Q2
    assertEquals(1, type);
  }

  @Test public void testIsoscele1() {
    int type = Triangle.classify(3, 3, 4); // Q3 Q4
    assertEquals(2, type);
  }

  @Test public void testIsoscele2() {
    int type = Triangle.classify(3, 4, 3); // Q3 Q4
    assertEquals(2, type);
  }

  @Test public void testIsoscele3() {
    int type = Triangle.classify(4, 3, 3); // Q3 Q4
    assertEquals(2, type);
  }

  @Test public void testInvalid1() {
    int type = Triangle.classify(0, 1, 1); // Q5
    assertEquals(-1, type);
  }

  @Test public void testInvalid2() {
    int type = Triangle.classify(-1, 1, 1); // Q6
    assertEquals(-1, type);
  }

  @Test public void testInvalid3() {
    int type = Triangle.classify(1, 2, 3); // Q7 Q8
    assertEquals(-1, type);
  }

  @Test public void testInvalid4() {
    int type = Triangle.classify(1, 3, 2); // Q7 Q8
    assertEquals(-1, type);
  }

  @Test public void testInvalid5() {
    int type = Triangle.classify(3, 1, 2); // Q7 Q8
    assertEquals(-1, type);
  }

  @Test public void testInvalid6() {
    int type = Triangle.classify(1, 2, 4); // Q9 Q10
    assertEquals(-1, type);
  }

  @Test public void testInvalid7() {
    int type = Triangle.classify(1, 4, 2); // Q9 Q10
    assertEquals(-1, type);
  }

  @Test public void testInvalid8() {
    int type = Triangle.classify(4, 1, 2); // Q9 Q10
    assertEquals(-1, type);
  }

  @Test public void testInvalid9() {
    int type = Triangle.classify(0, 0, 0); // Q11
    assertEquals(-1, type);
  }

  // Q12, float checking, cannot be executed on typed language.
  // Q13, short args, also cannot be executed on java.
  // Ofc, Q14 is considered on all the above test cases.
}
