package iterace;

import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Used for getting around a compatibility problem between Scala and jUnit:
 * Scala makes *all* fields private and exposes accessors
 * jUnit 4.10 can only annotate public fields
 * 
 * @author cos
 *
 */

public class JavaTest {
	@Rule public TestName testName = new TestName();
}
