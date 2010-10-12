
package wsj;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import wsj.HttpRequest.Key;

/**
 *
 * @author Will
 */
public class Logger extends Thread
{
	public static final String CRLF = System.getProperty("line.separator");

	public static final String out_print_request = Coordinator.getString("out.print_request");
	
	public static final String log_out = Coordinator.getString("log.out");
	public static final String log_err = Coordinator.getString("log.err");
	public static final String log_req = Coordinator.getString("log.req");
	public static final String log_request = Coordinator.getString("log.request");
	public static final String log_request_item = Coordinator.getString("log.request_item");
	
	private PrintWriter[] requestLogs;
	private PrintWriter[] stdoutLogs;
	private PrintWriter[] stderrLogs;

	public Logger()
	{
		requestLogs = new PrintWriter[1];
		stdoutLogs = new PrintWriter[2];
		stderrLogs = new PrintWriter[2];

		FileWriter req = openFile(Coordinator.appPath.getAbsolutePath() + Coordinator.getString("config.req"));
		FileWriter out = openFile(Coordinator.appPath.getAbsolutePath() + Coordinator.getString("config.log"));
		FileWriter err = openFile(Coordinator.appPath.getAbsolutePath() + Coordinator.getString("config.errlog"));

		requestLogs[0] = new PrintWriter(req);
		stdoutLogs[0] = new PrintWriter(System.out);
		stdoutLogs[1] = new PrintWriter(out);
		stderrLogs[0] = new PrintWriter(System.err);
		stderrLogs[1] = new PrintWriter(err);
	}

	private Logger(PrintStream... loggers)
	{
		
	}

	@Override
	public void run()
	{
		logOut("Shutting down program...");

		for (PrintWriter out : requestLogs)
			out.close();

		for (PrintWriter out : stderrLogs)
			out.close();

		for (PrintWriter out : stdoutLogs)
			out.close();
	}

	public void logEx(Exception logged)
    {
		logErr(logged.toString() + logged.getMessage());
    }

	public void logErr(String message)
    {
		log(String.format(log_err, Coordinator.getTimestamp(), message), stderrLogs);
    }

	public void logOut(String message)
    {
		log(String.format(log_out, Coordinator.getTimestamp(), message), stdoutLogs);
    }

	public void logRequest(String message)
    {
		log(String.format(log_req, Coordinator.getTimestamp(), message), requestLogs);
    }

	private void log(String message, PrintWriter[] arr)
    {
		for (PrintWriter out : arr)
		{
			out.print(message + CRLF);
			out.flush();
		}
    }

    public static String requestToStringVerbose(HttpRequest req)
    {
		StringBuffer str = new StringBuffer();

		for (String s : req.requestArray)
			str.append(String.format(log_request_item + CRLF, s));

		return String.format(log_request, req.source.getHostName(), CRLF + str.toString());
    }

	public static String requestToString(HttpRequest req)
    {
		return String.format(out_print_request,
			req.source.getHostName(), req.port,
			req.getValue(Key.KEEP_ALIVE), req.getValue(Key.RESOURCE));
    }

	public static FileWriter openFile(String input)
	{
		File log = new File(input);

		try
		{
			if (!log.exists()) log.createNewFile();

			return new FileWriter(log, true);
		}
		catch (Exception ex)
		{
			return null;
		}
	}
}
