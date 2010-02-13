package wsj;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Will
 */
public class ListenThread extends Thread
{
	private final Integer listenPort;

	private final Coordinator server;

	private ServerSocket listener;

	private Boolean isRunning;

	public ListenThread(Integer listenPort, Coordinator server) throws IOException
	{
		this.setName(this.getName().replace("Thread", "Listener"));
		
		this.listenPort = listenPort;

		this.server = server;

		this.isRunning = true;

		try
		{
			listener = new ServerSocket(listenPort);

			this.start();
		}
		catch (IOException ex)
		{
			this.listener = null;

			this.isRunning = false;

			ex = new IOException("Error listening on port " + listenPort, ex);

			System.err.println(ex.getMessage());
			server.logErr(ex);

			throw ex;
		}
	}

	@Override
	public void run()
	{
		System.out.format(Coordinator.getString("out.listen_started"), Coordinator.getTimestamp(), listenPort);

		listenLoop(listener);

		System.out.format(Coordinator.getString("out.listen_terminated"), Coordinator.getTimestamp(), listenPort);
	}

	public void halt()
	{
		try
		{
			isRunning = false;
			
			listener.close();
		}
		catch (IOException ex)
		{
			System.err.println(Coordinator.getString("err.listen_close_fail"));
			server.logErr(ex);
		}
	}

	private void listenLoop(ServerSocket listen)
	{
		server.registerThread(this);

		while (isRunning)
		{
			try
			{
				Socket temp = listen.accept();
				
				ServeThread s = server.popServerQueue();	// may take a while

				if (temp.isConnected())
					s.serve(temp);
			}
			catch (IOException ex)
			{
				if (isRunning)
				{
					System.err.println(Coordinator.getString("err.listen_accept_fail"));
					server.logErr(ex);
				}
			}
		}

		server.unregisterThread(this);
	}
}
