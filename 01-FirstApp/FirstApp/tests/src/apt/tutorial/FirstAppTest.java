package apt.tutorial;

import android.test.ActivityInstrumentationTestCase;

/**
 * This is a simple framework for a test of an Application.	See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class apt.tutorial.FirstAppTest \
 * apt.tutorial.tests/android.test.InstrumentationTestRunner
 */
public class FirstAppTest extends ActivityInstrumentationTestCase<FirstApp> {

		public FirstAppTest() {
				super("apt.tutorial", FirstApp.class);
		}

}