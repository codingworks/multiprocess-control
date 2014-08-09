package net.codingworks.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 * Unit tests for MultiprocessControl.
 * 
 * @author Rongqin Sheng
 * @version 1.0
 */
public class MultiprocessControlTest {
	static Log log = LogFactory.getLog(MultiprocessControlTest.class);

	@Test
	public void testRun0() {
		MultiprocessControl control = new MultiprocessControl(5, log);
		ArrayList<String> cmdList = new ArrayList<String>();
		cmdList.add("java -help");
		cmdList.add("ping www.google.com -n 5");
		cmdList.add("ping www.yahoo.com -n 5");
		cmdList.add("ping www.facebook.com -n 5");
		cmdList.add("ping www.linkedin.com -n 5");
		cmdList.add("ping www.github.com -n 5");
		cmdList.add("ping www.baidu.com -n 5");
		cmdList.add("java -help");
		cmdList.add("ping www.google.com -n 5");
		cmdList.add("ping www.yahoo.com -n 5");
		cmdList.add("ping www.facebook.com -n 5");
		cmdList.add("ping www.linkedin.com -n 5");
		cmdList.add("ping www.github.com -n 5");
		cmdList.add("ping www.baidu.com -n 5");
		cmdList.add("java -help");
		cmdList.add("ping www.google.com -n 5");
		cmdList.add("ping www.yahoo.com -n 5");
		cmdList.add("ping www.facebook.com -n 5");
		cmdList.add("ping www.linkedin.com -n 5");
		cmdList.add("ping www.github.com -n 5");
		cmdList.add("ping www.baidu.com -n 5");
		cmdList.add("java -help");
		cmdList.add("ping www.google.com -n 5");
		cmdList.add("ping www.yahoo.com -n 5");
		cmdList.add("ping www.facebook.com -n 5");
		cmdList.add("ping www.linkedin.com -n 5");
		cmdList.add("ping www.github.com -n 5");
		cmdList.add("ping www.baidu.com -n 5");
		cmdList.add("java -help");
		cmdList.add("ping www.google.com -n 5");
		cmdList.add("ping www.yahoo.com -n 5");
		cmdList.add("ping www.facebook.com -n 5");
		cmdList.add("ping www.linkedin.com -n 5");
		cmdList.add("ping www.github.com -n 5");
		cmdList.add("ping www.baidu.com -n 5");
		int status = control.run(cmdList);
		assertEquals(0, status);
	}

	@Test
	public void testRun1() {
		MultiprocessControl control = new MultiprocessControl(3, log);
		ArrayList<String> cmdList = new ArrayList<String>();
		cmdList.add("ping www.google.com -n 5");
		cmdList.add("ping www.yahoo.com -n 5");
		cmdList.add("ping www.facebook.com -n 5");
		cmdList.add("ping www.linkedin.com -n 5");
		cmdList.add("javac");
		cmdList.add("ping www.github.com -n 5");
		cmdList.add("ping www.baidu.com -n 5");
		int status = control.run(cmdList);
		assertEquals(1, status);
	}

	@Test
	public void testRunAll() {
		MultiprocessControl control = new MultiprocessControl(3, log);
		ArrayList<String> cmdList = new ArrayList<String>();
		cmdList.add("ping www.google.com -n 5");
		cmdList.add("ping www.yahoo.com -n 5");
		cmdList.add("ping www.facebook.com -n 5");
		cmdList.add("ping www.linkedin.com -n 5");
		cmdList.add("javac");
		cmdList.add("ping www.github.com -n 5");
		cmdList.add("ping www.baidu.com -n 5");
		try {
			List<Integer> exitValues = control.runAll(cmdList);
			for (int i = 0; i < cmdList.size(); i++) {
				if (i != 4) {
					assertTrue(exitValues.get(i) == 0);
				} else {
					assertFalse(exitValues.get(i) == 0);
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
