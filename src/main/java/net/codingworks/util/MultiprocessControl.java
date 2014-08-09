package net.codingworks.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;

/**
 * API for running multiple processes in parallel in a controlled way. A limited
 * number of processes are allowed to run at the same time.
 * 
 * @author Rongqin Sheng
 * @version 1.0
 */
public class MultiprocessControl {
	/**
	 * The maximum number of processes allowed to run at the same time
	 */
	private int concurrencyLimit;

	/**
	 * Message log (the underlying logging system must be thread-safe)
	 */
	private Log log;

	/**
	 * The set of running processes
	 */
	private Set<Process> runningProcesses;

	/**
	 * The number of failed processes
	 */
	private AtomicInteger failedProcessCount = new AtomicInteger();

	/**
	 * Constructor
	 * 
	 * @param concurrencyLimit
	 *            the limit for the number of processes allowed to run at the
	 *            same time
	 * @param log
	 *            message log
	 */
	public MultiprocessControl(int concurrencyLimit, Log log) {
		this.concurrencyLimit = concurrencyLimit;
		runningProcesses = Collections.synchronizedSet(new HashSet<Process>());
		this.log = log;
	}

	/**
	 * Run a list of commands. Return immediately if any of the processes fails. 
	 * 
	 * @param commandList
	 *            a list of commands (A command may contain arguments)
	 * @return 0 for success and 1 for failure
	 */
	public int run(List<String> commandList) {
		int numberOfCommands = commandList.size();
		Process[] processArray = new Process[numberOfCommands];
		ProcessHandler[] processHandlerArray = new ProcessHandler[numberOfCommands];
		runningProcesses.clear();

		int ret = 0;
		int jobCount = 0;
		try {
			for (int i = 0; i < numberOfCommands; i++) {
				synchronized (runningProcesses) {
					if (runningProcesses.size() == concurrencyLimit) {
						runningProcesses.wait();
					}
				}

				if (failedProcessCount.get() > 0) {
					break;
				}

				jobCount++;
				String cmdStr = commandList.get(i);
				String[] cmd = cmdStr.split(" ");
				processArray[i] = new ProcessBuilder(cmd).start();
				runningProcesses.add(processArray[i]);
				log.info("Running job " + (i + 1) + ": " + cmdStr);

				// set process handler
				processHandlerArray[i] = new ProcessHandler(processArray[i],
						i + 1, false);
				processHandlerArray[i].start();
			}

			int exitVal = 0;
			for (int i = 0; i < jobCount; i++) {
				if (failedProcessCount.get() > 0) {
					ret = 1;
					try {
						exitVal = processArray[i].exitValue();
						if (exitVal != 0) {
							log.info("Job " + (i + 1) + ": exit value => "
									+ exitVal);
						}
					} catch (IllegalThreadStateException itse) {
						processArray[i].destroy();
						log.info("Job " + (i + 1) + ": terminated.");
					}
				} else {
					exitVal = processArray[i].waitFor();
					if (exitVal != 0) {
						log.info("Job " + (i + 1) + ": exit value => "
								+ exitVal);
						ret = 1;
					}
				}
			}

			for (int i = 0; i < jobCount; i++) {
				processHandlerArray[i].join();
			}
		} catch (Exception e) {
			log.error("Parallel processing failed", e);
			synchronized (runningProcesses) {
				Iterator<Process> it = runningProcesses.iterator();
				while (it.hasNext()) {
					it.next().destroy();
				}
			}
			ret = 1;
		}
		return ret;
	}

	/**
	 * Run all commands in a list.
	 * 
	 * @param commandList a list of commands (A command may contain arguments)
	 * @return a list of exit values corresponding to the list of commands
	 * @throws Exception
	 */
	public List<Integer> runAll(List<String> commandList) throws Exception {
		int numberOfCommands = commandList.size();
		Process[] processArray = new Process[numberOfCommands];
		ProcessHandler[] processHandlerArray = new ProcessHandler[numberOfCommands];
		runningProcesses.clear();

		List<Integer> ret = new ArrayList<Integer>();
		
		try {
			for (int i = 0; i < numberOfCommands; i++) {
				synchronized (runningProcesses) {
					if (runningProcesses.size() == concurrencyLimit) {
						runningProcesses.wait();
					}
				}

				String cmdStr = commandList.get(i);
				String[] cmd = cmdStr.split(" ");
				processArray[i] = new ProcessBuilder(cmd).start();
				runningProcesses.add(processArray[i]);
				log.info("Running job " + (i + 1) + ": " + cmdStr);

				// set process handler
				processHandlerArray[i] = new ProcessHandler(processArray[i],
						i + 1, true);
				processHandlerArray[i].start();
			}

			for (int i = 0; i < numberOfCommands; i++) {	
					int exitVal = processArray[i].waitFor();
					if (exitVal != 0) {
						log.info("Job " + (i + 1) + ": exit value => "
								+ exitVal);
					}
					ret.add(exitVal);
			}

			for (int i = 0; i < numberOfCommands; i++) {
				processHandlerArray[i].join();
			}
		} catch (Exception e) {
			log.error("Parallel processing failed", e);
			synchronized (runningProcesses) {
				Iterator<Process> it = runningProcesses.iterator();
				while (it.hasNext()) {
					it.next().destroy();
				}
			}
			throw new Exception(e.getMessage());
		}
		return ret;
	}	
	
	/**
	 * Inner class for handling a process. It reads from output stream of the
	 * process and writes to the message log. It also monitors the process.
	 * 
	 */
	class ProcessHandler extends Thread {
		Process process;
		int jobId;
		boolean allowFailure;

		ProcessHandler(Process process, int jobId, boolean allowFailure) {
			this.process = process;
			this.jobId = jobId;
			this.allowFailure = allowFailure;
		}

		public void run() {
			try {
				// handle error stream
				ErrorStreamHandler errorStreamHandler = new ErrorStreamHandler(
						process.getErrorStream());
				errorStreamHandler.start();

				// handle output stream
				InputStream is = process.getInputStream();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				String line = null;

				while ((line = br.readLine()) != null) {
					log.info(line);
				}

				// make sure error stream has been handled
				errorStreamHandler.join();

				// monitor process and update information
				// about running processes and the number
				// of failed processes
				int exitValue = process.waitFor();
				if (exitValue != 0) {
					failedProcessCount.incrementAndGet();
					if (allowFailure) {
						log.info("Job " + jobId + ": exit value => " + exitValue);
					}
				} else {
					log.info("Job " + jobId + ": exit value => 0");
				}
				runningProcesses.remove(process);
				synchronized (runningProcesses) {
					runningProcesses.notify();
				}
			} catch (Exception e) {
				log.error("Process handler failed", e);
			}
		}
	}

	/**
	 * Inner class for handling an error stream. It reads from the error stream
	 * and writes to the message log.
	 */
	class ErrorStreamHandler extends Thread {
		private InputStream is;

		ErrorStreamHandler(InputStream is) {
			this.is = is;
		}

		public void run() {
			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				String line = null;

				while ((line = br.readLine()) != null) {
					log.error(line);
				}

			} catch (IOException e) {
				log.error("Error stream handler failed", e);
			}
		}
	}
}
