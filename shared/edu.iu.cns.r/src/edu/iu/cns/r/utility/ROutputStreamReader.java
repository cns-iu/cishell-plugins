package edu.iu.cns.r.utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class ROutputStreamReader extends Thread {
	private InputStream inputStream;
	private boolean isStandardError;
	private boolean shouldPrintImmediately;
	private String output = "";

	public ROutputStreamReader(InputStream inputStream, boolean isStandardError) {
    	this(inputStream, isStandardError, true);
	}

	public ROutputStreamReader(
			InputStream inputStream, boolean isStandardError, boolean shouldPrintImmediately) {
    	this.inputStream = inputStream;
    	this.isStandardError = isStandardError;
    	this.shouldPrintImmediately = shouldPrintImmediately;
	}

	public String getOutput() {
		return this.output;
	}

	@Override
	public void run() {
		try {
			StringBuffer output = new StringBuffer();

			BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));
			String line = null;

			PrintStream stream = null;
			if (this.isStandardError) {
				stream = System.err;
			} else {
				stream = System.out;
			}
			
			while ((line = reader.readLine()) != null) {
				output.append(line);

				if (this.shouldPrintImmediately) {
					stream.println(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.output = output.toString();
		}
	}
}