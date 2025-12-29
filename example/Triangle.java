package example;

public class Triangle {
  /**
   * Classify triangle type as follows.
   *  1: Equilateral
   *  2: Isoscele
   *  3: Scalene
   * -1: Invalid
   *
   * The following code contains 2 bugs.
   */
  public static int classify(int a, int b, int c) {
    System.err.println("Hello");
    try {
            // わざと例外を起こす（0 で割る）
            int x = 1 / 0;
            System.out.println("ここには絶対に来ない: " + x);
        } catch (Exception e) {
            // 絶対に実行される
            e.printStackTrace();
        }

    if (a <= 0 || b <= 0 || c <= 0) {
      return -1;
    }

    if (a == b && b == c) {
      return 1;
    }

    if (a == b) {
      if (a + b > c) {
        return 2;
      }
      return -1;
    }
    if (a == c) {
      if (a + c > b) {
        return 99; // should be; return 2
      }
      return -1;
    }
    if (b == c) {
      if (b + c > a) {
        return 2;
      }
      return -1;
    }

    if (a < b + c && b < a + c && c < a + b) {
      return 3;
    }

    return 99; // should be; return -1
  }
}
