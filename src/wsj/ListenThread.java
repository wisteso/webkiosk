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

			this.start();	// FIXME start via register method or other way
		}
		catch (IOException ex)
		{
			this.listener = null;

			this.isRunning = false;

			ex = new IOException("Error listening on port " + listenPort, ex);

			server.getLogger().logEx(ex);

			throw ex;
		}
	}

	@Override
	public void run()
	{
		server.getLogger().logOut(String.format(
				Coordinator.getString("out.listen_started"), listenPort));

		listenLoop(listener);

		server.getLogger().logOut(String.format(
				Coordinator.getString("out.listen_terminated"), listenPort));
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
			server.getLogger().logErr(Coordinator.getString("err.listen_close_fail"));
			server.getLogger().logEx(ex);
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
					server.getLogger().logErr(Coordinator.getString("err.listen_accept_fail"));
					server.getLogger().logEx(ex);
				}
			}
		}

		server.unregisterThread(this);
	}
}
