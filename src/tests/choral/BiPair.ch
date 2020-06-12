public class BiPair@( A, B )< L@X, R@Y > {
   private L@A left;
   private R@B right;

   public BiPair( L@A left, R@B right ) {
     this.left = left;
     this.right = right;
   }

   public L@A left() {
      return this.left;
   }
   public R@B right() {
      return this.right;
   }
}