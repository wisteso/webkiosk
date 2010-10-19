package http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import http.HttpRequest.Key;

/**
 *
 * @author Will
 */
public class ServeThread extends Thread
{
	private volatile Socket sock;

	private volatile Boolean isRunning;

	private final Coordinator server;

	public ServeThread(Coordinator server)
	{
		this.setName(this.getName().replace("Thread", "Worker"));
		this.server = server;
		this.isRunning = true;
	}

	public void serve(Socket in)
	{
		synchronized (this)
		{
			if (sock == null)
			{
				sock = in;

				notify();
			}
			else
			{
				throw new IllegalStateException();
			}
		}
	}

	public void halt()
	{
		isRunning = false;

		synchronized (this)
		{
			if (getState() == State.WAITING)
				notify();
		}
	}

	@Override
	public void run()
	{
		server.registerThread(this);

		server.getLogger().logOut(String.format(
				Coordinator.getString("out.start_thread"), this.getName()));

		while (isRunning)
		{
			try
			{
				synchronized (this)
				{
					server.getLogger().logOut(String.format(
							Coordinator.getString("out.waiting_thread"), this.getName()));

					while (sock == null)
						this.wait();

					server.getLogger().logOut(String.format(
							Coordinator.getString("out.notified_thread"), this.getName()));

					serveRequests(sock);	// blocking method

					sock = null;
				}
			}
			catch (InterruptedException ex)
			{
				// TODO Put error string into resource file
				server.getLogger().logErr("Server thread was unexpectedly interrupted: " + ex);
				server.getLogger().logEx(ex);
			}
			finally
			{
				server.pushServerQueue(this);
			}
		}

		server.getLogger().logOut(String.format(
				Coordinator.getString("out.stop_thread"), this.getName()));

		server.unregisterThread(this);
	}

	private void serveRequests(Socket sockOut)
	{
		BufferedOutputStream out = null;

		try
		{
			out = new BufferedOutputStream(sockOut.getOutputStream(), 4096);

			HttpRequest request = null;
			Integer timeout = 15;
			String[] temp;

			while (isRunning)
			{
				if (request != null)
				{
					try
					{
						timeout = Integer.parseInt(request.getValue(Key.KEEP_ALIVE));
					}
					catch (Exception ex)
					{
						timeout = 180;
					}

					timeout = 15;
				}

				temp = server.readRequest(sockOut, timeout);

				if (temp.length == 0) break;

				request = new HttpRequest(temp, sockOut.getInetAddress(), sockOut.getLocalPort());

				server.getLogger().logOut(Logger.requestToString(request));
				server.getLogger().logRequest(Logger.requestToStringVerbose(request));

				if (request.getValue(Key.METHOD).equals("GET"))
				{
					server.sendGetRequest(request, out);
				}
				else if (request.getValue(Key.METHOD).equals("HEAD"))
				{
					String buffer = Coordinator.unescapeURL(request.getValue(Key.RESOURCE));

					InputStream inBody = Coordinator.openResource(buffer);
					InputStream inHead = Coordinator.createHeader(200, buffer, inBody.available());
					Coordinator.sendResponseSilent(out, new InputStreamMixer(inHead, inBody));
				}
				else
				{
					String buffer = Coordinator.getString("pages.501");

					InputStream inBody = Coordinator.openResource(buffer);
					InputStream inHead = Coordinator.createHeader(501, buffer, inBody.available());
					Coordinator.sendResponseSilent(out, new InputStreamMixer(inHead, inBody));
				}

				out.flush();
			}
		}
		catch (HttpRequest.BadRequestException ex)
		{
			server.getLogger().logErr(Coordinator.getString("err.invalid_request") + ex);
			server.getLogger().logEx(ex);
		}
		catch (IOException ex)
		{
			server.getLogger().logErr(Coordinator.getString("err.critical_fail") + ex);
			server.getLogger().logEx(ex);
		}

		try
		{
			if (out != null) out.close();
			if (sockOut != null) sockOut.close();
		}
		catch (IOException ex)
		{
			server.getLogger().logErr(Coordinator.getString("err.serve_close") + ex);
			server.getLogger().logEx(ex);
		}
	}
}
