/**
 * Tests all tags are invalid when parent annotation is private or package default
 */
public @interface test14 {

    public @interface inner1 {

        /**
		 * @noextend
		 * @noinstantiate
		 * @noreference
		 */
        public static class Clazz {
        }

        /**
		 * @noextend
		 * @noimplement
		 * @noreference
		 */
        public interface inter {
        }

        /**
		 * @noreference
		 */
        public int field = 0;

        /**
		 * @noreference
		 */
        public @interface annot {
        }

        /**
		 * @noreference
		 */
        enum enu implements  {

            ;
        }
    }
}
