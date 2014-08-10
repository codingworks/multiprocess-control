multiprocess-control
====================

Java API for running multiple processes in parallel in a controlled way

###Documentation

The API documentation is available [here](http://www.codingworks.net/doc/multiprocess-control/index.html).

###Usages

* Return immediately if any of the processes fails  
Example:
```
import java.util.ArrayList;

import net.codingworks.util.MultiprocessControl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Example1 {
	static Log log = LogFactory.getLog(Example1.class);

	public static void main(String[] args) {
		MultiprocessControl control = new MultiprocessControl(3, log);
		ArrayList<String> cmdList = new ArrayList<String>();
		cmdList.add("ping www.google.com -n 5");
		cmdList.add("ping www.yahoo.com -n 5");
		cmdList.add("ping www.facebook.com -n 5");
		cmdList.add("ping www.linkedin.com -n 5");
		cmdList.add("ping www.github.com -n 5");
		int status = control.run(cmdList);
		if (status == 0) {
			System.out.println("Completed");
		} else {
			System.err.println("Failed");
			System.exit(1);
		}
	}
}
```
* Complete all the processes regardless of the exit status  
Example:
```
import java.util.ArrayList;
import java.util.List;

import net.codingworks.util.MultiprocessControl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Example2 {
	static Log log = LogFactory.getLog(Example2.class);

	public static void main(String[] args) {
		MultiprocessControl control = new MultiprocessControl(3, log);
		ArrayList<String> cmdList = new ArrayList<String>();
		cmdList.add("ping www.google.com -n 5");
		cmdList.add("ping www.yahoo.com -n 5");
		cmdList.add("ping www.facebook.com -n 5");
		cmdList.add("javac");
		cmdList.add("ping www.linkedin.com -n 5");
		cmdList.add("ping www.github.com -n 5");
		try {
			List<Integer> exitValues = control.runAll(cmdList);
			for (Integer exitValue : exitValues) {
				System.out.println(exitValue);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
```
For commons-logging and Log4j configuration files, please see examples [here](src/test/resources).  

###Download

* [multiprocess-control-1.0.jar](http://www.codingworks.net/lib/multiprocess-control-1.0.jar)
