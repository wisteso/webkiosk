package ftp;

import java.io.*;
import java.net.*;

/**
 *
 * @author wsoderbe
 */
public class ServeThread extends Thread
{
	private boolean running;
	private Socket io;

	public ServeThread(Socket io)
	{
		this.io = io;
	}

	@Override
	public void run()
	{
		if (running)
			startServing();
	}

	public void startServer()
	{
		running = true;
		this.start();
	}

	private void startServing()
	{
		InputStream in = null;
		OutputStream out = null;

		try
		{
			in = io.getInputStream();
			out = io.getOutputStream();

			BufferedReader input = new BufferedReader(new InputStreamReader(in));

			String line;

			out.write("220 WebKiosk FTP Server\n".getBytes());

			while (running)
			{
				line = input.readLine();

				if (line == null)
					running = false;
				else
					System.out.println(line);
			}
		}
		catch (IOException ex)
		{
			System.out.println("Serve thread fatal exception");
		}
	}
}
