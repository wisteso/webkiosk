package ftp;

import java.io.*;
import java.net.*;

/**
 * @author wsoderbe
 */
public class ListenThread
{
	private ServerSocket sock;
	private Thread impl;
	private boolean running;

	public ListenThread(int port) throws IOException
	{
		sock = new ServerSocket(port);

		impl = new ListenThreadImpl();
	}

	public void startServer()
	{
		running = true;
		
		impl.start();
	}

	public void stopServer()
	{
		running = false;

		try
		{
			sock.close();
		}
		catch (IOException _)
		{
			throw new RuntimeException("Listen thread encountered a fatal error.");
		}
	}

	private void runForever()
	{
		Socket connection;

		while (running)
		{
			try
			{
				connection = sock.accept();

				new ServeThread(connection).startServer();
			}
			catch (SocketTimeoutException _)
			{
				if (running)
				{
					throw new RuntimeException("Listen thread encountered a fatal error.");
				}
			}
			catch (IOException ex)
			{
				if (running)
				{
					throw new RuntimeException("Listen thread encountered a fatal error.");
				}
			}
		}
	}

	private class ListenThreadImpl extends Thread
	{
		@Override
		public void run()
		{
			runForever();
		}
	}

	public static void main(String[] args) throws Exception
	{
		ListenThread lt = new ListenThread(21);
		lt.startServer();
	}
}